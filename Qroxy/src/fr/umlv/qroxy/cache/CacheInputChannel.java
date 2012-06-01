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

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public class CacheInputChannel implements Closeable {

    private File cacheFile;
    private FileChannel cacheFileChannel;

    public CacheInputChannel(File cacheFile) throws FileNotFoundException {
        this.cacheFile = cacheFile;
        this.cacheFileChannel = new FileInputStream(cacheFile).getChannel();
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

    public File getFile() {
        return cacheFile;
    }
}