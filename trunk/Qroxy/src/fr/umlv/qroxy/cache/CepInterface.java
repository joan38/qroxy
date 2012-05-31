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

import java.nio.channels.Channel;

/**
 *
 * @author Guillaume
 */
public interface CepInterface {
    
    
    
    public enum Method {
        WHOAS {
            public void handle(Channel c) {
                // TODO read a WHOAS message
                // TODO perform an action
            }
        },
        OWN {
            public void handle(Channel c) {
                // TODO read a OWN message
                // TODO perform an action
            }
        },
        GET {
            public void handle(Channel c) {
                // TODO read a GET message
                // TODO perform an action
            }
        };
        
        public abstract void handle(Channel c);
    }
}
