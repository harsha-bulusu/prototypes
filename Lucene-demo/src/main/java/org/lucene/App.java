package org.lucene;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Hello world!
 */
public class App {
    public static void main(String[] args) throws IOException {
        Analyzer analyzer = new StandardAnalyzer();

        String indexPath = "/Users/sriharsha/index2";
        Directory index = FSDirectory.open(Paths.get(indexPath));

        IndexWriterConfig config = new IndexWriterConfig(analyzer);

        IndexWriter indexWriter = new IndexWriter(index, config);
        IndexWriter indexWriter2 = new IndexWriter(index, config);

        Document document = new Document();
        document.add(new StringField("id", "1", Field.Store.YES));
        document.add(new StringField("name", "Harsha", Field.Store.YES));

        indexWriter.addDocument(document);
        indexWriter.close();

        System.out.println("✅ Document indexed successfully.");
    }
}
