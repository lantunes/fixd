/*
 * Copyright (C) 2013 BigTesting.org
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

import static org.bigtesting.fixd.routing.RouteHelper.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author Luis Antunes
 */
public class Route {

    private final String resourcePath;
    private final List<PathParameterElement> pathParamElements;
    
    public Route(String paramPath) {
        if (paramPath == null) {
            throw new IllegalArgumentException("path cannot be null");
        }
        this.resourcePath = paramPath;
        this.pathParamElements = extractPathParamElements();
    }
    
    private List<PathParameterElement> extractPathParamElements() {
        List<PathParameterElement> elements = new ArrayList<PathParameterElement>();
        String path = CUSTOM_REGEX_PATTERN.matcher(resourcePath).replaceAll("");
        String[] pathElements = getPathElements(path);
        for (int i = 0; i < pathElements.length; i++) {
            String currentElement = pathElements[i];
            if (currentElement.startsWith(PARAM_PREFIX)) {
                currentElement = currentElement.substring(1);
                elements.add(new PathParameterElement(currentElement, i));
            }
        }
        return elements;
    }
    
    public String getResourcePath() {
        return resourcePath;
    }
    
    public String toString() {
        return resourcePath;
    }
    
    public List<PathParameterElement> pathParameterElements() {
        return pathParamElements;            
    }
    
    public int hashCode() {
        int hash = 1;
        hash = hash * 13 + (resourcePath == null ? 0 : resourcePath.hashCode());
        return hash;
    }
    
    public boolean equals(Object o) {
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof Route)) return false;
        Route that = (Route)o;
        return 
                (this.resourcePath == null ? that.resourcePath == null : 
                    this.resourcePath.equals(that.resourcePath));
    }
    
    public static class PathParameterElement {
        private final String name;
        private final int index;
        
        public PathParameterElement(String name, int index) {
            this.name = name;
            this.index = index;
        }
        
        public String name() {
            return name;
        }
        
        public int index() {
            return index;
        }
    }
}
