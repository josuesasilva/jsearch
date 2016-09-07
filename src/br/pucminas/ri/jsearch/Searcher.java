package br.pucminas.ri.jsearch;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;

/**
 *
 * @author josue
 */
public class Searcher {

    public static void search(String text) {
        try {
            Path path = Paths.get(Constants.INDEX_PATH);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            QueryParser queryParser = new QueryParser("contents", new StandardAnalyzer());
            Query query = queryParser.parse(text);
            TopDocs topDocs = indexSearcher.search(query, 10);

            System.out.println("totalHits " + topDocs.totalHits);

            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);
            }

        } catch (IOException | ParseException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
