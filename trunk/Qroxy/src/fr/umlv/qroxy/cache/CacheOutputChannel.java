/*
 * Copyright (C) 2012 joan
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
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

/**
 *
 * @author joan
 */
public class CacheOutputChannel {

    private static final Charset charset = Charset.forName("ISO-8859-1");
    private static final CharsetDecoder decoder = charset.newDecoder();
    private final File cacheFile;
    private FileChannel cacheFileChannel;

    public CacheOutputChannel(File cacheFile) {
        this.cacheFile = cacheFile;
    }

    public int write(ByteBuffer src) throws IOException {
        try {
            HttpResponseHeader responseHeader = HttpResponseHeader.parse(decoder.decode(src).toString());
        } catch (HttpMalformedHeaderException e) {
            return 0;
        } finally {
            src.rewind();
        }

        // Validations du header a faire ici !!!!!!!!!
        // Faire des throws de UncachableHttpResponse avec des messages explicite

        if (cacheFileChannel == null || !cacheFileChannel.isOpen()) {
            cacheFileChannel = new FileOutputStream(cacheFile).getChannel();
        }
        return cacheFileChannel.write(src);
    }

    public boolean isOpen() {
        return cacheFileChannel.isOpen();
    }

    public void close() throws IOException {
        if (cacheFileChannel.isOpen()) {
            cacheFileChannel.close();
        }
    }
}
