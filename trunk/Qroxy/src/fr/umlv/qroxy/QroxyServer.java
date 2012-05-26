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

import fr.umlv.qroxy.http.HttpHeaderOld;
import fr.umlv.qroxy.conf.Config;
import fr.umlv.qroxy.http.HttpRequestHeader;
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

    private static final Charset charset = Charset.forName("ISO-8859-1");
    private static final CharsetDecoder decoder = charset.newDecoder();
    private final Config config;
    private final SocketAddress listeningAddress;
    private ServerSocketChannel server;
    private Selector selector;

    public QroxyServer(SocketAddress bindAddress, Config config) {
        this.listeningAddress = bindAddress;
        this.config = config;
    }

    public void launch() throws IOException {
        server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(listeningAddress);

        selector = Selector.open();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        server.register(selector, SelectionKey.OP_ACCEPT);

        while (server.isOpen()) {
            selector.select();

            try {
                for (SelectionKey key : selectedKeys) {
                    if (key.isAcceptable()) {
                        doAcceptNewClient(key);
                    } else if (key.isConnectable()) {
                        doConnectServer(key);
                    } else if (!key.isValid()) {
                        closeClientServer(key);
                    } else if (key.isReadable()) {
                        SocketAttachment socketConfig = (SocketAttachment) key.attachment();
                        if (key == socketConfig.getClient()) {
                            doReadFromClient(key);
                        } else if (key == socketConfig.getServer()) {
                            doReadFromServer(key);
                        } else {
                            throw new IOException("SelectionKey is neither a client or a server !");
                        }
                    } else if (key.isWritable()) {
                        SocketAttachment socketConfig = (SocketAttachment) key.attachment();
                        if (key == socketConfig.getClient()) {
                            doWriteToClient(key);
                        } else if (key == socketConfig.getServer()) {
                            doWriteToServer(key);
                        } else {
                            throw new IOException("SelectionKey is neither a client or a server !");
                        }
                    }
                }
            } finally {
                selectedKeys.clear();
            }
        }
    }

    private void doAcceptNewClient(SelectionKey key) throws IOException {
        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
        try {
            client.configureBlocking(false);
            SocketAttachment socketConfig = new SocketAttachment();
            socketConfig.setClient(client.register(key.selector(), SelectionKey.OP_READ, socketConfig));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doConnectServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.finishConnect()) {
                key.interestOps(SelectionKey.OP_WRITE | SelectionKey.OP_READ);
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void doReadFromClient(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketAttachment = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketAttachment.getBufferClientToServer();

        try {
            buffer.compact();
            int nbReaded = channel.read(buffer);
            buffer.flip();

            if (socketAttachment.getServer() == null) {
                // Client not yet linked to the server
                String data = decoder.decode(buffer).toString();
                buffer.rewind();

                if (data.contains("\r\n\r\n") || data.contains("\n\n")) {
                    HttpRequestHeader httpHeader = HttpRequestHeader.parse(data);
                    URL url = httpHeader.getUri().toURL();
                    SocketChannel serverChannel = SocketChannel.open();
                    serverChannel.configureBlocking(false);
                    serverChannel.connect(new InetSocketAddress(url.getHost(), url.getPort()));
                    SelectionKey keyServer = serverChannel.register(selector, SelectionKey.OP_CONNECT, socketAttachment);
                    socketAttachment.setServer(keyServer);
                    key.interestOps(SelectionKey.OP_READ | SelectionKey.OP_WRITE);
                } else if (nbReaded == -1) {
                    channel.close();
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void doWriteToServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketConfig = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketConfig.getBufferClientToServer();

        try {
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void doReadFromServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketAttachment = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketAttachment.getBufferServerToClient();

        try {
            buffer.compact();
            channel.read(buffer);
            buffer.flip();
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void doWriteToClient(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketConfig = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketConfig.getBufferServerToClient();

        try {
            channel.write(buffer);
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void closeClientServer(SelectionKey key) {
        try {
            SocketAttachment socketConfig = (SocketAttachment) key.attachment();

            socketConfig.getClient().channel().close();
            SelectionKey serverKey = socketConfig.getServer();
            if (serverKey != null) {
                serverKey.channel().close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public void stop() throws IOException {
        server.close();
        selector.wakeup();
    }
}
