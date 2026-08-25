package com.vnsearch.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

public interface SearchQueryRepository extends MongoRepository<SearchQueryDocument, String> {

    Page<SearchQueryDocument> findByUsernameOrderBySearchedAtDesc(String username,
                                                                  Pageable pageable);

    @Query("{ 'username': ?0, 'normalized': { $regex: ?1, $options: 'i' } }")
    List<SearchQueryDocument> suggestByPrefix(String username, String anchoredPrefix,
                                              Pageable pageable);

    Optional<SearchQueryDocument> findByUsernameAndNormalized(String username, String normalized);

    long deleteByIdAndUsername(String id, String username);

    long deleteByUsernameAndSearchedAtBetween(String username, Instant from, Instant to);
}
