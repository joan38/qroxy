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

import java.io.Closeable;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;


/**
 * Represents a connection to the cache in order to read cached data for
 * the the outdoors environment. It encapsulates a FileChannel to read
 * from he cache.
 * @author jgoyau
 */
public class CacheInputChannel implements Closeable, AutoCloseable {
    private final FileChannel cacheFileChannel;

    CacheInputChannel(FileChannel cacheFileChannel) {
        this.cacheFileChannel = cacheFileChannel;
    }

    /**
     * Read data in the channel and store it in the ByteBuffer parsed in argument.
     * @param src
     * @return the number of read bytes
     * @throws IOException 
     */
    public int read(ByteBuffer src) throws IOException {
        return cacheFileChannel.read(src);
    }

    /**
     * Test whether or not the channel is open.
     * @return true if the channel is open, else false 
     */
    public boolean isOpen() {
        return cacheFileChannel.isOpen();
    }

    @Override
    public void close() throws IOException {
        cacheFileChannel.close();
    }

    /**
     * Set the reading pointer to the begin of the channel.
     * @throws IOException 
     */
    public void resetPosition() throws IOException {
        cacheFileChannel.position(0);
    }
}