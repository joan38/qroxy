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
import fr.umlv.qroxy.config.Category;
import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.config.QosRule;
import fr.umlv.qroxy.http.HttpHeader;
import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.HttpStatusCode;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import fr.umlv.qroxy.http.exceptions.HttpSendingErrorCodeException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedMethodException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedVersionException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.nio.channels.UnresolvedAddressException;
import java.util.*;
import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 *
 * @author joan
 */
public class HttpConnectionHandler implements LinkHandler, Delayed {

    private final CacheAccess cache;
    private final SelectionKey clientKey;
    private final CacheExchangingHandler cacheExchangingHandler;
    private Timer whohasCancelTimer;
    private final Collection<Category> categories;
    private final Proxy proxy;
    private ByteBuffer buffer;
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
    private Integer bytesLeftInSecond;
    private int clientPausedInterestOps;
    private int serverPausedInterestOps;
    private long minDate;

    public HttpConnectionHandler(SelectionKey client,
            CacheAccess cache,
            CacheExchangingHandler cacheExchangingHandler,
            Collection<Category> categories,
            Proxy proxy) {
        this.buffer = ByteBuffer.allocate(Config.MAX_HEADER_LENGTH);
        this.buffer.flip();
        this.cache = cache;
        this.clientKey = client;
        this.cacheExchangingHandler = cacheExchangingHandler;
        this.categories = categories;
        this.proxy = proxy;
    }

    @Override
    public void read(SelectionKey key) throws IOException {
        try {
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
        } catch (HttpSendingErrorCodeException e) {
            // Do nothing
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

    private void readIfModifiedResponseFromServer() throws HttpSendingErrorCodeException {
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
            
            //Qos
            if (bytesLeftInSecond != null) {
                bytesLeftInSecond -= nbReaded;
                if (bytesLeftInSecond <= 0) {
                    if (System.nanoTime() < minDate) {
                        proxy.addDelayedConnection(this);
                        return;
                    }
                    bytesLeftInSecond += requestedHeader.getCategory().getQosRule().getMaxSpeed();
                    minDate = System.nanoTime() + 1000000000;
                }
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
                System.out.println("Chunked");
                break;
            case NO_CONTENT:
                serverKey.interestOps(0);
        }
    }

    private void readFromClient() throws HttpSendingErrorCodeException {
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

    private boolean readRequestHeader() throws IOException, HttpSendingErrorCodeException {
        resetAllForNewRequest();
        String data = HttpHeader.DECODER.decode(buffer).toString();
        buffer.rewind();
        try {
            requestedHeader = HttpRequestHeader.parse(data);
            currentHeaderLength = data.indexOf("\r\n\r\n") + 4;

            // Qos
            try {
                requestedHeader.matchesCatagories(categories);
                if (requestedHeader.getCategory().getQosRule().getMaxSpeed() == 0) {
                    sendErrorCode(HttpStatusCode.NOT_ACCEPTABLE);
                }
                // Activate speed limit juste by puting something not null
                bytesLeftInSecond = 0;
            } catch (NullPointerException e) {
                // No QosRule
            }

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
                System.out.println("Chunked");
                break;
            case NO_CONTENT:
            // Do not set clientKey.interestOps(0); because it depends on cache connection in local
        }
    }

    private void inCache() throws HttpSendingErrorCodeException {
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

    private void doNotUseCacheForRequest() throws HttpSendingErrorCodeException {
        try {
            connectToServer(requestedHeader.getUri());
            clientKey.interestOps(0);
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
    }

    private void notInCache() throws HttpSendingErrorCodeException {
        try {
            // Ask on multicast
            cacheExchangingHandler.sendWhoHas(requestedHeader.getUri(), this);
            clientKey.interestOps(0);

            whohasCancelTimer = new Timer();
            whohasCancelTimer.schedule(new TimerTask() {

                @Override
                public void run() {
                    try {
                        cacheExchangingHandler.cancelWhoHas(requestedHeader.getUri());
                        connectToServer(requestedHeader.getUri());
                    } catch (IOException e) {
                        close();
                    } catch (HttpSendingErrorCodeException e) {
                        // Do nothing
                    }
                }
            }, Config.WHOHAS_CANCEL_TIME_OUT);
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
        try {
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
        } catch (HttpSendingErrorCodeException e) {
        }
    }

    private void cachedResourceExpired(HttpResponseHeader cachedResponseHeader) throws HttpSendingErrorCodeException {
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

    private void connectToServer(InetSocketAddress address) throws IOException, HttpSendingErrorCodeException {
        if (address.equals(currentServerAddress)) {
            return;
        }
        try {
            SocketChannel serverChannel = SocketChannel.open();
            serverChannel.configureBlocking(false);
            serverKey = serverChannel.register(clientKey.selector(), SelectionKey.OP_CONNECT, this);
            serverChannel.connect(address);
            currentServerAddress = address;
        } catch (UnresolvedAddressException e) {
            sendErrorCode(HttpStatusCode.NOT_FOUND);
        }
    }

    private void connectToServer(URI uri) throws IOException, HttpSendingErrorCodeException {
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
                if (!keepAlive) {
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
        bytesLeftInSecond = null;
        minDate = 0;
        clientKey.interestOps(SelectionKey.OP_READ);
        if (serverKey != null) {
            serverKey.interestOps(0);
        }
    }

    private void sendErrorCode(HttpStatusCode statusCode) throws HttpSendingErrorCodeException {
        buffer = ByteBuffer.wrap(statusCode.getHttpResponse().getBytes(HttpHeader.CHARSET));
        closed = true;
        cachedResponse = null;
        clientKey.interestOps(SelectionKey.OP_WRITE);
        if (serverKey != null) {
            serverKey.interestOps(0);
        }
        throw new HttpSendingErrorCodeException();
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

    public void pauseConnection() {
        clientPausedInterestOps = clientKey.interestOps();
        serverPausedInterestOps = serverKey.interestOps();
        clientKey.interestOps(0);
        serverKey.interestOps(0);
    }

    public void resumeConnection() {
        clientKey.interestOps(clientPausedInterestOps);
        serverKey.interestOps(serverPausedInterestOps);
    }

    @Override
    public long getDelay(TimeUnit unit) {
        switch (unit) {
            case DAYS:
                return (minDate - System.nanoTime()) / 1000000000000000000l;
            case HOURS:
                return (minDate - System.nanoTime()) / 1000000000000000l;
            case MINUTES:
                return (minDate - System.nanoTime()) / 1000000000000l;
            case SECONDS:
                return (minDate - System.nanoTime()) / 1000000000l;
            case MILLISECONDS:
                return (minDate - System.nanoTime()) / 1000000l;
            case MICROSECONDS:
                return (minDate - System.nanoTime()) / 1000l;
            case NANOSECONDS:
                return minDate - System.nanoTime();
            default:
                return 0;
        }
    }

    @Override
    public int compareTo(Delayed o) {
        return (getDelay(TimeUnit.NANOSECONDS) < o.getDelay(TimeUnit.NANOSECONDS) ? -1 : 1);
    }
}
