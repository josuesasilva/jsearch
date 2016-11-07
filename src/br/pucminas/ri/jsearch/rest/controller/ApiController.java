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
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletException;
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

        post("/fileUpload", (req, res) -> {
            return fileUpload(req, res);
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
            e.printStackTrace();
            return "Error on log.";
        }
    }

    public static Object fileUpload(Request req, Response res) {
        req.attribute("org.eclipse.jetty.multipartConfig", new MultipartConfigElement("/temp"));
        Path out = Paths.get("result/queries.txt");

        try (InputStream is = req.raw().getPart("uploaded_file").getInputStream()) {
            Path folder = Paths.get("result");
            if (Files.exists(folder)) {
                Files.delete(out);
            }
            Files.createDirectories(Paths.get("result"));
            Files.copy(is, out);
            return evalTests(req, res);
        } catch (IOException | ServletException e) {
            e.printStackTrace();
            return "Error on upload file";
        }
    }

    private static Object evalTests(Request req, Response res) {
        try {
            Searcher.performTests("result/queries.txt");

            String[] command = {"./tests.sh"};
            Process p = Runtime.getRuntime().exec(command);
            p.waitFor();
            
            Path path = Paths.get("result/output.txt");

            res.header("Content-Disposition", "attachment; filename=output.txt");
            res.type("application/force-download");

            byte[] data = null;
            try {
                data = Files.readAllBytes(path);
            } catch (Exception e1) {

                e1.printStackTrace();
            }

            res.raw().getOutputStream().write(data);
            try {
                res.raw().getOutputStream().write(data);
                res.raw().getOutputStream().flush();
                res.raw().getOutputStream().close();
            } catch (Exception e) {
                e.printStackTrace();
            }
            
            return res.raw();
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
            return "Error on execute tests.";
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
