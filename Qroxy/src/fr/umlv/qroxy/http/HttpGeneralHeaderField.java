/*
 * Copyright (C) 2012 Joan Goyeau <joan.goyeau@gmail.com>
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
package fr.umlv.qroxy.http;

import java.util.Objects;

/**
 * General Header Fields (see section 4.5 in RFC 2616)
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public enum HttpGeneralHeaderField {

    CACHE_CONTROL("Cache-Control"), // see section 14.9 in RFC 2616
    CONNECTION("Connection"), // see section 14.10 in RFC 2616
    DATE("Date"), // see section 14.18 in RFC 2616
    PRAGMA("Pragma"), // see section 14.32 in RFC 2616
    TRAILER("Trailer"), // see section 14.40 in RFC 2616
    TRANSFER_ENCODING("Transfer-Encoding"), // see section 14.41 in RFC 2616
    UPGRADE("Upgrade"), // see section 14.42 in RFC 2616
    VIA("Via"), // see section 14.45 in RFC 2616
    WARNING("Warning"); // see section 14.46 in RFC 2616
    private final String fieldName;

    private HttpGeneralHeaderField(String fieldName) {
        this.fieldName = fieldName;
    }

    public static HttpGeneralHeaderField valueFor(String fieldName) {
        Objects.requireNonNull(fieldName);
        
        for (HttpGeneralHeaderField name : HttpGeneralHeaderField.values()) {
            if (name.fieldName.equals(fieldName)) {
                return name;
            }
        }
        return null;
    }
}
