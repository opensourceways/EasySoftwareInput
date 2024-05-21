package com.easysoftwareinput.common.utils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.client.RestTemplate;

import com.easysoftwareinput.common.constant.MapConstant;
import com.easysoftwareinput.common.entity.MessageCode;
import com.fasterxml.jackson.databind.JsonNode;

public class HttpClientUtil {

    private static final Logger logger = LoggerFactory.getLogger(HttpClientUtil.class);

    public static String encode(String url) {
        String encoded = "";
        try {
            encoded = URLEncoder.encode(url, "utf-8");
        } catch (UnsupportedEncodingException e) {
            logger.info("can not encode url, url: {}", url);
        }
        return encoded.replaceAll("\\+", "%20");
    }

    public static void deleteRequest(String urlStr) {
        RestTemplate restTemplate = new RestTemplate();
        try {
            restTemplate.delete(urlStr);
        } catch (Exception e) {
            logger.error(MessageCode.EC0001.getMsgEn(), e);
        }
    }

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

    public static String postApp(String urlStr, String body) {
        RestTemplate restTemplate = new RestTemplate();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(body, headers);
        try {
            String res = restTemplate.postForObject(urlStr, request, String.class);
            return res;
        } catch (Exception e) {
            logger.error(MessageCode.EC0001.getMsgEn(), e);
        }
        return "";
    }

    public static String getHttpClient(String uri, String token, String userToken, String cookie) {
        logger.info("url: {}", uri);
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
            try {
                if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                    JsonNode infoData = info.get("data");
                    res = ObjectMapperUtil.jsonToMap(infoData);
                }
            } catch (Exception e) {
                logger.info(url);
            }
        }
        return res;
    }

    public static Map<String, String> getApiResponseMaintainer(String url) {
        Map<String, String> maintainer = MapConstant.MAINTAINER;
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        if (response != null) {
            JsonNode info = ObjectMapperUtil.toJsonNode(response);
            try {
                if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                    JsonNode infoData = info.get("data");
                    maintainer = ObjectMapperUtil.jsonToMap(infoData);
                    maintainer.put("id", maintainer.get("gitee_id"));
                }
            } catch (Exception e) {
                logger.info(url);
            }
        }
        return maintainer;
    }

    public static String getApiResponseData(String url) {
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        JsonNode info = ObjectMapperUtil.toJsonNode(response);
        try {
            if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                return info.get("data").asText();
            }
        } catch (Exception e) {
            logger.info(url);
        }
        return "0";
    }

    public static JsonNode getApiResponseJson(String url) {
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        if (response != null) {
            JsonNode info = ObjectMapperUtil.toJsonNode(response);
            try {
                if (info.get("code").asInt() == 200 && !info.get("data").isNull()) {
                    JsonNode infoData = info.get("data");
                    return infoData;
                }
            } catch (Exception e) {
                logger.info(url);
            }
        }
        return null;
    }

    public static Map<String, JsonNode> getMonitor(String name, String monUrl) {
        name = name.replaceAll(" ", "%20");
        String url = String.format(monUrl, name);
        String response = HttpClientUtil.getHttpClient(url, null, null, null);
        if (StringUtils.isBlank(response)) {
            return Collections.emptyMap();
        }
        JsonNode info = ObjectMapperUtil.toJsonNode(response);
        if (info == null) {
            return Collections.emptyMap();
        }

        JsonNode items = info.get("items");
        if (items == null) {
            return Collections.emptyMap();
        }

        Map<String, JsonNode> tagMap = new HashMap<>();
        for (JsonNode item : items) {
            tagMap.put(item.get("tag").asText(), item);
        }
        return tagMap;
    }

}

