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

import fr.umlv.qroxy.config.Category;
import fr.umlv.qroxy.http.exceptions.HttpMalformedHeaderException;
import fr.umlv.qroxy.http.exceptions.HttpPreconditionFailedException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedMethodException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;
import java.util.*;

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

    private HttpRequestHeader(String stringHeader) throws HttpMalformedHeaderException {
        super(stringHeader);
    }
    
    /**
     * Parse a request header (see section 5 in RFC 2616)
     *
     * @param httpRequestMessage
     * @return
     * @throws MalformedHttpHeaderException
     */
    public static HttpRequestHeader parse(String httpRequestMessage) throws HttpMalformedHeaderException {
        Objects.requireNonNull(httpRequestMessage);
        HttpRequestHeader httpHeader = new HttpRequestHeader(httpRequestMessage);
        StringTokenizer stringTokenizer = new StringTokenizer(httpRequestMessage);

        // Request-Line (see section 5.1 in RFC 2616)
        try {
            try {
                // Method (see section 5.1.1 in RFC 2616)
                httpHeader.method = HttpMethod.valueOf(stringTokenizer.nextToken());
            } catch (IllegalArgumentException e) {
                throw new HttpUnsupportedMethodException("Unsupported method (see section 5.1.1 in RFC 2616)", e);
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
                throw new HttpMalformedHeaderException("Invalid request URI (see section 5.1.2 in RFC 2616)", e);
            }

            // HTTP version (see section 3.1 in RFC 2616)
            httpHeader.version = HttpHeader.parseVersion(stringTokenizer.nextToken());

            while (stringTokenizer.hasMoreTokens()) {
                String fieldName = stringTokenizer.nextToken("\r\n:");
                String fieldValue = stringTokenizer.nextToken("\r\n");
                fieldValue = fieldValue.substring(2);   // Avoid ": "

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
            throw new HttpMalformedHeaderException("Unexpected end of the header (see section 5 in RFC 2616)", e);
        }

        return httpHeader;
    }

    private boolean setRequestHeaderField(String fieldName, String fieldValue) throws HttpMalformedHeaderException {
        switch (fieldName) {
            case "Accept":
                accept = fieldValue;
                return true;
            case "Accept-Charset":
                acceptCharset = fieldValue;
                return true;
            case "Accept-Encoding":
                acceptEncoding = fieldValue;
                return true;
            case "Accept-Language":
                acceptLanguage = fieldValue;
                return true;
            case "Authorization":
                authorization = fieldValue;
                return true;
            case "Expect":
                expect = fieldValue;
                return true;
            case "From":
                from = fieldValue;
                return true;
            case "Host":
                host = fieldValue;
                return true;
            case "If-Match":
                ifMatch = fieldValue;
                return true;
            case "If-Modified-Since":
                try {
                    ifModifiedSince = HttpHeader.DATE_FORMATER.parse(fieldValue);
                } catch (ParseException e) {
                    throw new HttpPreconditionFailedException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return true;
            case "If-None-Match":
                ifNoneMatch = fieldValue;
                return true;
            case "If-Range":
                ifRange = fieldValue;
                return true;
            case "Max-Forwards":
                maxForwards = fieldValue;
                return true;
            case "Proxy-Authorization":
                proxyAuthorization = fieldValue;
                return true;
            case "Range":
                range = fieldValue;
                return true;
            case "Referer":
                referer = fieldValue;
                return true;
            case "TE":
                te = fieldValue;
                return true;
            case "User-Agent":
                userAgent = fieldValue;
                return true;
            default:
                return false;
        }
    }

    public boolean matchesCatagory(Category category) {
        Map<String, String> regexs = category.getRegexs();
        Set<Map.Entry<String, String>> entrySet = regexs.entrySet();

        for (Map.Entry<String, String> entry : entrySet) {
            switch (entry.getKey()) {
                /**
                 * Requested URL
                 */
                case "url":
                    if (!uri.toString().matches(entry.getValue())) {
                        return false;
                    }
                /**
                 * General Header Fields
                 */
                case "Cache-Control":
                    if (!(cacheControl == null ? "" : cacheControl).matches(entry.getValue())) {
                        return false;
                    }
                case "Connection":
                    if (!(connection == null ? "" : connection).matches(entry.getValue())) {
                        return false;
                    }
                case "Date":
                    if (!(date == null ? "" : HttpHeader.DATE_FORMATER.format(date)).matches(entry.getValue())) {
                        return false;
                    }
                case "Pragma":
                    if (!(pragma == null ? "" : pragma).matches(entry.getValue())) {
                        return false;
                    }
                case "Trailer":
                    if (!(trailer == null ? "" : trailer).matches(entry.getValue())) {
                        return false;
                    }
                case "Transfer-Encoding":
                    if (!(transferEncoding == null ? "" : transferEncoding).matches(entry.getValue())) {
                        return false;
                    }
                case "Upgrade":
                    if (!(upgrade == null ? "" : upgrade).matches(entry.getValue())) {
                        return false;
                    }
                case "Via":
                    if (!(via == null ? "" : via).matches(entry.getValue())) {
                        return false;
                    }
                case "Warning":
                    if (!(warning == null ? "" : warning).matches(entry.getValue())) {
                        return false;
                    }
                /**
                 * Request Header Fields
                 */
                case "Accept":
                    if (!(accept == null ? "" : accept).matches(entry.getValue())) {
                        return false;
                    }
                case "Accept-Charset":
                    if (!(acceptCharset == null ? "" : acceptCharset).matches(entry.getValue())) {
                        return false;
                    }
                case "Accept-Encoding":
                    if (!(acceptEncoding == null ? "" : acceptEncoding).matches(entry.getValue())) {
                        return false;
                    }
                case "Accept-Language":
                    if (!(acceptLanguage == null ? "" : acceptLanguage).matches(entry.getValue())) {
                        return false;
                    }
                case "Authorization":
                    if (!(authorization == null ? "" : authorization).matches(entry.getValue())) {
                        return false;
                    }
                case "Expect":
                    if (!(expect == null ? "" : expect).matches(entry.getValue())) {
                        return false;
                    }
                case "From":
                    if (!(from == null ? "" : from).matches(entry.getValue())) {
                        return false;
                    }
                case "Host":
                    if (!(host == null ? "" : host).matches(entry.getValue())) {
                        return false;
                    }
                case "If-Match":
                    if (!(ifMatch == null ? "" : ifMatch).matches(entry.getValue())) {
                        return false;
                    }
                case "If-Modified-Since":
                    if (!(ifModifiedSince == null ? "" : HttpHeader.DATE_FORMATER.format(ifModifiedSince)).matches(entry.getValue())) {
                        return false;
                    }
                case "If-None-Match":
                    if (!(ifNoneMatch == null ? "" : ifNoneMatch).matches(entry.getValue())) {
                        return false;
                    }
                case "If-Range":
                    if (!(ifRange == null ? "" : ifRange).matches(entry.getValue())) {
                        return false;
                    }
                case "Max-Forwards":
                    if (!(maxForwards == null ? "" : maxForwards).matches(entry.getValue())) {
                        return false;
                    }
                case "Proxy-Authorization":
                    if (!(proxyAuthorization == null ? "" : proxyAuthorization).matches(entry.getValue())) {
                        return false;
                    }
                case "Range":
                    if (!(range == null ? "" : range).matches(entry.getValue())) {
                        return false;
                    }
                case "Referer":
                    if (!(referer == null ? "" : referer).matches(entry.getValue())) {
                        return false;
                    }
                case "TE":
                    if (!(te == null ? "" : te).matches(entry.getValue())) {
                        return false;
                    }
                case "User-Agent":
                    if (!(userAgent == null ? "" : userAgent).matches(entry.getValue())) {
                        return false;
                    }
                /**
                 * Entity Header Fields
                 */
                case "Allow":
                    if (!(allow == null ? "" : allow).matches(entry.getValue())) {
                        return false;
                    }
                case "Content-Encoding":
                    if (!(contentEncoding == null ? "" : contentEncoding).matches(entry.getValue())) {
                        return false;
                    }
                case "Content-Language":
                    if (!(contentLanguage == null ? "" : contentLanguage).matches(entry.getValue())) {
                        return false;
                    }
                case "Content-Length":
                    if (!new Integer(contentLength).toString().matches(entry.getValue())) {
                        return false;
                    }
                case "Content-Location":
                    if (!(contentLocation == null ? "" : contentLocation).matches(entry.getValue())) {
                        return false;
                    }
                case "Content-MD5":
                    if (!(contentMD5 == null ? "" : contentMD5).matches(entry.getValue())) {
                        return false;
                    }
                case "Content-Range":
                    if (!(contentRange == null ? "" : contentRange).matches(entry.getValue())) {
                        return false;
                    }
                case "Content-Type":
                    if (!(contentType == null ? "" : contentType).matches(entry.getValue())) {
                        return false;
                    }
                case "Expires":
                    if (!(expires == null ? "" : HttpHeader.DATE_FORMATER.format(expires)).matches(entry.getValue())) {
                        return false;
                    }
                case "Last-Modified":
                    if (!(lastModified == null ? "" : HttpHeader.DATE_FORMATER.format(lastModified)).matches(entry.getValue())) {
                        return false;
                    }
                default:
                    String extensionValue = extensionHeader.get(entry.getKey());
                    if (!(extensionValue == null ? "" : extensionValue).matches(entry.getValue())) {
                        return false;
                    }
            }
        }
        return true;
    }

    public Category matchesCatagories(ArrayList<Category> categories) {
        for (Category category : categories) {
            if (matchesCatagory(category)) {
                return category;
            }
        }
        return null;
    }

    @Override
    public ContentTransferMode contentTransferMode() {
        if (contentTransferMethod != null) {
            return contentTransferMethod;
        }

        if (contentLength != null) {
            contentTransferMethod = ContentTransferMode.CONTENT_LENGTH;
        } else if (contentLength == null && transferEncoding != null && !transferEncoding.equals("identity")) {
            contentTransferMethod = ContentTransferMode.CHUNKED;
        } else {
            contentTransferMethod = ContentTransferMode.NO_CONTENT;
        }

        return contentTransferMethod;
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

    @Override
    public String toString() {
        StringBuilder header = new StringBuilder(method.name()).append(" ").append(uri).append(" ").append(version).append("\r\n");
        header.append(toStringGeneralFields());
        if (accept != null) {
            header.append("Accept: ").append(accept).append("\r\n");
        }
        if (acceptCharset != null) {
            header.append("Accept-Charset: ").append(acceptCharset).append("\r\n");
        }
        if (acceptEncoding != null) {
            header.append("Accept-Encoding: ").append(acceptEncoding).append("\r\n");
        }
        if (acceptLanguage != null) {
            header.append("Accept-Language: ").append(acceptLanguage).append("\r\n");
        }
        if (authorization != null) {
            header.append("Authorization: ").append(authorization).append("\r\n");
        }
        if (expect != null) {
            header.append("Expect: ").append(expect).append("\r\n");
        }
        if (from != null) {
            header.append("From: ").append(from).append("\r\n");
        }
        if (host != null) {
            header.append("Host: ").append(host).append("\r\n");
        }
        if (ifMatch != null) {
            header.append("If-Match: ").append(ifMatch).append("\r\n");
        }
        if (ifModifiedSince != null) {
            header.append("If-Modified-Since: ").append(DATE_FORMATER.format(ifModifiedSince)).append("\r\n");
        }
        if (ifNoneMatch != null) {
            header.append("If-None-Match: ").append(ifNoneMatch).append("\r\n");
        }
        if (ifRange != null) {
            header.append("If-Range: ").append(ifRange).append("\r\n");
        }
        if (ifUnmodifiedSince != null) {
            header.append("If-Unmodified-Since: ").append(DATE_FORMATER.format(ifUnmodifiedSince)).append("\r\n");
        }
        if (maxForwards != null) {
            header.append("Max-Forwards: ").append(maxForwards).append("\r\n");
        }
        if (proxyAuthorization != null) {
            header.append("Proxy-Authorization: ").append(proxyAuthorization).append("\r\n");
        }
        if (range != null) {
            header.append("Range: ").append(range).append("\r\n");
        }
        if (referer != null) {
            header.append("Referer: ").append(referer).append("\r\n");
        }
        if (te != null) {
            header.append("TE: ").append(te).append("\r\n");
        }
        if (userAgent != null) {
            header.append("User-Agent: ").append(userAgent).append("\r\n");
        }
        header.append(toStringEntityFields());
        return header.append("\r\n").toString();
    }
}
