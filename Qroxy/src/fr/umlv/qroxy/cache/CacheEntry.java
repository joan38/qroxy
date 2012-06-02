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
 * Represents a cache entry. The cache entry is intended to store all data
 * related to a given cached ressource.
 * @author gdemurge
 */
class CacheEntry {
    private final HttpResponseHeader header;
    private final URI uri;
    
    /**
     * Constructor.
     * Package visibility to ensure nothing can instanciate it outdoor the 
     * cache package.
     * @param header
     * @param uri 
     */
    CacheEntry(HttpResponseHeader header, URI uri) {
        this.header = header;
        this.uri = uri;
    }
    
    public URI getUri() {
        return uri;
    }
   
    public HttpResponseHeader getHeader() {
        return header;
    }
   
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(o == null) {
            return false;
        }
        if(o.getClass() != this.getClass()) {
            return false;
        }
        CacheEntry ce = (CacheEntry)o;
        return uri.equals(ce.uri);
    }
}
