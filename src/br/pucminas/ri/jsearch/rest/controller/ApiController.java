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
import br.pucminas.ri.jsearch.querylog.Log;
import br.pucminas.ri.jsearch.querylog.LogController;
import br.pucminas.ri.jsearch.rest.model.AutoComplete;
import br.pucminas.ri.jsearch.rest.model.IndexerResponse;
import br.pucminas.ri.jsearch.rest.model.UserSearchResponse;
import br.pucminas.ri.jsearch.utils.JsonTransformer;
import br.pucminas.ri.jsearch.utils.StringList;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.classic.ParseException;
import spark.Request;
import spark.Response;
import spark.Spark;
import static spark.Spark.*;

/**
 *
 * @author josue
 */
public class ApiController {

    private static LogController log;

    public static void start() {

        Spark.staticFileLocation("/web");
        log = new LogController();

        get("/hello", (req, res) -> {
            return hello();
        });

        get("/performIndexer", (req, res) -> {
            return performIndex();
        }, new JsonTransformer());

        get("/search", (req, res) -> {
            return performUserQuery(req, res);
        }, new JsonTransformer());

        get("/autocomplete", (req, res) -> {
            return autocomplete(req, res, log);
        }, new JsonTransformer());

        post("/log", (req, res) -> {
            return log(req, res, log);
        });
    }

    private static Object hello() {
        return "Hello World";
    }

    private static Object log(Request req, Response res, LogController log) {
        String id = req.queryParams("doc");
        String query = req.queryParams("query");

        try {
            log.insert(req.ip(), query, id);
            return "Select doc " + id + ".\nQuery logged!";
        } catch (Exception e) {
            return "Error on log.";
        }
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

    private static AutoComplete autocomplete(Request req, Response res, LogController log) {
        AutoComplete ac = new AutoComplete(new String[]{});
        String query = req.queryParams("query");

        try {
            List<Log> logs = log.getLogsByIp(req.ip());
            int count = 0;
            StringList suggestions = new StringList();
            
            for (Log l : logs) {
                if (StringUtils.getJaroWinklerDistance(query, l.getQuery()) >= 0.80
                        && !suggestions.contains(l.getQuery())
                        && count < 5) {
                    suggestions.add(l.getQuery());
                    count++;
                }
            }
            
            ac.setSugestions(suggestions.toArray());
        } catch (Exception e) {
            System.err.println(e.getMessage());
        }
        return ac;
    }
}
