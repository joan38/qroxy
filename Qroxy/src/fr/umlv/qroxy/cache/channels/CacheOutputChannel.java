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
import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.HttpHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import java.io.*;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
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

    public int write(ByteBuffer src) throws IOException {
        if (!cachable) {
            // Check if it's cachable
            String data = HttpHeader.DECODER.decode(src).toString();
            try {
                HttpResponseHeader responseHeader = HttpResponseHeader.parse(data);
                if (!proxy.isValid(responseHeader)) {
                    throw new CacheException("Resource Not Cacheable");
                }
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

    public boolean isOpen() {
        return cacheFileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        cacheFileChannel.close();
    }
}
