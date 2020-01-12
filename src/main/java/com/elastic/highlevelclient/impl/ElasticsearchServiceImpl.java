package com.elastic.highlevelclient.impl;

import com.elastic.highlevelclient.IElasticsearchDao;
import com.elastic.highlevelclient.IElasticsearchService;
import com.google.gson.Gson;
import org.apache.http.HttpHost;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.util.EntityUtils;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.support.WriteRequest;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.Request;
import org.elasticsearch.client.Response;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.unit.Fuzziness;
import org.elasticsearch.common.unit.TimeValue;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.index.query.RangeQueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilders;
import org.elasticsearch.search.aggregations.AggregatorFactories.Builder;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.PipelineAggregatorBuilders;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.FieldSortBuilder;
import org.elasticsearch.search.sort.ScoreSortBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Repository
public class ElasticsearchServiceImpl implements IElasticsearchService {

    @Resource(name="elasticsearchDaoImpl")
    IElasticsearchDao elasticsearchDao;

    @Override
    public RestHighLevelClient getClient(){
        return elasticsearchDao.getClient();
    }



    @Override
    public Map<String, Object> callElasticApi(String method, String url, Object obj, String jsonData) {
        Map<String, Object> result = new HashMap<>();

        String jsonString;
        //json형태의 파라미터가 아니라면 gson으로 만들어주자.
        if (jsonData == null) {
            Gson gson = new Gson();
            jsonString = gson.toJson(obj);
        } else {
            jsonString = jsonData;
        }

        //엘라스틱서치에서 제공하는 restClient를 통해 엘라스틱서치에 접속한다
        try (RestClient restClient = elasticsearchDao.getClient().getLowLevelClient()) {
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

//    == 기능 ==
//    1. 업데이트
    @Override
    public UpdateRequest getUpdateRequest(String index, String type, String id){
        return new UpdateRequest(index, type, id);
    }

    @Override
    public UpdateRequest getUpsertRequest(UpdateRequest updateRequest, Map source){
        return updateRequest.upsert(source);
    }

    @Override
    public UpdateResponse getUpdateResponse(UpdateRequest updateRequest){
        return elasticsearchDao.updateQuery(updateRequest);
    }


//    2. 벌크
    @Override
    public BulkRequest getBulkRequest(){
        BulkRequest bulkRequest = new BulkRequest();
        bulkRequest.timeout(TimeValue.timeValueMinutes(2)).setRefreshPolicy(WriteRequest.RefreshPolicy.IMMEDIATE);
        return bulkRequest;
    }

    @Override
    public BulkResponse getBulkResponse(BulkRequest bulkRequest){
        return elasticsearchDao.bulkQuery(bulkRequest);
    }


//    3. 맵핑
    @Override
    public GetMappingsResponse getMappingsResponse(String index){
        GetMappingsRequest request = new GetMappingsRequest();
        request.indices(index);
        request.types("_doc");
        return elasticsearchDao.getMapping(request);
    }


//    4. 검색
    @Override
    public SearchResponse getSearchResponse(SearchRequest searchRequest){
        return elasticsearchDao.searchQuery(searchRequest);
    }

    @Override
    public SearchRequest getSearchRequest(SearchSourceBuilder searchSourceBuilder, String index){
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(index);
        return searchRequest;
    }

    /*type은 없어질 예정으로 사용안함*/
    public SearchRequest getSearchRequest(SearchSourceBuilder searchSourceBuilder, String index, String type){
        SearchRequest searchRequest = new SearchRequest();
        searchRequest.source(searchSourceBuilder);
        searchRequest.indices(index);
        searchRequest.types(type);
        return searchRequest;
    }



//    == 검색소스 빌더(SearchSourceBuilder) ==
    @Override
    public HighlightBuilder getHighlightBuilder(String[] fieldArr) {
        HighlightBuilder highlightBuilder = new HighlightBuilder();
        for (String field : fieldArr) {
            HighlightBuilder.Field highlightTitle = new HighlightBuilder.Field(field);
            highlightTitle.highlighterType("unified");
            highlightBuilder.field(highlightTitle);
        }
        return highlightBuilder;
    }

    @Override
    public SortBuilder getSortBuilder(String field, String sortOrder) {
        //set order option
        SortBuilder sortBuilder = null;
        if (field != null && !field.equals("")) {
            FieldSortBuilder sb = new FieldSortBuilder(field);
            if (sortOrder == null || "desc".equals(sortOrder)) {
                sb.order(SortOrder.DESC);
            } else if ("asc".equals(sortOrder)) {
                sb.order(SortOrder.ASC);
            }
            sortBuilder = sb;
        } else {
            sortBuilder = new ScoreSortBuilder().order(SortOrder.DESC);
        }

        return sortBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackScores(true);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList, String[] fetchIncludeArr) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.fetchSource(fetchIncludeArr, null);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.trackScores(true);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.fetchSource(fetchIncludeArr, null);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.highlighter(highlightBuilder);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.fetchSource(fetchIncludeArr, null);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList, float minScore) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.minScore(minScore);
        searchSourceBuilder.trackScores(true);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList, String[] fetchIncludeArr, float minScore) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(0);
        searchSourceBuilder.size(10000);
        searchSourceBuilder.minScore(minScore);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.fetchSource(fetchIncludeArr, null);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size, float minScore) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.minScore(minScore);
        searchSourceBuilder.trackScores(true);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr, float minScore) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.minScore(minScore);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.fetchSource(fetchIncludeArr, null);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }
        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size, float minScore) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.minScore(minScore);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.highlighter(highlightBuilder);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }

    @Override
    public SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr, float minScore) {
        SearchSourceBuilder searchSourceBuilder = new SearchSourceBuilder();
        searchSourceBuilder.query(queryBuilder); //쿼리 옵션 적용
        searchSourceBuilder.timeout(new TimeValue(60, TimeUnit.SECONDS)); //검색 타임아웃
        searchSourceBuilder.from(from);
        searchSourceBuilder.size(size);
        searchSourceBuilder.minScore(minScore);
        searchSourceBuilder.trackScores(true);
        searchSourceBuilder.highlighter(highlightBuilder);
        searchSourceBuilder.fetchSource(fetchIncludeArr, null);
        if (sortBuilderList != null && sortBuilderList.size() > 0) {
            for (SortBuilder sortBuilder : sortBuilderList) {
                searchSourceBuilder.sort(sortBuilder);
            }
        }

        if (aggregationBuilderWrapper != null && aggregationBuilderWrapper.count() > 0) {
            for (AggregationBuilder aggregationBuilder : aggregationBuilderWrapper.getAggregatorFactories()) {
                searchSourceBuilder.aggregation(aggregationBuilder);
            }
        }

        return searchSourceBuilder;
    }


