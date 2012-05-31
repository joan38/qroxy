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

import fr.umlv.qroxy.http.HttpResponseHeader;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;

/**
 *
 * @author Guillaume
 */
class CacheEntry {
    private final HttpResponseHeader header;
    private final int  hashedUri;
    
    public CacheEntry(HttpResponseHeader header, int uri, FileInputStream data) {
        this.header = header;
        this.data = data;
        this.hashedUri = uri;
    }
    
    public int read(ByteBuffer bb, int size) throws IOException {
        byte[] bytes = new byte[size];
        bb.flip();
        data.read(bytes);
        bb.put(bytes);
        return size;
    }
    
    public int getHashedUri() {
        return hashedUri;
    }
   
    @Override
    public int hashCode() {
        return 0;
    }
    
    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        }
        if(o == null) {
            return false;
        }
        if(o.getClass() != this.getClass()) {
            return false;
        }
        CacheEntry ce = (CacheEntry)o;
        return hashedUri==ce.hashedUri ;
    }
}
