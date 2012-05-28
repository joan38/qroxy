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
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;
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
 * Objet représentant les config Parse le fichier de conf formaté à la entete
 * HTTP port du serveur d'infos
 *
 * @author joan
 */
public class Config extends DefaultHandler {

    private static final Double XML_CONFIG_VERSION = 1.0;
    private static final int DEFAULT_WEBUI_BIND_PORT = 7777;
    private final ArrayList<Category> categories = new ArrayList<>();
    private SocketAddress webUiBindAddress;
    private String cachePath;

    private Config() {
    }

    public static Config loadFromXml(File configFile) throws XMLQroxyConfigException {
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();
            Config config = new Config();
            saxParser.parse(configFile, config);
            
            return config;
        } catch (ParserConfigurationException | SAXException | IOException e) {
            throw new XMLQroxyConfigException("Error while parsing the config file: " + e.getMessage(), e);
        }
    }
    /**
     * Attributs for parsing XML
     */
    boolean qroxyConfigTags;
    boolean webUiTags;
    boolean bindAddressTags;
    boolean bindPortTags;
    boolean cacheTags;
    boolean pathTags;
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
    String bindAddress;
    int bindPort = DEFAULT_WEBUI_BIND_PORT;
    String currentCategoryName;
    HashMap<String, String> currentRegexs;
    String currentApplyOn;
    QosRule currentQosRule;
    Integer currentMinSpeed;
    Integer currentMaxSpeed;
    Integer currentPriority;
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
        } else if (qName.equalsIgnoreCase("webUi")) {
            webUiTags = true;
        } else if (qName.equalsIgnoreCase("bindAddress")) {
            bindAddressTags = true;
        } else if (qName.equalsIgnoreCase("bindPort")) {
            bindPortTags = true;
        } else if (qName.equalsIgnoreCase("cache")) {
            cacheTags = true;
        } else if (qName.equalsIgnoreCase("path")) {
            pathTags = true;
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
        if (bindAddressTags) {
            bindAddress = new String(chars, start, length);
            bindAddressTags = false;
        } else if (bindPortTags) {
            String value = new String(chars, start, length);
            try {
                bindPort = Integer.parseInt(value);
            } catch (NumberFormatException e) {
                throw new SAXException("Invalid port number format: " + value, e);
            }
            bindPortTags = false;
        } else if (pathTags) {
            cachePath = new String(chars, start, length);
            pathTags = false;
        } else if (regexTags) {
            currentRegexs.put(currentApplyOn, new String(chars, start, length));
            regexTags= false;
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
        if (qName.equalsIgnoreCase("webUi") && webUiTags) {
            if (bindAddress == null) {
                webUiBindAddress = new InetSocketAddress(bindPort);
            } else {
                webUiBindAddress = new InetSocketAddress(bindAddress, bindPort);
            }
        } else if (qName.equalsIgnoreCase("qosRule") && qosRuleTags) {
            try {
                currentQosRule = new QosRule(currentMinSpeed, currentMaxSpeed, currentPriority);
            } catch (NullPointerException e) {
                throw new SAXException("At least one rule of minSpeed, maxSpeed and priority has to be defined", e);
            }
        } else if (qName.equalsIgnoreCase("cacheRule") && cacheRuleTags) {
            currentCacheRule = new CacheRule(currentMaxSize);
        } else if (qName.equalsIgnoreCase("category") && categoryTags) {
            try {
                categories.add(new Category(currentCategoryName, currentRegexs, currentQosRule, currentCacheRule));
            } catch (NullPointerException e) {
                throw new SAXException("categoryName, regexs and at least one of qosRule or/and cacheRule is required", e);
            }
        }
    }

    public String getCachePath() {
        return cachePath;
    }

    public Collection<Category> getCategories() {
        return Collections.unmodifiableCollection(categories);
    }

    public SocketAddress getWebUiBindAddress() {
        return webUiBindAddress;
    }
}
