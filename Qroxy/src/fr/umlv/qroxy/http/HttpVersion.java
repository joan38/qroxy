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
    HTTP_1_1(1, 1);
    private int major;
    private int minor;

    private HttpVersion(int major, int minor) {
        this.major = major;
        this.minor = minor;
    }

    public static HttpVersion valueFor(int major, int minor) {
        for (HttpVersion httpVersion : HttpVersion.values()) {
            if (httpVersion.major == major && httpVersion.minor == minor) {
                return httpVersion;
            }
        }
        return null;
    }
}
