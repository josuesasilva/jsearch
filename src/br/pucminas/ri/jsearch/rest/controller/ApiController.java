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
package br.pucminas.ri.jsearch.rest.controller;

import br.pucminas.ri.jsearch.Indexer;
import br.pucminas.ri.jsearch.Searcher;
import br.pucminas.ri.jsearch.rest.model.IndexerResponse;
import br.pucminas.ri.jsearch.rest.model.UserSearchResponse;
import br.pucminas.ri.jsearch.utils.JsonTransformer;
import java.io.IOException;
import java.util.Arrays;
import org.apache.lucene.queryparser.classic.ParseException;
import spark.Request;
import spark.Response;
import static spark.Spark.*;

/**
 *
 * @author josue
 */
public class ApiController {
    
    public static void start() {
        
        get("/hello", (req, res) -> {
            return hello();
        });
        
        get("/performIndexer", (req, res) -> {
            return performIndex();
        }, new JsonTransformer());
        
        get("/search", (req, res) -> {
            return performUserQuery(req, res);
        }, new JsonTransformer());
    }
    
    private static Object hello() {
        return "Hello World";
    }
    
    private static IndexerResponse performIndex() {
        IndexerResponse res;
        
        try {
            res = Indexer.performIndex();
        } catch (IOException ex) {
            res = new IndexerResponse();
            res.setError(true);
            System.err.println(Arrays.toString(ex.getStackTrace()));
        }
        
        return res;
    }
    
    private static UserSearchResponse performUserQuery(Request req, Response res) {
        UserSearchResponse userRes;
        
        try {
            userRes = Searcher.performUserQuery(req.queryParams("query"));
        } catch (IOException | ParseException ex) {
            userRes = new UserSearchResponse();
            userRes.setError(true);
            System.err.println(ex.getMessage());
        }
        
        return userRes;
    }
}
