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
package fr.umlv.qroxy.webui;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 * 
 * @author joan
 */
public class Server {

    private final SocketAddress bindAddress;
    private ServerSocketChannel serverSocket;

    public Server(SocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }

    public void launch() throws IOException {
        serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(bindAddress);

        Selector selector = Selector.open();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);

        while (serverSocket.isOpen()) {
            selector.select(1000);
            try {
                for (SelectionKey key : selectedKeys) {
                    if (key.isAcceptable()) {
                        SocketChannel client = ((ServerSocketChannel) key.channel()).accept();
                        client.configureBlocking(false);
                        ByteBuffer buffer = ByteBuffer.wrap(HtmlPageGenerators.getConfigPage().getBytes());
                        client.register(selector, SelectionKey.OP_WRITE, buffer);
                    }

                    if (key.isValid() && key.isWritable()) {
                        SocketChannel channel = (SocketChannel) key.channel();
                        ByteBuffer buffer = (ByteBuffer) key.attachment();
                        channel.write(buffer);

                        if (buffer.remaining() == 0) {
                            channel.close();
                        }
                    }
                }
            } finally {
                selectedKeys.clear();
            }
        }
    }

    public void stop() throws IOException {
        serverSocket.close();
    }

    public static void main(String[] args) throws IOException {
        final Server webui = new Server(new InetSocketAddress(7777));

        new Timer().schedule(new TimerTask() {

            @Override
            public void run() {
                try {
                    webui.stop();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                System.out.println("Arrete");
            }
        }, 10000);
        
        webui.launch();
        System.out.println("Ceeeeee bon c arrete");
    }
}