/*
 * Copyright (C) 2016 793604
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.pucminas.ri.jsearch.queryexpansion;

import br.pucminas.ri.jsearch.Constants;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.LeafReader;
import org.apache.lucene.index.LeafReaderContext;
import org.apache.lucene.index.PostingsEnum;
import org.apache.lucene.index.Terms;
import org.apache.lucene.index.TermsEnum;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.LockObtainFailedException;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.BytesRef;

/**
 *
 * @author Josué
 */
public class RocchioQueryExpansion {

    private final int MAX_DOCS = 10;
    private final int TERMS_LIMIT = 10;
    private final float BETA = 0.8f;
    private final float ALPHA = 1.0f;

    private final IndexSearcher indexSeacher;
    private final IndexReader indexReader;
    private final QueryParser queryParser;
    private final ArrayList<Document> relevatDocuments;

    public RocchioQueryExpansion(IndexReader indexReader, IndexSearcher indexSeacher, QueryParser queryParser) {
        relevatDocuments = new ArrayList<>();
        this.indexSeacher = indexSeacher;
        this.queryParser = queryParser;
        this.indexReader = indexReader;
    }

    public RocchioQuery expandQuery(String qId, String strQuery) {
        
        loadTopDocs(strQuery);

        try {
            Directory index = createIndex(relevatDocuments);

            List<Entry<String, Double>> termsVector = getTermScoreList(index);

            for (String term : strQuery.split("\\s+")) {
                double score = ALPHA * getScore(index, term);
                boolean found = false;
                for (Entry<String, Double> entry : termsVector) {
                    if (entry.getKey().equalsIgnoreCase(term)) {
                        entry.setValue(entry.getValue() + score);
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    termsVector.add(new SimpleEntry<>(term, score));
                }
            }
            
            Collections.sort(termsVector, new Comparator<Entry<String, Double>>() {
                @Override
                public int compare(Entry<String, Double> o1, Entry<String, Double> o2) {
                    return o1.getValue().compareTo(o2.getValue());
                }
            });
            Collections.reverse(termsVector);
            
            StringBuilder rocchioTerms = new StringBuilder();
            
            for (int i = 0; i < TERMS_LIMIT && i < termsVector.size(); i++) {
                rocchioTerms.append(' ').append(termsVector.get(i).getKey());
            }
            
            return new RocchioQuery(qId, rocchioTerms.toString().trim());

        } catch (LockObtainFailedException ex) {
            Logger.getLogger(RocchioQueryExpansion.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RocchioQueryExpansion.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return new RocchioQuery(qId, "");
    }

    private void loadTopDocs(String strQuery) {
        Query query = null;

        try {
            query = queryParser.parse(strQuery);
        } catch (ParseException ex) {
            Logger.getLogger(RocchioQueryExpansion.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (query != null) {
            TopDocs topDocs = null;

            try {
                topDocs = indexSeacher.search(query, MAX_DOCS);
            } catch (IOException ex) {
                Logger.getLogger(RocchioQueryExpansion.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (topDocs != null) {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

                    Document doc = null;

                    try {
                        doc = indexSeacher.doc(scoreDoc.doc);
                    } catch (IOException ex) {
                        Logger.getLogger(RocchioQueryExpansion.class.getName()).log(Level.SEVERE, null, ex);
                    }

                    if (doc != null) {
                        relevatDocuments.add(doc);
                    }
                }
            }
        }
    }

    private Directory createIndex(ArrayList<Document> relevantDocs)
            throws CorruptIndexException, LockObtainFailedException, IOException {

        Directory index = new RAMDirectory();
        Analyzer analyzer = new StandardAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);

        try (IndexWriter idxWriter = new IndexWriter(index, conf)) {
            for (Document d : relevantDocs) {
                idxWriter.addDocument(d);
            }
        }

        return index;
    }

    private List<Entry<String, Double>> getTermScoreList(Directory directory)
            throws CorruptIndexException, IOException {

        Map<String, Double> termScoreMap = new HashMap<>();

        try (IndexReader idxReader = DirectoryReader.open(directory)) {

            idxReader.leaves().stream().map((leaf) -> leaf.reader()).forEach((reader) -> {
                try {
                    Terms terms = reader.terms(Constants.DOC_CONTENT);
                    TermsEnum termsEnum = terms.iterator();
                    PostingsEnum postingsEnum = termsEnum.postings(null);
                    int docsNum = idxReader.numDocs();

                    BytesRef text;
                    while ((text = termsEnum.next()) != null) {
                        double tf = tf(termsEnum.totalTermFreq());
                        double idf = idf(docsNum, termsEnum.docFreq());
                        double tfidf = tf * idf;
                        termScoreMap.put(text.utf8ToString(), BETA * tfidf);
                    }

                } catch (IOException ex) {
                    Logger.getLogger(RocchioQueryExpansion.class.getName())
                            .log(Level.SEVERE, null, ex);
                } finally {
                    try {
                        idxReader.close();
                    } catch (IOException ex) {
                        Logger.getLogger(RocchioQueryExpansion.class.getName())
                                .log(Level.SEVERE, null, ex);
                    }
                }
            });

        }

        return new ArrayList<>(termScoreMap.entrySet());
    }

    private double getScore(Directory directory, String term)
            throws CorruptIndexException, IOException {

        try (IndexReader idxReader = DirectoryReader.open(directory)) {

            for (LeafReaderContext context : idxReader.leaves()) {
                LeafReader reader = context.reader();

                try {
                    Terms terms = reader.terms(Constants.DOC_CONTENT);
                    TermsEnum termsEnum = terms.iterator();
                    PostingsEnum postingsEnum = termsEnum.postings(null);
                    int docsNum = idxReader.numDocs();

                    BytesRef text;
                    while ((text = termsEnum.next()) != null) {
                        if (text.utf8ToString().equalsIgnoreCase(term)) {
                            double tf = tf(termsEnum.totalTermFreq());
                            double idf = idf(docsNum, termsEnum.docFreq());
                            double tfidf = tf * idf;
                            idxReader.close();
                            return tfidf;
                        }
                    }

                } catch (IOException ex) {
                    Logger.getLogger(RocchioQueryExpansion.class.getName())
                            .log(Level.SEVERE, null, ex);
                }
            }

        }

        return 0;
    }
    
    private double tf(long tfInCollection) {
        return 1 + ((Math.log(tfInCollection) / Math.log(2)));
    }
    
    private double idf(int docsSize, int docFreq) {
        return (Math.log(docsSize/docFreq) / Math.log(2));
    }
}
