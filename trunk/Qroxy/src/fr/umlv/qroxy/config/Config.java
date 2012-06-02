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
package fr.umlv.qroxy.config;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * Object representing configurations.
 *
 * @author joan
 */
public class Config extends DefaultHandler {

    public static final int WHOHAS_CANCEL_TIME_OUT = 100;
    public static final int MAX_HEADER_LENGTH = 4096;
    private static final Double XML_CONFIG_VERSION = 1.0;
    private static final int DEFAULT_WEBUI_BIND_PORT = 7777;
    private static final int DEFAULT_PROXY_BIND_PORT = 8080;
    private final ArrayList<Category> categories = new ArrayList<>();
    private InetSocketAddress proxyBindSocketAddress;
    private SocketAddress webUiBindSocketAddress;
    private String cachePath;
    private int cacheDefaultMaxSize;
    private InetAddress cacheExchangingMulticastAddress;

    public Config(InetSocketAddress setListeningAddress, SocketAddress webUiBindAddress, String cachePath, int cacheDefaultMaxSize, InetAddress cacheExchangingMulticastAddress) {
        this.proxyBindSocketAddress = setListeningAddress;
        this.webUiBindSocketAddress = webUiBindAddress;
        this.cachePath = cachePath;
        this.cacheDefaultMaxSize = cacheDefaultMaxSize;
        this.cacheExchangingMulticastAddress = cacheExchangingMulticastAddress;
    }

