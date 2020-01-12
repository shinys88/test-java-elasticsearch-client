package com.service.index;

import com.elastic.highlevelclient.IElasticsearchService;
import com.elastic.lowlevelclient.ElasticApi;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.annotation.Resource;
import java.util.Map;

@Controller
public class IndexController {

    @Resource(name = "elasticApi")
    ElasticApi elasticApi;

    @Resource(name = "elasticsearchServiceImpl")
    IElasticsearchService es;

    @RequestMapping("/ll")
    public String executeTest(Model model) {

        String query = "{\n" +
                "  \"query\": {\n" +
                "    \"match\": {\n" +
                "      \"name\": \"호텔1\"\n" +
                "    }\n" +
                "  }\n" +
                "}";

//        Map<String, Object> resultMap = elasticApi.callElasticApi("POST", "hotels/_search", "jsonimport", query);

        Map<String, Object> resultMap = es.callElasticApi("POST", "hotels/_search", "jsonimport", query);

        model.addAttribute("resultBody", resultMap.get("resultBody"));
        model.addAttribute("resultJson", resultMap.get("resultJson"));
        model.addAttribute("resultCode", resultMap.get("resultCode"));

        return "test/lowTest";
    }


    @RequestMapping("/")
    public String executeTest2(Model model, @RequestParam Map<String, Object> paramMap,
                               @RequestParam(value = "marketName", defaultValue = "")String marketName,
                               @RequestParam(value = "prodNm", defaultValue = "")String prodNm) {

        String getRangePrimary = (String) paramMap.get("rangePrimary");
        String[] rangePrimary;

        if(getRangePrimary == null){
            rangePrimary = new String[2];
            rangePrimary[0] = "0";
            rangePrimary[1] = "350000";
        }else{
            rangePrimary = getRangePrimary.split(";");
        }

        model.addAllAttributes(paramMap);

        // 메서드 빌드 순서 : QueryBuilder > SearchSourceBuilder > SearchRequest > SearchResponse

        //3. boolQueryBuilder
        BoolQueryBuilder bqb1 = es.getBoolQueryBuilder();
        if(!marketName.equals("")){
            QueryBuilder qb = es.getMatchQueryBuilder("marketName", marketName, "true");
            bqb1.must().add(qb);
        }
        if (!prodNm.equals("")){
            QueryBuilder qb2 = es.getMatchQueryBuilder("prodNm", prodNm, "true");
            bqb1.must().add(qb2);
        }

        QueryBuilder qb3 = es.getRangeQueryBuilder("price",rangePrimary[0], rangePrimary[1]);
        bqb1.must().add(qb3);



//        //2. sourceBuilder
////        SearchSourceBuilder ssb = es.getSearchSourceBuilder(bqb, null, null);
        SearchSourceBuilder ssb = es.getSearchSourceBuilder(bqb1, null, null);
        //1. request
        SearchRequest sr = es.getSearchRequest(ssb, "prodlist");

        //0. response
        SearchResponse searchResponse = es.getSearchResponse(sr);

        SearchHits searchHits = searchResponse.getHits();
        for (SearchHit hit : searchHits.getHits()) {
            Map<String, Object> source = hit.getSourceAsMap();
            System.out.println("상품명 : " + source.get("prodNm"));
        }


        model.addAttribute("resultBody", searchResponse);
        try {
            model.addAttribute("resultJson", new JSONParser().parse(String.valueOf(searchResponse)));
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return "service/index";
    }
}
