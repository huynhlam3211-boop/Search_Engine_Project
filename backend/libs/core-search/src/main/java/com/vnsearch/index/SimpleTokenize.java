
package com.vnsearch.index;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public final class SimpleTokenize implements Tokenizer {

    @Override
    public List<Token> tokenize(String text) {
        List<Token> tokens = new ArrayList<>();

        if (text == null || text.isBlank()) {
            return tokens;
        }

        String[] words = text.toLowerCase(Locale.ROOT)
                .trim()
                .split("\\s+");

        int position = 0;

        for (String word : words) {
            if (word.isEmpty()) {
                continue;
            }

            tokens.add(new Token(word, position));
            position++;
        }

        return tokens;
    }

    @Override
    public String name() {
        return "SimpleTokenizer(whitespace, giu dau)";
    }

    public static void main(String[] args) {

        // Tạo một SimpleTokenize
        SimpleTokenize tokenizer = new SimpleTokenize();

        // Text muốn test
        String text = "Hello   WORLD   Java Programming";

        // Gọi hàm tokenize()
        List<Token> tokens = tokenizer.tokenize(text);

        // In kết quả
        System.out.println("Tokenizer: " + tokenizer.name());
        System.out.println("Text: " + text);
        System.out.println();

        for (Token token : tokens) {
            System.out.println(
                    "term = " + token.term()
                    + ", position = " + token.position()
            );
        }
    }
}

