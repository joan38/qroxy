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
package fr.umlv.qroxy.proxy;

import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

/**
 *
 * @author joan
 */
public class SocketAttachment {

    private final ByteBuffer bufferClientToServer;
    private final ByteBuffer bufferServerToClient;
    private HttpRequestHeader requestHeader;
    private HttpResponseHeader responseHeader;
    private int readedByte;
    private boolean closed;
    private SelectionKey client;
    private SelectionKey server;

    public SocketAttachment() {
        bufferClientToServer = ByteBuffer.allocate(8192);
        bufferClientToServer.flip();
        
        bufferServerToClient = ByteBuffer.allocate(8192);
        bufferServerToClient.flip();
    }

    public boolean isClosed() {
        return closed;
    }

    public void close() {
        this.closed = true;
    }

    public int readedByte() {
        return readedByte;
    }
    
    public void resetReadedByte() {
        this.readedByte = 0;
    }

    public void addReadedByte(int readedByte) {
        this.readedByte += readedByte;
    }

    public HttpRequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(HttpRequestHeader requestHeader) {
        this.requestHeader = requestHeader;
    }

    public HttpResponseHeader getResponseHeader() {
        return responseHeader;
    }

    public void setResponseHeader(HttpResponseHeader responseHeader) {
        this.responseHeader = responseHeader;
    }

    public ByteBuffer getBufferClientToServer() {
        return bufferClientToServer;
    }

    public ByteBuffer getBufferServerToClient() {
        return bufferServerToClient;
    }

    public void setClient(SelectionKey client) {
        this.client = client;
    }

    public void setServer(SelectionKey server) {
        this.server = server;
    }

    public SelectionKey getClient() {
        return client;
    }

    public SelectionKey getServer() {
        return server;
    }
}
