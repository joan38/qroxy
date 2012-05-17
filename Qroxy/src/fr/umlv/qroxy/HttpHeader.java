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

import java.net.MalformedURLException;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;
import java.util.Scanner;

/**
 *
 * @author joan
 */
public class HttpHeader {

    private Method method;
    private URL url;
    private String cacheControl;          // Cache-Control: max-age=0
    private Date ifModifiedSince;         // If-Modified-Since: Tue, 24 Apr 2012 03:09:33 GMT\r\n
    private String unparsedHeader;

    private HttpHeader() {
    }

    public static HttpHeader parse(String string) throws MalformedHttpHeaderException {
        HttpHeader httpHeader = new HttpHeader();

        // Verify if the header is right formated
        StringBuilder methodsRegex = new StringBuilder("(");
        Method[] methods = Method.values();
        for (Method m : methods) {
            methodsRegex.append(m).append("|");
        }
        methodsRegex.deleteCharAt(methodsRegex.length() - 1);
        methodsRegex.append(")");

        if (!string.matches(methodsRegex + " http\\://[a-zA-Z0-9\\-\\.]{2,}\\.[a-zA-Z]{2,}(/[a-zA-Z0-9\\-\\.%]*)* HTTP/[0-1].[0-9]\r\n"
                + "([a-zA-Z\\-]+: .+\r\n)*"
                + "\r\n")) {
            throw new MalformedHttpHeaderException("The HTTP header is malformed");
        }

        httpHeader.unparsedHeader = string;

        Scanner scanner = new Scanner(string);
        String line = scanner.nextLine();
        String[] split = line.split(" ");

        // Method
        httpHeader.method = Method.valueOf(split[0]);
        try {
            // URL
            httpHeader.url = new URL(split[1]);
        } catch (MalformedURLException e) {
            throw new MalformedHttpHeaderException("Unparsable URL", e);
        }

        // Header fields
        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            split = line.split(" ");

            switch (split[0]) {
                case "Cache-Control:":
                    httpHeader.cacheControl = split[1];
                case "If-Modified-Since:":
                    try {
                        httpHeader.ifModifiedSince = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(line.substring(19));
                    } catch (ParseException e) {
                        throw new MalformedHttpHeaderException("Unparsable date", e);
                    }
            }
        }

        return httpHeader;
    }

    public boolean headerMatches(String regex) {
        Scanner scanner = new Scanner(unparsedHeader);

        // Avoid the first line (Ex: GET http://toto.com/ HTTP/1.1)
        scanner.nextLine();

        while (scanner.hasNextLine()) {
            if (scanner.nextLine().matches(regex)) {
                return true;
            }
        }

        return false;
    }

    public Method getMethod() {
        return method;
    }

    public URL getUrl() {
        return url;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public Date getIfModifiedSince() {
        return ifModifiedSince;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final HttpHeader other = (HttpHeader) obj;
        if (this.method != other.method) {
            return false;
        }
        if (!Objects.equals(this.url, other.url)) {
            return false;
        }
        if (!Objects.equals(this.cacheControl, other.cacheControl)) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.method != null ? this.method.hashCode() : 0);
        hash = 29 * hash + Objects.hashCode(this.url);
        hash = 29 * hash + Objects.hashCode(this.cacheControl);
        hash = 29 * hash + Objects.hashCode(this.ifModifiedSince);
        return hash;
    }

    public enum Method {

        GET, POST, OPTIONS, HEAD, PUT, DELETE, TRACE, CONNECT;

        public static boolean isSupported(String methodName) {
            try {
                Method.valueOf(methodName);
            } catch (IllegalArgumentException e) {
                return false;
            }

            return true;
        }
    }
}
