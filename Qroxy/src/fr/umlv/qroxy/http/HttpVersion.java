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
 * HTTP Version (see section 3.1 in RFC 2616)
 * 
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public enum HttpVersion {

    /**
     * HTTP version 1.1
     */
    HTTP_1_1("HTTP/1.1");
    private String version;

    private HttpVersion(String version) {
        this.version = version;
    }

    public static HttpVersion valueFor(String version) {
        for (HttpVersion httpVersion : HttpVersion.values()) {
            if (httpVersion.version.equals(version)) {
                return httpVersion;
            }
        }
        return null;
    }
    
    @Override
    public String toString() {
        return version;
    }
}
