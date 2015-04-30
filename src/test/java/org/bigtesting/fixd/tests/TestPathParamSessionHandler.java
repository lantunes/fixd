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
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.session.PathParamSessionHandler;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
import org.bigtesting.routd.Route;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestPathParamSessionHandler {

    @Test
    public void onCreateIsHandled() {
        
        HttpRequest request = mock(HttpRequest.class);
        when(request.getPath()).thenReturn("/first-name/John/last-name/Doe");
        
        Session session = new Session();
        when(request.getSession()).thenReturn(session);
        
        Route route = new Route("/first-name/:firstName/last-name/:lastName");
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
        when(request.getPath()).thenReturn("/");
        
        Session session = new Session();
        when(request.getSession()).thenReturn(session);
        
        Route route = new Route("/");
        when(request.getRoute()).thenReturn(route);
        
        newSessionHandler().onCreate(request);
        
        assertEquals("[]", session.getAttributeNames().toString());
    }
    
    private SessionHandler newSessionHandler() {
        return new PathParamSessionHandler();
    }
}
