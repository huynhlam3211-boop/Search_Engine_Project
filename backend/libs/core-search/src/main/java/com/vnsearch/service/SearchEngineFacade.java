package com.vnsearch.service;

import com.vnsearch.analytics.CorpusStats;
import com.vnsearch.crawler.ContentStorage;
import com.vnsearch.datastructure.LRUCache;
import com.vnsearch.index.IndexPersistence;
import com.vnsearch.index.InvertedIndex;
import com.vnsearch.index.SearchIndex;
import com.vnsearch.index.Tokenizer;
import com.vnsearch.model.SearchResponse;
import com.vnsearch.model.SearchResult;
import com.vnsearch.model.WebDocument;
import com.vnsearch.query.CandidateResolver;
import com.vnsearch.query.QueryParser;
import com.vnsearch.ranking.PageRankService;
import com.vnsearch.ranking.RelevanceScorer;
import com.vnsearch.ranking.ResultRanker;
import com.vnsearch.ranking.ScorerFactory;
import com.vnsearch.storage.DocumentStore;
import com.vnsearch.storage.JsonDocumentStore;
import com.vnsearch.storage.PostgresDocumentStore;
import jakarta.annotation.PostConstruct;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

@Service 
public class SearchEngineFacade {
    
}