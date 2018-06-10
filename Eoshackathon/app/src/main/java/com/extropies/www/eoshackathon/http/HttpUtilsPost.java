package com.extropies.www.eoshackathon.http;

import android.os.AsyncTask;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL; 

/**
 * Created by inst on 18-6-9.
 */

public class HttpUtilsPost extends AsyncTask<Integer, Integer, String> {
    HttpUtilsCallback callback;
    private String targetUrl;
    private String postParams;

    public HttpUtilsPost(String targetUrl,String postData,HttpUtilsCallback callback) {
        this.callback = callback;
        this.targetUrl = targetUrl;
        this.postParams = postData;
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

            urlConn.setRequestMethod("POST");
            urlConn.setDoInput(true);                  //打开输入流，以便从服务器获取数据
            urlConn.setDoOutput(true);                 //打开输出流，以便向服务器提交数据
            urlConn.setUseCaches(false);               //使用Post方式不能使用缓存

            urlConn.connect();
            urlConn.getOutputStream().write(postParams.getBytes());// 获取向服务器写数据的输出流

            int code = urlConn.getResponseCode();
            if (url.toString().endsWith("push_transaction")){
                String teststring = "";
            }
            if (HttpURLConnection.HTTP_OK == code) {
                buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "utf-8"));
                while ((line = buffer.readLine()) != null) {
                    responseData = responseData + line;
                }
            } else if (HttpURLConnection.HTTP_INTERNAL_ERROR == code){
                System.out.println("===== urlConn.getResponseCode ====="+ code + "  url:" + url  );
                if (url.toString().endsWith("unlock") || url.toString().endsWith("sign_transaction")|| url.toString().endsWith("push_transaction") ) {
                    JSONObject jsonObj = new JSONObject();
                    jsonObj.put("code", 500);
                    jsonObj.put("message", "Internal Service Error");
                    responseData = jsonObj.toString();
                }
            } else if (HttpURLConnection.HTTP_CREATED == code) {
                System.out.println("===== urlConn.getResponseCode ====="+ code + "  url:" + url  );
                if (url.toString().endsWith("sign_transaction")) {
                    buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "utf-8"));
                    while ((line = buffer.readLine()) != null) {
                        responseData = responseData + line;
                    }
                }
            } else if (HttpURLConnection.HTTP_ACCEPTED == code) {
                System.out.println("===== urlConn.getResponseCode ====="+ code + "  url:" + url  );
                if (url.toString().endsWith("push_transaction")) {
                    buffer = new BufferedReader(new InputStreamReader(urlConn.getInputStream(), "utf-8"));
                    while ((line = buffer.readLine()) != null) {
                        responseData = responseData + line;
                    }
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
        System.out.println("===== onPostExecute ====="+ s  );
        callback.httpResponse(targetUrl,s);
    }
}
