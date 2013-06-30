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
package org.bigtesting.fixd.tests;

import static org.junit.Assert.*;

import java.util.List;

import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.routing.Route.PathParameterElement;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestRoute {

    @Test
    public void newRoute_NullPathThrowsException() {
        try {
            new Route(null);
            fail("should have thrown IllegalArgumentException");
        } catch (IllegalArgumentException e) {
        }
    }
    
    @Test
    public void equals_NotEqual() {
        Route r1 = new Route("/");
        Route r2 = new Route("/cntrl");
        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_NotEqual_WithController() {
        Route r1 = new Route("/cntrl");
        Route r2 = new Route("/cntrl2");
        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_NotEqual_WithControllerAndAction() {
        Route r1 = new Route("/cntrl/actn");
        Route r2 = new Route("/cntrl/actn2");
        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_NotEqual_WithControllerAndActionAndParams() {
        Route r1 = new Route("/cntrl/actn/:id");
        Route r2 = new Route("/cntrl/actn2/:id");
        assertFalse(r1.equals(r2));
        assertFalse(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_Equal_Root() {
        Route r1 = new Route("/");
        Route r2 = new Route("/");
        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_Equal_WithController() {
        Route r1 = new Route("/cntrl");
        Route r2 = new Route("/cntrl");
        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_Equal_WithControllerAndAction() {
        Route r1 = new Route("/cntrl/actn");
        Route r2 = new Route("/cntrl/actn");
        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void equals_Equal_WithControllerAndActionAndParams() {
        Route r1 = new Route("/cntrl/actn/:id");
        Route r2 = new Route("/cntrl/actn/:id");
        assertTrue(r1.equals(r2));
        assertTrue(r1.hashCode() == r2.hashCode());
    }
    
    @Test
    public void toString_WithController_NoAction_NoParamPath() {
        Route r = new Route("/cntrl");
        String expected = "/cntrl";
        String actual = r.toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void toString_WithController_WithAction_NoParamPath() {
        Route r = new Route("/cntrl/actn");
        String expected = "/cntrl/actn";
        String actual = r.toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void toString_WithController_WithAction_WithParamPath() {
        Route r = new Route("/cntrl/actn/clients/:id");
        String expected = "/cntrl/actn/clients/:id";
        String actual = r.toString();
        assertEquals(expected, actual);
    }

    @Test
    public void pathParameterElements_NoneExist() {
        Route r = new Route("/actn");
        List<PathParameterElement> params = r.pathParameterElements();
        assertTrue(params.isEmpty());
    }
    
    @Test
    public void pathParameterElements_OneExistsWithAction() {
        Route r = new Route("/actn/:id");
        List<PathParameterElement> params = r.pathParameterElements();
        assertEquals(1, params.size());
        PathParameterElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(1, elem.index());
    }
    
    @Test
    public void pathParameterElements_OneExistsAlone() {
        Route r = new Route("/:id");
        List<PathParameterElement> params = r.pathParameterElements();
        assertEquals(1, params.size());
        PathParameterElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(0, elem.index());
    }
    
    @Test
    public void pathParameterElements_ManyExistAlone() {
        Route r = new Route("/:id/:name");
        List<PathParameterElement> params = r.pathParameterElements();
        assertEquals(2, params.size());
        PathParameterElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(0, elem.index());
        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(1, elem.index());
    }
    
    @Test
    public void pathParameterElements_ManyExistWithControllerAndAction() {
        Route r = new Route("/cntrl/actn/:id/:name");
        List<PathParameterElement> params = r.pathParameterElements();
        assertEquals(2, params.size());
        PathParameterElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(2, elem.index());
        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
    }
    
    @Test
    public void pathParameterElements_ManyExistWithRegexWithControllerAndAction() {
        Route r = new Route("/cntrl/actn/:id<[0-9]+>/:name<[a-z]+>");
        List<PathParameterElement> params = r.pathParameterElements();
        assertEquals(2, params.size());
        PathParameterElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(2, elem.index());
        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
    }
    
    @Test
    public void pathParameterElements_OneExistsWithRegexWithSlashWithControllerAndAction() {
        Route r = new Route("/cntrl/actn/:id<[^/]+>/:name<[a-z]+>");
        List<PathParameterElement> params = r.pathParameterElements();
        assertEquals(2, params.size());
        PathParameterElement elem = params.get(0);
        assertEquals("id", elem.name());
        assertEquals(2, elem.index());
        elem = params.get(1);
        assertEquals("name", elem.name());
        assertEquals(3, elem.index());
    }
}
