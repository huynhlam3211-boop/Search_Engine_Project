package com.vnsearch.eval;

import com.vnsearch.crawler.ContentStorage;
import com.vnsearch.index.InvertedIndex;
import com.vnsearch.model.WebDocument;
import com.vnsearch.ranking.BM25Scorer;
import com.vnsearch.ranking.PageRankService;
import com.vnsearch.ranking.TfIdfScorer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map; 

public class EvaluationRunner { 
    
}