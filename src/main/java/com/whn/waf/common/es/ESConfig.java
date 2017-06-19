package com.whn.waf.common.es;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.whn.waf.common.es.repository.impl.ESBaseRepositoryImpl;
import com.whn.waf.common.es.support.CustomEntityMapper;
import com.whn.waf.common.es.support.MetaDataResultMapper;
import com.whn.waf.common.exception.WafBizException;
import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.common.transport.TransportAddress;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.*;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.ResultsMapper;
import org.springframework.data.elasticsearch.core.mapping.SimpleElasticsearchMappingContext;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.elasticsearch.repository.support.ElasticsearchRepositoryFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * ElasticSearch配置
 *
 * @author weihainan.
 * @since 0.1 created on 2017/6/16.
 */
@Configuration
@EnableElasticsearchRepositories(
        value = {"com.whn"},
        includeFilters =
        @ComponentScan.Filter(type = FilterType.REGEX, pattern = "com.whn.waf.common.es.*"),
        repositoryBaseClass = ESBaseRepositoryImpl.class
)
@PropertySource(value = {"classpath:es.properties"})
public class ESConfig {

    @Value("${es.transport.port}")
    private int port;

    @Value("${es.host}")
    private String hostname;

    @Value("${es.cluster.name}")
    private String clusterName;

    @Bean
    public ElasticsearchTemplate elasticsearchTemplate() {
        return new ElasticsearchTemplate(client(), resultsMapper());
    }

    @Bean
    public ElasticsearchOperations elasticsearchOperations() {
        return new ElasticsearchTemplate(client(), resultsMapper());
    }

    @Bean
    public ResultsMapper resultsMapper() {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
        objectMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);
        objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
        CustomEntityMapper entityMapper = new CustomEntityMapper(objectMapper);
        return new MetaDataResultMapper(new SimpleElasticsearchMappingContext(), entityMapper);
    }

    @Bean
    public Client client() {
        Settings settings = Settings.settingsBuilder()
                .put("cluster.name", clusterName)
                .build();
        TransportClient client = TransportClient.builder().settings(settings).build();
        TransportAddress address;
        try {
            address = new InetSocketTransportAddress(InetAddress.getByName(hostname), port);
        } catch (UnknownHostException e) {
            throw WafBizException.of("UnknownHostException:" + hostname, e);
        }
        client.addTransportAddress(address);
        return client;
    }

    @Bean
    public ElasticsearchRepositoryFactory elasticsearchRepositoryFactory() {
        return new ElasticsearchRepositoryFactory(elasticsearchOperations());
    }

    public ESConfig() {
        // empty
    }
}

