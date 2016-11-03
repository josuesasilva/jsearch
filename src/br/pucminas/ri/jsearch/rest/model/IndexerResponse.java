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

/**
 *
 * @author josue
 */
public class IndexerResponse {
    
    private long time;
    private Integer numDocs;
    private boolean error;
    
    public IndexerResponse() {
        this.time = 0;
        this.numDocs = 0;
        this.error = false;
    }
    
    public IndexerResponse(Integer numDocs, long time) {
        this.time = time;
        this.numDocs = numDocs;
        this.error = false;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public Integer getNumDocs() {
        return numDocs;
    }

    public void setNumDocs(Integer numDocs) {
        this.numDocs = numDocs;
    }

    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
    
}
