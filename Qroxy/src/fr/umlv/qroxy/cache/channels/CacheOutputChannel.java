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
package fr.umlv.qroxy.cache.channels;

import fr.umlv.qroxy.cache.CacheException;
import fr.umlv.qroxy.cache.CacheProxy;
import fr.umlv.qroxy.http.HttpHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;

import java.io.Closeable;
import java.io.IOException;

import java.net.URI;

import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 * Represents a connection to the cache in order to write a new resource in the
 * cache.
 *
 * @author jgoyau
 */
public class CacheOutputChannel implements Closeable, AutoCloseable {

    private final CacheProxy proxy;
    private final URI uri;
    private FileChannel cacheFileChannel;
    private boolean cachable;

    CacheOutputChannel(CacheProxy proxy, URI uri) {
        this.uri = uri;
        this.proxy = proxy;
    }

    /**
     * Write data contained in the ByteBuffer parsed in argument in the cache.
     * To avoid the storage of the resource while test if it is cacheable, we
     * check it directly in this method.
     *
     * @param src
     * @return the number of written bytes
     * @throws IOException
     */
    public int write(ByteBuffer src) throws IOException {
        if (!cachable) {
            // Check if it's cachable
            String data = HttpHeader.DECODER.decode(src).toString();
            try {
                HttpResponseHeader responseHeader = HttpResponseHeader.parse(data);
                cachable = true;
                cacheFileChannel = proxy.add(responseHeader, uri);
            } catch (HttpMalformedHeaderException e) {
                if (data.contains("\r\n\r\n")) {
                    throw new CacheException("No response header in this message");
                }
                return 0;
            }
        }
        return cacheFileChannel.write(src);
    }

    /**
     * Test wether or not the channel is open.
     *
     * @return true if it is open, else false
     */
    public boolean isOpen() {
        return cacheFileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        if (cacheFileChannel != null) {
            cacheFileChannel.close();
        }
    }
}