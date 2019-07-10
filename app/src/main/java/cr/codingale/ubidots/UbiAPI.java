package cr.codingale.ubidots;

import java.util.ArrayList;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface UbiAPI {
    @POST("/api/v1.6/collections/values")
    public Call<ResponseBody> sendValue(
            @Body ArrayList<Data> dataList, @Query("token") String token);
}
