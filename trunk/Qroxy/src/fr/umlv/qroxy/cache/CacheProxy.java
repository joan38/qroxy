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

import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;

/**
 *
 * @author Guillaume
 */
public class CacheProxy implements CacheAccess {
    private final CacheInputControler cacheInputControler = new CacheInputControler();
    private final CacheInputChannelFactory inputChannelFactory = new CacheInputChannelFactory();
    private final CacheOutputChannelFactory outputChannelFactory = new CacheOutputChannelFactory();
    private final Cache cache;

    public CacheProxy(Cache cache) {
        this.cache = cache;
    }
    
    @Override
    public CacheInputChannel getResource(HttpRequestHeader requestHeader) throws CacheException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public CacheOutputChannel cacheResource(HttpResponseHeader responseHeader) throws CacheException {
        boolean cacheable = cacheInputControler.isCacheable(responseHeader);
        if(cacheable) {
            cache.put(responseHeader);
        }
        return cache
        
    }

    @Override
    public boolean corruptCachedResource(CacheInputChannel resource) {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}
