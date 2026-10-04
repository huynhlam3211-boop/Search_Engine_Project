package com.vnsearch.index;

public interface SearchIndex{
    List<Posting> getPostings(String term);

    int getDocumentFrequency(String term);

    int getTotalDocs();
    
}