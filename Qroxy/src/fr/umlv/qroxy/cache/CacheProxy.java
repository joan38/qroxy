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

import fr.umlv.qroxy.cache.channels.CacheInputChannel;
import fr.umlv.qroxy.cache.channels.CacheOutputChannel;
import fr.umlv.qroxy.cache.channels.CacheInputChannelFactory;
import fr.umlv.qroxy.cache.channels.CacheOutputChannelFactory;
import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;

import java.net.URI;

import java.nio.channels.FileChannel;

import java.util.Objects;

/**
 *
 * @author Guillaume
 */
public class CacheProxy implements CacheAccess {
    private final ExpirationModel expirationModel = new ExpirationModel();
    private final CacheInputChannelFactory inputChannelFactory = new CacheInputChannelFactory();
    private final CacheOutputChannelFactory outputChannelFactory = new CacheOutputChannelFactory();
    private final CacheEntryFactory cacheEntryFactory = new CacheEntryFactory();
    private final Cache cache;

    public CacheProxy(Config config) {
        cache = new Cache(config);
    }

    @Override
    public CacheInputChannel getResource(HttpRequestHeader requestHeader) throws CacheException {
        Objects.requireNonNull(requestHeader);
        CacheEntry entry = cache.getCacheEntry(requestHeader.getUri());
        if(!checkIfCacheResourceMatchRequest(requestHeader, entry.getHeader())) {
            throw new CacheException("Cannot return cached resource for request: "+requestHeader);
        }
        return inputChannelFactory.createCacheInputeChannel(cache.getCacheFileChannel(entry));
    }

    @Override
    public CacheOutputChannel cacheResource(URI uri) throws CacheException {
        Objects.requireNonNull(uri);
        return outputChannelFactory.createOutputChannel(this, uri);
    }

    @Override
    public boolean corruptCachedResource(CacheInputChannel resource) {
        Objects.requireNonNull(resource);
        throw new UnsupportedOperationException("Not supported yet.");
    }

    private boolean isValid(HttpResponseHeader responseHeader) {
        Objects.requireNonNull(responseHeader);
        return expirationModel.isExpired(responseHeader);
    }

    public FileChannel add(HttpResponseHeader responseHeader, URI uri) throws CacheException {
        Objects.requireNonNull(responseHeader);
        Objects.requireNonNull(uri);
        if(!isValid(responseHeader)) {
            throw new CacheException("Resource must not be cached");
        }
        return cache.addCacheEntry(cacheEntryFactory.createCacheEntry(responseHeader, uri));
    }

    private boolean checkIfCacheResourceMatchRequest(HttpRequestHeader requestHeader, HttpResponseHeader cacheheader) {
        return true;
    }
}