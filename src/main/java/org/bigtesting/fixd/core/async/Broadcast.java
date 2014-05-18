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
package org.bigtesting.fixd.core.async;

import org.bigtesting.routd.Route;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class Broadcast {
    
    private final Request request;
    private final Route route;
    
    public Broadcast(Request request, Route route) {
        this.request = request;
        this.route = route;
    }
    
    public Request getRequest() {
        return request;
    }
    
    public Route getRoute() {
        return route;
    }
}
