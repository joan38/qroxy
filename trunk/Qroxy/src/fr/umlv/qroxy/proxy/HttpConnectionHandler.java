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

import fr.umlv.qroxy.cache.CacheAccess;
import fr.umlv.qroxy.cache.CacheException;
import fr.umlv.qroxy.cache.channels.CacheInputChannel;
import fr.umlv.qroxy.cache.channels.CacheOutputChannel;
import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.HttpHeader;
import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.HttpStatusCode;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedMethodException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedVersionException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.Date;
import java.util.Objects;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author joan
 */
public class HttpConnectionHandler implements LinkHandler {

    private static final int WHOHAS_CANCEL_TIME_OUT = 100;
    private final ByteBuffer buffer;
    private final CacheAccess cache;
    private final SelectionKey clientKey;
    private final CacheExchangingHandler cacheExchangingHandler;
    private final Timer whohasCancelTimer = new Timer();
    private SelectionKey serverKey;
    private HttpRequestHeader requestedHeader;
    private HttpResponseHeader respondedHeader;
    private boolean closed;
    private boolean keepAlive = true;
    private CacheInputChannel cachedResponse;
    private CacheOutputChannel cacher;
    private InetSocketAddress currentServerAddress;
    private long nbReadedByte;
    private int currentHeaderLength;

    public HttpConnectionHandler(SelectionKey client, CacheAccess cache, CacheExchangingHandler cacheExchangingHandler) {
        this.buffer = ByteBuffer.allocate(Config.MAX_HEADER_LENGTH);
        this.buffer.flip();
        this.cache = cache;
        this.clientKey = client;
        this.cacheExchangingHandler = cacheExchangingHandler;
    }

    @Override
    public void read(SelectionKey key) throws IOException {
        if (key == clientKey) {
            readFromClient();
        } else if (key == serverKey) {
            if (cachedResponse == null) {
                readFromServer();
            } else {
                // If modified sent
                readIfModifiedResponseFromServer();
            }
        } else {
            throw new IOException("SelectionKey is neither a client or a server !");
        }
    }

    @Override
    public void write(SelectionKey key) throws IOException {
        if (key == clientKey) {
            if (cachedResponse == null) {
                writeToClientFromBuffer();
            } else {
                writeToClientFromCache();
            }
        } else if (key == serverKey) {
            writeToServer();
        } else {
            throw new IOException("SelectionKey is neither a client or a server !");
        }
    }

    private void readIfModifiedResponseFromServer() {
        if (cachedResponse != null) {
            // If modified response is expected
            if (respondedHeader.getStatusCode() == HttpStatusCode.NOT_MODIFIED) {
                if (requestedHeader == null) {
                    clientKey.interestOps(SelectionKey.OP_WRITE);
                } else {
                    readFromServer();
                }
            } else {
                cachedResourceExpired(respondedHeader);
            }
        }
    }

    private void readFromServer() {
        SocketChannel channel = (SocketChannel) serverKey.channel();
        try {
            buffer.compact();
            int nbReaded = channel.read(buffer);
            buffer.flip();

            if (nbReaded == -1) {
                channel.shutdownInput();
                serverKey.interestOps(0);
                closed = true;
                return;
            }
            nbReadedByte += nbReaded;

            if (respondedHeader == null) {
                if (!readResponseHeader()) {
                    return;
                }
                clientKey.interestOps(SelectionKey.OP_WRITE);
            }
            readContentFromServer();
        } catch (IOException e) {
            try {
                channel.shutdownInput();
            } catch (IOException ex) {
            }
            serverKey.interestOps(0);
            closed = true;
        }
    }

    private boolean readResponseHeader() throws IOException {
        String data = HttpHeader.DECODER.decode(buffer).toString();
        buffer.rewind();
        try {
            respondedHeader = HttpResponseHeader.parse(data);
            currentHeaderLength = data.indexOf("\r\n\r\n") + 4;
            try {
                cacher = cache.cacheResource(requestedHeader.getUri());
            } catch (CacheException e) {
                // Not cachable
            }
            clientKey.interestOps(SelectionKey.OP_WRITE);
            return true;
        } catch (HttpMalformedHeaderException e) {
            if (data.contains("\r\n\r\n")) {
                throw e;
            }
        }
        return false;
    }

