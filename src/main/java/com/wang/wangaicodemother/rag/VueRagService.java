package com.wang.wangaicodemother.rag;

import co.elastic.clients.elasticsearch._types.KnnQuery;
import com.wang.wangaicodemother.model.entity.VueKnowledge;
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

    public List<VueKnowledge> searchByVector(List<Float> vector) {

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

        SearchHits<VueKnowledge> hits =
                elasticsearchOperations.search(query, VueKnowledge.class);

        return hits.stream()
                .map(hit -> hit.getContent())
                .toList();
    }
}