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
package br.pucminas.ri.jsearch;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

/**
 *
 * @author josue
 */
public class GZIPFile {
    
    public static boolean unpackFilesInDirectory(String source, boolean removeSource) {
        
        File file = new File(source);
        
        if (file.isDirectory()) {
            File[] files = file.listFiles();
            if (files != null) {
                for (File f : files) {
                    if (!unpackFile(f.getPath(), removeSource)) 
                        return false;
                }
            }
            
        } else {
            return unpackFile(source, removeSource);
        }
        
        return true;
    }

    public static boolean unpackFile(String source, boolean removeSource) {
        byte[] buffer = new byte[1024];

        if (!isGzipFile(source)) return true;
        
        try {

            GZIPInputStream gzip = new GZIPInputStream(new FileInputStream(source));

            FileOutputStream out = new FileOutputStream(pathNameSourceToTarget(source));

            int len;
            while ((len = gzip.read(buffer)) > 0) {
                out.write(buffer, 0, len);
            }

            gzip.close();
            out.close();

            if (removeSource) {
                File file = new File(source);
                file.delete();
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            return false;
        }
        
        return true;
    }
    
    private static String pathNameSourceToTarget(String sourcePath) {
        String target = "";
        Pattern pattern = Pattern.compile("[^\\\\]+(?=\\.gz$)");
        Matcher matcher = pattern.matcher(sourcePath);
        
        while (matcher.find()) {
            target = matcher.group();
        }
        
        return target;
    }
    
    private static boolean isGzipFile(String source) {
        Pattern pattern = Pattern.compile("\\.[^.]*$");
        Matcher matcher = pattern.matcher(source);
        
        while (matcher.find()) {
            return true;
        }
        
        return false;
    }

}
