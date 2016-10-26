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
package br.pucminas.ri.jsearch.querylog;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.*;

/**
 *
 * @author josue
 */
@Entity
public class QueryLogModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue
    private long id;

    private String ip;
    private String query;
    private String docId;
    private Date date;

    public QueryLogModel() {
    }

    public QueryLogModel(String ip, String query, String doc) {
        this.ip = ip;
        this.query = query;
        this.date = new Date();
        this.docId = doc;
    }

    public Long getId() {
        return id;
    }

    public String getIp() {
        return ip;
    }

    public String getQuery() {
        return query;
    }

    public Date getDate() {
        return date;
    }

    public String getDocId() {
        return docId;
    }

    @Override
    public String toString() {
        return String.format("(%s, %s, %s)", this.ip, this.query, this.date);
    }

}
