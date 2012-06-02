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
import java.net.URI;

/**
 *
 * @author Guillaume
 */
public interface CacheAccess {

    /**
     * Retrieve cached resource.
     *
     * @param requestHeader
     * @return The FileInputStream of resource or null if this resource is not
     * available in cache.
     * @throws CacheException If the HttpRequestHeader doesn't match preconditions.
     */
    public CacheInputChannel getResource(HttpRequestHeader requestHeader) throws CacheException;

    /**
     * Cache resource
     * 
     * @return The FileOutputStream were to cache the resource
     * @throws CacheException If the HttpRequestHeader doesn't match preconditions.
     */
    public CacheOutputChannel cacheResource(URI uri) throws CacheException;
    
    /**
     * Advertise a corrupt resource.
     * 
     * @param resource The corrupt resource FileInputStream
     * @return If the resource has been removed.
     */
    public boolean corruptCachedResource(CacheInputChannel resource);
}