    /**
     * Create a config file from an XML formated file.
     *
     * @param configFile The XML file
     * @return The configuration file
     * @throws XMLQroxyConfigException
     */
    public void loadFromXml(File configFile) throws XMLQroxyConfigException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            saxParser.parse(configFile, this);
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XMLQroxyConfigException("Error while parsing the config file: " + e.getMessage(), e);
        }
    }
    /**
     * Attributs for parsing XML
     */
    boolean qroxyConfigTags;
    boolean proxyTags;
    boolean proxyBindAddressTags;
    boolean proxyBindPortTags;
    boolean webUiTags;
    boolean cacheBindAddressTags;
    boolean cacheBindPortTags;
    boolean cacheTags;
    boolean pathTags;
    boolean defaultMaxSizeTags;
    boolean cacheExchangingMulticastAddressTags;
    boolean categoriesTags;
    boolean categoryTags;
    boolean regexsTags;
    boolean regexTags;
    boolean qosRuleTags;
    boolean minSpeedTags;
    boolean maxSpeedTags;
    boolean priorityTags;
    boolean cacheRuleTags;
    boolean maxSizeTags;
    String proxyBindAddress;
    int proxyBindPort = DEFAULT_PROXY_BIND_PORT;
    String webUiBindAddress;
    int webUiBindPort = DEFAULT_WEBUI_BIND_PORT;
    String currentCategoryName;
    HashMap<String, String> currentRegexs;
    String currentApplyOn;
    QosRule currentQosRule;
    Integer currentMinSpeed;
    Integer currentMaxSpeed;
    int currentPriority;
    CacheRule currentCacheRule;
    int currentMaxSize;

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        if (qName.equalsIgnoreCase("qroxyConfig")) {
            String version = attributes.getValue("version");
            if (!version.equals(XML_CONFIG_VERSION.toString())) {
                throw new SAXException("Invalid file version. Found: " + version + " Expected: " + XML_CONFIG_VERSION.toString());
            }
            qroxyConfigTags = true;
        } else if (qName.equalsIgnoreCase("proxy")) {
            proxyTags = true;
        } else if (qName.equalsIgnoreCase("bindAddress")) {
            proxyBindAddressTags = true;
        } else if (qName.equalsIgnoreCase("bindPort")) {
            proxyBindPortTags = true;
        } else if (qName.equalsIgnoreCase("webUi")) {
            webUiTags = true;
        } else if (qName.equalsIgnoreCase("bindAddress")) {
            cacheBindAddressTags = true;
        } else if (qName.equalsIgnoreCase("bindPort")) {
            cacheBindPortTags = true;
        } else if (qName.equalsIgnoreCase("cache")) {
            cacheTags = true;
        } else if (qName.equalsIgnoreCase("path")) {
            pathTags = true;
        } else if (qName.equalsIgnoreCase("maxDefaultSize")) {
            defaultMaxSizeTags = true;
        } else if (qName.equalsIgnoreCase("exchangingMulticastAddress")) {
            cacheExchangingMulticastAddressTags = true;
        } else if (qName.equalsIgnoreCase("categories")) {
            categoriesTags = true;
        } else if (qName.equalsIgnoreCase("category")) {
            currentCategoryName = attributes.getValue("name");
            categoryTags = true;
        } else if (qName.equalsIgnoreCase("regexs")) {
            currentRegexs = new HashMap<>();
            regexsTags = true;
        } else if (qName.equalsIgnoreCase("regex")) {
            currentApplyOn = attributes.getValue("applyOn");
            regexTags = true;
        } else if (qName.equalsIgnoreCase("qosRule")) {
            qosRuleTags = true;
        } else if (qName.equalsIgnoreCase("minSpeed")) {
            minSpeedTags = true;
        } else if (qName.equalsIgnoreCase("maxSpeed")) {
            maxSpeedTags = true;
        } else if (qName.equalsIgnoreCase("priority")) {
            priorityTags = true;
        } else if (qName.equalsIgnoreCase("cacheRule")) {
            cacheRuleTags = true;
        } else if (qName.equalsIgnoreCase("maxSize")) {
            maxSizeTags = true;
        }
    }

    @Override
    public void characters(char[] chars, int start, int length) throws SAXException {
        if (proxyBindAddressTags) {
            proxyBindAddress = new String(chars, start, length);
            proxyBindAddressTags = false;
        } else if (proxyBindPortTags) {
            String value = new String(chars, start, length);
            try {
                proxyBindPort = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid port number format: " + value, e);
            }
            proxyBindPortTags = false;
        } else if (cacheBindAddressTags) {
            webUiBindAddress = new String(chars, start, length);
            cacheBindAddressTags = false;
        } else if (cacheBindPortTags) {
            String value = new String(chars, start, length);
            try {
                webUiBindPort = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid port number format: " + value, e);
            }
            cacheBindPortTags = false;
        } else if (defaultMaxSizeTags) {
            String value = new String(chars, start, length);
            try {
                cacheDefaultMaxSize = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid cache default max size number format: " + value, e);
            }
            defaultMaxSizeTags = false;
        } else if (cacheExchangingMulticastAddressTags) {
            String value = new String(chars, start, length);
            try {
                cacheExchangingMulticastAddress = InetAddress.getByName(value);
            } catch (UnknownHostException e) {
                throw new SAXException("Invalid cache exchanging multicast address. No IP address for the host could be found, or a scope_id was specified for a global IPv6 address: " + value, e);
            } catch (SecurityException e) {
                throw new SAXException("Operation not allowed", e);
            }
            cacheExchangingMulticastAddressTags = false;
        } else if (pathTags) {
            cachePath = new String(chars, start, length);
            pathTags = false;
        } else if (regexTags) {
            currentRegexs.put(currentApplyOn, new String(chars, start, length));
            regexTags = false;
        } else if (minSpeedTags) {
            String value = new String(chars, start, length);
            try {
                currentMinSpeed = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid minSpeed number format: " + value, e);
            }
            minSpeedTags = false;
        } else if (maxSpeedTags) {
            String value = new String(chars, start, length);
            try {
                currentMaxSpeed = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid maxSpeed number format: " + value, e);
            }
            maxSpeedTags = false;
        } else if (priorityTags) {
            String value = new String(chars, start, length);
            try {
                currentPriority = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid priority number format: " + value, e);
            }
            priorityTags = false;
        } else if (maxSizeTags) {
            String value = new String(chars, start, length);
            try {
                currentMaxSize = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid maxSize number format: " + value, e);
            }
            maxSizeTags = false;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (qName.equalsIgnoreCase("proxy") && proxyTags) {
            if (proxyBindAddress == null) {
                proxyBindSocketAddress = new InetSocketAddress(proxyBindPort);
            } else {
                proxyBindSocketAddress = new InetSocketAddress(proxyBindAddress, proxyBindPort);
            }
        } else if (qName.equalsIgnoreCase("webUi") && webUiTags) {
            if (webUiBindAddress == null) {
                webUiBindSocketAddress = new InetSocketAddress(webUiBindPort);
            } else {
                webUiBindSocketAddress = new InetSocketAddress(webUiBindAddress, webUiBindPort);
            }
        } else if (qName.equalsIgnoreCase("qosRule") && qosRuleTags) {
            try {
                currentQosRule = new QosRule(currentMinSpeed, currentMaxSpeed, currentPriority);
            } catch (NullPointerException e) {
                throw new SAXException(e.getMessage(), e);
            }
        } else if (qName.equalsIgnoreCase("cacheRule") && cacheRuleTags) {
            currentCacheRule = new CacheRule(currentMaxSize);
        } else if (qName.equalsIgnoreCase(
                "category") && categoryTags) {
            try {
                categories.add(new Category(currentCategoryName, currentRegexs, currentQosRule, currentCacheRule));
            } catch (NullPointerException e) {
                throw new SAXException(e.getMessage(), e);
            }
        }
    }

    public SocketAddress getWebUiBindAddress() {
        return webUiBindSocketAddress;
    }

    public String getCachePath() {
        return cachePath;
    }

    public int getCacheDefaultMaxSize() {
        return cacheDefaultMaxSize;
    }

    public Collection<Category> getCategories() {
        return Collections.unmodifiableCollection(categories);
    }
    
    public InetAddress getCacheExchangingMulticastAddress() {
        return cacheExchangingMulticastAddress;
    }
    
    public InetSocketAddress getProxyListeningAddress() {
        return proxyBindSocketAddress;
    }
}
