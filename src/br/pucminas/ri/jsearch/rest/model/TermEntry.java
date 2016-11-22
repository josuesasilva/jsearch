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
package br.pucminas.ri.jsearch.rest.model;

import java.util.Comparator;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author josue
 */
public class TermEntry implements Map.Entry<String, Float> , Comparable<Map.Entry<String, Float>>, Comparator<Map.Entry<String, Float>> {

    private String term;
    private Float tfidf;

    public TermEntry(String term, Float tfidf) {
        this.term = term;
        this.tfidf = tfidf;
    }
    
    @Override
    public String getKey() {
        return getTerm();
    }

    @Override
    public Float getValue() {
        return getTfidf();
    }

    @Override
    public Float setValue(Float value) {
        this.tfidf = value;
        return this.tfidf;
    }

    public String getTerm() {
        return term;
    }

    public void setTerm(String term) {
        this.term = term;
    }

    public Float getTfidf() {
        return tfidf;
    }

    public void setTfidf(Float tfidf) {
        this.tfidf = tfidf;
    }

    @Override
    public int compareTo(Map.Entry<String, Float> o) {
        return o.getValue().compareTo(this.getValue());
    }

    @Override
    public int compare(Map.Entry<String, Float> o1, Map.Entry<String, Float> o2) {
        return o2.getValue().compareTo(o1.getValue());
    }

    @Override
    public boolean equals(Object obj) {
        TermEntry  out = (TermEntry) obj;
        return this.term.equals(out.getTerm());
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 89 * hash + Objects.hashCode(this.term);
        hash = 89 * hash + Objects.hashCode(this.tfidf);
        return hash;
    }
}
