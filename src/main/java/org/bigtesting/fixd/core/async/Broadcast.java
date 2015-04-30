/*
 * Copyright (C) 2015 BigTesting.org
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

import org.bigtesting.fixd.capture.impl.SimpleCapturedRequest;
import org.bigtesting.fixd.core.Upon;
import org.bigtesting.routd.Route;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class Broadcast {
    
    private final Request request;
    private final Route route;
    private final Upon upon;
    private final SimpleCapturedRequest captured;
    
    public Broadcast(Request request, Route route, Upon upon, 
            SimpleCapturedRequest captured) {
        
        this.request = request;
        this.route = route;
        this.upon = upon;
        this.captured = captured;
    }
    
    public Request getRequest() {
        
        return request;
    }
    
    public Route getRoute() {
        
        return route;
    }
    
    public boolean isFor(Subscriber subscriber) {
        
        if (upon != null) {
            return upon.getHandler().equals(subscriber.getHandler());
        }
        return false;
    }
    
    public void sent(boolean sent) {
        
        if (captured != null) {
            captured.setBroadcast(sent);
        }
    }
}
