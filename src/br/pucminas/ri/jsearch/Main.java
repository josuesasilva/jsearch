package br.pucminas.ri.jsearch;

import br.pucminas.ri.jsearch.queryexpansion.QueryExpansion;
import br.pucminas.ri.jsearch.querylog.LogController;
import br.pucminas.ri.jsearch.querylog.Log;
import br.pucminas.ri.jsearch.utils.RankingEnum;
import br.pucminas.ri.jsearch.utils.Constants;
import br.pucminas.ri.jsearch.utils.PorterStemAnalyzer;
import br.pucminas.ri.jsearch.utils.StringList;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.IndexWriterConfig.OpenMode;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

/**
 *
 * @author josue
 */
public class Main {

    private static String[] getUserParam(String[] args) {
        
        String[] param = new String[2];

        if (args.length > 1 && args[0].equals("-q")) {
            if (args.length > 2) {
                for (int i = 1; i < args.length; i++) {
                    if (i == args.length - 2) {
                        param[0] += args[i];
                    } else {
                        param[0] = param[0] + " " + args[i] + " ";
                    }
                }
            } else {
                param[0] = args[1];
            }
        } else if (args.length > 1 && args[0].equals("-f")) {
            param[0] = "";
            param[1] = args[1];
        } else if (args.length == 0) {
            param[0] = "test";
            param[1] = "";
        } else {
            System.out.println("error: unsupported option");
            System.out.print("\nUsage:");
            System.out.print("\njsearch.jar [option]");
            System.out.print("\n-q userquery");
            System.out.print("\n-f file with queries\n");
            System.exit(0);
        }

        return param;
    }

    private static void performIndexer(File docDir, Directory dir, IndexWriterConfig iwc) throws IOException {
        System.out.println("Indexing to directory '" + Constants.INDEX_PATH + "'...");

        Date start = new Date();

        try (IndexWriter writer = new IndexWriter(dir, iwc)) {
            Indexer.indexDocs(writer, docDir);
            System.out.printf("Indexed %d docs", writer.numDocs());
        }

        Date end = new Date();
        System.out.println("\nIndex time: " + (end.getTime() - start.getTime()
                + " milliseconds\n"));
        
    }

    private static void performUserQuery(String userQuery) {
        System.out.println("Query: " + userQuery);

        System.out.println("User query: " + userQuery);
        String ip = "127.0.0.1";
        LogController qc = new LogController();
        
        Date start = new Date();
        qc.insert(ip, userQuery, "0");
        Date end = new Date();
        System.out.println("\nLog query time: " + (end.getTime() - start.getTime()
                + " total milliseconds"));
        
        start = new Date();
        List<Log> queries = qc.getLogsByIp(ip);
        end = new Date();
        System.out.println("\nRecover log time: " + (end.getTime() - start.getTime()
                + " total milliseconds"));
        
        System.out.println("\nSuggestions: ");
        StringList result = new StringList();
        int count = 0;
        for (Log q : queries) {
            if (StringUtils.getJaroWinklerDistance(userQuery, q.getQuery()) >= 0.80
                    && !result.contains(q.getQuery())
                    && count < 5) {
                result.add(q.getQuery());
                System.out.println(q.getQuery());
                count++;
            }
        }
        
        QueryExpansion qe = new QueryExpansion("0", userQuery);
        if (!result.contains(qe.expandQuery().getQuery())) {
            System.out.println(qe.expandQuery().getQuery());
        }

        Searcher.search("0", userQuery, RankingEnum.BM25, null);
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {        
        //
        // Begin - Parse user query
        //
        String userQuery = getUserParam(args)[0];
        String queriesFile = getUserParam(args)[1];
        //
        // End - Parse user query
        //
        //
        // Begin - Get documents to perform indexer
        //
        final File docDir = new File(Constants.DOCS_PATH);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }
        //
        // End - Get documents to perform indexer
        //

        try {
            //
            // Begin - Setup
            //
            Path path = Paths.get(Constants.INDEX_PATH);
            Directory dir = FSDirectory.open(path);
            PorterStemAnalyzer analyzer = new PorterStemAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(OpenMode.CREATE);
            iwc.setRAMBufferSizeMB(512.0);
            
            //
            // End - Setup
            //

            //
            // Begin - Perform Index
            //
            performIndexer(docDir, dir, iwc);
            
            //
            // End - Perform Index
            //
            //
            // Begin - Perform Search
            //
            if (!userQuery.isEmpty()) {
                performUserQuery(userQuery);
            } else if (!queriesFile.isEmpty()) {
                HashMap<String, String> queries = getQueriesFromFile(queriesFile);
                Searcher.search(queries, RankingEnum.BM25);
                Searcher.search(queries, RankingEnum.ROCCHIO);
                Searcher.search(queries, RankingEnum.QUERY_EXPANSION);
            } else {
                System.exit(1);
            }
            //
            // End - Perform Search
            //

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
