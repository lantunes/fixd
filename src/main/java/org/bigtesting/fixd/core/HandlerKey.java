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
package org.bigtesting.fixd.core;

import org.bigtesting.fixd.routing.Route;

/**
 * 
 * @author Luis Antunes
 */
public class HandlerKey {

    private final String method;
    private final Route route;
    private final String contentType;
    
    public HandlerKey(String method, Route route, String contentType) {
        
        this.method = method;
        this.route = route;
        this.contentType = contentType;
    }
    
    @Override
    public int hashCode() {
        
        int hash = 1;
        hash = hash * 17 + method.hashCode();
        hash = hash * 31 + route.hashCode();
        hash = hash * 13 + (contentType == null ? 0 : contentType.hashCode());
        return hash;
    }
    
    @Override
    public boolean equals(Object o) {
        
        if (o == null) return false;
        if (o == this) return true;
        if (!(o instanceof HandlerKey)) return false;
        HandlerKey that = (HandlerKey)o;
        return 
                this.method.equals(that.method) && 
                this.route.equals(that.route) && 
                (this.contentType == null ? that.contentType == null : 
                    this.contentType.equals(that.contentType));
    }
}
