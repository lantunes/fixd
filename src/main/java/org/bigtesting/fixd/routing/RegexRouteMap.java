/*
 * Copyright (C) 2011-2013 Bigtesting.org
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.bigtesting.fixd.routing;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;

/**
 * 
 * @author Luis Antunes
 */
public class RegexRouteMap implements RouteMap {

private final Set<RegexRoute> routes = new HashSet<RegexRoute>();
    
    public void add(Route route) {
        routes.add(new RegexRoute(route));
    }
    
    public Route getRoute(String path) {
        
        for (RegexRoute route : routes) {
            Matcher m = route.pattern().matcher(path);
            if (m.find()) {
                return route.getRoute();
            }
        }
        
        return null;
    }
}
