package com.vnsearch.history;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;

@Document(collection = "visits")
@CompoundIndex(name = "ix_visits_user_time", def ="{'username':1, 'visitedAt':-1}")
@CompoundIndex(name = "ix_visits_user_url", def = "{'username': 1, 'url': 1}")
public record VisitDocument(
    @Id String id,
    String username,
    String url,
    String title,
    String host,
    @Indexed(name = "ix_visits_ttl", expireAfter = "90d")
    Instant visitedAt,
    int visitCount,
    boolean incognito
) {

}