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
import java.net.SocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Set;

/**
 * 
 * @author joan
 */
public class Server {

    private final SocketAddress bindAddress;

    public Server(SocketAddress bindAddress) {
        this.bindAddress = bindAddress;
    }
    
    public void launch() throws IOException {
        ServerSocketChannel serverSocket = ServerSocketChannel.open();
        serverSocket.configureBlocking(false);
        serverSocket.bind(bindAddress);
        
        Selector selector = Selector.open();
        Set<SelectionKey> selectedKeys = selector.selectedKeys();
        serverSocket.register(selector, SelectionKey.OP_ACCEPT);
        
        selector.select();
        
        for (SelectionKey key : selectedKeys) {
            if(key.isAcceptable()) {
                SocketChannel client = serverSocket.accept();
                client.configureBlocking(false);
                client.register(selector, SelectionKey.OP_READ);
            }
            
        }
    }
    
    public void stop() {
        
    } 
}