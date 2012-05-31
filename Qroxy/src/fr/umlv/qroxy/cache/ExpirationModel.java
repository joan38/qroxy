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

import fr.umlv.qroxy.http.HttpHeader;
import fr.umlv.qroxy.http.HttpRequestHeader;
import fr.umlv.qroxy.http.HttpResponseHeader;
import java.util.Date;

/**
 *
 * @author Guillaume
 */
public class ExpirationModel {
    
    public static boolean isExpired(HttpRequestHeader request, HttpResponseHeader cachedResponse) {
        Date responseExpiration = cachedResponse.getExpires();
        String cacheControl = cachedResponse.getCacheControl();
        Date freshnessTime;
        Date currentAge, expiresValue, maxAgeValue, dateValue;
        if(responseExpiration == null) {
            reponseExpiration = workOutAge(cachedResponse);
        }
        return freshnessTime.compareTo(currentAge) > 0;
    }
    
    private static Date workOutAge(HttpResponseHeader cachedResponse) {
        return new Date(System.currentTimeMillis());
    }
}
