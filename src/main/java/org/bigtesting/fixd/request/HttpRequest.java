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
package org.bigtesting.fixd.request;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Set;

import org.bigtesting.fixd.session.Session;
import org.bigtesting.routd.Route;

/**
 * 
 * @author Luis Antunes
 */
public interface HttpRequest {

    String getPath();
    
    String getUndecodedPath();
    
    Set<String> getRequestParameterNames();
    
    String getRequestParameter(String name);
    
    String getPathParameter(String name);
    
    List<String> getHeaderNames();
    
    String getHeaderValue(String name);
    
    String getBody();
    
    InputStream getBodyAsStream() throws IOException;
    
    <T> T getBody(Class<T> type);
    
    long getContentLength();
    
    String getContentType();
    
    String getMethod();
    
    long getTime();
    
    String getQuery();
    
    int getMajor();
    
    int getMinor();
    
    String getTarget();
    
    /**
     * @return a session, if it exists, or null if it does not
     */
    Session getSession();
    
    Route getRoute();
}
