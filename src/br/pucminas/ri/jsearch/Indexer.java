package br.pucminas.ri.jsearch;

/**
 *
 * @author josue
 */
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;

public class Indexer {

    public static void indexDocs(IndexWriter writer, File file)
            throws IOException {

        if (file.canRead()) {
            if (file.isDirectory()) {
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
                    
//                    TextField content = new TextField(Constants.FILE_CONTENT, 
//                            new FileReader(file));
                    
                    TextField fileName = new TextField(Constants.FILE_NAME, 
                            file.getName(), Field.Store.YES);
                    
//                    TextField filePath = new TextField(Constants.FILE_PATH, 
//                            file.getCanonicalPath(), Field.Store.YES);
                    
                    doc.add(fileName);
                    //doc.add(filePath);
                    //doc.add(content);
                    
                    writer.addDocument(doc);
                }
            }
        }
    }
}
