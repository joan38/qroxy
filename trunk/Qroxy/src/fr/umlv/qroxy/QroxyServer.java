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

import fr.umlv.qroxy.conf.Config;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Set;

/**
 *
 * @author joan
 */
public class QroxyServer {

    private static Charset charset = Charset.forName("ISO-8859-1");
    private static CharsetDecoder decoder = charset.newDecoder();
    private final Config config;
    private final SocketAddress listeningAddress;
    private ServerSocketChannel serverSocket;
    private Selector clientSelector;
    private Selector serverSelector;

    public QroxyServer(SocketAddress bindAddress, Config config) {
        this.listeningAddress = bindAddress;
        this.config = config;
    }

    public void launch() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(listeningAddress);

        clientSelector = Selector.open();
        Set<SelectionKey> clientSelectedKeys = clientSelector.selectedKeys();
        serverSocket.register(clientSelector, SelectionKey.OP_ACCEPT);

        serverSelector = Selector.open();
        Set<SelectionKey> serverSelectedKeys = serverSelector.selectedKeys();

        while (serverSocket.isOpen()) {
            clientSelector.select(1000);
            serverSelector.select(1000); // TODO: A bouger dans une autre thread

            try {
                for (SelectionKey key : clientSelectedKeys) {
                    if (key.isAcceptable()) {
                        doAcceptNewClient(key);
                    }

                    if (key.isValid() && key.isReadable()) {
                        doReadFromClient(key);
                    }

                    if (key.isValid() && key.isWritable()) {
                        doWriteToClient(key);
                    }
                }

                for (SelectionKey key : serverSelectedKeys) {
                    if (key.isValid() && key.isReadable()) {
                        doReadFromServer(key);
                    }

                    if (key.isValid() && key.isWritable()) {
                        doWriteToServer(key);
                    }
                }
            } finally {
                serverSelectedKeys.clear();
            }
        }
    }

    private void doAcceptNewClient(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        try {
            client.configureBlocking(false);
            client.register(key.selector(), SelectionKey.OP_READ, new SocketConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doReadFromClient(SelectionKey key) {

        SocketChannel channel = (SocketChannel) key.channel();
        SocketConfig socketConfig = (SocketConfig) key.attachment();
        ByteBuffer buffer = socketConfig.getBuffer();

        try {
            buffer.compact();
            int nbReaded = channel.read(buffer);
            buffer.flip();

            if (socketConfig.getServer() == null) {
                String data = decoder.decode(buffer).toString();
                buffer.rewind();

                if (data.contains("\r\n\r\n") || data.contains("\n\n")) {
                    HttpHeader httpHeader = new HttpHeader(data);
                    URL url = httpHeader.getUrl();
                    SocketChannel serverChannel = SocketChannel.open();
                    serverChannel.connect(new InetSocketAddress(url.getHost(), url.getPort()));
                    SelectionKey keyServer = serverChannel.register(serverSelector, SelectionKey.OP_WRITE, socketConfig);
                    socketConfig.setServer(keyServer);
                } else if (nbReaded == -1) {
                    channel.close();
                }
            } else if (nbReaded == -1) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();

            try {
                key.channel().close();
                SelectionKey server = socketConfig.getServer();
                if (server != null) {
                    server.channel().close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doWriteToServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketConfig socketConfig = (SocketConfig) key.attachment();
        ByteBuffer buffer = socketConfig.getBuffer();

        try {
            channel.write(buffer);

            if (!buffer.hasRemaining() && socketConfig.getClient().interestOps() == SelectionKey.OP_WRITE) {
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();

            try {
                key.channel().close();
                socketConfig.getClient().channel().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doReadFromServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketConfig socketConfig = (SocketConfig) key.attachment();
        ByteBuffer buffer = socketConfig.getBuffer();

        try {
            buffer.compact();
            int nbReaded = channel.read(buffer);
            buffer.flip();

            if (nbReaded == -1) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();

            try {
                key.channel().close();
                socketConfig.getClient().channel().close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    private void doWriteToClient(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketConfig socketConfig = (SocketConfig) key.attachment();
        ByteBuffer buffer = socketConfig.getBuffer();

        try {
            channel.write(buffer);

            if (!buffer.hasRemaining() && socketConfig.getServer().interestOps() == SelectionKey.OP_WRITE) {
                key.interestOps(SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();

            try {
                key.channel().close();
                SelectionKey server = socketConfig.getServer();
                if (server != null) {
                    server.channel().close();
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }
}
