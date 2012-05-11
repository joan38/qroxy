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
package fr.umlv.qroxy.conf;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

/**
 * Objet représentant les config
 * Parse le fichier de conf formaté à la entete HTTP
 * port du serveur d'infos
 *
 * @author joan
 */
public class Config {

    private final File confFile;
    private String webUiBindingAddress;
    private int webUiBindingPort;
    private List<QosRule> qosRules;
    private List<CacheRule> cacheRules;

    public Config(File confFile) throws FileNotFoundException {
        this.confFile = confFile;

        Scanner scanner = new Scanner(confFile);
        while (scanner.hasNext()) {
            String line = scanner.nextLine();
            String[] split = line.split(" ");

            if (line.equals("")) {
                continue;
            }

            switch (split[0]) {
                case "Webui-binding-address:":
                    if (split.length != 2) {
                        webUiBindingAddress = null;
                    } else {
                    webUiBindingAddress = split[1];
                    }
                    break;
                case "Webui-binding-port:":
                    if (split.length != 2) {
                        webUiBindingPort = 0;
                    } else {
                        webUiBindingPort = Integer.parseInt(split[1]);
                    }
                    break;
                case "QoS:":
                    qosRules = parseQos(scanner);
                    break;
                case "Cache:":
                    cacheRules = parseCache(scanner);
                    break;
            }
        }
    }

    private static List<QosRule> parseQos(Scanner scanner) {
        List<QosRule> qosRules = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split("( |\t)+");
            
            if (line.startsWith("#")) {
                continue;
            }
            
            if (split.length != 4) {
                break;
            }
            
            try {
                qosRules.add(new QosRule(split[0], Integer.parseInt(split[1]), Integer.parseInt(split[2]), Integer.parseInt(split[3])));
            } catch (NumberFormatException e) {
                break;
            }
        }

        return qosRules;
    }

    private static List<CacheRule> parseCache(Scanner scanner) {
        List<CacheRule> cacheRules = new ArrayList<>();

        while (scanner.hasNextLine()) {
            String line = scanner.nextLine();
            String[] split = line.split("( |\t)+");
            
            if (line.startsWith("#")) {
                continue;
            }
            
            if (split.length != 2) {
                break;
            }
            
            try {
                cacheRules.add(new CacheRule(split[0], Integer.parseInt(split[1])));
            } catch (NumberFormatException e) {
                break;
            }
        }

        return cacheRules;
    }

    public static void main(String[] args) throws IOException {
        //parseQos(new Scanner(Paths.get("/Users/joan/Desktop/conf.txt")));
        Config config = new Config(new File("/Users/joan/Desktop/conf.txt"));
    }
}
