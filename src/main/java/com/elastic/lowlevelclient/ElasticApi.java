package com.elastic.lowlevelclient;

import com.common.ConstantMap;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class ElasticApi {

    private static final String ip = ConstantMap.ELASTICSEARCH.IP;
    private static final int port = ConstantMap.ELASTICSEARCH.PORT;
    private static final String id = ConstantMap.ELASTICSEARCH.ID;
    private static final String pw = ConstantMap.ELASTICSEARCH.PW;

    /**
     * 엘라스틱서치에서 제공하는 api를 이용한 전송메소드
     *
     * @param method
     * @param url
     * @param obj
     * @param jsonData
     * @return
     */
    public Map<String, Object> callElasticApi(String method, String url, Object obj, String jsonData) {
        Map<String, Object> result = new HashMap<>();

        String jsonString;
        //json형태의 파라미터가 아니라면 gson으로 만들어주자.
        if (jsonData == null) {

            /*검색 쿼리 VO로 사용시*/
//            Gson gson = new Gson();
//            String jsonStr = gson.toJson(obj);
//            jsonString = "{\n" +
//                    "  \"query\": {\n" +
//                    "    \"match\":" +
//                    "      "+jsonStr+"\n" +
//                    "  }\n" +
//                    "}";


            Gson gson = new Gson();
            jsonString = gson.toJson(obj);
        } else {
            jsonString = jsonData;
        }

        //엘라스틱서치에서 제공하는 restClient를 통해 엘라스틱서치에 접속한다
        try (RestClient restClient = RestClient.builder(new HttpHost(ip, port, "http")).build()) {
//            Map<String, String> params =  Collections.singletonMap("pretty", "true");
            //엘라스틱서치에서 제공하는 response 객체
            Response response = null;
            Request request = new Request(method, url);

            //GET, DELETE 메소드는 HttpEntity가 필요없다
            if (method.equals("GET") || method.equals("DELETE")) {
                request.addParameter("pretty", "true");
                response = restClient.performRequest(request);
            } else {
                request.setEntity(new NStringEntity(jsonString, ContentType.APPLICATION_JSON));
                request.addParameter("pretty", "true");
                response = restClient.performRequest(request);
            }
            //앨라스틱서치에서 리턴되는 응답코드를 받는다
            int statusCode = response.getStatusLine().getStatusCode();
            //엘라스틱서치에서 리턴되는 응답메시지를 받는다
            String responseBody = EntityUtils.toString(response.getEntity());
            result.put("resultCode", statusCode);
            result.put("resultBody", responseBody);
            result.put("resultJson", new JSONParser().parse(responseBody));
        } catch (Exception e) {
            result.put("resultCode", -1);
            result.put("resultBody", e.toString());
            result.put("resultJson", e.toString());
        }
        return result;
    }

}
