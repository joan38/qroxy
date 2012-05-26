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

/**
 * Entity Header Fields (see section 7.1 in RFC 2616)
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public enum HttpEntityHeaderField {

    ALLOW("Allow"), // see section 14.9 in RFC 2616
    CONTENT_ENCODING("Content-Encoding"), // see section 14.10 in RFC 2616
    CONTENT_LANGUAGE("Content-Language"), // see section 14.18 in RFC 2616
    CONTENT_LENGTH("Content-Length"), // see section 14.32 in RFC 2616
    CONTENT_LOCATION("Content-Location"), // see section 14.40 in RFC 2616
    CONTENT_MD5("Content-MD5"), // see section 14.41 in RFC 2616
    CONTENT_RANGE("Content-Range"), // see section 14.42 in RFC 2616
    CONTENT_TYPE("Content-Type"), // see section 14.45 in RFC 2616
    EXPIRES("Expires"), // see section 14.46 in RFC 2616
    LAST_MODIFIED("Last-Modified"); // see section 14.46 in RFC 2616
    
    private final String fieldName;

    private HttpEntityHeaderField(String fieldName) {
        this.fieldName = fieldName;
    }

    /**
     * The extension-header mechanism allows additional entity-header fields to
     * be defined without changing the protocol, but these fields cannot be
     * assumed to be recognizable by the recipient. Unrecognized header fields
     * SHOULD be ignored by the recipient and MUST be forwarded by transparent
     * proxies.
     */
    public static HttpEntityHeaderField valueFor(String fieldName) {
        for (HttpEntityHeaderField name : HttpEntityHeaderField.values()) {
            if (name.fieldName.equals(fieldName)) {
                return name;
            }
        }
        return null;
    }
}
