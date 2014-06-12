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

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.bigtesting.fixd.marshalling.Unmarshaller;
import org.bigtesting.fixd.marshalling.UnmarshallerProvider;
import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.util.RequestUtils;
import org.bigtesting.routd.Route;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class SimpleHttpRequest implements HttpRequest {

    private final Request request;
    
    private final Session session;
    
    private final Route route;
    
    private final UnmarshallerProvider unmarshallerProvider;
    
    public SimpleHttpRequest(Request request, Session session, Route route, 
            UnmarshallerProvider unmarshallerProvider) {
        
        this.request = request;
        this.session = session;
        this.route = route;
        this.unmarshallerProvider = unmarshallerProvider;
    }
    
    public String getPath() {
        
        return request.getPath().getPath();
    }
    
    public String getUndecodedPath() {
        
        return RequestUtils.getUndecodedPath(request);
    }

    public Set<String> getRequestParameterNames() {
        
        return request.getQuery().keySet();
    }

    public String getRequestParameter(String name) {
        
        return request.getParameter(name);
    }
    
    public String getPathParameter(String name) {
        
        return getRoute().getNamedParameter(name, getPath());
    }
    
    public List<String> getHeaderNames() {
        
        return request.getNames();
    }
    
    public String getHeaderValue(String name) {
        
        return request.getValue(name);
    }
    
    public String getBody() {
        
        try {
            return new String(RequestUtils.readBody(getBodyAsStream()));
        } catch (IOException e) {
            throw new RuntimeException("error getting body", e);
        }
    }
    
    public InputStream getBodyAsStream() throws IOException {
        
        return request.getInputStream();
    }
    
    public <T> T getBody(Class<T> type) {
        
        Unmarshaller unmarshaller = unmarshallerProvider.getUnmarshaller(getContentType());
        if (unmarshaller == null) {
            throw new RuntimeException("cannot unmarshall body as type " + 
                    type.getName() + ", as no unmarshaller is available for" +
                    		"content-type: " + getContentType());
        }
        try {
            return unmarshaller.unmarshal(getBodyAsStream(), type);
        } catch (Exception e) {
            throw new RuntimeException("error unmarshalling", e);
        }
    }
    
    public long getContentLength() {
        
        return request.getContentLength();
    }
    
    public String getContentType() {
        
        return request.getContentType().getType();
    }
    
    public String getMethod() {
        
        return request.getMethod();
    }
    
    public long getTime() {
        
        return request.getRequestTime();
    }
    
    public String getQuery() {
        
        return request.getQuery().toString();
    }
    
    public int getMajor() {
        
        return request.getMajor();
    }
    
    public int getMinor() {
        
        return request.getMinor();
    }
    
    public String getTarget() {
        
        return request.getTarget();
    }
    
    public Session getSession() {
        
        return session;
    }
    
    public Route getRoute() {
        
        return route;
    }
}
