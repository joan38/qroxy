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

import fr.umlv.qroxy.http.HttpHeader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 *
 * @author joan
 */
public class SocketAttachment {

    private final ByteBuffer bufferClientToServer;
    private final ByteBuffer bufferServerToClient;
    private SelectionKey client;
    private SelectionKey server;

    public SocketAttachment() {
        bufferClientToServer = ByteBuffer.allocate(8192);
        bufferClientToServer.flip();
        
        bufferServerToClient = ByteBuffer.allocate(8192);
        bufferServerToClient.flip();
    }

    public void setClient(SelectionKey client) {
        this.client = client;
    }

    public void setServer(SelectionKey server) {
        this.server = server;
    }

    public ByteBuffer getBufferClientToServer() {
        return bufferClientToServer;
    }

    public ByteBuffer getBufferServerToClient() {
        return bufferServerToClient;
    }

    public SelectionKey getClient() {
        return client;
    }

    public SelectionKey getServer() {
        return server;
    }
}