//    == 집계함수 빌더(AggregationBuilder) ==
    @Override
    public AggregationBuilder getFilterAggregationBuilder(String field, QueryBuilder filter) {
        return AggregationBuilders.filter("filter_" + field, filter);
    }

    @Override
    public AggregationBuilder getTermsAggregationBuilder(String field) {
//      return this.getTermsAggregationBuilder(field, ConstantMap.ELASTICSEARCH.SEARCH.MAX_COUNT);
        return this.getTermsAggregationBuilder(field, 1000);
    }

    @Override
    public AggregationBuilder getTermsAggregationBuilder(String field, int size) {
        return AggregationBuilders.terms("terms_" + field).field(field).size(size);
    }

    @Override
    public AggregationBuilder getSumAggregationBuilder(String field) {
        return AggregationBuilders.sum("sum_" + field).field(field);
    }

    @Override
    public AggregationBuilder getAvgAggregationBuilder(String field) {
        return AggregationBuilders.avg("avg_" + field).field(field);
    }

    @Override
    public AggregationBuilder getMaxAggregationBuilder(String field) {
        return AggregationBuilders.max("max_" + field).field(field);
    }

    @Override
    public AggregationBuilder getMinAggregationBuilder(String field) {
        return AggregationBuilders.min("min_" + field).field(field);
    }

    @Override
    public AggregationBuilder getTopHitsAggregationBuilder(String field, int size) {
        return AggregationBuilders.topHits(field).size(size);
    }

    @Override
    public AggregationBuilder getDateRangeAggregationBuilder(String field, String from, String to) {
        return AggregationBuilders.dateRange("dateRange_" + field).field(field).addRange(from, to);
    }

    @Override
    public AggregationBuilder getDateHistogramAggregationBuilder(String field, DateHistogramInterval DATEHISTOGRAMINTERVAL) {
        return AggregationBuilders.dateHistogram("dateHistogram_" + field).field(field).dateHistogramInterval(DATEHISTOGRAMINTERVAL);
    }

    @Override
    public PipelineAggregationBuilder getMaxBucketAggregationBuilder(String maxTargetAggregationBuilderName, boolean isNumericFieldTheTarget) {
        if (!isNumericFieldTheTarget) {
            maxTargetAggregationBuilderName = "_count";
        }
        return PipelineAggregatorBuilders.maxBucket("maxBucket_" + maxTargetAggregationBuilderName, maxTargetAggregationBuilderName);
    }

    @Override
    public PipelineAggregationBuilder getSumBucketAggregationBuilder(String sumTargetAggregationBuilderName, boolean isNumericFieldTheTarget) {
        if (!isNumericFieldTheTarget) {
            sumTargetAggregationBuilderName = "_count";
        }
        return PipelineAggregatorBuilders.sumBucket("sumBucket_" + sumTargetAggregationBuilderName, sumTargetAggregationBuilderName);
    }

    @Override
    public PipelineAggregationBuilder getCumulativeSumAggregationBuilder(String sumTargetAggregationBuilderName, boolean isNumericFieldTheTarget) {
        if (!isNumericFieldTheTarget) {
            sumTargetAggregationBuilderName = "_count";
        }
        return PipelineAggregatorBuilders.cumulativeSum("cumulativeSum_" + sumTargetAggregationBuilderName, sumTargetAggregationBuilderName);
    }


