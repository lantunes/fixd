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
import static org.mockito.Mockito.*;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.bigtesting.fixd.interpolation.ResponseBodyInterpolator;
import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.routd.PathParameterElement;
import org.bigtesting.routd.Route;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestResponseBodyInterpolator {

    @Test
    public void testBodyReturnedUnmodifiedWhenNoInstructionsGiven() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello World!", 
                ResponseBodyInterpolator.interpolate("Hello World!", req));
    }
    
    @Test
    public void testBodyReturnedUnmodifiedWhenNoInstructionsGiven_WithSession() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello World!", 
                ResponseBodyInterpolator.interpolate("Hello World!", req));
    }
    
    @Test
    public void testSinglePathParamSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/Tim");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello :name", req));
    }
    
    @Test
    public void testSinglePathParamSubstituedInPresenceOfOtherColons() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/Tim");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello : Tim :", 
                ResponseBodyInterpolator.interpolate("Hello : :name :", req));
    }
    
    @Test
    public void testSinglePathParamDoublyPrefixedIsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/Tim");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :Tim", 
                ResponseBodyInterpolator.interpolate("Hello ::name", req));
    }
    
    @Test
    public void testSinglePathParamEnclosedByColonsIsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/Tim");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :Tim:", 
                ResponseBodyInterpolator.interpolate("Hello ::name:", req));
    }
    
    @Test
    public void testSinglePathParamSubstituedInPresenceOfOtherColonPrefixedTerms() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/Tim");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :someTerm Tim", 
                ResponseBodyInterpolator.interpolate("Hello :someTerm :name", req));
    }
    
    @Test
    public void testSinglePathParamSubstituedWithColonContainingValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/:Tim");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :Tim", 
                ResponseBodyInterpolator.interpolate("Hello :name", req));
    }
    
    @Test
    public void testMultiplePathParamsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/John/Doe");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1, null));
        elements.add(new PathParameterElement("lastName", 2, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John Doe", 
                ResponseBodyInterpolator.interpolate("Hello :firstName :lastName", req));
    }
    
    @Test
    public void testMultiplePathParamsSubstituedInPresenceOfOtherColons() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/John/Doe");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1, null));
        elements.add(new PathParameterElement("lastName", 2, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello : John : Doe :", 
                ResponseBodyInterpolator.interpolate("Hello : :firstName : :lastName :", req));
    }
    
    @Test
    public void testMultiplePathParamsSubstituedWithColonContainingValues() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/:John/:Doe");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1, null));
        elements.add(new PathParameterElement("lastName", 2, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :John :Doe", 
                ResponseBodyInterpolator.interpolate("Hello :firstName :lastName", req));
    }
    
    @Test
    public void testSingleSessionValueSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello {name}", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraces() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello { Tim } {", 
                ResponseBodyInterpolator.interpolate("Hello { {name} } {", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraces_NoSpaces() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {Tim} {", 
                ResponseBodyInterpolator.interpolate("Hello {{name}} {", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraceEnclosedTerms() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {there} Tim", 
                ResponseBodyInterpolator.interpolate("Hello {there} {name}", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedWithBraceEnclosedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "{Tim}");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {Tim}", 
                ResponseBodyInterpolator.interpolate("Hello {name}", req));
    }
    
    @Test
    public void testMultipleSessionValuesSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello John Doe", 
                ResponseBodyInterpolator.interpolate("Hello {firstName} {lastName}", req));
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedInPresenceOfOtherBraces() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello { John } { Doe } {", 
                ResponseBodyInterpolator.interpolate("Hello { {firstName} } { {lastName} } {", req));
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedInPresenceOfOtherBraces_NoSpaces() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {John} {Doe} {", 
                ResponseBodyInterpolator.interpolate("Hello {{firstName}} {{lastName}} {", req));
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedInPresenceOfOtherBraceEnclosedTerms() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {there} John Doe", 
                ResponseBodyInterpolator.interpolate("Hello {there} {firstName} {lastName}", req));
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedWithBraceEnclosedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("firstName", "{John}");
        session.set("lastName", "{Doe}");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {John} {Doe}", 
                ResponseBodyInterpolator.interpolate("Hello {firstName} {lastName}", req));
    }
    
    @Test
    public void testPathParamAndSessionValues_PathParamValuesComeFirst() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/greet/Mr.");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("salutation", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Mr. John Doe", 
                ResponseBodyInterpolator.interpolate("Hello :salutation {firstName} {lastName}", req));
    }
    
    @Test
    public void testPathParamAndSessionValues_SessionValuesComeFirst() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/greet/John/Doe");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1, null));
        elements.add(new PathParameterElement("lastName", 2, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("salutation", "Mr.");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Mr. John Doe", 
                ResponseBodyInterpolator.interpolate("Hello {salutation} :firstName :lastName", req));
    }
    
    @Test
    public void testColonPrefixedSessionValueNotHandled() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/John");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("{name}", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello John", 
                ResponseBodyInterpolator.interpolate("Hello :{name}", req));
    }
    
    @Test
    public void testBraceEnclosedPathParamValueDoesNotSubstituteSessionValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/name/John");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set(":name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {John}", 
                ResponseBodyInterpolator.interpolate("Hello {:name}", req));
    }
    
    @Test
    public void testRequestUnknownValuesIgnored() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: [some.value]", 
                ResponseBodyInterpolator.interpolate("Message: [some.value]", req));
    }
    
    @Test
    public void testRequestBodySubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: Hello World!", 
                ResponseBodyInterpolator.interpolate("Message: [request.body]", req));
    }
    
    @Test
    public void testRequestBodySubstituedInPresenceOfOtherBrackets() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: [ Hello World! ] [", 
                ResponseBodyInterpolator.interpolate("Message: [ [request.body] ] [", req));
    }
    
    @Test
    public void testRequestBodySubstituedInPresenceOfOtherBrackets_NoSpaces() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: [Hello World!] [", 
                ResponseBodyInterpolator.interpolate("Message: [[request.body]] [", req));
    }
    
    @Test
    public void testRequestBodySubstituedInPresenceOfOtherBracketEnclosedTerms() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Tim"));
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [there] Tim", 
                ResponseBodyInterpolator.interpolate("Hello [there] [request.body]", req));
    }
    
    @Test
    public void testRequestBodySubstituedWithBracketEnclosedValue() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("[Tim]"));
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [Tim]", 
                ResponseBodyInterpolator.interpolate("Hello [request.body]", req));
    }
    
    @Test
    public void testPathParamAndSessionAndRequestUsedTogether() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Doe"));
        when(req.getPath()).thenReturn("/name/John");
        Route route = mock(Route.class);
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1, null));
        when(route.pathParameterElements()).thenReturn(elements);
        when(req.getRoute()).thenReturn(route);
        Session session = new Session();
        session.set("salutation", "Mr.");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Mr. John Doe", 
                ResponseBodyInterpolator.interpolate("Hello {salutation} :name [request.body]", req));
    }
    
    @Test
    public void testRequestParameterSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getRequestParameter("name")).thenReturn("Tim");
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello [request?name]", req));
    }
    
    @Test
    public void testRequestHeaderSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getHeaderValue("User-Agent")).thenReturn("Mozilla/5.0");
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Value: Mozilla/5.0", 
                ResponseBodyInterpolator.interpolate("Value: [request$User-Agent]", req));
    }
    
    @Test
    public void testRequestMethodSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: GET", 
                ResponseBodyInterpolator.interpolate("Message: [request.method]", req));
    }
    
    @Test
    public void testRequestTimeSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getTime()).thenReturn(123L);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: 123", 
                ResponseBodyInterpolator.interpolate("Message: [request.time]", req));
    }
    
    @Test
    public void testRequestPathSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getPath()).thenReturn("/abc");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: /abc", 
                ResponseBodyInterpolator.interpolate("Message: [request.path]", req));
    }
    
    @Test
    public void testRequestQuerySubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getQuery()).thenReturn("a=b&c=d");
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: a=b&c=d", 
                ResponseBodyInterpolator.interpolate("Message: [request.query]", req));
    }
    
    @Test
    public void testRequestMajorSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getMajor()).thenReturn(123);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: 123", 
                ResponseBodyInterpolator.interpolate("Message: [request.major]", req));
    }
    
    @Test
    public void testRequestMinorSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getMinor()).thenReturn(123);
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: 123", 
                ResponseBodyInterpolator.interpolate("Message: [request.minor]", req));
    }
    
    @Test
    public void testRequestTargetSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getTarget()).thenReturn("/index");
        when(req.getPath()).thenReturn("/");
        Route route = mock(Route.class);
        when(route.pathParameterElements()).thenReturn(emptyPathParamList());
        when(req.getRoute()).thenReturn(route);
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: /index", 
                ResponseBodyInterpolator.interpolate("Message: [request.target]", req));
    }
    
    /*------------------------------------------------*/
    
    private List<PathParameterElement> emptyPathParamList() {
        return new ArrayList<PathParameterElement>();
    }
    
    private InputStream body(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
