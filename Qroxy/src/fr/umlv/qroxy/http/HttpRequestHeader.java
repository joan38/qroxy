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
package fr.umlv.qroxy.http;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.ParseException;
import java.util.Date;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringTokenizer;
import sun.net.www.protocol.http.HttpURLConnection;

/**
 * ava.net.URI; import java.net.URISyntaxException; import java.net.URL; import
 * java.text.ParseException; import java.text.SimpleDateFormat; import
 * java.util.*;
 *
 * @author joan
 */
public class HttpRequestHeader extends HttpHeader {

    /**
     * <b>Request method (see section 5.1.1 in RFC 2616)</b><p>
     */
    protected HttpMethod method;
    protected URI uri;
    /**
     * Request Header Fields (see section 5.3 in RFC 2616)
     */
    /**
     *
     */
    protected String accept;
    protected String acceptCharset;
    protected String acceptEncoding;
    protected String acceptLanguage;
    protected String authorization;
    protected String expect;
    protected String from;
    protected String host;
    protected String ifMatch;
    protected Date ifModifiedSince;
    protected String ifNoneMatch;
    protected String ifRange;
    protected Date ifUnmodifiedSince;
    protected String maxForwards;
    protected String proxyAuthorization;
    protected String range;
    protected String referer;
    protected String te;
    protected String userAgent;

    /**
     * Parse a request header (see section 5 in RFC 2616)
     *
     * @param httpRequestMessage
     * @return
     * @throws MalformedHttpHeaderException
     */
    public static HttpRequestHeader parse(String httpRequestMessage) throws MalformedHttpHeaderException {
        Objects.requireNonNull(httpRequestMessage);
        StringTokenizer stringTokenizer = new StringTokenizer(httpRequestMessage);

        // Request-Line (see section 5.1 in RFC 2616)
        HttpRequestHeader httpHeader = new HttpRequestHeader();

        try {
            try {
                // Method (see section 5.1.1 in RFC 2616)
                httpHeader.method = HttpMethod.valueOf(stringTokenizer.nextToken());
            } catch (IllegalArgumentException e) {
                throw new MalformedHttpHeaderException("Unsupported method (see section 5.1.1 in RFC 2616)", e);
            }

            try {
                // Request-URI (see section 5.1.2 in RFC 2616)
                httpHeader.uri = new URI(stringTokenizer.nextToken());
                if (httpHeader.uri.getPort() == -1) {
                    httpHeader.uri = new URI(httpHeader.uri.getScheme(),
                            httpHeader.uri.getUserInfo(),
                            httpHeader.uri.getHost(),
                            80,
                            httpHeader.uri.getPath(),
                            httpHeader.uri.getQuery(),
                            httpHeader.uri.getFragment());
                }
            } catch (URISyntaxException e) {
                throw new MalformedHttpHeaderException("Invalid request URI (see section 5.1.2 in RFC 2616)", e);
            }

            // HTTP version (see section 3.1 in RFC 2616)
            if (!stringTokenizer.nextToken("/").substring(1).equals("HTTP")) {
                throw new MalformedHttpHeaderException("Invalid HTTP version (see section 3.1 in RFC 2616)");
            }
            httpHeader.parseVersion(stringTokenizer);

            while (stringTokenizer.hasMoreTokens()) {
                String fieldName = stringTokenizer.nextToken(" ");
                fieldName = fieldName.substring(0, fieldName.length() - 1);
                String fieldValue = stringTokenizer.nextToken("\r\n");

                // general-header (see section 4.5 in RFC 2616)
                if (httpHeader.setGeneralHeaderField(fieldName, fieldValue)) {
                    continue;
                }
                // request-header (see section 5.3 in RFC 2616)
                if (httpHeader.setRequestHeaderField(fieldName, fieldValue)) {
                    continue;
                }
                // entity-header (see section 7.1 in RFC 2616)
                httpHeader.setEntityHeaderField(fieldName, fieldValue);
            }
        } catch (NoSuchElementException e) {
            throw new MalformedHttpHeaderException("Unexpected end of the header (see section 5 in RFC 2616)", e);
        }

        return httpHeader;
    }

    private boolean setRequestHeaderField(String fieldName, String fieldValue) throws MalformedHttpHeaderException {
        HttpRequestHeaderField requestHeaderField = HttpRequestHeaderField.valueFor(fieldName);
        if (requestHeaderField == null) {
            return false;
        }

        switch (requestHeaderField) {
            case ACCEPT:
                accept = fieldValue;
                return true;
            case ACCEPT_CHARSET:
                acceptCharset = fieldValue;
                return true;
            case ACCEPT_ENCODING:
                acceptEncoding = fieldValue;
                return true;
            case ACCEPT_LANGUAGE:
                acceptLanguage = fieldValue;
                return true;
            case AUTHORIZATION:
                authorization = fieldValue;
                return true;
            case EXPECT:
                expect = fieldValue;
                return true;
            case FROM:
                from = fieldValue;
                return true;
            case HOST:
                host = fieldValue;
                return true;
            case IF_MATCH:
                ifMatch = fieldValue;
                return true;
            case IF_MODIFIED_SINCE:
                try {
                    ifModifiedSince = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new MalformedHttpHeaderException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return true;
            case IF_NONE_MATCH:
                ifNoneMatch = fieldValue;
                return true;
            case IF_RANGE:
                ifRange = fieldValue;
                return true;
            case MAX_FORWARDS:
                maxForwards = fieldValue;
                return true;
            case PROXY_AUTHORIZATION:
                proxyAuthorization = fieldValue;
                return true;
            case RANGE:
                range = fieldValue;
                return true;
            case REFERER:
                referer = fieldValue;
                return true;
            case TE:
                te = fieldValue;
                return true;
            case USER_AGENT:
                userAgent = fieldValue;
                return true;
            default:
                return false;
        }
    }

    public String getAccept() {
        return accept;
    }

    public String getAcceptCharset() {
        return acceptCharset;
    }

    public String getAcceptEncoding() {
        return acceptEncoding;
    }

    public String getAcceptLanguage() {
        return acceptLanguage;
    }

    public String getAuthorization() {
        return authorization;
    }

    public String getExpect() {
        return expect;
    }

    public String getFrom() {
        return from;
    }

    public String getHost() {
        return host;
    }

    public String getIfMatch() {
        return ifMatch;
    }

    public Date getIfModifiedSince() {
        return ifModifiedSince;
    }

    public String getIfNoneMatch() {
        return ifNoneMatch;
    }

    public String getIfRange() {
        return ifRange;
    }

    public Date getIfUnmodifiedSince() {
        return ifUnmodifiedSince;
    }

    public String getMaxForwards() {
        return maxForwards;
    }

    public HttpMethod getMethod() {
        return method;
    }

    public String getProxyAuthorization() {
        return proxyAuthorization;
    }

    public String getRange() {
        return range;
    }

    public String getReferer() {
        return referer;
    }

    public String getTe() {
        return te;
    }

    public URI getUri() {
        return uri;
    }

    public String getUserAgent() {
        return userAgent;
    }
}
