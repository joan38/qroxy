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

import fr.umlv.qroxy.cache.CacheProxy;
import fr.umlv.qroxy.cache.CacheTmpImpl;
import fr.umlv.qroxy.config.Config;
import fr.umlv.qroxy.config.XMLQroxyConfigException;
import fr.umlv.qroxy.proxy.Proxy;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;
import java.io.File;
import java.io.IOException;
import java.net.InetSocketAddress;

/**
 *
 * @author joan
 */
public class Main {
    
    public static final String APPLICATION_NAME = "Qroxy";

    public static void main(String[] args) {
        File configFile = null;

        LongOpt[] longopts = new LongOpt[3];
        longopts[0] = new LongOpt("config", LongOpt.REQUIRED_ARGUMENT, null, 'c');

        Getopt getopt = new Getopt(APPLICATION_NAME, args, ":c:", longopts);

        int c;
        while ((c = getopt.getopt()) != -1) {
            String arg = getopt.getOptarg();
            switch (c) {
                case 'c':
                    configFile = new File(arg);
                    break;
                case ':':
                    System.out.println("You need an argument for option " + (char) getopt.getOptopt());
                    break;
                default:
                    printUsage();
            }
        }

        if (configFile == null || getopt.getOptind() == args.length) {
            printUsage();
        }

        InetSocketAddress listeningAddress = null;
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

        Config config = new Config(listeningAddress, null, null, 0, null);
        try {
            config.loadFromXml(configFile);
        } catch (XMLQroxyConfigException e) {
            System.err.println("Configuration file malformated: " + e.getMessage());
        }

        try {
            new Proxy(config, new CacheTmpImpl()).launch(); // CacheProxy
        } catch (IOException e) {
            System.err.println("Proxy stopped due to a network error: " + e.getMessage());
        }
    }

    private static void printUsage() {
        System.err.println("Usage :\n"
                + "--config=<config file> <listening host>:<listening port>\n"
                + "-c <config file> <listening port>");
        System.exit(-1);
    }
}
