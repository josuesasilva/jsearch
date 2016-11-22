/*
 * Copyright (C) 2016 josue
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

import br.pucminas.ri.jsearch.rest.model.TermEntry;
import br.pucminas.ri.jsearch.utils.Constants;
import br.pucminas.ri.jsearch.utils.PorterStemAnalyzer;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.Query;
import org.apache.lucene.wordnet.SynonymMap;

/**
 *
 * @author josue
 */
public class QueryExpansion {
    
    private final int MAX_SYNS = 5;
    
    private String userQuery;
    private String queryId;
    
    private static SynonymMap map;
    
    private static final int EXPANSION_SIZE = 10;
    
    public QueryExpansion(String qid, String query) {
        userQuery = query;
        
        try {
            loadSynonims();
        } catch (IOException e) {
            System.err.println("Could't possible load Wordnet database.");
        }
    }
    
    public static Query expandQuery(String userQuery, List<TermEntry> terms) {
        PorterStemAnalyzer analyzer = new PorterStemAnalyzer();
        QueryParser queryParser = new QueryParser(Constants.DOC_CONTENT, analyzer);
        
        Query query = null;
        
        try {

            if (userQuery.split(" ").length < 2) return queryParser.parse(userQuery);

            userQuery = getTopTerms(userQuery, terms).stream().map((term) -> " " + term)
                    .reduce(userQuery, String::concat);
            userQuery = userQuery.trim();
            query = queryParser.parse(userQuery);
        } catch (ParseException ex) {
            Logger.getLogger(QueryExpansion.class.getName()).log(Level.SEVERE, null, ex);
        }
        
        return query;
    }
    
    public QueryExpanded expandQuery() {
        QueryExpanded query = new QueryExpanded(queryId, userQuery);
        StringBuilder newQuery = new StringBuilder();
        ArrayList<String[]> terms = new ArrayList<>();
        if (userQuery != null) {
            String[] termsArray = userQuery.split("\\s+");
            
            for (String term : termsArray) {
                newQuery.append(term);
                newQuery.append(" ");
                terms.add(termSynonyms(term));
            }
            
            terms.stream().forEach((array) -> {
                newQuery.append(concatTerms(" " , array));
                newQuery.append(" ");
            });
            
            query.setQuery(newQuery.toString().trim());
        }
        
        return query;
    }
    
    private String[] termSynonyms(String term) {
        if (term != null && !term.isEmpty()) {
            String[] syns = map.getSynonyms(term);
            
            // Caso houver mais de MAX_SYNS sinonimos por termo preencher preferencialmente
            // com sinonimos que iniciam com a mesma letra.
            if (syns.length > MAX_SYNS) {
                ArrayList<String> filterTerms = new ArrayList<>();
                
                for (String t : syns) {
                    if (t.charAt(0) == term.charAt(0)) {
                        filterTerms.add(t);
                    }
                }
                
                if (filterTerms.size() < MAX_SYNS) {
                    for (String t : syns) {
                        if (!filterTerms.contains(t)) {
                            filterTerms.add(t);
                        }
                    }    
                }
                
                String[] newSyns;
                
                if (filterTerms.size() > MAX_SYNS) {
                    newSyns = new String[MAX_SYNS];
                } else {
                    newSyns = new String[filterTerms.size()];
                }
                
                for (int i = 0; i < MAX_SYNS && i < filterTerms.size(); i++) {
                    newSyns[i] = filterTerms.get(i);
                }
                
                return newSyns;
            }
            
            return syns;
        } else {
            return new String[0];
        }
    }
    
    private String concatTerms(String term, String[] terms) {
        StringBuilder newTerm = new StringBuilder();
        
        newTerm.append(term);
        
        for (String t : terms) {
            newTerm.append(" ");
            newTerm.append(t);
        }
        
        return newTerm.toString().trim();
    }
    
    private void loadSynonims() throws IOException {
        if (map == null) {
            String dataPath = String.format("%s/%s", Constants.RESULT_PATH, 
                    Constants.WORDNET_DATA);
            map = new SynonymMap(new FileInputStream(dataPath));
        }
    }
    
    public static List<String> getTopTerms(String userQuery, List<TermEntry> terms) {
        ArrayList<String> result = new ArrayList<>();
        Collections.sort(terms);
        
        int count = 0;
        for (TermEntry t : terms) {
            
            if (count == EXPANSION_SIZE) {
                return result;
            }
            
            if (!result.contains(t.getTerm()) && !userQuery.contains(t.getTerm())) {
                result.add(t.getTerm());
                count++;
            }
            
        }
        
        return result;
    }
}
