/*
 * Copyright (C) 2016 793604
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

import org.apache.lucene.search.similarities.ClassicSimilarity;

/**
 *
 * @author josue
 */
public class ConcreteTFIDFSimilarity extends ClassicSimilarity {
	
	@Override
	public float idf(long docFreq, long numDocs){
		return (float) Math.log10((double)numDocs/(double)docFreq);
	}
	
	@Override
	public float tf(float freq){
		return (float) (1 + Math.log10((double)freq));
	}
}
