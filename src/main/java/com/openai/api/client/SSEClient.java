package com.openai.api.client;

import com.openai.api.handler.AswMsgHandler;
import org.springframework.http.HttpHeaders;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;

/**
 * @author hyd
 */
public class SSEClient {

    /**
     * 获取SSE输入流。
     *
     * @param urlPath
     * @return
     * @throws IOException
     */
    public void sendHttp(String urlPath, String method, HttpHeaders headers, byte[] body,Proxy proxy, AswMsgHandler msgHandler) throws IOException {
        URL url = new URL(urlPath);
        HttpURLConnection urlConnection;
        if(proxy != null){
            urlConnection = (HttpURLConnection) url.openConnection(proxy);
        }else{
            urlConnection = (HttpURLConnection) url.openConnection();
        }
        // 这儿根据自己的情况选择get或post
        urlConnection.setRequestMethod(method);
        headers.forEach((key, value) -> {
            urlConnection.setRequestProperty(key, value.toString());
        });
        urlConnection.setRequestProperty("Connection", "Keep-Alive");
        urlConnection.setRequestProperty("Authorization", "Bearer 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" );
        urlConnection.setRequestProperty("Content-Type", "application/json; charset=UTF-8");
        urlConnection.setRequestProperty("Charset", "UTF-8");
        urlConnection.setReadTimeout(60 * 1000);
        urlConnection.setDoInput(true);
        urlConnection.setUseCaches(false);
        if (body != null) {
            urlConnection.setDoOutput(true);
            OutputStream outputStream = urlConnection.getOutputStream();
            outputStream.write(body);
            outputStream.flush();
            outputStream.close();
        }
        //读取过期时间（很重要，建议加上）
        InputStream inputStream = urlConnection.getInputStream();
        BufferedInputStream is = new BufferedInputStream(inputStream);
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(is));
            String line = "";
            while ((line = reader.readLine()) != null) {
                // 处理数据接口
                msgHandler.actMsg(is, line);
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new IOException("关闭数据流！");
        } finally {
            if (reader != null) {
                reader.close();
            }
            urlConnection.disconnect();
        }
    }

}
