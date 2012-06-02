/*
 * Copyright (C) 2012 Joan Goyeau <joan.goyeau@gmail.com>
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
import fr.umlv.qroxy.http.HttpRequestHeader;
import java.net.URI;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public class CacheTmpImpl implements CacheAccess {

    @Override
    public CacheInputChannel getResource(HttpRequestHeader requestHeader) throws CacheException {
        throw new CacheException("Not supported yet.");
    }

    @Override
    public CacheOutputChannel cacheResource(URI uri) throws CacheException {
        throw new CacheException("Not supported yet.");
    }

    @Override
    public boolean corruptCachedResource(CacheInputChannel resource) {
        return false;
    }
    
}
