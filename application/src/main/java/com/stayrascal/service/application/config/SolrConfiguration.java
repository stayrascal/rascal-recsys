package com.stayrascal.service.application.config;

import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SolrConfiguration {
    @Autowired
    private SolrProperties solrProperties;

    @Bean
    public SolrClient solrClient() {
        return new HttpSolrClient.Builder(solrProperties.getAddress())
                .withConnectionTimeout(solrProperties.getConnectionTimeout())
                .withSocketTimeout(solrProperties.getSocketTimeout())
                .build();
    }


}
