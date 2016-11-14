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

import br.pucminas.ri.jsearch.utils.ConcreteTFIDFSimilarity;
import br.pucminas.ri.jsearch.utils.Constants;
import br.pucminas.ri.jsearch.utils.PorterStemAnalyzer;
import java.io.IOException;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    private final IndexSearcher indexSearcher;
    private final IndexReader indexReader;
    private final QueryParser queryParser;
    private final ArrayList<Document> relevatDocuments;

    public RocchioQueryExpansion(IndexReader indexReader, IndexSearcher indexSeacher, QueryParser queryParser) {
        relevatDocuments = new ArrayList<>();
        this.indexSearcher = indexSeacher;
        this.queryParser = queryParser;
        this.indexReader = indexReader;
    }

    public QueryExpanded expandQuery(String qId, String strQuery) {

        loadTopDocs(strQuery);

        try {
            Directory index = createIndex(relevatDocuments);

            List<Entry<String, Float>> termsVector = getTermScoreList(index);

            for (String term : strQuery.split("\\s+")) {
                float score = ALPHA * getScore(index, term);
                boolean found = false;
                for (Entry<String, Float> entry : termsVector) {
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

            Collections.sort(termsVector, 
                    (Entry<String, Float> o1, Entry<String, Float> o2) -> 
                            o1.getValue().compareTo(o2.getValue()));
            
            Collections.reverse(termsVector);

            StringBuilder rocchioTerms = new StringBuilder();

            for (int i = 0; i < TERMS_LIMIT && i < termsVector.size(); i++) {
                rocchioTerms.append(' ').append(termsVector.get(i).getKey());
            }

            return new QueryExpanded(qId, rocchioTerms.toString().trim());

        } catch (LockObtainFailedException ex) {
            Logger.getLogger(RocchioQueryExpansion.class.getName())
                    .log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(RocchioQueryExpansion.class.getName())
                    .log(Level.SEVERE, null, ex);
        }

        return new QueryExpanded(qId, "");
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
                topDocs = indexSearcher.search(query, MAX_DOCS);
            } catch (IOException ex) {
                Logger.getLogger(RocchioQueryExpansion.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (topDocs != null) {
                for (ScoreDoc scoreDoc : topDocs.scoreDocs) {

                    Document doc = null;

                    try {
                        doc = indexSearcher.doc(scoreDoc.doc);
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
        PorterStemAnalyzer analyzer = new PorterStemAnalyzer();
        IndexWriterConfig conf = new IndexWriterConfig(analyzer);

        try (IndexWriter idxWriter = new IndexWriter(index, conf)) {
            for (Document d : relevantDocs) {
                idxWriter.addDocument(d);
            }
        }

        return index;
    }

    private List<Entry<String, Float>> getTermScoreList(Directory directory)
            throws CorruptIndexException, IOException {

        Map<String, Float> termScoreMap = new HashMap<>();

        ConcreteTFIDFSimilarity sim = new ConcreteTFIDFSimilarity();

        try (IndexReader idxReader = DirectoryReader.open(directory)) {

            idxReader.leaves().stream().map((leaf) -> leaf.reader()).forEach((reader) -> {
                try {
                    Terms terms = reader.terms(Constants.DOC_CONTENT);
                    TermsEnum termsEnum = terms.iterator();
                    PostingsEnum postings = null;
                    int docsNum = idxReader.numDocs();

                    BytesRef text;
                    while ((text = termsEnum.next()) != null) {
                        
                        postings = termsEnum.postings(postings);

                        while (postings.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
                            int freq = postings.freq();
                            float tf = sim.tf(freq);
                            float idf = sim.idf(termsEnum.docFreq(), indexReader.numDocs());
                            termScoreMap.put(text.utf8ToString(), BETA * (tf*idf));
                        }
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

    private float getScore(Directory directory, String term)
            throws CorruptIndexException, IOException {

        try (IndexReader idxReader = DirectoryReader.open(directory)) {

            ConcreteTFIDFSimilarity sim = new ConcreteTFIDFSimilarity();

            for (LeafReaderContext context : idxReader.leaves()) {
                LeafReader reader = context.reader();

                try {
                    Terms terms = reader.terms(Constants.DOC_CONTENT);
                    TermsEnum termsEnum = terms.iterator();
                    PostingsEnum postings = null;

                    BytesRef text;
                    while ((text = termsEnum.next()) != null) {
                        postings = termsEnum.postings(postings);
                        if (text.utf8ToString().equalsIgnoreCase(term)) {

                            while (postings.nextDoc() != PostingsEnum.NO_MORE_DOCS) {
                                int freq = postings.freq();
                                float tf = sim.tf(freq);
                                float idf = sim.idf(termsEnum.docFreq(), indexReader.numDocs());
                                return tf * idf;
                            }
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

}
