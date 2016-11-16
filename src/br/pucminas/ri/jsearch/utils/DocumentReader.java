package br.pucminas.ri.jsearch.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Iterator;
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
    protected String fileName;

    public DocumentReader(File file) throws FileNotFoundException {
        rdr = new BufferedReader(new FileReader(file));
        fileName = file.toString();
        System.out.println("Loading " + fileName);
    }

    @Override
    public Document next() {
        Document doc = new Document();
        StringBuilder sb = new StringBuilder();

        try {
            String line;
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

                sb.append(line);
            }
            
            if (sb.length() > 0) {
                FieldType typeContent = new FieldType();
                typeContent.setIndexOptions(IndexOptions.DOCS_AND_FREQS_AND_POSITIONS_AND_OFFSETS);
                typeContent.setStored(true);
                typeContent.setStoreTermVectors(true);
                typeContent.setStoreTermVectorOffsets(true);
                typeContent.setStoreTermVectorPayloads(true);
                typeContent.setStoreTermVectorPositions(true);
                typeContent.setTokenized(true);
                
                FieldType typeHtml = new FieldType();
                typeHtml.setStored(true);
                
                String html = sb.toString();
                
                try {
                    String content = HtmlParser.docToString(html);
                    String title = HtmlParser.docTitle(html);
                    doc.add(new Field(Constants.DOC_HTML, html, typeHtml));
                    doc.add(new Field(Constants.DOC_CONTENT, content, typeContent));
                    doc.add(new StringField(Constants.DOC_TITLE, title, Field.Store.YES));   
                } catch (Exception e) {
                    System.out.println("Error on " + fileName);
                }
            }
            
            return doc;
        } catch (Exception e) {
            doc = null;
            e.printStackTrace();
            return doc;
        }
    }
    
    @Override
    public boolean hasNext() {
        return !endOfFile;
    }

    @Override
    public void remove() {
    }
}
