/*
 * Copyright (C) 2012 Joan Goyeau & Guillaume Demurger
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
package fr.umlv.qroxy.cache;

import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 *
 * @author Guillaume
 */
public class Cache {
    private final Config config;
    private final Map<CacheEntry, File> cache = new HashMap<>();
    
    public Cache(Config config) {
        this.config = config;
    }
    
    public void addCacheEntry(CacheEntry entry) {
        String path = config.getCachePath();
        File f = cache.remove(entry);
        f = new File(path);
        cache.put(entry, f);
    }
    
    /*
    public FileInputStream getFileStream(int hashedUri) {
        File f = cache.get(hashedUri); 
        try {
            return new FileInputStream(f);
        } finally {
            return null;
        }
    }*/

    File getCacheEntry(CacheEntry entry) throws CacheException {
        File f = cache.get(entry);
        if(f == null) {
            throw new CacheException("Ressource not found");
        }
        return f;
    }
}

