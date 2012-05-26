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

import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.StringTokenizer;

/**
 *
 *
 * @author joan
 */
public class HttpResponseHeader extends HttpHeader {

    /**
     * <b>Status code (see section 10 in RFC 2616)</b><p>
     *
     * If null see extensionStatusCode
     *
     * @see #extentionStatusCode
     */
    protected HttpStatusCode statusCode;
    /**
     * <b>Extention status code</b><p>
     *
     * Status code which are not supported by the RFC 2616
     */
    protected int extensionStatusCode;
    /**
     * Response Header Fields (see section 6.2 in RFC 2616)
     */
    /**
     *
     */
    protected String acceptRanges;
    protected String age;
    protected String eTag;
    protected String location;
    protected String proxyAuthenticate;
    protected String retryAfter;
    protected String server;
    protected String vary;
    protected String wwwAuthenticate;

    /**
     * Parse a response header (see section 6 in RFC 2616)
     *
     * @param httpResponseMessage
     * @return
     * @throws MalformedHttpHeaderException
     */
    public static HttpResponseHeader parse(String httpResponseMessage) throws MalformedHttpHeaderException {
        Objects.requireNonNull(httpResponseMessage);
        StringTokenizer stringTokenizer = new StringTokenizer(httpResponseMessage);

        // Status-Line (see section 6.1 in RFC 2616)
        HttpResponseHeader httpHeader = new HttpResponseHeader();

        try {
            // HTTP-Version (see section 3.1 in RFC 2616)
            if (!stringTokenizer.nextToken("/").equals("HTTP")) {
                throw new MalformedHttpHeaderException("Invalid HTTP response header (see section 6.1 in RFC 2616)");
            }
            httpHeader.parseVersion(stringTokenizer);

            try {
                // Status code (see section 6.1.1 in RFC 2616)
                int statusCode = Integer.parseInt(stringTokenizer.nextToken());
                httpHeader.statusCode = HttpStatusCode.valueFor(statusCode);
                if (httpHeader.statusCode == null) {
                    httpHeader.extensionStatusCode = statusCode;
                }
            } catch (NumberFormatException e) {
                throw new MalformedHttpHeaderException("Invalid HTTP status code (see section 6.1.1 in RFC 2616)", e);
            }

            while (stringTokenizer.hasMoreTokens()) {
                String fieldName = stringTokenizer.nextToken(" ");
                fieldName = fieldName.substring(0, fieldName.length() - 1);
                String fieldValue = stringTokenizer.nextToken("\r\n");

                // general-header (see section 4.5 in RFC 2616)
                if (httpHeader.setGeneralHeaderField(fieldName, fieldValue)) {
                    continue;
                }
                // request-header (see section 5.3 in RFC 2616)
                if (httpHeader.setResponseHeaderField(fieldName, fieldValue)) {
                    continue;
                }
                // entity-header (see section 7.1 in RFC 2616)
                httpHeader.setEntityHeaderField(fieldName, fieldValue);
            }
        } catch (NoSuchElementException e) {
            throw new MalformedHttpHeaderException("Unexpected end of the header (see section 6 in RFC 2616)", e);
        }

        return httpHeader;
    }

    private boolean setResponseHeaderField(String fieldName, String fieldValue) {
        HttpResponseHeaderField responseHeaderField = HttpResponseHeaderField.valueFor(fieldName);
        if (responseHeaderField == null) {
            return false;
        }
        
        switch (HttpResponseHeaderField.valueFor(fieldName)) {
            case ACCEPT_RANGES:
                acceptRanges = fieldValue;
                return true;
            case AGE:
                age = fieldValue;
                return true;
            case ETAG:
                eTag = fieldValue;
                return true;
            case LOCATION:
                location = fieldValue;
                return true;
            case PROXY_AUTHENTICATE:
                proxyAuthenticate = fieldValue;
                return true;
            case RETRY_AFTER:
                retryAfter = fieldValue;
                return true;
            case SERVER:
                server = fieldValue;
                return true;
            case VARY:
                vary = fieldValue;
                return true;
            case WWW_AUTHENTICATE:
                wwwAuthenticate = fieldValue;
                return true;
            default:
                return false;
        }
    }

    public String getAcceptRanges() {
        return acceptRanges;
    }

    public String getAge() {
        return age;
    }

    public String geteTag() {
        return eTag;
    }

    /**
     * <b>Status code (see section 10 in RFC 2616)</b><p>
     *
     * If null see extensionStatusCode
     *
     * @see #getExtensionStatusCode() 
     */
    public HttpStatusCode getStatusCode() {
        return statusCode;
    }

    /**
     * <b>Extention status code</b><p>
     *
     * Status code which are not supported by the RFC 2616.
     * To be used only if getStatusCode() return null.
     * 
     * @see #getStatusCode() 
     */
    public int getExtensionStatusCode() {
        return extensionStatusCode;
    }

    public String getLocation() {
        return location;
    }

    public String getProxyAuthenticate() {
        return proxyAuthenticate;
    }

    public String getRetryAfter() {
        return retryAfter;
    }

    public String getServer() {
        return server;
    }

    public String getVary() {
        return vary;
    }

    public String getWwwAuthenticate() {
        return wwwAuthenticate;
    }
}
