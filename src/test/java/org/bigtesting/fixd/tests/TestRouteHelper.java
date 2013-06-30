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

import java.util.Arrays;

import org.bigtesting.fixd.routing.RouteHelper;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestRouteHelper {

    @Test
    public void getPathElements_NoController_NoAction_NoParamPath() {
        String[] expected = new String[]{""};
        String[] actual = RouteHelper.getPathElements("/");
        assertTrue(Arrays.equals(expected, actual));
    }
    
    @Test
    public void getPathElements_WithController_WithAction_WithParamPath() {
        String[] expected = new String[]{"cntrl","actn","clients",":id"};
        String[] actual = RouteHelper.getPathElements("/cntrl/actn/clients/:id");
        assertTrue(Arrays.equals(expected, actual));
    }
    
    @Test
    public void getPathElements_NoController_NoAction_WithParamPath() {
        String[] expected = new String[]{"clients",":id"};
        String[] actual = RouteHelper.getPathElements("/clients/:id");
        assertTrue(Arrays.equals(expected, actual));
    }
    
    @Test
    public void getPathElements_NoController_NoAction_WithParamPathWithRegex() {
        String[] expected = new String[]{"clients",":id<[0-9]+>"};
        String[] actual = RouteHelper.getPathElements("/clients/:id<[0-9]+>");
        assertTrue(Arrays.equals(expected, actual));
    }
    
    @Test
    public void escapeNonCustomRegex() {
        String path = "/cntrl/[](){}*^?$.\\/a+b/:id<[^/]+>/:name<[a-z]+>";
        String expected = 
                "/cntrl/\\[\\]\\(\\)\\{\\}\\*\\^\\?\\$\\.\\\\/a\\+b/:id<[^/]+>/:name<[a-z]+>";
        String actual = RouteHelper.escapeNonCustomRegex(path);
        assertEquals(expected, actual);
    }
}
