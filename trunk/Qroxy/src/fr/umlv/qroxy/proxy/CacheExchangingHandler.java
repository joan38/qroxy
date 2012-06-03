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
package fr.umlv.qroxy.proxy;

import fr.umlv.qroxy.cache.CacheAccess;
import fr.umlv.qroxy.cache.channels.CacheInputChannel;
import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.*;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.DatagramChannel;
import java.nio.channels.SelectionKey;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentHashMap;

/**
 *
 * @author joan
 */
public class CacheExchangingHandler implements LinkHandler {

    private static final int BUFFER_SIZE = 2048;
    private final CacheAccess cache;
    private final Config config;
    private final LinkedList<CacheInputChannel> myResources = new LinkedList();
    private final ConcurrentHashMap<URI, HttpConnectionHandler> requestedResources = new ConcurrentHashMap();

    public CacheExchangingHandler(CacheAccess cache, Config config) {
        this.cache = cache;
        this.config = config;
    }

    @Override
    public void read(SelectionKey key) {
        try {
            // Searching for a WHOHAS request
            DatagramChannel channel = (DatagramChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
            buffer.flip();

            SocketAddress source;
            while (true) {
                buffer.compact();
                source = channel.receive(buffer);
                if (source == null) {
                    break;
                }
                buffer.flip();

                String data = HttpHeader.DECODER.decode(buffer).toString();
                try {
                    HttpRequestHeader whohasRequest = HttpRequestHeader.parse(data);
                    if (whohasRequest.getMethod().equals(HttpMethod.WHOHAS)) {
                        // Is a WHOHAS request
                        CacheInputChannel resource = cache.getResource(whohasRequest);
                        if (resource != null) {
                            myResources.add(resource);
                            key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                        }
                    }
                } catch (HttpMalformedHeaderException e) {
                    HttpResponseHeader ownResponse = HttpResponseHeader.parse(data);
                    if (ownResponse.getStatusCode().equals(HttpStatusCode.OWN)
                            && requestedResources.containsKey(ownResponse.getLocation())) {
                        // Is a OWN response and requested resource
                        requestedResources.get(ownResponse.getLocation()).ownResponse((InetSocketAddress) source, ownResponse);
                    }
                }
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    @Override
    public void write(SelectionKey key) {
        try {
            // Send OWN response
            DatagramChannel channel = (DatagramChannel) key.channel();

            for (CacheInputChannel resource : myResources) {
                ByteBuffer buffer = ByteBuffer.allocate(BUFFER_SIZE);
                resource.read(buffer);
                HttpResponseHeader responseHeader = HttpResponseHeader.parse(HttpHeader.DECODER.decode(buffer).toString());
                HttpResponseHeader ownResponse = HttpResponseHeader.getOwnResponse(responseHeader);

                // Send OWN on the multicast if in furtur version proxy remember resource owners
                int nbWrited = channel.send(ByteBuffer.wrap(ownResponse.toString().getBytes(HttpHeader.CHARSET)),
                        new InetSocketAddress(config.getCacheExchangingMulticastAddress(), config.getProxyListeningAddress().getPort()));
                
                if (nbWrited == 0) {
                    break;
                }
                myResources.removeFirst();
            }
            
            if (myResources.isEmpty()) {
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            // Do nothing
        }
    }

    public void sendWhoHas(URI resource, HttpConnectionHandler requester) throws IOException {
        DatagramChannel udpChannel = DatagramChannel.open();
        udpChannel.send(ByteBuffer.wrap(HttpRequestHeader.getWhoHasRequest(resource).toString().getBytes(HttpHeader.CHARSET)),
                new InetSocketAddress(config.getCacheExchangingMulticastAddress(), config.getProxyListeningAddress().getPort()));
        requestedResources.put(resource, requester);
    }
    
    public void cancelWhoHas(URI resource) {
        requestedResources.remove(resource);
    }

    @Override
    public int priority() {
        return 10;
    }
}
