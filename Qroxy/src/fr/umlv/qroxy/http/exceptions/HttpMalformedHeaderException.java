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
package fr.umlv.qroxy.http.exceptions;

import java.io.IOException;

/**
 *
 * @author joan
 */
public class HttpMalformedHeaderException extends IOException {

    public HttpMalformedHeaderException() {
        super();
    }
    
    public HttpMalformedHeaderException(String string) {
        super(string);
    }

    public HttpMalformedHeaderException(Throwable cause) {
        super(cause);
    }

    public HttpMalformedHeaderException(String message, Throwable cause) {
        super(message, cause);
    }
}
