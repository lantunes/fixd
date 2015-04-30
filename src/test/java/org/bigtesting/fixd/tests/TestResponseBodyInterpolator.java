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

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import org.bigtesting.fixd.interpolation.ResponseBodyInterpolator;
import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.session.Session;
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
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello World!", 
                ResponseBodyInterpolator.interpolate("Hello World!", req));
    }
    
    @Test
    public void testBodyReturnedUnmodifiedWhenNoInstructionsGiven_WithSession() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello World!", 
                ResponseBodyInterpolator.interpolate("Hello World!", req));
    }
    
    @Test
    public void testSinglePathParamSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello :name", req));
    }
    
    @Test
    public void testSinglePathParamSubstituedInPresenceOfOtherColons() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello : Tim :", 
                ResponseBodyInterpolator.interpolate("Hello : :name :", req));
    }
    
    @Test
    public void testSinglePathParamDoublyPrefixedIsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :Tim", 
                ResponseBodyInterpolator.interpolate("Hello ::name", req));
    }
    
    @Test
    public void testSinglePathParamEnclosedByColonsIsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :Tim:", 
                ResponseBodyInterpolator.interpolate("Hello ::name:", req));
    }
    
    @Test
    public void testSinglePathParamSubstituedInPresenceOfOtherColonPrefixedTerms() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :someTerm Tim", 
                ResponseBodyInterpolator.interpolate("Hello :someTerm :name", req));
    }
    
    @Test
    public void testSinglePathParamSubstituedWithColonContainingValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/:Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :Tim", 
                ResponseBodyInterpolator.interpolate("Hello :name", req));
    }
    
    @Test
    public void testSimilarPathParamsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim/John");
        when(req.getRoute()).thenReturn(new Route("/name/:name/:name1"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim John", 
                ResponseBodyInterpolator.interpolate("Hello :name :name1", req));
    }
    
    @Test
    public void testSimilarPathParamNotSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :name1", 
                ResponseBodyInterpolator.interpolate("Hello :name1", req));
    }
    
    @Test
    public void testPathParamSubstitutedWithNameOfAnotherPathParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/:id/1");
        when(req.getRoute()).thenReturn(new Route("/name/:name/:id"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :id 1", 
                ResponseBodyInterpolator.interpolate("Hello :name :id", req));
    }
    
    @Test
    public void testMultiplePathParamsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/:firstName/:lastName"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John Doe", 
                ResponseBodyInterpolator.interpolate("Hello :firstName :lastName", req));
    }
    
    @Test
    public void testMultiplePathParamsSubstituedInPresenceOfOtherColons() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/:firstName/:lastName"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello : John : Doe :", 
                ResponseBodyInterpolator.interpolate("Hello : :firstName : :lastName :", req));
    }
    
    @Test
    public void testMultiplePathParamsSubstituedWithColonContainingValues() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/:John/:Doe");
        when(req.getRoute()).thenReturn(new Route("/name/:firstName/:lastName"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :John :Doe", 
                ResponseBodyInterpolator.interpolate("Hello :firstName :lastName", req));
    }
    
    @Test
    public void testSingleSessionValueSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello {name}", req));
    }
    
    @Test
    public void testSessionValueWithNamedParamNameNotSubstituted() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        Session session = new Session();
        session.set("name", ":name");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello :name", 
                ResponseBodyInterpolator.interpolate("Hello {name}", req));
    }
    
    @Test
    public void testNamedParamWithSessionValueNameNotSubstituted() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/{name}");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {name}", 
                ResponseBodyInterpolator.interpolate("Hello :name", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraces() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello { Tim } {", 
                ResponseBodyInterpolator.interpolate("Hello { {name} } {", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraces_NoSpaces() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {Tim} {", 
                ResponseBodyInterpolator.interpolate("Hello {{name}} {", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraceEnclosedTerms() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {there} Tim", 
                ResponseBodyInterpolator.interpolate("Hello {there} {name}", req));
    }
    
    @Test
    public void testSingleSessionValueSubstituedWithBraceEnclosedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "{Tim}");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {Tim}", 
                ResponseBodyInterpolator.interpolate("Hello {name}", req));
    }
    
    @Test
    public void testMultipleSessionValuesSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
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
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
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
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
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
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
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
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
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
        when(req.getUndecodedPath()).thenReturn("/greet/Mr.");
        when(req.getRoute()).thenReturn(new Route("/greet/:salutation"));
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
        when(req.getUndecodedPath()).thenReturn("/greet/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/greet/:firstName/:lastName"));
        Session session = new Session();
        session.set("salutation", "Mr.");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Mr. John Doe", 
                ResponseBodyInterpolator.interpolate("Hello {salutation} :firstName :lastName", req));
    }
    
    @Test
    public void testColonPrefixedSessionValue() {
        
        /*
         * In this test, the result is not "Hello John" because
         * :{name} is not a valid prefixed variable, as its name
         * contains non-alphanumeric characters.
         */
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John");
        when(req.getRoute()).thenReturn(new Route("/name/:{name}"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello :Tim", 
                ResponseBodyInterpolator.interpolate("Hello :{name}", req));
    }
    
    @Test
    public void testBraceEnclosedPathParamValueSubstitutesSessionValue_OneOccurrence() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        Session session = new Session();
        session.set(":name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello {:name}", req));
    }
    
    @Test
    public void testBraceEnclosedPathParamValueSubstitutesSessionValue_TwoOccurrences() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/:first/:last"));
        Session session = new Session();
        session.set(":first", "Tom");
        session.set(":last", "Jerry");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Tom Jerry", 
                ResponseBodyInterpolator.interpolate("Hello {:first} {:last}", req));
    }
    
    @Test
    public void testRequestUnknownValuesIgnored() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: [some.value]", 
                ResponseBodyInterpolator.interpolate("Message: [some.value]", req));
    }
    
    @Test
    public void testRequestBodySubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: Hello World!", 
                ResponseBodyInterpolator.interpolate("Message: [request.body]", req));
    }
    
    @Test
    public void testRequestBodySubstituedInPresenceOfOtherBrackets() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: [ Hello World! ] [", 
                ResponseBodyInterpolator.interpolate("Message: [ [request.body] ] [", req));
    }
    
    @Test
    public void testRequestBodySubstituedInPresenceOfOtherBrackets_NoSpaces() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Hello World!"));
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: [Hello World!] [", 
                ResponseBodyInterpolator.interpolate("Message: [[request.body]] [", req));
    }
    
    @Test
    public void testRequestBodySubstituedInPresenceOfOtherBracketEnclosedTerms() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Tim"));
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [there] Tim", 
                ResponseBodyInterpolator.interpolate("Hello [there] [request.body]", req));
    }
    
    @Test
    public void testRequestBodySubstituedWithBracketEnclosedValue() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("[Tim]"));
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [Tim]", 
                ResponseBodyInterpolator.interpolate("Hello [request.body]", req));
    }
    
    @Test
    public void testPathParamAndSessionAndRequestUsedTogether() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getBodyAsStream()).thenReturn(body("Doe"));
        when(req.getUndecodedPath()).thenReturn("/name/John");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
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
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello [request?name]", req));
    }
    
    @Test
    public void testRequestHeaderSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getHeaderValue("User-Agent")).thenReturn("Mozilla/5.0");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Value: Mozilla/5.0", 
                ResponseBodyInterpolator.interpolate("Value: [request$User-Agent]", req));
    }
    
    @Test
    public void testRequestMethodSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getMethod()).thenReturn("GET");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: GET", 
                ResponseBodyInterpolator.interpolate("Message: [request.method]", req));
    }
    
    @Test
    public void testRequestTimeSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getTime()).thenReturn(123L);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: 123", 
                ResponseBodyInterpolator.interpolate("Message: [request.time]", req));
    }
    
    @Test
    public void testRequestPathSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/abc");
        when(req.getPath()).thenReturn("/abc");
        when(req.getRoute()).thenReturn(new Route("/abc"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: /abc", 
                ResponseBodyInterpolator.interpolate("Message: [request.path]", req));
    }
    
    @Test
    public void testRequestQuerySubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getQuery()).thenReturn("a=b&c=d");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: a=b&c=d", 
                ResponseBodyInterpolator.interpolate("Message: [request.query]", req));
    }
    
    @Test
    public void testRequestMajorSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getMajor()).thenReturn(123);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: 123", 
                ResponseBodyInterpolator.interpolate("Message: [request.major]", req));
    }
    
    @Test
    public void testRequestMinorSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getMinor()).thenReturn(123);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: 123", 
                ResponseBodyInterpolator.interpolate("Message: [request.minor]", req));
    }
    
    @Test
    public void testRequestTargetSubstituted() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getTarget()).thenReturn("/index");
        when(req.getUndecodedPath()).thenReturn("/index");
        when(req.getRoute()).thenReturn(new Route("/index"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Message: /index", 
                ResponseBodyInterpolator.interpolate("Message: [request.target]", req));
    }
    
    @Test
    public void testSingleSplatParamSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello *[0]", req));
    }
    
    @Test
    public void testSingleSplatParamSubstituedMultipleTimes() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Tim Tim", 
                ResponseBodyInterpolator.interpolate("Hello *[0] *[0]", req));
    }
    
    @Test
    public void testSingleSplatParamNotSubstituedWithIncorrectIndex() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[1]", 
                ResponseBodyInterpolator.interpolate("Hello *[1]", req));
    }
    
    @Test
    public void testSingleSplatParamNotSubstituedIfFormattedIncorrectly() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[ 0]", 
                ResponseBodyInterpolator.interpolate("Hello *[ 0]", req));
    }
    
    @Test
    public void testSingleSplatParamSubstituedWithSplatContainingValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/*[0]");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[0]", 
                ResponseBodyInterpolator.interpolate("Hello *[0]", req));
    }
    
    @Test
    public void testSingleSplatParamSubstituedWithEmptyValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ", 
                ResponseBodyInterpolator.interpolate("Hello *[0]", req));
    }
    
    @Test
    public void testSplatParamSubstituedWithDoubleDigitIndex() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/a/b/c/d/e/f/g/h/i/j/k");
        when(req.getRoute()).thenReturn(new Route("/name/*/*/*/*/*/*/*/*/*/*/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello k", 
                ResponseBodyInterpolator.interpolate("Hello *[10]", req));
    }
    
    @Test
    public void testTwoSplatParamsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/*/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John Doe", 
                ResponseBodyInterpolator.interpolate("Hello *[0] *[1]", req));
    }
    
    @Test
    public void testOnlySecondOfTwoSplatParamsSubstitued() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/*/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello Doe", 
                ResponseBodyInterpolator.interpolate("Hello *[1]", req));
    }
    
    @Test
    public void testSplatParamSubsitutedInPresenceOfOtherParamTypes() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/say/hello/to/John/1/x");
        when(req.getRoute()).thenReturn(new Route("/say/*/to/:name/:times<[0-9]+>/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("hello x", 
                ResponseBodyInterpolator.interpolate("*[0] *[1]", req));
    }
    
    @Test
    public void testSubstitutionIncreasesOverallLengthOfString() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/hello/there");
        when(req.getRoute()).thenReturn(new Route("/*/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("hello there", 
                ResponseBodyInterpolator.interpolate("*[0] *[1]", req));
    }
    
    @Test
    public void testEscapeWithSingleNamedParamWhichExists() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :name", 
                ResponseBodyInterpolator.interpolate("Hello ^:name", req));
    }
    
    @Test
    public void testEscapeWithSingleNamedParamWhichDoesNotExist() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:id"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :name", 
                ResponseBodyInterpolator.interpolate("Hello ^:name", req));
    }
    
    @Test
    public void testEscapeWithMultipleNamedParams_OneEscaped() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/:name1/:name2"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :name1 Doe", 
                ResponseBodyInterpolator.interpolate("Hello ^:name1 :name2", req));
    }
    
    @Test
    public void testEscapeWithMultipleNamedParams_MultipleEscaped() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/:name1/:name2"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :name1 :name2", 
                ResponseBodyInterpolator.interpolate("Hello ^:name1 ^:name2", req));
    }
    
    @Test
    public void testEscapeWithMultipleNamedParams_MultipleEscapedConcatenated() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/:name1/:name2"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello :name1:name2", 
                ResponseBodyInterpolator.interpolate("Hello ^:name1^:name2", req));
    }
    
    @Test
    public void testDoubleEscapeWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^John", 
                ResponseBodyInterpolator.interpolate("Hello ^^:name", req));
    }
    
    @Test
    public void testTripleEscapeWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^:name", 
                ResponseBodyInterpolator.interpolate("Hello ^^^:name", req));
    }
    
    @Test
    public void testQuadrupleEscapeWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^John", 
                ResponseBodyInterpolator.interpolate("Hello ^^^^:name", req));
    }
    
    @Test
    public void testEscapeOnItsOwn() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^ there", 
                ResponseBodyInterpolator.interpolate("Hello ^ there", req));
    }
    
    @Test
    public void testDoubleEscapeOnItsOwn() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^ there", 
                ResponseBodyInterpolator.interpolate("Hello ^^ there", req));
    }
    
    @Test
    public void testTripleEscapeOnItsOwn() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^^ there", 
                ResponseBodyInterpolator.interpolate("Hello ^^^ there", req));
    }
    
    @Test
    public void testQuadrupleEscapeOnItsOwn() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^^^ there", 
                ResponseBodyInterpolator.interpolate("Hello ^^^^ there", req));
    }
    
    @Test
    public void testEscapeOnItsOwnWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^ John", 
                ResponseBodyInterpolator.interpolate("Hello ^ :name", req));
    }
    
    @Test
    public void testDoubleEscapeOnItsOwnWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^ John", 
                ResponseBodyInterpolator.interpolate("Hello ^^ :name", req));
    }
    
    @Test
    public void testTripleEscapeOnItsOwnWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^^ John", 
                ResponseBodyInterpolator.interpolate("Hello ^^^ :name", req));
    }
    
    @Test
    public void testQuadrupleEscapeOnItsOwnWithNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^^^ John", 
                ResponseBodyInterpolator.interpolate("Hello ^^^^ :name", req));
    }
    
    @Test
    public void testEscapeNextToNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^there", 
                ResponseBodyInterpolator.interpolate("Hello ^there", req));
    }
    
    @Test
    public void testDoubleEscapeNextToNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^there", 
                ResponseBodyInterpolator.interpolate("Hello ^^there", req));
    }
    
    @Test
    public void testTripleEscapeNextToNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^^there", 
                ResponseBodyInterpolator.interpolate("Hello ^^^there", req));
    }
    
    @Test
    public void testQuadrupleEscapeNextToNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello ^^^^there", 
                ResponseBodyInterpolator.interpolate("Hello ^^^^there", req));
    }
    
    @Test
    public void testEscapeAfterNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John^", 
                ResponseBodyInterpolator.interpolate("Hello :name^", req));
    }
    
    @Test
    public void testDoubleEscapeAfterNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John^^", 
                ResponseBodyInterpolator.interpolate("Hello :name^^", req));
    }
    
    @Test
    public void testTripleEscapeAfterNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John^^^", 
                ResponseBodyInterpolator.interpolate("Hello :name^^^", req));
    }
    
    @Test
    public void testQuadrupleEscapeAfterNamedParam() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello John^^^^", 
                ResponseBodyInterpolator.interpolate("Hello :name^^^^", req));
    }
    
    @Test
    public void testEscapeAfterNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello there^", 
                ResponseBodyInterpolator.interpolate("Hello there^", req));
    }
    
    @Test
    public void testDoubleEscapeAfterNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello there^^", 
                ResponseBodyInterpolator.interpolate("Hello there^^", req));
    }
    
    @Test
    public void testTripleEscapeAfterNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello there^^^", 
                ResponseBodyInterpolator.interpolate("Hello there^^^", req));
    }
    
    @Test
    public void testQuadrupleEscapeAfterNonInterpolatedValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello there^^^^", 
                ResponseBodyInterpolator.interpolate("Hello there^^^^", req));
    }
    
    @Test
    public void testEscapeIgnoredInsideSessionValue() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("^name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello Tim", 
                ResponseBodyInterpolator.interpolate("Hello {^name}", req));
    }
    
    @Test
    public void testEscapeWithSingleSessionValueWhichExists() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {name}", 
                ResponseBodyInterpolator.interpolate("Hello ^{name}", req));
    }
    
    @Test
    public void testEscapeWithSingleSessionValueWhichDoesNotExist() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("id", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {name}", 
                ResponseBodyInterpolator.interpolate("Hello ^{name}", req));
    }
    
    @Test
    public void testEscapeWithMultipleSessionValues_OneEscaped() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {firstName} Doe", 
                ResponseBodyInterpolator.interpolate("Hello ^{firstName} {lastName}", req));
    }
    
    @Test
    public void testEscapeWithMultipleSessionValues_MultipleEscaped() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {firstName} {lastName}", 
                ResponseBodyInterpolator.interpolate("Hello ^{firstName} ^{lastName}", req));
    }
    
    @Test
    public void testEscapeWithSingleRequestValueWhichExists() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getRequestParameter("name")).thenReturn("Tim");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [request?name]", 
                ResponseBodyInterpolator.interpolate("Hello ^[request?name]", req));
    }
    
    @Test
    public void testEscapeWithSingleRequestValueWhichDoesNotExist() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getRequestParameter("id")).thenReturn("Tim");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [request?name]", 
                ResponseBodyInterpolator.interpolate("Hello ^[request?name]", req));
    }
    
    @Test
    public void testEscapeWithMultipleRequestValues_OneEscaped() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getRequestParameter("name1")).thenReturn("John");
        when(req.getRequestParameter("name2")).thenReturn("Doe");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [request?name1] Doe", 
                ResponseBodyInterpolator.interpolate("Hello ^[request?name1] [request?name2]", req));
    }
    
    @Test
    public void testEscapeWithMultipleRequestValues_MultipleEscaped() throws Exception {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getRequestParameter("name1")).thenReturn("John");
        when(req.getRequestParameter("name2")).thenReturn("Doe");
        when(req.getUndecodedPath()).thenReturn("/");
        when(req.getRoute()).thenReturn(new Route("/"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello [request?name1] [request?name2]", 
                ResponseBodyInterpolator.interpolate("Hello ^[request?name1] ^[request?name2]", req));
    }
    
    @Test
    public void testEscapeWithSingleSplatParamWhichExists() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[0]", 
                ResponseBodyInterpolator.interpolate("Hello ^*[0]", req));
    }
    
    @Test
    public void testEscapeWithSingleSplatParamWhichDoesNotExist() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/Tim");
        when(req.getRoute()).thenReturn(new Route("/name/:name"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[0]", 
                ResponseBodyInterpolator.interpolate("Hello ^*[0]", req));
    }
    
    @Test
    public void testEscapeWithMultipleSplatParams_OneEscaped() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/*/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[0] Doe", 
                ResponseBodyInterpolator.interpolate("Hello ^*[0] *[1]", req));
    }
    
    @Test
    public void testEscapeWithMultipleSplatParams_MultipleEscaped() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/name/John/Doe");
        when(req.getRoute()).thenReturn(new Route("/name/*/*"));
        when(req.getSession()).thenReturn(null);
        
        assertEquals("Hello *[0] *[1]", 
                ResponseBodyInterpolator.interpolate("Hello ^*[0] ^*[1]", req));
    }
    
    @Test
    public void testEscapeSessionValueWithNamedParamName() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/John");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        Session session = new Session();
        session.set("name", ":name");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello {name}", 
                ResponseBodyInterpolator.interpolate("Hello ^{name}", req));
    }
    
    @Test
    public void testEscapeNamedParamWithSessionValueName() {
        
        HttpRequest req = mock(HttpRequest.class);
        when(req.getUndecodedPath()).thenReturn("/{name}");
        when(req.getRoute()).thenReturn(new Route("/:name"));
        Session session = new Session();
        session.set("name", "Tim");
        when(req.getSession()).thenReturn(session);
        
        assertEquals("Hello :name", 
                ResponseBodyInterpolator.interpolate("Hello ^:name", req));
    }
    
    /*------------------------------------------------*/
    
    private InputStream body(String content) {
        return new ByteArrayInputStream(content.getBytes());
    }
}
