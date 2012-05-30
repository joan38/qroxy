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

import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import fr.umlv.qroxy.http.HttpStatusCode;
import fr.umlv.qroxy.http.exceptions.HttpPreconditionFailedException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedMethodException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedVersionException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.NoRouteToHostException;
import java.net.SocketAddress;
import java.net.URL;
import java.nio.ByteBuffer;
import java.nio.channels.*;
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
                        System.out.println("Accept new client");
                    } else if (key.isConnectable()) {
                        doConnectServer(key);
                        System.out.println("Connect to server");
                    } else if (!key.isValid()) {
                        closeClientServer(key);
                    } else if (key.isReadable()) {
                        SocketAttachment socketConfig = (SocketAttachment) key.attachment();
                        if (key == socketConfig.getClient()) {
                            doReadFromClient(key);
                            System.out.println("Read from client");
                        } else if (key == socketConfig.getServer()) {
                            doReadFromServer(key);
                            System.out.println("Read from server");
                        } else {
                            throw new IOException("SelectionKey is neither a client or a server !");
                        }
                    } else if (key.isWritable()) {
                        SocketAttachment socketConfig = (SocketAttachment) key.attachment();
                        if (key == socketConfig.getClient()) {
                            doWriteToClient(key);
                            System.out.println("Write to client");
                        } else if (key == socketConfig.getServer()) {
                            doWriteToServer(key);
                            System.out.println("Write to server");
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
                key.interestOps(SelectionKey.OP_WRITE);
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
            if (nbReaded == -1) {
                channel.shutdownInput();
                key.interestOps(0);
                socketAttachment.close();
            }
            socketAttachment.addReadedByte(nbReaded);

            if (socketAttachment.getRequestHeader() == null) {
                // Waiting for a new request header
                String data = decoder.decode(buffer).toString();
                buffer.rewind();

                if (!data.contains("\r\n\r\n")) {
                    // No header yet
                    if (buffer.limit() == buffer.capacity()) {
                        // Buffer is full, so header is too long
                        socketAttachment.getBufferServerToClient().clear();
                        socketAttachment.getBufferServerToClient().put(HttpStatusCode.REQUEST_ENTITY_TOO_LARGE.getHttpResponse().getBytes(charset));
                        socketAttachment.getBufferServerToClient().flip();
                        socketAttachment.close();
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    return;
                }

                // New header is here !
                HttpRequestHeader httpHeader = HttpRequestHeader.parse(data);
                socketAttachment.setRequestHeader(httpHeader);

                if (socketAttachment.getServer() == null) {
                    // Not yet linked to the server
                    SocketChannel serverChannel = SocketChannel.open();
                    serverChannel.configureBlocking(false);
                    SelectionKey keyServer = serverChannel.register(selector, SelectionKey.OP_CONNECT, socketAttachment);
                    socketAttachment.setServer(keyServer);
                    URL url = httpHeader.getUri().toURL();
                    serverChannel.connect(new InetSocketAddress(url.getHost(), url.getPort()));
                } else {
                    // Already linked, so write to the server
                    socketAttachment.getServer().interestOps(SelectionKey.OP_WRITE);
                }
            }

            // Detect the transfert method of HTTP message
            switch (socketAttachment.getRequestHeader().contentTransferMethod()) {
                case CONTENT_LENGTH:
                    if (socketAttachment.readedByte() >= socketAttachment.getRequestHeader().getHeaderLength() + socketAttachment.getRequestHeader().getContentLength()) {
                        socketAttachment.resetReadedByte();
                        key.interestOps(0);
                    }
                    return;
                case NO_CONTENT:
                    socketAttachment.resetReadedByte();
                    key.interestOps(0);
                    return;
                case CHUNKED:
                    // TODO
//                    System.out.println("Chunked !");
//                    closeClientServer(key);
                    return;
                default:
                    // No content transfer method matches: unable to detect the end of HTTP message
                    socketAttachment.getBufferServerToClient().clear();
                    socketAttachment.getBufferServerToClient().put(HttpStatusCode.LENGTH_REQUIRED.getHttpResponse().getBytes(charset));
                    socketAttachment.getBufferServerToClient().flip();
                    socketAttachment.close();
                    key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (UnresolvedAddressException | NoRouteToHostException e) {
            socketAttachment.getBufferServerToClient().clear();
            socketAttachment.getBufferServerToClient().put(HttpStatusCode.NOT_FOUND.getHttpResponse().getBytes(charset));
            socketAttachment.getBufferServerToClient().flip();
            socketAttachment.close();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (HttpPreconditionFailedException e) {
            socketAttachment.getBufferServerToClient().clear();
            socketAttachment.getBufferServerToClient().put(HttpStatusCode.PRECONDITION_FAILED.getHttpResponse().getBytes(charset));
            socketAttachment.getBufferServerToClient().flip();
            socketAttachment.close();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (HttpUnsupportedMethodException e) {
            socketAttachment.getBufferServerToClient().clear();
            socketAttachment.getBufferServerToClient().put(HttpStatusCode.METHOD_NOT_ALLOWED.getHttpResponse().getBytes(charset));
            socketAttachment.getBufferServerToClient().flip();
            socketAttachment.close();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (HttpUnsupportedVersionException e) {
            socketAttachment.getBufferServerToClient().clear();
            socketAttachment.getBufferServerToClient().put(HttpStatusCode.HTTP_VERSION_NOT_SUPPORTED.getHttpResponse().getBytes(charset));
            socketAttachment.getBufferServerToClient().flip();
            socketAttachment.close();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void doWriteToServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketAttachment = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketAttachment.getBufferClientToServer();

        try {
            channel.write(buffer);
        } catch (IOException e) {
            closeClientServer(key);
            return;
        }

        if (!buffer.hasRemaining()) {
            if (socketAttachment.isClosed()) {
                closeClientServer(key);
                return;
            }
            if (socketAttachment.getClient().interestOps() == 0) {
                // Request sent, now read the response
                socketAttachment.setRequestHeader(null);
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }

    private void doReadFromServer(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketAttachment = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketAttachment.getBufferServerToClient();

        try {
            buffer.compact();
            int nbReaded = channel.read(buffer);
            buffer.flip();
            if (nbReaded == -1) {
                channel.shutdownInput();
                key.interestOps(0);
                socketAttachment.close();
            }
            socketAttachment.addReadedByte(nbReaded);

            if (socketAttachment.getResponseHeader() == null) {
                // Waiting for a new response header
                String data = decoder.decode(buffer).toString();
                buffer.rewind();

                if (!data.contains("\r\n\r\n")) {
                    // No header yet
                    if (buffer.limit() == buffer.capacity()) {
                        // Buffer is full, so header is too long
                        socketAttachment.getBufferClientToServer().flip();
                        socketAttachment.getBufferClientToServer().put(HttpStatusCode.REQUEST_ENTITY_TOO_LARGE.getHttpResponse().getBytes(charset));
                        socketAttachment.close();
                        key.interestOps(SelectionKey.OP_WRITE);
                    }
                    return;
                }

                // New header is here !
                HttpResponseHeader httpHeader = HttpResponseHeader.parse(data);
                socketAttachment.setResponseHeader(httpHeader);
                socketAttachment.getClient().interestOps(SelectionKey.OP_WRITE);
            }

            // Detect the transfert method of HTTP message
            switch (socketAttachment.getResponseHeader().contentTransferMethod()) {
                case CONTENT_LENGTH:
                    if (socketAttachment.readedByte() >= socketAttachment.getResponseHeader().getHeaderLength() + socketAttachment.getResponseHeader().getContentLength()) {
                        socketAttachment.resetReadedByte();
                        key.interestOps(0);
                    }
                    return;
                case NO_CONTENT:
                    socketAttachment.resetReadedByte();
                    key.interestOps(0);
                    return;
                case CONNECTION_CLOSE:
                    return;
                case CHUNKED:
                    // TODO
//                    System.out.println("Chunked !");
//                    closeClientServer(key);
                    return;
                default:
                    // No content transfer method matches: unable to detect the end of HTTP message
                    socketAttachment.getBufferClientToServer().clear();
                    socketAttachment.getBufferClientToServer().put(HttpStatusCode.LENGTH_REQUIRED.getHttpResponse().getBytes(charset));
                    socketAttachment.getBufferClientToServer().flip();
                    socketAttachment.close();
                    key.interestOps(SelectionKey.OP_WRITE);
            }
        } catch (HttpUnsupportedVersionException e) {
            socketAttachment.getBufferServerToClient().clear();
            socketAttachment.getBufferServerToClient().put(HttpStatusCode.HTTP_VERSION_NOT_SUPPORTED.getHttpResponse().getBytes(charset));
            socketAttachment.getBufferServerToClient().flip();
            socketAttachment.close();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (HttpPreconditionFailedException e) {
            socketAttachment.getBufferServerToClient().clear();
            socketAttachment.getBufferServerToClient().put(HttpStatusCode.PRECONDITION_FAILED.getHttpResponse().getBytes(charset));
            socketAttachment.getBufferServerToClient().flip();
            socketAttachment.close();
            key.interestOps(SelectionKey.OP_WRITE);
        } catch (IOException e) {
            e.printStackTrace();
            closeClientServer(key);
        }
    }

    private void doWriteToClient(SelectionKey key) {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketAttachment socketAttachment = (SocketAttachment) key.attachment();
        ByteBuffer buffer = socketAttachment.getBufferServerToClient();

        try {
            channel.write(buffer);
        } catch (IOException e) {
            closeClientServer(key);
            return;
        }

        if (!buffer.hasRemaining()) {
            if (socketAttachment.isClosed()) {
                closeClientServer(key);
                return;
            }
            if (socketAttachment.getServer().interestOps() == 0) {
                // Response sent, now read the next request
                socketAttachment.setResponseHeader(null);
                key.interestOps(SelectionKey.OP_READ);
            }
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
