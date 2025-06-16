package com.example.luceneapp;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.*;
import org.apache.lucene.index.*;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.*;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BenchMarking {

    public static void main(String[] args) throws Exception {
        int NUM_DOCS = 1_000_000;
        String fuzzyTerm = "documnt~2";
        String wildcardTerm = "docum*";

        Analyzer analyzer = new StandardAnalyzer();
        String indexPath = "/Users/sriharsha/index";  // Change this to your desired path

        // 1. Indexing
        IndexWriterConfig config = new IndexWriterConfig(analyzer);
        config.setOpenMode(IndexWriterConfig.OpenMode.CREATE); // overwrite if index exists

        long startIndex = System.currentTimeMillis();
        try (Directory luceneIndex = FSDirectory.open(Paths.get(indexPath));
             IndexWriter writer = new IndexWriter(luceneIndex, config)) {

            System.out.println("Indexing " + NUM_DOCS + " documents...");
            for (int i = 0; i < NUM_DOCS; i++) {
                Document doc = new Document();
                doc.add(new StringField("id", String.valueOf(i), Field.Store.YES));
                doc.add(new TextField("title", "Title " + i, Field.Store.NO));
                doc.add(new TextField("content", "Document number " + i, Field.Store.NO));
                writer.addDocument(doc);

                if (i > 0 && i % 100_000 == 0) {
                    System.out.println("Indexed " + i + " docs");
                }
            }
        }
        long endIndex = System.currentTimeMillis();
        System.out.println("Indexing took: " + (endIndex - startIndex) + " ms");

        // 2. Search
        try (Directory luceneIndex = FSDirectory.open(Paths.get(indexPath));
             DirectoryReader reader = DirectoryReader.open(luceneIndex)) {

            IndexSearcher searcher = new IndexSearcher(reader);
            QueryParser parser = new QueryParser("content", analyzer);

            // Fuzzy Search
            Query fuzzyQuery = parser.parse(fuzzyTerm);
            long startFuzzy = System.currentTimeMillis();
            TopDocs fuzzyResults = searcher.search(fuzzyQuery, 10);
            long endFuzzy = System.currentTimeMillis();

            System.out.println("Fuzzy search '" + fuzzyTerm + "' took: " + (endFuzzy - startFuzzy) + " ms");
            System.out.println("Fuzzy search total hits: " + fuzzyResults.totalHits.value);

            // Wildcard Search
            Query wildcardQuery = parser.parse(wildcardTerm);
            long startWildcard = System.currentTimeMillis();
            TopDocs wildcardResults = searcher.search(wildcardQuery, 10);
            long endWildcard = System.currentTimeMillis();

            System.out.println("Wildcard search '" + wildcardTerm + "' took: " + (endWildcard - startWildcard) + " ms");
            System.out.println("Wildcard search total hits: " + wildcardResults.totalHits.value);
        }

        // 3. Naive Java List search
        System.out.println("Preparing Java list...");
        List<HashMap<String, String>> javaList = new ArrayList<>();
        for (int i = 0; i < NUM_DOCS; i++) {
            HashMap<String, String> doc = new HashMap<>();
            doc.put("id", String.valueOf(i));
            doc.put("title", "Title " + i);
            doc.put("content", "Document number " + i);
            javaList.add(doc);
        }

        // Fuzzy simulation (just contains)
        String fuzzySearchTerm = "documnt";
        long startJavaFuzzy = System.currentTimeMillis();
        int javaFuzzyHits = 0;
        for (HashMap<String, String> doc : javaList) {
            if (doc.get("content").contains(fuzzySearchTerm)) {
                javaFuzzyHits++;
            }
        }
        long endJavaFuzzy = System.currentTimeMillis();
        System.out.println("Java naive fuzzy search (contains '" + fuzzySearchTerm + "') took: " + (endJavaFuzzy - startJavaFuzzy) + " ms");
        System.out.println("Java naive fuzzy search hits: " + javaFuzzyHits);

        // Wildcard simulation (startsWith)
        String wildcardSearchTerm = "Docum";
        long startJavaWildcard = System.currentTimeMillis();
        int javaWildcardHits = 0;
        for (HashMap<String, String> doc : javaList) {
            if (doc.get("content").startsWith(wildcardSearchTerm)) {
                javaWildcardHits++;
            }
        }
        long endJavaWildcard = System.currentTimeMillis();
        System.out.println("Java naive wildcard search (startsWith '" + wildcardSearchTerm + "') took: " + (endJavaWildcard - startJavaWildcard) + " ms");
        System.out.println("Java naive wildcard search hits: " + javaWildcardHits);
    }
}
