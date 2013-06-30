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

import org.bigtesting.fixd.routing.RegexRoute;
import org.bigtesting.fixd.routing.Route;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestRegexRoute {

    @Test
    public void pattern_Root() {
        RegexRoute r = new RegexRoute(new Route("/"));
        String expected = "^/$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_WithController_NoAction_NoParamPath() {
        RegexRoute r = new RegexRoute(new Route("/cntrl"));
        String expected = "^/cntrl$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_WithController_WithAction_NoParamPath() {
        RegexRoute r = new RegexRoute(new Route("/cntrl/actn"));
        String expected = "^/cntrl/actn$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_WithController_WithAction_WithParamPath() {
        RegexRoute r = new RegexRoute(new Route("/cntrl/actn/clients/:id"));
        String expected = "^/cntrl/actn/clients/([^/]+)$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_NoController_WithAction_WithParamPath() {
        RegexRoute r = new RegexRoute(new Route("/actn/clients/:id"));
        String expected = "^/actn/clients/([^/]+)$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_NoController_NoAction_WithParamPath() {
        RegexRoute r = new RegexRoute(new Route("/clients/:id"));
        String expected = "^/clients/([^/]+)$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_WithController_WithAction_WithRegexParamPath() {
        RegexRoute r = new RegexRoute(new Route("/cntrl/actn/clients/:id<[0-9]+>"));
        String expected = "^/cntrl/actn/clients/([0-9]+)$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_WithController_WithAction_WithMultiRegexParamPath() {
        RegexRoute r = new RegexRoute(new Route("/cntrl/actn/clients/:id<[0-9]+>/:name"));
        String expected = "^/cntrl/actn/clients/([0-9]+)/([^/]+)$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
    
    @Test
    public void pattern_WithController_WithAction_WithRegexSymbols() {
        RegexRoute r = new RegexRoute(new Route("/cntrl/actn/a+b/:id<[0-9]+>/:name"));
        String expected = "^/cntrl/actn/a\\+b/([0-9]+)/([^/]+)$";
        String actual = r.pattern().toString();
        assertEquals(expected, actual);
    }
}
