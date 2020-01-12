package com.elastic.highlevelclient.impl;

import com.common.ConstantMap;
import com.elastic.highlevelclient.IElasticsearchDao;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsRequest;
import org.elasticsearch.action.admin.indices.mapping.get.GetMappingsResponse;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.action.update.UpdateResponse;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestClientBuilder;
import org.elasticsearch.client.RestHighLevelClient;
import org.springframework.stereotype.Repository;

import java.io.IOException;

@Repository
public class ElasticsearchDaoImpl implements IElasticsearchDao {

    final CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
    private RestHighLevelClient client;

    private static final String ip = ConstantMap.ELASTICSEARCH.IP;
    private static final int port = ConstantMap.ELASTICSEARCH.PORT;
    private static final String id = ConstantMap.ELASTICSEARCH.ID;
    private static final String pw = ConstantMap.ELASTICSEARCH.PW;

    public ElasticsearchDaoImpl(){

        credentialsProvider.setCredentials(AuthScope.ANY,new UsernamePasswordCredentials(id, pw));
        RestClientBuilder builder = RestClient.builder(new HttpHost(ip, port, "http"))
                .setHttpClientConfigCallback(httpClientBuilder -> httpClientBuilder.setDefaultCredentialsProvider(credentialsProvider))
                .setRequestConfigCallback(restClientBuilder -> restClientBuilder
                        .setConnectTimeout(5000)
                        .setSocketTimeout(300000));

        client = new RestHighLevelClient(builder);

    }

    @Override
    public SearchResponse searchQuery(SearchRequest searchRequest){
        SearchResponse searchResponse = null;
        try {
            searchResponse = client.search(searchRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return searchResponse;
    }

    @Override
    public UpdateResponse updateQuery(UpdateRequest updateRequest){
        UpdateResponse updateResponse = null;
        try {
            updateResponse = client.update(updateRequest, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return updateResponse;
    }

    @Override
    public BulkResponse bulkQuery(BulkRequest bulkRequest){
        BulkResponse bulkResponse = null;
        try {
            bulkResponse = client.bulk(bulkRequest,RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return bulkResponse;
    }

    @Override
    public GetMappingsResponse getMapping(GetMappingsRequest request){
        GetMappingsResponse getMappingsResponse = null;
        try {
            getMappingsResponse = client.indices().getMapping(request, RequestOptions.DEFAULT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return getMappingsResponse;
    }

    @Override
    public RestHighLevelClient getClient(){
        return client;
    }

}
