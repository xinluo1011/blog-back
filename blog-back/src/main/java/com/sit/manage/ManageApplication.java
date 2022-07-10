package com.sit.manage;

import org.apache.http.HttpHost;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@MapperScan("com.sit.manage.mapper")
@SpringBootApplication
public class ManageApplication {


    public static void main(String[] args) {
        SpringApplication.run(ManageApplication.class, args);
    }

    @Bean
    public RestHighLevelClient newRestHighLevelClient(){
        return new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.10.10:9200")
        ));
    }

}
