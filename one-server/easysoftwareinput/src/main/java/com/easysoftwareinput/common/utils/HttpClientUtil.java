package com.easysoftwareinput.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.fasterxml.jackson.databind.JsonNode;

public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String getRequest(String urlStr) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            logger.error(MessageCode.EC0001.getMsgEn(), e);
        }
        return null;
    }

    public static String postRequest(String urlStr, String body) {
        try {
            URL url = new URL(urlStr);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/json");
            connection.setDoOutput(true);
            OutputStream outputStream = connection.getOutputStream();
            outputStream.write(body.getBytes());
            outputStream.close();
    
            BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "UTF-8"));
            String line;
            StringBuilder response = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();
            return response.toString();
        } catch (Exception e) {
            logger.error(MessageCode.EC0001.getMsgEn(), e);
        }
        return null;
    }

    public static String getHttpClient(String uri, String token, String userToken, String cookie) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpGet httpGet = new HttpGet(uri);

        if (token != null) httpGet.addHeader("token", token);
        if (userToken != null) httpGet.addHeader("user-token", userToken);
        if (cookie != null) httpGet.addHeader("Cookie", "_Y_G_=" + cookie);

        try {
            HttpResponse response = httpClient.execute(httpGet);
            String responseBody = EntityUtils.toString(response.getEntity());
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException(MessageCode.EC0001.getMsgEn());
        }
    }

    public static String postHttpClient(String uri, String requestBody) {
        HttpClient httpClient = HttpClients.createDefault();
        HttpPost httpPost = new HttpPost(uri);
        try {
            httpPost.setHeader("Content-Type", "application/json");
            StringEntity stringEntity = new StringEntity(requestBody);
            httpPost.setEntity(stringEntity);
            HttpResponse response = httpClient.execute(httpPost);
            String responseBody = EntityUtils.toString(response.getEntity());
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException(MessageCode.EC0001.getMsgEn());
        }
    }

    public static Map<String, String> getApiResponseMap(String url) {
        Map<String, String> res = new HashMap<>();
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        if (response != null) {
            JsonNode info = ObjectMapperUtil.toJsonNode(response);
            if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                JsonNode infoData = info.get("data");
                res = ObjectMapperUtil.jsonToMap(infoData);
            }
        }
        return res;
    }

    public static Map<String, String> getApiResponseMaintainer(String url) {
        Map<String, String> maintainer = MapConstant.MAINTAINER;
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        if (response != null) {
            JsonNode info = ObjectMapperUtil.toJsonNode(response);
            if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                JsonNode infoData = info.get("data");
                maintainer = ObjectMapperUtil.jsonToMap(infoData);
                maintainer.put("id", maintainer.get("gitee_id"));
            }
        }
        return maintainer;
    }

    public static String getApiResponseData(String url) {
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        JsonNode info = ObjectMapperUtil.toJsonNode(response);
        if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
            return info.get("data").asText();
        }
        return "0";
    }
}

