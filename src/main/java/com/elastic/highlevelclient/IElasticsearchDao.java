package com.elastic.highlevelclient;

import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RestHighLevelClient;

public interface IElasticsearchDao {
    SearchResponse searchQuery(SearchRequest searchRequest);

    UpdateResponse updateQuery(UpdateRequest updateRequest);

    BulkResponse bulkQuery(BulkRequest bulkRequest);

    GetMappingsResponse getMapping(GetMappingsRequest request);

    RestHighLevelClient getClient();
}
