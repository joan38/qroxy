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
import fr.umlv.qroxy.http.HttpResponseHeader;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Guillaume
 */
public class CacheInputControler {
    List<URI> uris = new ArrayList<>();

    boolean isCacheable(HttpResponseHeader responseHeader) {
                String cacheControl = responseHeader.getCacheControl();
        int contentLength = responseHeader.getContentLength();
        if(contentLength >= Cache.CACHEABLE_RSRC_MAX_SIZE) {
            return false;
        }
        switch(cacheControl) {
            case "public":
                return true;
            case "private":
                return false;
            case "no_cache":
                return false;
            case "no-store":
                return false;
            default:
                return false;
        }
    }
}
