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
import java.io.IOException;
import java.net.URI;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Represents a cache. It supports the following operations:
 * - add an entry to the cache 
 * - get an entry given an URI
 * - get a FileChannel to read data from the cache
 * Note: No algorithm of size management has been implemented due to a lack of 
 * time
 * @author gdemurge
 */
public class Cache {

    private final Config config;
    private final Map<CacheEntry, Path> cache = new HashMap<>();

    Cache(Config config) {
        this.config = config;
    }

    /**
     * Add an entry to the cache. It returns a FileChannel in order to 
     * write data in the cache after the given entry has been added.
     * @param entry
     * @return the channel to write the new resource in the cache
     * @throws CacheException 
     */
    FileChannel addCacheEntry(CacheEntry entry) throws CacheException {
        Objects.requireNonNull(entry);
        Path path = cache.remove(entry);
        cache.put(entry, Paths.get(entry.getUri()));
        try {
            return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        } catch (IOException e) {
            throw new CacheException(e.getMessage(), e.getCause());
        }
    }

    /**
     * Returns a channel to read cached data related to the given entry 
     * from the cache.
     * @param entry
     * @return the channel to read a cached ressource
     * @throws CacheException 
     */
    FileChannel getCacheFileChannel(CacheEntry entry) throws CacheException {
        Objects.requireNonNull(entry);
        Path path = cache.get(entry);        
        if (path == null) {
            throw new CacheException("Ressource not found");
        }
        try {
            return FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.READ);
        } catch (IOException e) {
            throw new CacheException(e.getMessage(), e.getCause());
        }
    }
    
    /**
     * Return the entry containing the given URI.
     * Note: For the moment the implementation of the class Cache implies that
     * this method work in linear complexity. It has to be improve in a future
     * version.
     * @param uri
     * @return the CacheEntry containing the given uri if it exists
     * @throws CacheException 
     */
    CacheEntry getCacheEntry(URI uri) throws CacheException {
        Set<CacheEntry> entries = cache.keySet();
        for(CacheEntry e: entries) {
            if(e.getUri().equals(uri)) {
                return e;
            }
        }
        throw new CacheException("Entry does not exist in cache");
    }
}

