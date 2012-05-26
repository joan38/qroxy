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
 * Request Header Fields (see section 5.3 in RFC 2616)
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public enum HttpRequestHeaderField {

    /**
     * see section 14.1 in RFC 2616
     */
    ACCEPT("Accept"),
    /**
     * see section 14.2 in RFC 2616
     */
    ACCEPT_CHARSET("Accept-Charset"),
    /**
     * see section 14.3 in RFC 2616
     */
    ACCEPT_ENCODING("Accept-Encoding"),
    /**
     * see section 14.4 in RFC 2616
     */
    ACCEPT_LANGUAGE("Accept-Language"),
    /**
     * see section 14.8 in RFC 2616
     */
    AUTHORIZATION("Authorization"),
    /**
     * see section 14.20 in RFC 2616
     */
    EXPECT("Expect"),
    /**
     * see section 14.22 in RFC 2616
     */
    FROM("From"),
    /**
     * see section 14.23 in RFC 2616
     */
    HOST("Host"),
    /**
     * see section 14.24 in RFC 2616
     */
    IF_MATCH("If-Match"),
    /**
     * see section 14.25 in RFC 2616
     */
    IF_MODIFIED_SINCE("If-Modified-Since"),
    /**
     * see section 14.26 in RFC 2616
     */
    IF_NONE_MATCH("If-None-Match"),
    /**
     * see section 14.27 in RFC 2616
     */
    IF_RANGE("If-Range"),
    /**
     * see section 14.28 in RFC 2616
     */
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),
    /**
     * see section 14.31 in RFC 2616
     */
    MAX_FORWARDS("Max-Forwards"),
    /**
     * see section 14.34 in RFC 2616
     */
    PROXY_AUTHORIZATION("Proxy-Authorization"),
    /**
     * see section 14.35 in RFC 2616
     */
    RANGE("Range"),
    /**
     * see section 14.36 in RFC 2616
     */
    REFERER("Referer"),
    /**
     * see section 14.39 in RFC 2616
     */
    TE("TE"),
    /**
     * see section 14.43 in RFC 2616
     */
    USER_AGENT("User-Agent");
    
    private final String fieldName;

    private HttpRequestHeaderField(String fieldName) {
        this.fieldName = fieldName;
    }

    public static HttpRequestHeaderField valueFor(String fieldName) {
        Objects.requireNonNull(fieldName);
        
        for (HttpRequestHeaderField name : HttpRequestHeaderField.values()) {
            if (name.fieldName.equals(fieldName)) {
                return name;
            }
        }
        return null;
    }
}
