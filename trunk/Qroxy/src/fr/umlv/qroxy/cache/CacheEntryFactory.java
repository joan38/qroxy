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

import fr.umlv.qroxy.http.HttpResponseHeader;
import java.net.URI;

/**
 * Factory to create cache entry objects.
 * @author gdemurge
 */
class CacheEntryFactory {
    /**
     * Create the cache entry given the HTTP header and the URi parsed in 
     * argument.
     * @param header
     * @param uri
     * @return the created CacheEntry
     */
    public CacheEntry createCacheEntry(HttpResponseHeader header, URI uri) {
        return new CacheEntry(header, uri);
    }
            
}
