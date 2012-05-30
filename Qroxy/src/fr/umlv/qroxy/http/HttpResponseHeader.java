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

import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
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
    
    private HttpResponseHeader(String stringHeader) throws HttpMalformedHeaderException {
        super(stringHeader);
    }

    /**
     * Parse a response header (see section 6 in RFC 2616)
     *
     * @param httpResponseMessage
     * @return
     * @throws MalformedHttpHeaderException
     */
    public static HttpResponseHeader parse(String httpResponseMessage) throws HttpMalformedHeaderException {
        Objects.requireNonNull(httpResponseMessage);
        HttpResponseHeader httpHeader = new HttpResponseHeader(httpResponseMessage);
        StringTokenizer stringTokenizer = new StringTokenizer(httpHeader.stringHeader);

        // Status-Line (see section 6.1 in RFC 2616)
        try {
            // HTTP-Version (see section 3.1 in RFC 2616)
            httpHeader.version = parseVersion(stringTokenizer.nextToken());

            try {
                // Status code (see section 6.1.1 in RFC 2616)
                int statusCode = Integer.parseInt(stringTokenizer.nextToken());
                httpHeader.statusCode = HttpStatusCode.valueFor(statusCode);
                if (httpHeader.statusCode == null) {
                    httpHeader.extensionStatusCode = statusCode;
                }
                // Avoid the status message
                stringTokenizer.nextToken("\r\n");
            } catch (NumberFormatException e) {
                throw new HttpMalformedHeaderException("Invalid HTTP status code (see section 6.1.1 in RFC 2616)", e);
            }

            while (stringTokenizer.hasMoreTokens()) {
                String fieldName = stringTokenizer.nextToken("\r\n:");
                String fieldValue = stringTokenizer.nextToken("\r\n");
                fieldValue = fieldValue.substring(2);   // Avoid ": "

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
            throw new HttpMalformedHeaderException("Unexpected end of the header (see section 6 in RFC 2616)", e);
        }

        return httpHeader;
    }

    private boolean setResponseHeaderField(String fieldName, String fieldValue) {
        switch (fieldName) {
            case "Accept-Ranges":
                acceptRanges = fieldValue;
                return true;
            case "Age":
                age = fieldValue;
                return true;
            case "ETag":
                eTag = fieldValue;
                return true;
            case "Location":
                location = fieldValue;
                return true;
            case "Proxy-Authenticate":
                proxyAuthenticate = fieldValue;
                return true;
            case "Retry-After":
                retryAfter = fieldValue;
                return true;
            case "Server":
                server = fieldValue;
                return true;
            case "Vary":
                vary = fieldValue;
                return true;
            case "WWW-Authenticate":
                wwwAuthenticate = fieldValue;
                return true;
            default:
                return false;
        }
    }

    @Override
    public ContentTransferMethod contentTransferMethod() {
        if (contentTransferMethod != null) {
            return contentTransferMethod;
        }

        if (contentLength != null) {
            contentTransferMethod = ContentTransferMethod.CONTENT_LENGTH;
        } else if (contentLength == null && transferEncoding != null && !transferEncoding.equals("identity")) {
            contentTransferMethod = ContentTransferMethod.CHUNKED;
        } else if (contentLength == null && !"chunked".equals(transferEncoding)) {
            contentTransferMethod = ContentTransferMethod.CONNECTION_CLOSE;
        } else {
            contentTransferMethod = ContentTransferMethod.NO_CONTENT;
        }

        return contentTransferMethod;
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
     * Status code which are not supported by the RFC 2616. To be used only if
     * getStatusCode() return null.
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
