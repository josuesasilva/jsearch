/*
 * Copyright (C) 2016 jjd
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
 * @author jjd
 */
public class AutoComplete {
    
    private String[] suggestions;

    public AutoComplete(String[] sugestions) {
        this.suggestions = sugestions;
    }

    public String[] getSugestions() {
        return suggestions;
    }

    public void setSugestions(String[] sugestions) {
        this.suggestions = sugestions;
    }
}
