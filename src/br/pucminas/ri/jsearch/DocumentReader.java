package br.pucminas.ri.jsearch;

import br.pucminas.ri.jsearch.utils.Constants;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.FieldType;
import org.apache.lucene.document.StringField;
import org.apache.lucene.index.IndexOptions;

/**
 *
 * @author josue
 */
public class DocumentReader implements Iterator<Document> {

    protected BufferedReader rdr;
    protected boolean endOfFile = false;

    public DocumentReader(File file) throws FileNotFoundException {
        rdr = new BufferedReader(new FileReader(file));
        System.out.println("Loading " + file.toString());
    }

    @Override
    public Document next() {
        Document doc = new Document();
        StringBuilder sb = new StringBuilder();

        try {
            String line;
            Pattern docNoTag = Pattern.compile("<DOCNO>\\s*(\\S+)\\s*<");
            boolean insideDocument = false;
            
            while (true) {
                line = rdr.readLine();
                
                if (line == null) {
                    endOfFile = true;
                    break;
                }
                
                if (!insideDocument) {
                    if (line.startsWith("<DOC>")) {
                        insideDocument = true;
                    } else {
                        continue;
                    }
                }
                
                if (line.startsWith("</DOC>")) {
                    sb.append(line);
                    break;
                }

                Matcher m = docNoTag.matcher(line);
                if (m.find()) {
                    String docno = m.group(1);
                    doc.add(new StringField(Constants.DOC_TITLE, 
                            docno, Field.Store.YES));
                }

                sb.append(line);
            }
            if (sb.length() > 0) {
                FieldType type = new FieldType();
                type.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS);
                type.setStored(true);
                type.setStoreTermVectors(true);
//                doc.add(new TextField(Constants.DOC_CONTENT,
//                        sb.toString(), Field.Store.YES));
                doc.add(new Field(Constants.DOC_CONTENT, sb.toString(), type));

            }

        } catch (Exception e) {
            doc = null;
            System.err.println(e);
        }
        return doc;
    }
    
    @Override
    public boolean hasNext() {
        return !endOfFile;
    }

    @Override
    public void remove() {
    }
}
