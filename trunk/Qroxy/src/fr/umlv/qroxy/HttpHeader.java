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
import java.util.regex.Pattern;

/**
 *
 * @author joan
 */
public class HttpHeader {

    private Method method;
    private URL url;
    private String cacheControl;          // Cache-Control: max-age=0
    private Date ifModifiedSince;         // If-Modified-Since: Tue, 24 Apr 2012 03:09:33 GMT\r\n
    private boolean keepAlive;            // Keep-Alive: close ou Keep-Alive
    private String unparsedHeaderFields;

    public HttpHeader(Method method, URL url, String cacheControl, Date ifModifiedSince) {
        this.method = method;
        this.url = url;
        this.cacheControl = cacheControl;
    }
    
    public HttpHeader(String httpHeader) {
        StringBuilder methodsRegex = new StringBuilder("(");
        Method[] methods = Method.values();
        for (Method m : methods) {
            methodsRegex.append(m).append("|");
        }
        methodsRegex.deleteCharAt(methodsRegex.length() - 1);
        methodsRegex.append(")");
        
        
        
        Pattern.matches(methodsRegex + " " + Pattern. + "\r\n"
                + "\r\n"
                + "\r\n", httpHeader);
        
        
    }

    public HttpHeader(Scanner scanner) throws MalformedHttpHeaderException, MalformedURLException, ParseException {
        String line = scanner.nextLine();
        String[] split = line.split(" ");

        // Method
        String httpMethod = split[0];
        if (Method.isSupported(httpMethod) == false) {
            throw new MalformedHttpHeaderException("Unsupported method");
        }
        this.method = Method.valueOf(httpMethod);

        // URL
        url = new URL(split[1]);

        while (scanner.hasNextLine()) {
            line = scanner.nextLine();
            split = line.split(" ");

            switch (split[0]) {
                case "Cache-Control:":
                    cacheControl = split[1];
                case "If-Modified-Since:":
                    ifModifiedSince = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US).parse(line.substring(19));
            }
        }
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

    public int hashCode() {
        int hash = 3;
        hash = 29 * hash + (this.method != null ? this.method.hashCode() : 0);
        hash = 29 * hash + Objects.hashCode(this.url);
        hash = 29 * hash + Objects.hashCode(this.cacheControl);
        hash = 29 * hash + Objects.hashCode(this.ifModifiedSince);
        return hash;
    }

    public enum Method {

        GET, POST;

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