//    == 빌더 ==
    @Override
    public Builder getBuilder(){
        return new Builder();
    }

    @Override
    public BoolQueryBuilder getBoolQueryBuilder(){
        return QueryBuilders.boolQuery();
    }

//    == 쿼리 빌더 ==
    @Override
    public QueryBuilder getMatchQueryBuilder(String field, String searchTerm, String fuzziness) {
        if(fuzziness == null || fuzziness.equals("")){
            return QueryBuilders.matchQuery(field, searchTerm);
        }else{
            return QueryBuilders.matchQuery(field, searchTerm).fuzziness(Fuzziness.AUTO).prefixLength(3).maxExpansions(10);
        }
    }

    @Override
    public QueryBuilder getMatchPhraseQueryBuilder(String field, String searchTerm) {
        return QueryBuilders.matchPhraseQuery(field, searchTerm);
    }

    @Override
    public QueryBuilder getMatchAllQueryBuilder() {
        return QueryBuilders.matchAllQuery();
    }

    @Override
    public QueryBuilder getTermQueryBuilder(String field, String searchTerm) {
        return QueryBuilders.termQuery(field, searchTerm);
    }

    @Override
    public QueryBuilder getTermsQueryBuilder(String field, String[] searchTermArr) {
        return QueryBuilders.termsQuery(field, searchTermArr);
    }

    @Override
    public QueryBuilder getRangeQueryBuilder(String field, String format, String from, String to) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field).format(format);
        if (from != null && !from.equals("")) {
            rangeQueryBuilder.gte(from);
        }
        if (to != null && !to.equals("")) {
            rangeQueryBuilder.lte(to);
        }
        return rangeQueryBuilder;
    }

    @Override
    public QueryBuilder getRangeQueryBuilder(String field, String from, String to) {
        RangeQueryBuilder rangeQueryBuilder = QueryBuilders.rangeQuery(field);
        if (from != null && !from.equals("")) {
            rangeQueryBuilder.gte(from);
        }
        if (to != null && !to.equals("")) {
            rangeQueryBuilder.lte(to);
        }
        return rangeQueryBuilder;
    }

    @Override
    public QueryBuilder getMultiMatchQueryBuilder(String[] fieldArr, String searchTerm) {
        return QueryBuilders.multiMatchQuery(searchTerm, fieldArr).fuzziness(Fuzziness.AUTO).prefixLength(3).maxExpansions(10);
    }

}
