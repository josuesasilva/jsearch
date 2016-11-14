package br.pucminas.ri.jsearch;

import br.pucminas.ri.jsearch.utils.RankingEnum;
import br.pucminas.ri.jsearch.utils.Constants;
import br.pucminas.ri.jsearch.queryexpansion.RocchioQueryExpansion;
import br.pucminas.ri.jsearch.queryexpansion.QueryExpanded;
import br.pucminas.ri.jsearch.queryexpansion.QueryExpansion;
import br.pucminas.ri.jsearch.rest.model.SimpleDocument;
import br.pucminas.ri.jsearch.rest.model.TermEntry;
import br.pucminas.ri.jsearch.rest.model.UserSearchResponse;
import br.pucminas.ri.jsearch.utils.ConcreteTFIDFSimilarity;
import br.pucminas.ri.jsearch.utils.PorterStemAnalyzer;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Lock;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author josue
 */
public class Searcher {

    public static String documentHtml(int id) {
        Lock lock;
        Path path = Paths.get(Constants.INDEX_PATH);
        String html = "";
        
        try (Directory directory = FSDirectory.open(path)) {
            lock = directory.obtainLock(Constants.LOCK);
            lock.ensureValid();
            
            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                Document d = indexSearcher.doc(id);
                html = d.get(Constants.DOC_HTML);
            }
            
            lock.close();
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        return html;
    }
    
    public static UserSearchResponse performUserQuery(String userQuery) throws IOException, ParseException {
        UserSearchResponse result;
        PorterStemAnalyzer analyzer = new PorterStemAnalyzer();
        QueryParser queryParser = new QueryParser(Constants.DOC_CONTENT, analyzer);
        
        Query query = queryParser.parse(userQuery);
        
        UserSearchResponse firstTesult = search(query);
        
        Query newQuery = QueryExpansion.expandQuery(userQuery, firstTesult.getTerms());
        
        result = search(newQuery);
        
        return result;
    }

    private static HashMap<String, String> getQueriesFromFile(String queriesFile) {
        File file = new File(queriesFile);

        if (file.isFile()) {
            Scanner in = null;
            HashMap<String, String> queries = new HashMap();

            try {
                in = new Scanner(file);
            } catch (IOException e) {
                System.err.println(Arrays.toString(e.getStackTrace()));
            }

            while (in != null && in.hasNextLine()) {

                StringBuilder qid = new StringBuilder();
                StringBuilder query = new StringBuilder();
                String line = in.nextLine();

                if (line.contains("|")) {
                    boolean flag = false;
                    for (char c : line.toCharArray()) {
                        if (c == '|') {
                            flag = true;
                            continue;
                        }

                        if (!flag) {
                            qid.append(c);
                        } else {
                            query.append(c);
                        }
                    }
                } else {
                    boolean flag = false;
                    for (char c : line.toCharArray()) {
                        if (c == ' ') {
                            flag = true;
                        }

                        if (!flag) {
                            qid.append(c);
                        } else {
                            query.append(c);
                        }
                    }
                }

                queries.put(qid.toString(), query.toString());
            }

            if (in != null) {
                in.close();
            }

            return queries;
        }

        return new HashMap<>();
    }
    
