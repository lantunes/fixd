/*
 * Copyright (C) 2014 BigTesting.org
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

import org.bigtesting.fixd.request.impl.SimpleHttpRequest;
import org.bigtesting.routd.Route;
import org.junit.Test;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class TestSimpleHttpRequest {

    @Test
    public void getPathParameter() {
        
        Request request = mock(Request.class);
        Path path = mock(Path.class);
        when(path.getPath()).thenReturn("/first-name/John/last-name/Doe");
        when(request.getPath()).thenReturn(path);
        
        Route route = new Route("/first-name/:firstName/last-name/:lastName");
        
        SimpleHttpRequest req = new SimpleHttpRequest(request, null, route, null);
        
        assertEquals("John", req.getPathParameter("firstName"));
        assertEquals("Doe", req.getPathParameter("lastName"));
        assertNull(req.getPathParameter("nonExistentPathParam"));
    }
}
