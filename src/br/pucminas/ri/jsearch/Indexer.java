package br.pucminas.ri.jsearch;

/**
 *
 * @author josue
 */
import br.pucminas.ri.jsearch.rest.model.IndexerResponse;
import br.pucminas.ri.jsearch.utils.DocumentReader;
import br.pucminas.ri.jsearch.utils.Constants;
import br.pucminas.ri.jsearch.utils.PorterStemAnalyzer;
import br.pucminas.ri.jsearch.zip.GZIPFile;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Date;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.Lock;

public class Indexer {

    public static IndexerResponse performIndex() throws IOException {
        Lock lock;
        Path path = Paths.get(Constants.INDEX_PATH);
        Date start, end;
        IndexWriter writer;
        int numDocs;
        
        try (Directory dir = FSDirectory.open(path)) {
            lock = dir.obtainLock(Constants.LOCK);
            lock.ensureValid();
        
            PorterStemAnalyzer analyzer = new PorterStemAnalyzer();
            IndexWriterConfig iwc = new IndexWriterConfig(analyzer);
            File docDir = new File(Constants.DOCS_PATH);
            start = new Date();
            
            writer = new IndexWriter(dir, iwc);
            Indexer.indexDocs(writer, docDir);
            
            numDocs = writer.numDocs();
            System.out.printf("Indexed %d docs", numDocs);
            
            writer.flush();
            writer.close();
        }
        
        lock.close();
        
        end = new Date();
        
        return new IndexerResponse(numDocs, end.getTime() - start.getTime());
    }
    
    private static void indexDocs(IndexWriter writer, File file)
            throws IOException {
        
        if (file.canRead()) {
            if (file.isDirectory()) {
                GZIPFile.unpackFilesInDirectory(file.getPath(), true);
                
                String[] files = file.list();
                
                if (files != null) {
                    for (String f : files) {
                        indexDocs(writer, new File(file, f));
                    }
                }
            } else {
                DocumentReader docs = new DocumentReader(file);
                Document doc;
                while (docs.hasNext()) {
                    doc = docs.next();
                    
                    TextField fileName = new TextField(Constants.FILE_NAME, 
                            file.getName(), Field.Store.YES);
                    
                    doc.add(fileName);
                    writer.addDocument(doc);
                }
            }
        }
    }
}
