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
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

/**
 *
 * @author joan
 */
public class Main {

    public static void main(String[] args) throws IOException {
        Config config = null;

        LongOpt[] longopts = new LongOpt[3];
        longopts[0] = new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'c');

        Getopt getopt = new Getopt("Qroxy", args, ":c:", longopts);

        int c;
        while ((c = getopt.getopt()) != -1) {
            String arg = getopt.getOptarg();
            switch (c) {
                case 'c':
                    try {
                        config = new Config(new File(arg));
                    } catch (FileNotFoundException e) {
                        System.err.println("Not found config file " + e.getMessage() + "\n");
                    }
                    break;
                case ':':
                    System.out.println("You need an argument for option " + (char) getopt.getOptopt());
                    break;
                default:
                    printUsage();
            }
        }

        if (config == null || getopt.getOptind() == args.length) {
            printUsage();
        }

        SocketAddress listeningAddress = null;
        String hostPort = args[getopt.getOptind()];

        try {
            if (hostPort.contains(":")) {
                String[] split = hostPort.split(":");
                listeningAddress = new InetSocketAddress(split[0], Integer.parseInt(split[1]));
            } else {
                listeningAddress = new InetSocketAddress(Integer.parseInt(hostPort));
            }
        } catch (NumberFormatException e) {
            System.err.println("Please give a number for the port !\n");
            printUsage();
        }

        new QroxyServer(listeningAddress, config).launch();
    }

    private static void printUsage() {
        System.err.println("Usage :\n"
                + "--config=<config file> <listening host>:<listening port>\n"
                + "-c <config file> <listening port>");
        System.exit(-1);
    }
}
