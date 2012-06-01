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

import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.HttpHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public class CacheOutputChannel implements Closeable, AutoCloseable {

    private Path path;
    private FileChannel cacheFileChannel;
    private boolean cachable;

    public CacheOutputChannel() {
    }

    public int write(ByteBuffer src) throws IOException {
        if (!cachable) {
            // Check if it's cachable
            String data = HttpHeader.DECODER.decode(src).toString();
            try {
                HttpResponseHeader responseHeader = HttpResponseHeader.parse(data);
                // Preconditions for caching

                // Si c'est pas cachable
                // throw new CacheException("C'est pas cachable !!!!! Pk ?");

                // Ok it's cachable
                cachable = true;
                // http://docs.oracle.com/javase/7/docs/api/java/nio/file/StandardOpenOption.html
                path = ?;
                cacheFileChannel = FileChannel.open(path, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE, StandardOpenOption.READ);
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
        // Check if the resource writed successfully
        try {
            ByteBuffer buffer = ByteBuffer.allocate(Config.MAX_HEADER_LENGTH);
            cacheFileChannel.read(buffer);
            HttpResponseHeader responseHeader = HttpResponseHeader.parse(HttpHeader.DECODER.decode(buffer).toString());

            // Preconditions
            if (responseHeader.getHeaderLength() + responseHeader.getContentLength() != cacheFileChannel.size()) {
                throw new CacheException("Writted resource length doesn't equals to the length of the HTTP header");
            }
            if (responseHeader.getLocation() == null) {
                throw new CacheException("Location header field is required");
            }
        } catch (IOException e) {
            cacheFileChannel.close();
            throw new CacheException("Resource not cached because of a corrupt writing", e);
        }
    }
}