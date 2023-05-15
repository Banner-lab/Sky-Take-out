package com.sky.test;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.http.HttpEntity;
import org.apache.http.HttpHeaders;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.io.InputStream;

/**
 * @ClassName Test
 * @Description TODO
 * @Author XMING
 * @Date 2023/5/15 16:55
 * @Version 1.0
 */
public class Test {
    @org.junit.Test
    public void testAk() throws IOException {
        CloseableHttpClient httpClient = HttpClients.createDefault();

        HttpGet httpGet = new HttpGet("https://api.map.baidu.com/geocoding/v3/?address=湖南省长沙市高新区东方红中路368号&output=jso" +
                "n&ak=VjZ1T3j1lrGDTGkfGalwlEZjAH46iGDG");

        CloseableHttpResponse response = httpClient.execute(httpGet);
        HttpEntity httpEntity = response.getEntity();
        String string = EntityUtils.toString(httpEntity);
        System.out.println(string);
    }
}
