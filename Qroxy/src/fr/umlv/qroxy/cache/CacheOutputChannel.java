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
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileChannel.MapMode;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public class CacheOutputChannel implements Closeable {

    private static final Charset charset = Charset.forName("ISO-8859-1");
    private static final CharsetDecoder decoder = charset.newDecoder();
    private final File cacheFile;
    private FileChannel cacheFileChannel;

    public CacheOutputChannel(File cacheFile) throws FileNotFoundException {
        this.cacheFile = cacheFile;
        this.cacheFileChannel = new FileOutputStream(cacheFile).getChannel();
    }

    public int write(ByteBuffer src) throws IOException {
        return cacheFileChannel.write(src);
    }

    public boolean isOpen() {
        return cacheFileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        // Check if the resource writed successfully
        try {
            MappedByteBuffer map = cacheFileChannel.map(MapMode.READ_ONLY, 0, Config.MAX_HEADER_LENGTH);
            HttpResponseHeader responseHeader = HttpResponseHeader.parse(decoder.decode(map).toString());

            // Preconditions
            if (responseHeader.getHeaderLength() + responseHeader.getContentLength() != cacheFile.length()) {
                throw new CacheException("Writted resource length doesn't equals to the length of the HTTP header");
            }
        } catch (IOException e) {
            cacheFileChannel.close();
            cacheFile.delete();
            throw new CacheException("Resource not cached because of a corrupt writing", e);
        }
    }

    public File getFile() {
        return cacheFile;
    }
}