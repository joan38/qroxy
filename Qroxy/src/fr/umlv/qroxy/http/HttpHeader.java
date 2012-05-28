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
    protected int contentLength;
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

    public static HttpHeader parse(String httpMessage) throws MalformedHttpHeaderException {
        Objects.requireNonNull(httpMessage);

        // Cut the message body
        httpMessage = httpMessage.substring(0, httpMessage.indexOf("\r\n\r\n"));

        HttpHeader httpHeader;

        try {
            StringTokenizer stringTokenizer = new StringTokenizer(httpMessage);
            String nextToken = stringTokenizer.nextToken();
            if (HttpMethod.isSupported(nextToken)) {
                // Request (see section 5 in RFC 2616)
                httpHeader = HttpRequestHeader.parse(httpMessage);
            } else if (stringTokenizer.nextToken("/").equals("HTTP")) {
                // Response (see section 6 in RFC 2616)
                httpHeader = HttpResponseHeader.parse(httpMessage);
            } else {
                throw new MalformedHttpHeaderException("Unsupported HTTP version (see section 3.1 in RFC 2616)");
            }
        } catch (NoSuchElementException e) {
            throw new MalformedHttpHeaderException("Unexpected end of the header (see section 4 in RFC 2616)", e);
        }

        return httpHeader;
    }

    protected void parseVersion(StringTokenizer stringTokenizer) throws MalformedHttpHeaderException {
        Objects.requireNonNull(stringTokenizer);

        try {
            int major = Integer.parseInt(stringTokenizer.nextToken(".").substring(1));
            int minor = Integer.parseInt(stringTokenizer.nextToken(" \t\n\r\f").substring(1));
            version = HttpVersion.valueFor(major, minor);
            if (version == null) {
                throw new MalformedHttpHeaderException("Unsupported HTTP version (see section 3.1 in RFC 2616)");
            }
        } catch (NumberFormatException e) {
            throw new MalformedHttpHeaderException("Invalid HTTP version number (see section 3.1 in RFC 2616)", e);
        }
    }

    /**
     * general-header (see section 4.5 in RFC 2616)
     *
     * @param fieldName
     * @param fieldValue
     * @return
     */
    protected boolean setGeneralHeaderField(String fieldName, String fieldValue) throws MalformedHttpHeaderException {
        HttpGeneralHeaderField generalHeaderField = HttpGeneralHeaderField.valueFor(fieldName);
        if (generalHeaderField == null) {
            return false;
        }
        
        switch (generalHeaderField) {
            case CACHE_CONTROL:
                cacheControl = fieldValue;
                return true;
            case CONNECTION:
                connection = fieldValue;
                return true;
            case DATE:
                try {
                    date = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new MalformedHttpHeaderException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return true;
            case PRAGMA:
                pragma = fieldValue;
                return true;
            case TRAILER:
                trailer = fieldValue;
                return true;
            case TRANSFER_ENCODING:
                transferEncoding = fieldValue;
                return true;
            case UPGRADE:
                upgrade = fieldValue;
                return true;
            case VIA:
                via = fieldValue;
                return true;
            case WARNING:
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
    protected void setEntityHeaderField(String fieldName, String fieldValue) throws MalformedHttpHeaderException {
        HttpEntityHeaderField entityHeaderField = HttpEntityHeaderField.valueFor(fieldName);
        if (entityHeaderField == null) {
            extensionHeader.put(fieldName, fieldValue);
            return;
        }

        switch (entityHeaderField) {
            case ALLOW:
                allow = fieldValue;
                return;
            case CONTENT_ENCODING:
                contentEncoding = fieldValue;
                return;
            case CONTENT_LANGUAGE:
                contentLanguage = fieldValue;
                return;
            case CONTENT_LENGTH:
                try {
                contentLength = Integer.parseInt(fieldValue);
                } catch (NumberFormatException e) {
                    throw new MalformedHttpHeaderException("Invalid number format of the field " + fieldName + ": " + fieldValue, e);
                }
                return;
            case CONTENT_LOCATION:
                contentLocation = fieldValue;
                return;
            case CONTENT_MD5:
                contentMD5 = fieldValue;
                return;
            case CONTENT_RANGE:
                contentRange = fieldValue;
                return;
            case CONTENT_TYPE:
                contentType = fieldValue;
                return;
            case EXPIRES:
                try {
                    expires = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new MalformedHttpHeaderException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
                return;
            case LAST_MODIFIED:
                try {
                    lastModified = dateFormater.parse(fieldValue);
                } catch (ParseException e) {
                    throw new MalformedHttpHeaderException("Invalid date format of the field " + fieldName + ": " + fieldValue, e);
                }
        }
    }

//    public boolean headerMatches(, String regex) {
//        
//        
//        
//        Scanner scanner = new Scanner(unparsedHeader);
//
//        // Avoid the first line (Ex: GET http://toto.com/ HTTP/1.1)
//        scanner.nextLine();
//
//        while (scanner.hasNextLine()) {
//            if (scanner.nextLine().matches(regex)) {
//                return true;
//            }
//        }
//
//        return false;
//    }
    
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

    public int getContentLength() {
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
