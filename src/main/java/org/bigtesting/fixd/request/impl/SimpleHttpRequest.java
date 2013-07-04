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
package org.bigtesting.fixd.request.impl;

import java.util.Set;

import org.bigtesting.fixd.request.HttpRequest;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class SimpleHttpRequest implements HttpRequest {

    private final Request request;
    
    public SimpleHttpRequest(Request request) {
        
        this.request = request;
    }
    
    public String getPath() {
        
        return request.getPath().getPath();
    }

    public Set<String> getParameterNames() {
        
        return request.getQuery().keySet();
    }

    public String getParameter(String name) {
        
        return request.getParameter(name);
    }
}
