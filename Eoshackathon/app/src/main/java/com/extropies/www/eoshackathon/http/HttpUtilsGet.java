package com.extropies.www.eoshackathon.http;

import android.os.AsyncTask;

import java.io.BufferedReader;
import java.io.InputStreamReader; 
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by inst on 18-6-9.
 */

public class HttpUtilsGet extends AsyncTask<Integer, Integer, String> {
    HttpUtilsCallback callback;
    private String targetUrl;

    public HttpUtilsGet(String targetUrl,HttpUtilsCallback callback) {
        this.callback = callback;
        this.targetUrl = targetUrl;
    }

    @Override
    protected String doInBackground(Integer... params) {
        String responseData = "";
        String line = "";
        BufferedReader buffer = null;
        URL url = null;
        try {
            url = new URL(this.targetUrl);
            HttpURLConnection urlConn = (HttpURLConnection) url.openConnection();

            urlConn.setRequestMethod("GET");
            int code = urlConn.getResponseCode();
            if (HttpURLConnection.HTTP_OK == code) {
                buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "utf-8"));
                while ((line = buffer.readLine()) != null) {
                    responseData = responseData + line;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                buffer.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        System.out.println(responseData);
        return responseData;
    }

    @Override
    protected void onPostExecute(String s) {
        if (s.equals("")) {
            return;
        }
        System.out.println("===== onGetExecute ====="+ s  );
        callback.httpResponse(targetUrl,s);
    }
}
