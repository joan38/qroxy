/*
 * Copyright (C) 2012 Joan Goyeau <joan.goyeau@gmail.com>
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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author Joan Goyeau <joan.goyeau@gmail.com>
 */
public class Category {
    private final String name;
    private final HashMap<String, String> regexs;
    private final QosRule qosRule;
    private final CacheRule cacheRule;

    public Category(String name, HashMap<String, String> regexs, QosRule qosRule, CacheRule cacheRule) {
        Objects.requireNonNull(name);
        Objects.requireNonNull(regexs);
        if (qosRule == null && cacheRule == null) {
            throw new NullPointerException("At least one of QosRule or/and CacheRule is required");
        }
        
        this.name = name;
        this.regexs = regexs;
        this.qosRule = qosRule;
        this.cacheRule = cacheRule;
    }

    public String getName() {
        return name;
    }

    public CacheRule getCacheRule() {
        return cacheRule;
    }

    public QosRule getQosRule() {
        return qosRule;
    }

    public Map<String, String> getRegexs() {
        return Collections.unmodifiableMap(regexs);
    }
}
