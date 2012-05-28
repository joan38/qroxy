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

/**
 *
 * @author joan
 */
public class QosRule {
    
    private final Integer minSpeed;
    private final Integer maxSpeed;
    private final Integer priority;

    public QosRule(Integer minSpeed, Integer maxSpeed, Integer priority) {
        if (minSpeed == null && maxSpeed == null && priority == null) {
            throw new NullPointerException("At least one rule is required");
        }
        
        this.minSpeed = minSpeed;
        this.maxSpeed = maxSpeed;
        this.priority = priority;
    }

    public Integer getMaxSpeed() {
        return maxSpeed;
    }

    public Integer getMinSpeed() {
        return minSpeed;
    }

    public Integer getPriority() {
        return priority;
    }
}
