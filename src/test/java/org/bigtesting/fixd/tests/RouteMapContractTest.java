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

import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.routing.RouteMap;
import org.junit.Before;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public abstract class RouteMapContractTest {
    
    private RouteMap rm;
    
    @Before
    public void beforeEachTest() {
        rm = newRouteMap();
    }
    
    protected abstract RouteMap newRouteMap();
    
    @Test
    public void getRoute_Root() {
        Route r1 = new Route("/");
        rm.add(r1);
        assertEquals(r1, rm.getRoute("/"));
    }
    
    @Test
    public void getRoute_SimilarMatchesConstant() {
        Route r1 = new Route("/clients/all");
        Route r2 = new Route("/clients/:id");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r1, rm.getRoute("/clients/all"));
    }
    
    @Test
    public void getRoute_SimilarMatchesParam() {
        Route r1 = new Route("/clients/all");
        Route r2 = new Route("/clients/:id");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r2, rm.getRoute("/clients/123"));
    }
    
    @Test
    public void getRoute_IgnoresParamRegion() {
        Route r1 = new Route("/cntrl");
        Route r2 = new Route("/cntrl/clients/:id");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r1, rm.getRoute("/cntrl"));
    }
    
    @Test
    public void getRoute_FindsParamRegion() {
        Route r1 = new Route("/cntrl");
        Route r2 = new Route("/cntrl/clients/:id");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r2, rm.getRoute("/cntrl/clients/23455"));
    }
    
    @Test
    public void getRoute_DistinguishesBetweenDifferentRoutes() {
        Route r1 = new Route("/cntrl");
        Route r2 = new Route("/actn");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r2, rm.getRoute("/actn"));
    }
    
    @Test
    public void getRoute_NotFound() {
        Route r1 = new Route("/cntrl");
        Route r2 = new Route("/actn");
        rm.add(r1);
        rm.add(r2);
        assertNull(rm.getRoute("/test"));
    }
    
    @Test
    public void getRoute_MultiParamRegions_Multiple() {
        Route r1 = new Route("/cntrl/actn/:id");
        Route r2 = new Route("/cntrl/actn/:id/:name");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r2, rm.getRoute("/cntrl/actn/123/bob"));
    }
    
    @Test
    public void getRoute_MultiParamRegions_Single() {
        Route r1 = new Route("/cntrl/actn/:id");
        Route r2 = new Route("/cntrl/actn/:id/:name");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r1, rm.getRoute("/cntrl/actn/123"));
    }
    
    @Test
    public void getRoute_CustomRegexAlpha() {
        Route r1 = new Route("/cntrl/actn/:id<[0-9]+>");
        Route r2 = new Route("/cntrl/actn/:id<[a-z]+>");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r2, rm.getRoute("/cntrl/actn/bob"));
    }
    
    @Test
    public void getRoute_CustomRegexNumeric() {
        Route r1 = new Route("/cntrl/actn/:id<[0-9]+>");
        Route r2 = new Route("/cntrl/actn/:id<[a-z]+>");
        rm.add(r1);
        rm.add(r2);
        assertEquals(r1, rm.getRoute("/cntrl/actn/123"));
    }
}
