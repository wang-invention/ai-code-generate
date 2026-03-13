package com.wang.wangaicodemother.rag.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

/**
 * Vue 知识库实体类
 * 对应 ES 索引：vue_knowledge
 */
@Data
@Document(indexName = "rag_docs_xiao")  // 对应你的索引名
public class RagDocsXiao {

    // ES 文档 ID（String 类型）
    @Id
    private String id;

    // 标题
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String title;

    // 内容（知识库正文）
    @Field(type = FieldType.Text, analyzer = "ik_max_word")
    private String content;

    @Field(type = FieldType.Dense_Vector, dims = 384)
    private List<Double> vector;

    // 无参构造
    public RagDocsXiao() {
    }

}