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
package org.bigtesting.fixd.core;

import org.bigtesting.fixd.Method;

/**
 * 
 * @author Luis Antunes
 */
public class Upon {

    private final Method method;
    private final String resource;
    private final String contentType;
    private final RequestHandlerImpl handler;
    
    public Upon(Method method, String resource, RequestHandlerImpl handler) {
        this(method, resource, null, handler);
    }
    
    public Upon(Method method, String resource, String contentType, 
            RequestHandlerImpl handler) {
        
        this.method = method;
        this.resource = resource;
        this.contentType = contentType;
        this.handler = handler;
    }

    public Method getMethod() {
        return method;
    }
    
    public String getResource() {
        return resource;
    }
    
    public String getContentType() {
        return contentType;
    }
    
    public RequestHandlerImpl getHandler() {
        return handler;
    }
}
