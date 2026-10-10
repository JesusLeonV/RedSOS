package com.example.redsos.network;

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class ServidorSync {

    private static final String TAG = "ServidorSync";

    private static final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(10, TimeUnit.SECONDS)
            .writeTimeout(15, TimeUnit.SECONDS)
            .readTimeout(15, TimeUnit.SECONDS)
            .build();

    private static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

    private static String BASE_URL = "http://100.76.140.47:3000/api";

    public static void setBaseUrl(String url) {
        BASE_URL = url;
    }

    public static boolean enviar(String endpoint, JSONObject json) {
        try {
            RequestBody body = RequestBody.create(json.toString(), JSON);
            Request request = new Request.Builder()
                    .url(BASE_URL + endpoint)
                    .post(body)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                Log.d(TAG, "POST " + endpoint + " → " + response.code());
                return response.isSuccessful();
            }
        } catch (IOException e) {
            Log.e(TAG, "Error: " + e.getMessage());
            return false;
        }
    }
}
