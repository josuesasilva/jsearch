package br.pucminas.ri.jsearch;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;

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
                    doc.add(new StringField("docno", docno, Field.Store.YES));
                }

                sb.append(line);
            }
            if (sb.length() > 0) {
                doc.add(new TextField("contents", sb.toString(), Field.Store.NO));
            }

        } catch (Exception e) {
            doc = null;
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
