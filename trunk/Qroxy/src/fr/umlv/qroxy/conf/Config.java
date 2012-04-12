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
import java.net.InetSocketAddress;

/**
 * Objet représentant les config
 * Parse le fichier de conf formaté à la entete HTTP
 * port du serveur d'infos
 *
 * @author joan
 */
public class Config {
    
    private final File confFile;
    private final InetSocketAddress bindingAddress;

    public Config(File confFile, InetSocketAddress bindingAddress) {
        this.confFile = confFile;
        this.bindingAddress = bindingAddress;
    }
    
    
}
