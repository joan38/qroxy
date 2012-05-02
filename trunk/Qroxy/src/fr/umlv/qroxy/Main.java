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

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.text.ParseException;
import java.util.Scanner;

/**
 *
 * @author joan
 */
public class Main {

    public static void main(String[] args) throws IOException, MalformedHttpHeaderException, MalformedURLException, ParseException {        
        ServerSocketChannel server = ServerSocketChannel.open();
        server.bind(new InetSocketAddress("127.0.0.1", 7777));

        SocketChannel clientSocket = server.accept();

        Scanner scanner = new Scanner(clientSocket);
        HttpHeader httpHeader = new HttpHeader(scanner);
        
        String host = httpHeader.getUrl().getHost();
        int port = httpHeader.getUrl().getPort();
        SocketChannel serverSocket = SocketChannel.open(new InetSocketAddress(host, port));

        new Thread(new Forwarder(clientSocket, serverSocket)).start();
        new Thread(new Forwarder(serverSocket, clientSocket)).start();
    }
}
