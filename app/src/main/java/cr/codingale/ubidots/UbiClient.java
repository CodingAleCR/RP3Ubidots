package cr.codingale.ubidots;

import android.util.Log;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class UbiClient {
    private static final String TAG = UbiClient.class.getSimpleName();
    private static final String UBI_BASE_URL = "http://things.ubidots.com/";
    private static UbiClient client;
    private UbiAPI api;
    private Retrofit retroClient;

    private UbiClient() {
        retroClient = new Retrofit.Builder()
                .baseUrl(UBI_BASE_URL).addConverterFactory(GsonConverterFactory.create()).build();
    }

    public static final UbiClient getClient() {
        if (client != null) return client;
        client = new UbiClient();
        return client;
    }

    private UbiAPI getUbiClient() {
        return retroClient.create(UbiAPI.class);
    }

    public void sendData(ArrayList<Data> dList, String token) {
        api = client.getUbiClient();
        Call c = api.sendValue(dList, token);
        c.enqueue(new Callback() {
            @Override
            public void onResponse(Call call, Response response) {
                Log.d(TAG, "onResponse");
                Log.d(TAG, "Result: " + response.isSuccessful() + " - " + response.message());
            }

            @Override
            public void onFailure(Call call, Throwable t) {
                t.printStackTrace();
            }
        });
    }
}
