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

import java.util.Collections;
import java.util.List;

/**
 *
 * @author josue
 */
public class UserSearchResponse {
    
    private List<SimpleDocument> docs;
    private long time;
    private boolean error;
    private String userQuery;
    private List<TermEntry> terms;
    
    public UserSearchResponse() {
    }

    public UserSearchResponse(List<SimpleDocument> docs, long time, boolean error, String userQuery, List<TermEntry> terms) {
        this.docs = docs;
        this.time = time;
        this.error = error;
        this.userQuery = userQuery;
        this.terms = terms;
    }

    public List<SimpleDocument> getDocs() {
        return docs;
    }

    public void setDocs(List<SimpleDocument> docs) {
        this.docs = docs;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }

    public String getUserQuery() {
        return userQuery;
    }

    public void setUserQuery(String userQuery) {
        this.userQuery = userQuery;
    }

    public List<TermEntry> getTerms() {
        return terms;
    }

    public void setTerms(List<TermEntry> terms) {
        this.terms = terms;
    }
    
}