    private void readContentFromServer() {
        switch (respondedHeader.contentTransferMode()) {
            case CONTENT_LENGTH:
                if (nbReadedByte >= currentHeaderLength + respondedHeader.getContentLength()) {
                    serverKey.interestOps(0);
                }
                break;
            case CONNECTION_CLOSE:
                break;
            case CHUNKED:
                // TODO
                break;
            case NO_CONTENT:
                serverKey.interestOps(0);
        }
    }

    private void readFromClient() {
        try {
            SocketChannel channel = (SocketChannel) clientKey.channel();
            buffer.compact();
            int nbReaded = channel.read(buffer);
            buffer.flip();

            if (nbReaded == -1) {
                close();
                return;
            }
            nbReadedByte += nbReaded;

            if (respondedHeader == null) {
                if (!readRequestHeader()) {
                    return;
                }
                
                // Connection
                try {
                    cachedResponse = cache.getResource(requestedHeader);
                    if (cachedResponse != null) {
                        // Is in local cache
                        inCache();
                    } else {
                        notInCache();
                    }
                } catch (CacheException e) {
                    // Do not use cache
                    doNotUseCacheForRequest();
                }
            }
            readContentFromClient();
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    private boolean readRequestHeader() throws IOException {
        String data = HttpHeader.DECODER.decode(buffer).toString();
        buffer.rewind();
        try {
            requestedHeader = HttpRequestHeader.parse(data);
            currentHeaderLength = data.indexOf("\r\n\r\n") + 4;
            return true;
        } catch (HttpUnsupportedVersionException e) {
            sendErrorCode(HttpStatusCode.HTTP_VERSION_NOT_SUPPORTED);
        } catch (HttpUnsupportedMethodException e) {
            sendErrorCode(HttpStatusCode.METHOD_NOT_ALLOWED);
        } catch (HttpMalformedHeaderException e) {
            if (buffer.limit() == buffer.capacity()) {
                // Buffer is full, so header is too long
                sendErrorCode(HttpStatusCode.REQUEST_ENTITY_TOO_LARGE);
            } else if (data.contains("\r\n\r\n")) {
                sendErrorCode(HttpStatusCode.BAD_REQUEST);
            }
        }
        return false;
    }

    private void readContentFromClient() {
        switch (requestedHeader.contentTransferMode()) {
            case CONTENT_LENGTH:
                if (nbReadedByte >= currentHeaderLength + requestedHeader.getContentLength()) {
                    clientKey.interestOps(0);
                }
                break;
            case CHUNKED:
                // TODO
                break;
            case NO_CONTENT:
                // Do not set clientKey.interestOps(0); because it depends on cache connection in local
        }
    }

    private void inCache() {
        // Is in local cache
        ByteBuffer cachedData = ByteBuffer.allocate(Config.MAX_HEADER_LENGTH);
        HttpResponseHeader cachedResponseHeader;
        try {
            cachedResponse.read(cachedData);
            cachedData.flip();
            cachedResponseHeader = HttpResponseHeader.parse(HttpHeader.DECODER.decode(cachedData).toString());
            cachedResponse.resetPosition();
        } catch (IOException e) {
            cache.corruptCachedResource(cachedResponse);
            cachedResponse = null;
            notInCache();
            return;
        }

        if (cachedResponseHeader.getExpires().before(new Date())) {
            // Expired
            cachedResourceExpired(cachedResponseHeader);
        } else {
            // Not expired
            clientKey.interestOps(SelectionKey.OP_WRITE);
        }
    }

    private void doNotUseCacheForRequest() {
        try {
            connectToServer(requestedHeader.getUri());
            clientKey.interestOps(0);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    private void notInCache() {
        try {
            // Ask on multicast
            cacheExchangingHandler.sendWhoHas(requestedHeader.getUri(), this);
            clientKey.interestOps(0);

            whohasCancelTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    cacheExchangingHandler.cancelWhoHas(requestedHeader.getUri());
                }
            }, WHOHAS_CANCEL_TIME_OUT);
        } catch (IOException e) {
            try {
                connectToServer(requestedHeader.getUri());
                clientKey.interestOps(0);
            } catch (IOException ex) {
                e.printStackTrace();
                close();
            }
        }
    }

    public void ownResponse(InetSocketAddress source, HttpResponseHeader ownResponse) {
        whohasCancelTimer.cancel();
        if (ownResponse.getExpires().before(new Date())) {
            // Expired
            cachedResourceExpired(ownResponse);
        } else {
            // Not expired  
            try {
                respondedHeader = null;
                connectToServer(source);
                clientKey.interestOps(SelectionKey.OP_WRITE);
            } catch (IOException e) {
                try {
                    connectToServer(requestedHeader.getUri());
                    clientKey.interestOps(0);
                } catch (IOException ex) {
                    e.printStackTrace();
                    close();
                }
            }
        }
    }

    private void cachedResourceExpired(HttpResponseHeader cachedResponseHeader) {
        try {
            // Modify the header with If-Modified-Since, If-None-Match and Connection: close;
            requestedHeader.setIfModifiedCheckFields(cachedResponseHeader.getDate(), cachedResponseHeader.getETag());
            connectToServer(requestedHeader.getUri());
            clientKey.interestOps(0);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    private void connectToServer(InetSocketAddress address) throws IOException {
        if (address.equals(currentServerAddress)) {
            return;
        }
        SocketChannel serverChannel = SocketChannel.open();
        serverChannel.configureBlocking(false);
        serverKey = serverChannel.register(clientKey.selector(), SelectionKey.OP_CONNECT, this);
        serverChannel.connect(address);
        currentServerAddress = address;
    }

    private void connectToServer(URI uri) throws IOException {
        connectToServer(new InetSocketAddress(uri.getHost(), uri.getPort()));
    }

    private void writeToClientFromCache() {
        Objects.requireNonNull(cachedResponse);
        try {
            SocketChannel channel = (SocketChannel) clientKey.channel();
            buffer.compact();
            int nbReaded = cachedResponse.read(buffer);
            buffer.flip();
            channel.write(buffer);

            if (!buffer.hasRemaining() && nbReaded == -1) {
                if (keepAlive) {
                    resetAllForNewRequest();
                } else {
                    close();
                }
            }
        } catch (IOException e) {
            close();
        }
    }

    private void writeToServer() {
        SocketChannel channel = (SocketChannel) serverKey.channel();

        try {
            channel.write(buffer);
        } catch (IOException e) {
            close();
            return;
        }

        if (!buffer.hasRemaining()) {
            if (closed) {
                close();
                return;
            }
            if (clientKey.interestOps() == 0) {
                // Request sent, now read the response
                respondedHeader = null;
                nbReadedByte = 0;
                serverKey.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void writeToClientFromBuffer() {
        SocketChannel channel = (SocketChannel) clientKey.channel();

        try {
            int limit = buffer.limit();
            if (cacher != null) {
                cacher.write(buffer);
                buffer.limit(buffer.position());
                buffer.rewind();
            }
            channel.write(buffer);
            buffer.limit(limit);
        } catch (IOException e) {
            close();
            return;
        }

        if (!buffer.hasRemaining()) {
            if (closed) {
                close();
                return;
            }
            if (serverKey.interestOps() == 0) {
                // Response sent
                if (!keepAlive) {
                    close();
                    return;
                }
                // Now reset all
                resetAllForNewRequest();
            }
        }
    }

    private void resetAllForNewRequest() {
        requestedHeader = null;
        respondedHeader = null;
        cachedResponse = null;
        cacher = null;
        currentServerAddress = null;
        nbReadedByte = 0;
        currentHeaderLength = 0;
        clientKey.interestOps(SelectionKey.OP_READ);
        if (serverKey != null) {
            serverKey.interestOps(0);
        }
    }

    private void sendErrorCode(HttpStatusCode statusCode) {
        buffer.clear();
        buffer.put(statusCode.getHttpResponse().getBytes(HttpHeader.CHARSET));
        buffer.flip();
        closed = true;
        cachedResponse = null;
        clientKey.interestOps(SelectionKey.OP_WRITE);
    }

    public void close() {
        try {
            clientKey.channel().close();
            if (serverKey != null) {
                serverKey.channel().close();
            }
            if (cacher != null) {
                cacher.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
