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
package fr.umlv.qroxy.cache.channels;

import fr.umlv.qroxy.cache.CacheProxy;

import java.net.URI;
/**
 * Factory to create CacheOutputChannel instances.
 * @author gdemurge
 */
public class CacheOutputChannelFactory {
    /**
     * Returns a CacheOutputChannel instance initialized with the owning 
     * CacheProxy instance and the given URI;
     * @param proxy
     * @param uri
     * @return a CacheOutputChannel instance
     */
    public CacheOutputChannel createOutputChannel(CacheProxy proxy, URI uri) {
        return new CacheOutputChannel(proxy, uri);
    }
}