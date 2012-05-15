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
package fr.umlv.qroxy;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 *
 * @author joan
 */
public class SocketConfig {
    private ByteBuffer buffer;
    private SelectionKey server;
    private SelectionKey client;

    public SocketConfig() {
        buffer = ByteBuffer.allocate(4096);
        buffer.flip();
    }

    public void setClient(SelectionKey client) {
        this.client = client;
    }

    public void setServer(SelectionKey server) {
        this.server = server;
    }

    public ByteBuffer getBuffer() {
        return buffer;
    }

    public SelectionKey getClient() {
        return client;
    }

    public SelectionKey getServer() {
        return server;
    }
}
