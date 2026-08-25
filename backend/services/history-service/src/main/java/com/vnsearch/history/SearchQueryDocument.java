package com.vnsearch.history;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "search_queries")
@CompoundIndex(name = "ix_queries_user_time", def = "{'username': 1, 'searchedAt': -1}")

@CompoundIndex(name = "ix_queries_user_prefix", def = "{'username': 1, 'normalized': 1}")
public record SearchQueryDocument(
        @Id String id,
        String username,

        String query,

        String normalized,

        int resultCount,

        @Indexed(name = "ix_queries_ttl", expireAfter = "30d")
        Instant searchedAt) {
}
