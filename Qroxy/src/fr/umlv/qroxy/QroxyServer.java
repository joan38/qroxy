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

    private static final Charset charset = Charset.forName("ISO-8859-1");
    private static final CharsetDecoder decoder = charset.newDecoder();
    private final Config config;
    private final SocketAddress listeningAddress;
    private ServerSocketChannel serverSocket;
    private Selector selector;

    public QroxyServer(SocketAddress bindAddress, Config config) {
        this.listeningAddress = bindAddress;
        this.config = config;
    }

    public void launch() throws IOException {
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(listeningAddress);

        selector = Selector.open();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocket.isOpen()) {
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
                        SocketConfig socketConfig = (SocketConfig) key.attachment();
                        if (key == socketConfig.getClient()) {
                            doReadFromClient(key);
                        } else if (key == socketConfig.getServer()) {
                            doReadFromServer(key);
                        } else {
                            throw new IOException("SelectionKey is neither a client or a server !");
                        }
                    } else if (key.isWritable()) {
                        SocketConfig socketConfig = (SocketConfig) key.attachment();
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
            client.register(key.selector(), SelectionKey.OP_READ, new SocketConfig());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void doConnectServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        try {
            if (channel.finishConnect()) {
                key.interestOps(SelectionKey.OP_WRITE);
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        closeClientServer(key);
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

                if (data.contains("\r\n\r\n")) {
                    HttpHeader httpHeader = HttpHeader.parse(data);
                    URL url = httpHeader.getUrl();
                    SocketChannel serverChannel = SocketChannel.open();
                    serverChannel.connect(new InetSocketAddress(url.getHost(), url.getPort()));
                    SelectionKey keyServer = serverChannel.register(selector, SelectionKey.OP_CONNECT, socketConfig);
                    socketConfig.setServer(keyServer);
                } else if (nbReaded == -1) {
                    channel.close();
                }
            } else if (nbReaded == -1) {
                key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
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
            closeClientServer(key);
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
            closeClientServer(key);
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
            closeClientServer(key);
        }
    }

    private void closeClientServer(SelectionKey key) {
        try {
            SocketConfig socketConfig = (SocketConfig) key.attachment();

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
        serverSocket.close();
        selector.wakeup();
    }
}
