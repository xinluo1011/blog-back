package com.sit.manage;
import com.alibaba.fastjson.JSON;
import com.sit.manage.entity.BlogDoc;
import com.sit.manage.entity.SysBlog;
import com.sit.manage.service.SysBlogService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.List;


@SpringBootTest
class ManageApplicationTests {

    private RestHighLevelClient client;
    @Resource
    private SysBlogService blogService;

    @Test
    void contextLoads() {
    }


    @Test
    void addBulkBlog() throws IOException {
        List<SysBlog> blogs = (List<SysBlog>) blogService.findAll().getData();
        BulkRequest request = new BulkRequest();
        for(SysBlog blog : blogs){
            BlogDoc blogDoc = new BlogDoc(blog);
            request.add(new IndexRequest("blog")
                    .id(blogDoc.getId().toString())
                    .source(JSON.toJSONString(blogDoc), XContentType.JSON));
        }
        client.bulk(request, RequestOptions.DEFAULT);
    }


    @BeforeEach
    void setUp() {
        client = new RestHighLevelClient(RestClient.builder(
                HttpHost.create("http://192.168.10.10:9200")
        ));
    }

    @AfterEach
    void tearDown() throws IOException {
        client.close();
    }
}
