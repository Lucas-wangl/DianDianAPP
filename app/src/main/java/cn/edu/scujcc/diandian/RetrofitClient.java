package cn.edu.scujcc.diandian;

import com.squareup.moshi.Moshi;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.converter.moshi.MoshiConverterFactory;

/**
 * 使用单例模式创建Retrofit，避免资源浪费
 */
public class RetrofitClient {
    private static Retrofit INSTANCE = null;

    public  static Retrofit getInstance(){
        if (INSTANCE == null){
            Moshi moshi = new Moshi.Builder()
                    .add(new MyDateAdapter())
                    .build();
            OkHttpClient client = new OkHttpClient.Builder()
                    .addInterceptor(new AuthInterceptor())
                    .build();
             INSTANCE = new Retrofit.Builder()
                    .baseUrl("http://47.113.117.167:8080")
                    .addConverterFactory(MoshiConverterFactory.create(moshi))
                     .callFactory(client)
                    .build();
        }
        return INSTANCE;
    }
}
