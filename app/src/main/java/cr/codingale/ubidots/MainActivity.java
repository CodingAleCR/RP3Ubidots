package cr.codingale.ubidots;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.google.android.things.pio.Gpio;
import com.google.android.things.pio.GpioCallback;
import com.google.android.things.pio.PeripheralManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Random;

import nz.geek.android.things.drivers.adc.I2cAdc;

public class MainActivity extends Activity {
    public static final String TAG = "MainActivity::";

    // IDs Ubidots
    private final String token = "A1E-lqNZdrK5AjSyPveH9Om5fUZp16PZXe";
    private final String idIluminacion = "5d1aeb30c03f976f9052ddd2";
    private final String idBoton = "5d1aeb47c03f976f9052ddd3";
    private final String PIN_BUTTON = "BCM23";
    private Gpio mButtonGpio;
    private Double buttonstatus = 0.0;
    // Potentiometer
    private I2cAdc mADC;
    private int mChannel = 0;
    private Handler handler = new Handler();
    private Runnable runnable = new UpdateRunner();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        PeripheralManager service = PeripheralManager.getInstance();
        try {
            mButtonGpio = service.openGpio(PIN_BUTTON);
            mButtonGpio.setDirection(Gpio.DIRECTION_IN);
            mButtonGpio.setActiveType(Gpio.ACTIVE_LOW);
            mButtonGpio.setEdgeTriggerType(Gpio.EDGE_FALLING);
            mButtonGpio.registerGpioCallback(mCallback);

            I2cAdc.I2cAdcBuilder builder = I2cAdc.builder();
            mADC = builder.address(0).fourSingleEnded().withConversionRate(100).build();
            mADC.startConversions();
        } catch (IOException e) {
            Log.e(TAG, "Error en PeripheralIO API", e);
        }
        handler.post(runnable);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler = null;
        runnable = null;
        if (mButtonGpio != null) {
            mButtonGpio.unregisterGpioCallback(mCallback);
            try {
                mButtonGpio.close();
            } catch (IOException e) {
                Log.e(TAG, "Error en PeripheralIO API", e);
            }
        }
    }

    // Callback para envío asíncrono de pulsación de botón
    private GpioCallback mCallback = new GpioCallback() {
        @Override
        public boolean onGpioEdge(Gpio gpio) {
            Log.i(TAG, "Botón pulsado!");
            if (buttonstatus == 0.0) buttonstatus = 1.0;
            else buttonstatus = 0.0;
            final Data boton = new Data();
            boton.setVariable(idBoton);
            boton.setValue(buttonstatus);
            ArrayList<Data> message = new ArrayList<Data>() {{
                add(boton);
            }};
            UbiClient.getClient().sendData(message, token);
            return true; // Mantenemos el callback activo
        }
    };

    // Envío síncrono (5 segundos) del valor del fotorresistor
    private class UpdateRunner implements Runnable {
        @Override
        public void run() {
            readLDR();
            Log.i(TAG, "Ejecución de acción periódica");
            handler.postDelayed(this, 5000);
        }
    }

    private void readLDR() {
        Data iluminacion = new Data();
        ArrayList<Data> message = new ArrayList<>();
        int value = mADC.readChannel(mChannel);
        double volt = value * 3.3 / 255;
        iluminacion.setVariable(idIluminacion);
        iluminacion.setValue(volt);
        message.add(iluminacion);
        UbiClient.getClient().sendData(message, token);
    }
}
