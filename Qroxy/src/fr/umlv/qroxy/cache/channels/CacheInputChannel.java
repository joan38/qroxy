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

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public class CacheInputChannel implements Closeable, AutoCloseable {
    private final FileChannel cacheFileChannel;

    CacheInputChannel(Path cachePath) throws IOException {
        this.cacheFileChannel = FileChannel.open(cachePath, StandardOpenOption.READ);
    }

    public int read(ByteBuffer src) throws IOException {
        return cacheFileChannel.read(src);
    }

    public boolean isOpen() {
        return cacheFileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        cacheFileChannel.close();
    }

    public void resetPosition() throws IOException {
        cacheFileChannel.position(0);
    }
}