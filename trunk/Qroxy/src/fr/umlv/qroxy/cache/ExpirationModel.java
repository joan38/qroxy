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
package fr.umlv.qroxy.cache;

import fr.umlv.qroxy.http.HttpResponseHeader;
import java.util.Date;

/**
 * Objects containing the algorithm(s) to decide if a cached ressource is
 * fresh or not.
 * @author gdemurge
 */
public class ExpirationModel {
    
    /**
     * Return the current system time.
     * @return time in milliseconds
     */
    private long currentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Test whether or not a resource is expired. The resource is indentified 
     * by the given HTTP response header.
     * @param cachedResponse
     * @return if the cachedResponse is expired or not
     */
    boolean isExpired(HttpResponseHeader cachedResponse) {
//        Date responseExpiration = cachedResponse.getExpires();
//        String cacheControl = cachedResponse.getCacheControl();
//        Date freshnessTime, currentAge;
//        
//        long ageValue = System.currentTimeMillis() - cachedResponse.getDate().getTime();
//        currentAge = new Date(ageValue);
//        
//        String str = "max-age=";
//        if(cacheControl.contains(str)) {
//            freshnessTime = new Date(Long.valueOf(cacheControl.substring(str.length(), cacheControl.length())));
//            return freshnessTime.compareTo(currentAge) > 0;
//        }
//        
//        if(responseExpiration == null) {
//            responseExpiration = new Date(currentTime());
//        }
//        freshnessTime = new Date(responseExpiration.getTime()-cachedResponse.getDate().getTime());
//        return freshnessTime.compareTo(currentAge) > 0;
        return false;
    }
}
