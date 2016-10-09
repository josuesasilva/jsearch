package br.pucminas.ri.jsearch;

import br.pucminas.ri.jsearch.utils.RankingEnum;
import br.pucminas.ri.jsearch.utils.Constants;
import br.pucminas.ri.jsearch.queryexpansion.RocchioQueryExpansion;
import br.pucminas.ri.jsearch.queryexpansion.QueryExpanded;
import br.pucminas.ri.jsearch.queryexpansion.QueryExpansion;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import org.apache.lucene.analysis.Analyzer;
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
import org.apache.lucene.search.similarities.BM25Similarity;

/**
 *
 * @author josue
 */
public class Searcher {

    public static void search(HashMap<String, String> queries, RankingEnum ranking) throws FileNotFoundException {
        String tag = "default";

        switch (ranking) {
            case ROCCHIO:
                tag = "Rocchio";
                break;
            case BM25:
                tag = "BM25";
                break;
            case QUERY_EXPANSION:
                tag = "QueryExpansion";
                break;
            default:
                break;
        }

        File outDir = new File(Constants.RESULT_PATH);
        outDir.mkdir();
        String resultsFile = String.format("%s/%s.out",
                Constants.RESULT_PATH, tag);
        File outFile = new File(resultsFile);
        
        System.out.printf("Writing query results to '%s'...\n", resultsFile);
        
        Date start = new Date();
        
        try (PrintWriter writer = new PrintWriter(outFile)) {
            queries.keySet().stream().forEach((key) -> {
                String query = queries.get(key);
                search(key, query, ranking, writer);
            });    
            writer.close();
        }
        
        Date end = new Date();
        
        System.out.println("\nQuery average time: " + 
                ((end.getTime() - start.getTime())/queries.keySet().size()
                + " milliseconds\n"));
        
        System.out.println("\nDone.");
    }

    public static void search(String qid, String text, RankingEnum ranking,
            PrintWriter writer) {
        try {
            Path path = Paths.get(Constants.INDEX_PATH);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            Analyzer analyzer = new StandardAnalyzer();
            QueryParser queryParser = new QueryParser(Constants.DOC_CONTENT, analyzer);
            String rankingName = new String();

            QueryExpanded qe = null;

            switch (ranking) {
                case ROCCHIO:
                    rankingName = "Rocchio";
                    RocchioQueryExpansion exp = 
                            new RocchioQueryExpansion(indexReader, indexSearcher, queryParser);
                    qe = exp.expandQuery(qid, text);
                    break;
                case BM25:
                    rankingName = "BM25";
                    indexSearcher.setSimilarity(new BM25Similarity(1.2f, 0.75f));
                    break;
                case QUERY_EXPANSION:
                    rankingName = "QueryExpansion";
                    QueryExpansion qexp = new QueryExpansion(qid, text);
                    qe = qexp.expandQuery();
                    break;
                default:
                    break;
            }
            
            Query query = qe != null ? queryParser.parse(qe.getQuery()) : 
                    queryParser.parse(text);
            
            TopDocs topDocs = indexSearcher.search(query, Constants.MAX_SEARCH);
            ScoreDoc[] hits = topDocs.scoreDocs;
            
            int i = 0;
            for (ScoreDoc scoreDoc : topDocs.scoreDocs) {
                Document document = indexSearcher.doc(scoreDoc.doc);

                String out = String.format("%s %s %s %d %f %s", qid, "Q0",
                        document.get("docno"), i, hits[i].score, rankingName);

                if (writer != null) {
                    writer.println(out);
                } else {
                    System.out.println(out);
                }

                i++;
            }

        } catch (IOException | ParseException e) {
            System.out.println(Arrays.toString(e.getStackTrace()));
        }
    }
}
