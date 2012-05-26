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
 * Response Header Fields (see section 6.2 in RFC 2616)
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public enum HttpResponseHeaderField {
                       
    ACCEPT_RANGES("Accept-Ranges"), // see section 14.6 in RFC 2616
    AGE("Age"), // see section 14.19 in RFC 2616
    ETAG("ETag"), // see section 14.30 in RFC 2616
    LOCATION("Location"), // see section 14.33 in RFC 2616
    PROXY_AUTHENTICATE("Proxy-Authenticate"), // see section 14.37 in RFC 2616
    RETRY_AFTER("Retry-After"), // see section 14.38 in RFC 2616
    SERVER("Server"), // see section 14.44 in RFC 2616
    VARY("Vary"), // see section 14.47 in RFC 2616
    WWW_AUTHENTICATE("WWW-Authenticate"); // see section 14.47 in RFC 2616
    
    private final String fieldName;

    private HttpResponseHeaderField(String fieldName) {
        this.fieldName = fieldName;
    }

    public static HttpResponseHeaderField valueFor(String fieldName) {
        Objects.requireNonNull(fieldName);
        
        for (HttpResponseHeaderField name : HttpResponseHeaderField.values()) {
            if (name.fieldName.equals(fieldName)) {
                return name;
            }
        }
        return null;
    }
}
