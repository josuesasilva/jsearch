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

import java.util.List;
import javax.persistence.*;

/**
 *
 * @author josue
 */
public class QueryLogController {

    EntityManagerFactory emf;
    EntityManager em;
    
    private void createConnection() {
        emf = Persistence.createEntityManagerFactory("$objectdb/db/logs.odb");
        em = emf.createEntityManager();
    }
    
    private void closeConnection() {
        em.close();
        emf.close();
    }
    
    synchronized public void insert(String ip, String q) {
        QueryLogModel query = new QueryLogModel(ip, q);
        createConnection();
        em.getTransaction().begin();
        em.persist(query);
        em.getTransaction().commit();
        closeConnection();
    }
    
    synchronized public List<QueryLogModel> getAll() {
        createConnection();
        TypedQuery<QueryLogModel> query = 
                em.createQuery("SELECT l FROM QueryLogModel l", QueryLogModel.class);
        List<QueryLogModel> result = query.getResultList();
        closeConnection();
        return result;
    }
    
    synchronized public List<QueryLogModel> getLogsByIp(String ip) {
        createConnection();
        String queryString = 
                String.format("SELECT l FROM QueryLogModel l WHERE l.ip = '%s'", ip);
        Query query = em.createQuery(queryString);
        query.getResultList();
        List<QueryLogModel> result = query.getResultList();
        closeConnection();
        return result;
    }
}
