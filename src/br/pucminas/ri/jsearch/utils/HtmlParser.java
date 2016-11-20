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
package br.pucminas.ri.jsearch.utils;

import org.jsoup.Jsoup;

/**
 *
 * @author josue
 */
public class HtmlParser {

    public static String docToString(String doc) throws Exception {
        int begin = doc.indexOf("<html>"), end = doc.indexOf("</html>");
        String string = new String();
        
        if (begin < 0 && end < 0) {
            end = doc.indexOf("</DOCHDR>");
            doc = doc.substring(end, doc.length());
            string = Jsoup.parse(doc).text();   
        } else if (begin >= 0 && end >= 0){
            string = Jsoup.parse(doc.substring(begin, end)).text();
        } else {
            string = Jsoup.parse(doc).text();
        }
        
        string = string.replaceAll("<[^>]*>", "");
        return string;
    }

    public static String docTitle(String doc) throws Exception  {
        String title = Jsoup.parse(doc).select("title").text();

        if (title.isEmpty()) {
            title = Jsoup.parse(doc).select("DOCNO").text();
        }

        return title;
    }
    
    public static String docno(String doc) throws Exception  {
        return Jsoup.parse(doc).select("DOCNO").text();
    }
}
