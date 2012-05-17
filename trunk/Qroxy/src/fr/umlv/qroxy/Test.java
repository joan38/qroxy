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

import java.util.regex.Pattern;

/**
 *
 * @author joan
 */
public class Test {
    public static void main(String[] args) {
        String httpHeader = "GERT http://www.google.fr/ou%20e.php/ HTTP/1.1\r\n"
                + "Connection: Keep-Alive\r\n"
                + "Toto: oui\r\n"
                + "\r\n";
        boolean matches = Pattern.matches("(GET|POST) http\\://[a-zA-Z0-9\\-\\.]{2,}\\.[a-zA-Z]{2,}(/[a-zA-Z0-9\\-\\.%]*)* HTTP/[0-1].[0-9]\r\n"
                                  + "([a-zA-Z\\-]+: .+\r\n)*"
                                  + "\r\n", httpHeader);
        
    }
}
