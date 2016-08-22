package br.pucminas.ri.jsearch;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
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

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) {
        Date start = new Date();

        final File docDir = new File(Constants.DOCS_PATH);
        if (!docDir.exists() || !docDir.canRead()) {
            System.out.println("Document directory '" + docDir.getAbsolutePath() + "' does not exist or is not readable, please check the path");
            System.exit(1);
        }

        try {
            System.out.println("Indexing to directory '" + Constants.INDEX_PATH + "'...");

            Path path = Paths.get(Constants.INDEX_PATH);
            Directory dir = FSDirectory.open(path);
            Analyzer analyzer = new StandardAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);

            iwc.setOpenMode(OpenMode.CREATE);
            iwc.setRAMBufferSizeMB(512.0);

            try (IndexWriter writer = new IndexWriter(dir, iwc)) {
                Indexer.indexDocs(writer, docDir);
            }

            Date end = new Date();
            System.out.println("\nIndex time :" + (end.getTime() - start.getTime() + 
                    " total milliseconds\n"));
            
            String userQuery = "test";
            System.out.println("Query: " + userQuery);
            start  = new Date();
            Searcher.search(userQuery);
            end  = new Date();
            
            System.out.println("\nSearch time :" + (end.getTime() - start.getTime() + 
                    " total milliseconds"));

        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }
}
