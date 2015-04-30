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
package org.bigtesting.fixd.tests;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.session.RequestParamSessionHandler;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
import org.bigtesting.routd.Route;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestRequestParamSessionHandler {

    @Test
    public void onCreateIsHandled() {
        
        HttpRequest request = mock(HttpRequest.class);
        Set<String> params = new HashSet<String>();
        params.add("firstName");
        params.add("lastName");
        when(request.getRequestParameterNames()).thenReturn(params);
        when(request.getRequestParameter("firstName")).thenReturn("John");
        when(request.getRequestParameter("lastName")).thenReturn("Doe");
        
        Session session = new Session();
        when(request.getSession()).thenReturn(session);
        
        Route route = new Route("/");
        when(request.getRoute()).thenReturn(route);
        
        newSessionHandler().onCreate(request);
        
        List<String> attributeNames = new ArrayList<String>(session.getAttributeNames());
        Collections.sort(attributeNames);
        
        assertEquals("[firstName, lastName]", attributeNames.toString());
        
        assertEquals("John", session.get("firstName"));
        assertEquals("Doe", session.get("lastName"));
    }
    
    @Test
    public void onCreateIsHandledWithNoParams() {
        
        HttpRequest request = mock(HttpRequest.class);
        when(request.getRequestParameterNames()).thenReturn(new HashSet<String>());
        when(request.getRequestParameter(anyString())).thenReturn(null);
        
        Session session = new Session();
        when(request.getSession()).thenReturn(session);
        
        Route route = new Route("/");
        when(request.getRoute()).thenReturn(route);
        
        newSessionHandler().onCreate(request);
        
        assertEquals("[]", session.getAttributeNames().toString());
    }
    
    private SessionHandler newSessionHandler() {
        return new RequestParamSessionHandler();
    }
}