package com.vnsearch.index;

import java.util.List;

public interface Tokenizer {

    List<Token> tokenize(String text);

    String name();

    record Token(String term, int position){
        
    }
}