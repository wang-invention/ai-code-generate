package com.wang.wangaicodemother.rag;

import co.elastic.clients.elasticsearch._types.KnnQuery;
import com.wang.wangaicodemother.model.entity.VueKnowledge;
import com.wang.wangaicodemother.rag.domain.RagDocsXiao;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class VueRagService {

    @Autowired
    private ElasticsearchOperations elasticsearchOperations;

    public List<RagDocsXiao> searchByVector(List<Float> vector) {

        NativeQuery query = NativeQuery.builder()
                .withQuery(q -> q
                        .knn(knn -> knn
                                .field("vector")
                                .queryVector(vector)
                                .numCandidates(50)
                        )
                )
                .withMaxResults(5)
                .build();

        SearchHits<RagDocsXiao> hits =
                elasticsearchOperations.search(query, RagDocsXiao.class);

        return hits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }
}