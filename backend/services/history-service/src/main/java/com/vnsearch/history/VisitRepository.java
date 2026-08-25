package com.vnsearch.history;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.Instant;
import java.util.Optional;

public interface VisitRepository extends MongoRepository<VisitDocument, String> {

    Page<VisitDocument> findByUsernameOrderByVisitedAtDesc(String username, Pageable pageable);

    Page<VisitDocument> findByUsernameAndVisitedAtBetweenOrderByVisitedAtDesc(
            String username, Instant from, Instant to, Pageable pageable);

    @Query("{ 'username': ?0, $or: [ { 'title': { $regex: ?1, $options: 'i' } }, "
            + "{ 'url': { $regex: ?1, $options: 'i' } } ] }")
    Page<VisitDocument> searchByKeyword(String username, String keyword, Pageable pageable);

    Optional<VisitDocument> findByUsernameAndUrl(String username, String url);

    long deleteByIdAndUsername(String id, String username);

    long deleteByUsernameAndVisitedAtBetween(String username, Instant from, Instant to);

    long countByUsername(String username);
}