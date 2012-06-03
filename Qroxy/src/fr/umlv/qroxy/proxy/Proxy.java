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
import fr.umlv.qroxy.config.Config;
import java.io.IOException;
import java.net.InterfaceAddress;
import java.net.NetworkInterface;
import java.net.StandardProtocolFamily;
import java.net.StandardSocketOptions;
import java.nio.channels.*;
import java.util.*;
import java.util.concurrent.DelayQueue;

/**
 *
 * @author joan
 */
public class Proxy {

    private final Config config;
    private final CacheAccess cache;
    private Selector selector;
    private CacheExchangingHandler cacheExchangingHandler;
    private DelayQueue<HttpConnectionHandler> delayedConnections = new DelayQueue();

    public Proxy(Config config, CacheAccess cache) {
        this.config = config;
        this.cache = cache;
    }

    public void launch() throws IOException {
        // Selector
        selector = Selector.open();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();

        // Proxy
        ServerSocketChannel proxy = ServerSocketChannel.open();
        proxy.configureBlocking(false);
        proxy.bind(config.getProxyListeningAddress());
        proxy.register(selector, SelectionKey.OP_ACCEPT);

        // Cache Exchanger
        DatagramChannel cacheExchanger = DatagramChannel.open(StandardProtocolFamily.INET);
        cacheExchanger.configureBlocking(false);
        cacheExchanger.setOption(StandardSocketOptions.SO_REUSEADDR, true);
        cacheExchanger.bind(config.getProxyListeningAddress());

        // Join multicast on all up IPv4 complient interfaces except loopback
        Enumeration<NetworkInterface> networkInterfaces = NetworkInterface.getNetworkInterfaces();
        while (networkInterfaces.hasMoreElements()) {
            NetworkInterface nextElement = networkInterfaces.nextElement();
            if (nextElement != null && nextElement.isUp() && nextElement.supportsMulticast() && !nextElement.isLoopback()) {
                for (InterfaceAddress address : nextElement.getInterfaceAddresses()) {
                    if (address.getAddress().getAddress().length == 4) {
                        // Is IPv4
                        cacheExchanger.join(config.getCacheExchangingMulticastAddress(), nextElement);
                        break;
                    }
                }
            }
        }
        cacheExchangingHandler = new CacheExchangingHandler(cache, config);
        cacheExchanger.register(selector, SelectionKey.OP_READ, cacheExchangingHandler);

        for (int i = 0; selector.isOpen(); i = ++i % 10) {
            while (true) {
                HttpConnectionHandler connection = delayedConnections.poll();
                if (connection == null) {
                    break;
                }
                connection.resumeConnection();
            }
            
            for (SelectionKey key : selectedKeys) {
                if (key.isAcceptable()) {
                    doAcceptNewClient(key);
                } else if (key.isConnectable()) {
                    doConnectServer(key);
                } else if (!key.isValid()) {
                    ((HttpConnectionHandler) key.attachment()).close();
                } else if (key.isReadable() && ((LinkHandler) key.attachment()).priority() > i) {
                    ((LinkHandler) key.attachment()).read(key);
                } else if (key.isWritable() && ((LinkHandler) key.attachment()).priority() > i) {
                    ((LinkHandler) key.attachment()).write(key);
                }
            }
            
            selectedKeys.clear();
            selector.select(1000);
        }
    }

    private void doAcceptNewClient(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        try {
            client.configureBlocking(false);
            SelectionKey clientKey = client.register(key.selector(), SelectionKey.OP_READ);
            clientKey.attach(new HttpConnectionHandler(clientKey, cache, cacheExchangingHandler, config.getCategories(), this));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doConnectServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.finishConnect()) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            ((HttpConnectionHandler) key.attachment()).close();
        }
    }

    /**
     * Stop the proxy brutally
     */
    public void stop() throws IOException {
        selector.close();
    }

    public boolean addDelayedConnection(HttpConnectionHandler connection) {
        connection.pauseConnection();
        return delayedConnections.add(connection);
    }
}
