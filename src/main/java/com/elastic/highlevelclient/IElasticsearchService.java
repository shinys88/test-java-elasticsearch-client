package com.elastic.highlevelclient;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.search.aggregations.AggregationBuilder;
import org.elasticsearch.search.aggregations.AggregatorFactories;
import org.elasticsearch.search.aggregations.PipelineAggregationBuilder;
import org.elasticsearch.search.aggregations.bucket.histogram.DateHistogramInterval;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;

import java.util.List;
import java.util.Map;

public interface IElasticsearchService {
    SearchResponse getSearchResponse(SearchRequest searchRequest);

    RestHighLevelClient getClient();

    Map<String, Object> callElasticApi(String method, String url, Object obj, String jsonData);

    UpdateRequest getUpdateRequest(String index, String type, String id);

    UpdateRequest getUpsertRequest(UpdateRequest updateRequest, Map source);

    //    update
    UpdateResponse getUpdateResponse(UpdateRequest updateRequest);

    BulkRequest getBulkRequest();

    //    bulk
    BulkResponse getBulkResponse(BulkRequest bulkRequest);

    //    getmapping
    GetMappingsResponse getMappingsResponse(String index);

    SearchRequest getSearchRequest(SearchSourceBuilder searchSourceBuilder, String index);

    //    == 검색소스 빌더(SearchSourceBuilder) ==
    HighlightBuilder getHighlightBuilder(String[] fieldArr);

    SortBuilder getSortBuilder(String field, String sortOrder);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList, String[] fetchIncludeArr);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList, float minScore);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList, String[] fetchIncludeArr, float minScore);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size, float minScore);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr, float minScore);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size, float minScore);

    SearchSourceBuilder getSearchSourceBuilder(QueryBuilder queryBuilder, AggregatorFactories.Builder aggregationBuilderWrapper, HighlightBuilder highlightBuilder, List<SortBuilder> sortBuilderList
            , int from, int size, String[] fetchIncludeArr, float minScore);

    //    == 집계함수 빌더(AggregationBuilder) ==
    AggregationBuilder getFilterAggregationBuilder(String field, QueryBuilder filter);

    AggregationBuilder getTermsAggregationBuilder(String field);

    AggregationBuilder getTermsAggregationBuilder(String field, int size);

    AggregationBuilder getSumAggregationBuilder(String field);

    AggregationBuilder getAvgAggregationBuilder(String field);

    AggregationBuilder getMaxAggregationBuilder(String field);

    AggregationBuilder getMinAggregationBuilder(String field);

    AggregationBuilder getTopHitsAggregationBuilder(String field, int size);

    AggregationBuilder getDateRangeAggregationBuilder(String field, String from, String to);

    AggregationBuilder getDateHistogramAggregationBuilder(String field, DateHistogramInterval DATEHISTOGRAMINTERVAL);

    PipelineAggregationBuilder getMaxBucketAggregationBuilder(String maxTargetAggregationBuilderName, boolean isNumericFieldTheTarget);

    PipelineAggregationBuilder getSumBucketAggregationBuilder(String sumTargetAggregationBuilderName, boolean isNumericFieldTheTarget);

    PipelineAggregationBuilder getCumulativeSumAggregationBuilder(String sumTargetAggregationBuilderName, boolean isNumericFieldTheTarget);

    //    == 빌더 ==
    AggregatorFactories.Builder getBuilder();

    BoolQueryBuilder getBoolQueryBuilder();


    //    == 쿼리 빌더 ==
    QueryBuilder getMatchQueryBuilder(String field, String searchTerm, String fuzziness);

    QueryBuilder getMatchPhraseQueryBuilder(String field, String searchTerm);

    QueryBuilder getMatchAllQueryBuilder();

    QueryBuilder getTermQueryBuilder(String field, String searchTerm);

    QueryBuilder getTermsQueryBuilder(String field, String[] searchTermArr);

    QueryBuilder getRangeQueryBuilder(String field, String format, String from, String to);

    QueryBuilder getRangeQueryBuilder(String field, String from, String to);

    QueryBuilder getMultiMatchQueryBuilder(String[] fieldArr, String searchTerm);
}