    private static UserSearchResponse search(Query query) throws IOException, ParseException {
        UserSearchResponse res;
        Lock lock;
        
        Path path = Paths.get(Constants.INDEX_PATH);
        Date start;
        ArrayList<SimpleDocument> result;
        List<TermEntry> termsResult = new ArrayList<>();
        
        try (Directory directory = FSDirectory.open(path)) {
            lock = directory.obtainLock(Constants.LOCK);
            lock.ensureValid();
            
            try (IndexReader indexReader = DirectoryReader.open(directory)) {
                IndexSearcher indexSearcher = new IndexSearcher(indexReader);
                ConcreteTFIDFSimilarity sim = new ConcreteTFIDFSimilarity();
                
                indexSearcher.setSimilarity(new BM25Similarity(1.2f, 0.75f));
                
                start = new Date();
                
                TopDocs topDocs = indexSearcher.search(query, Constants.MAX_SEARCH);
                ScoreDoc[] hits = topDocs.scoreDocs;
                result = new ArrayList<>();
            
                for (ScoreDoc scoreDoc : hits) {
                    Document doc = indexSearcher.doc(scoreDoc.doc);
                    
                    Terms termVector = indexReader.getTermVector(scoreDoc.doc, Constants.DOC_CONTENT);
                    PostingsEnum postings = null;
                    TermsEnum itr = termVector.iterator();
                    BytesRef bytesRef;
                    
                    while ((bytesRef = itr.next()) != null) {
                        postings = itr.postings(postings);
                        String termText = bytesRef.utf8ToString();
                        
                        while(postings.nextDoc() != PostingsEnum.NO_MORE_DOCS ) {
                            int freq = postings.freq();
                            float tf = sim.tf(freq);
                            float idf = sim.idf(itr.docFreq(), indexReader.numDocs());
                            termsResult.add(new TermEntry(termText, tf*idf));
                        }
                    }
                    
                    result.add(new SimpleDocument(scoreDoc.doc, 
                            doc.get(Constants.DOC_TITLE), 
                            doc.get(Constants.DOC_CONTENT)));
                }
            }
        }
        
        lock.close();
        
        Date end = new Date();

        Collections.sort(termsResult);
        
        res = new UserSearchResponse(result, end.getTime() - start.getTime(), 
                false, query.toString(), termsResult);
        
        res.setError(false);
        
        return res;
    }

    private static void search(HashMap<String, String> queries, RankingEnum ranking) throws FileNotFoundException {
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

        System.out.println("\nQuery average time: "
                + ((end.getTime() - start.getTime()) / queries.keySet().size()
                + " milliseconds\n"));

        System.out.println("\nDone.");
    }
    
    public static void performTests(String queriesFile) throws FileNotFoundException {
        HashMap<String, String> queries = getQueriesFromFile(queriesFile);
        search(queries, RankingEnum.BM25);
        search(queries, RankingEnum.ROCCHIO);
        search(queries, RankingEnum.QUERY_EXPANSION);
    }

    public static void search(String qid, String text, RankingEnum ranking, PrintWriter writer) {
        try {
            Path path = Paths.get(Constants.INDEX_PATH);
            Directory directory = FSDirectory.open(path);
            IndexReader indexReader = DirectoryReader.open(directory);
            IndexSearcher indexSearcher = new IndexSearcher(indexReader);
            PorterStemAnalyzer analyzer = new PorterStemAnalyzer();
            QueryParser queryParser = new QueryParser(Constants.DOC_CONTENT, analyzer);
            String rankingName = new String();

            QueryExpanded qe = null;
            String queryString = "";

            switch (ranking) {
                case ROCCHIO:
                    rankingName = "Rocchio";
                    RocchioQueryExpansion exp
                            = new RocchioQueryExpansion(indexReader, indexSearcher, queryParser);
                    qe = exp.expandQuery(qid, text);
                    queryString = qe.getQuery();
                    break;
                case BM25:
                    rankingName = "BM25";
                    indexSearcher.setSimilarity(new BM25Similarity(1.2f, 0.75f));
                    queryString = text;
                    break;
                case QUERY_EXPANSION:
                    rankingName = "QueryExpansion";
                    QueryExpansion qexp = new QueryExpansion(qid, text);
                    qe = qexp.expandQuery();
                    indexSearcher.setSimilarity(new ConcreteTFIDFSimilarity());
                    queryString = qe.getQuery();
                    break;
                default:
                    break;
            }

            Query query = queryParser.parse(queryString);

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
            
            directory.close();
        } catch (IOException | ParseException e) {
            System.err.println(Arrays.toString(e.getStackTrace()));
        } 
    }
}
