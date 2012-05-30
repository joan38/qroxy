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
import fr.umlv.qroxy.http.exceptions.HttpPreconditionFailedException;
import fr.umlv.qroxy.http.exceptions.HttpUnsupportedVersionException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * ava.net.URI; import java.net.URISyntaxException; import java.net.URL; import
 * java.text.ParseException; import java.text.SimpleDateFormat; import
 * java.util.*;
 *
 * @author joan
 */
public abstract class HttpHeader {

    protected final static SimpleDateFormat dateFormater = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss z", Locale.US);
    protected int headerLength;
    ContentTransferMethod contentTransferMethod;
    /**
     * <b>HTTP Version (see section 3.1 in RFC 2616)
     */
    protected HttpVersion version;
    /**
     * General Header Fields (see section 4.5 in RFC 2616)
     */
    /**
     *
     */
    protected String cacheControl;
    protected String connection;
    protected Date date;
    protected String pragma;
    protected String trailer;
    protected String transferEncoding;
    protected String upgrade;
    protected String via;
    protected String warning;
    /**
     * Entity Header Fields (see section 7.1 in RFC 2616)
     */
    /**
     *
     */
    protected String allow;
    protected String contentEncoding;
    protected String contentLanguage;
    protected Integer contentLength;
    protected String contentLocation;
    protected String contentMD5;
    protected String contentRange;
    protected String contentType;
    protected Date expires;
    protected Date lastModified;
    /**
     * <b>extension-header (see section 7.1 in RFC 2616)</b><p>
     *
     * The extension-header mechanism allows additional entity-header fields to
     * be defined without changing the protocol, but these fields cannot be
     * assumed to be recognizable by the recipient. Unrecognized header fields
     * SHOULD be ignored by the recipient and MUST be forwarded by transparent
     * proxies.
     */
    protected HashMap<String, String> extensionHeader = new HashMap<>();

    protected HttpHeader() {
    }

    public static HttpHeader parse(String httpMessage) throws HttpMalformedHeaderException {
        Objects.requireNonNull(httpMessage);

        HttpHeader httpHeader;
        StringTokenizer stringTokenizer = new StringTokenizer(httpMessage);
        try {
            String nextToken = stringTokenizer.nextToken();
            if (HttpMethod.isSupported(nextToken)) {
                // Request (see section 5 in RFC 2616)
                httpHeader = HttpRequestHeader.parse(httpMessage);
            } else {
                parseVersion(nextToken);
                // Response (see section 6 in RFC 2616)
                httpHeader = HttpResponseHeader.parse(httpMessage);
            }
        } catch (HttpMalformedHeaderException e) {
            throw new HttpMalformedHeaderException("Unsupported HTTP header (see section 4 in RFC 2616)");
        } catch (NoSuchElementException e) {
            throw new HttpMalformedHeaderException("Unexpected end of the header (see section 4 in RFC 2616)", e);
        }

        return httpHeader;
    }

    protected static HttpVersion parseVersion(String string) throws HttpUnsupportedVersionException {
        Objects.requireNonNull(string);
        StringTokenizer stringTokenizer = new StringTokenizer(string);

        HttpVersion version = HttpVersion.valueFor(stringTokenizer.nextToken());
        if (version == null) {
            throw new HttpUnsupportedVersionException("Unsupported HTTP version (see section 3.1 in RFC 2616)");
        }
        return version;
    }

    /**
     * general-header (see section 4.5 in RFC 2616)
     *
     * @param fieldName
     * @param fieldValue
     * @return
     */
    protected boolean setGeneralHeaderField(String fieldName, String fieldValue) throws HttpMalformedHeaderException {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldValue);

        switch (fieldName) {
            case "Cache-Control":
                cacheControl = fieldValue;
                return true;
            case "Connection":
                connection = fieldValue;
                return true;
            case "Date":
                try {
                    date = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new HttpPreconditionFailedException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return true;
            case "Pragma":
                pragma = fieldValue;
                return true;
            case "Trailer":
                trailer = fieldValue;
                return true;
            case "Transfer-Encoding":
                transferEncoding = fieldValue;
                return true;
            case "Upgrade":
                upgrade = fieldValue;
                return true;
            case "Via":
                via = fieldValue;
                return true;
            case "Warning":
                warning = fieldValue;
                return true;
            default:
                return false;
        }
    }

    /**
     * entity-header (see section 7.1 in RFC 2616)
     *
     * @param fieldName
     * @param fieldValue
     * @return
     */
    protected void setEntityHeaderField(String fieldName, String fieldValue) throws HttpMalformedHeaderException {
        Objects.requireNonNull(fieldName);
        Objects.requireNonNull(fieldValue);

        switch (fieldName) {
            case "Allow":
                allow = fieldValue;
                return;
            case "Content-Encoding":
                contentEncoding = fieldValue;
                return;
            case "Content-Language":
                contentLanguage = fieldValue;
                return;
            case "Content-Length":
                try {
                    contentLength = Integer.parseInt(fieldValue);
                } catch (NumberFormatException e) {
                    throw new HttpPreconditionFailedException("Invalid number format of the field " + fieldName + ": " + fieldValue, e);
                }
                return;
            case "Content-Location":
                contentLocation = fieldValue;
                return;
            case "Content-MD5":
                contentMD5 = fieldValue;
                return;
            case "Content-Range":
                contentRange = fieldValue;
                return;
            case "Content-Type":
                contentType = fieldValue;
                return;
            case "Expires":
                try {
                    expires = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new HttpPreconditionFailedException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return;
            case "Last-Modified":
                try {
                    lastModified = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new HttpPreconditionFailedException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return;
            default:
                extensionHeader.put(fieldName, fieldValue);
        }
    }

    /**
     * Method returning the content transfert method. This method is usefull to
     * find the way to detect the end of a HTTP message.
     *
     * (see section 4.4 in RFC 2616)
     *
     * @return
     */
    abstract public ContentTransferMethod contentTransferMethod();

    public int getHeaderLength() {
        return headerLength;
    }

    public String getAllow() {
        return allow;
    }

    public String getCacheControl() {
        return cacheControl;
    }

    public String getConnection() {
        return connection;
    }

    public String getContentEncoding() {
        return contentEncoding;
    }

    public String getContentLanguage() {
        return contentLanguage;
    }

    public Integer getContentLength() {
        return contentLength;
    }

    public String getContentLocation() {
        return contentLocation;
    }

    public String getContentMD5() {
        return contentMD5;
    }

    public String getContentRange() {
        return contentRange;
    }

    public String getContentType() {
        return contentType;
    }

    public Date getDate() {
        return date;
    }

    public static SimpleDateFormat getDateFormater() {
        return dateFormater;
    }

    public Date getExpires() {
        return expires;
    }

    /**
     * <b>extension-header (see section 7.1 in RFC 2616)</b><p>
     *
     * The extension-header mechanism allows additional entity-header fields to
     * be defined without changing the protocol, but these fields cannot be
     * assumed to be recognizable by the recipient. Unrecognized header fields
     * SHOULD be ignored by the recipient and MUST be forwarded by transparent
     * proxies.
     */
    public String getFromExtensionHeader(String fieldName) {
        return extensionHeader.get(fieldName);
    }

    public Date getLastModified() {
        return lastModified;
    }

    public String getPragma() {
        return pragma;
    }

    public String getTrailer() {
        return trailer;
    }

    public String getTransferEncoding() {
        return transferEncoding;
    }

    public String getUpgrade() {
        return upgrade;
    }

    public HttpVersion getVersion() {
        return version;
    }

    public String getVia() {
        return via;
    }

    public String getWarning() {
        return warning;
    }
}
