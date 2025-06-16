package org.lucene.analyzers;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.CharArraySet;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.core.KeywordAnalyzer;
import org.apache.lucene.analysis.core.SimpleAnalyzer;
import org.apache.lucene.analysis.core.StopAnalyzer;
import org.apache.lucene.analysis.core.WhitespaceAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.util.BytesRef;

import java.io.IOException;
import java.util.List;

public class Analyzers {
    public static void main(String[] args) throws IOException {
//        analyze(new StandardAnalyzer());
//        System.out.println("==========================");
//        analyze(new WhitespaceAnalyzer());
//        System.out.println("==========================");
//        analyze(new SimpleAnalyzer());
//        analyze(new StopAnalyzer(new CharArraySet(List.of("The"), true)));
//        analyze(new KeywordAnalyzer());
    }

    private static void analyze(Analyzer analyzer) throws IOException {
        //  ✅ Useful for:
        //        Sorting
        //        Keyword fields
        //        Exact-match comparisons
        //  ❌ Not for:
        //        Full-text indexing/search
        //        runs only character filters + normalization filters, not tokenizers.
        // normalize() is meant for lower casing, ASCII folding, and similar lightweight transformations, not HTML stripping or complex tokenization.
        BytesRef normalized = analyzer.normalize("name", "<h1>The Fourth Working day</h1>");

        /*
            ✅ Used when:
                You want to index content
                You want to parse queries
                You want to inspect or debug the token stream
         */
        TokenStream tokenStream = analyzer.tokenStream("name", "<h1>The Fourth Working day</h1>");
        CharTermAttribute termAttribute = tokenStream.addAttribute(CharTermAttribute.class);
        System.out.println(new String(normalized.bytes));

        tokenStream.reset();
        while(tokenStream.incrementToken()) {
            System.out.println(termAttribute.toString());
        }
        tokenStream.end();
        tokenStream.close();
    }

}
