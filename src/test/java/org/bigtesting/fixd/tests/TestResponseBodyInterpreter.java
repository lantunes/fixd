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

import java.util.ArrayList;
import java.util.List;

import org.bigtesting.fixd.Session;
import org.bigtesting.fixd.routing.Route.PathParameterElement;
import org.bigtesting.fixd.util.ResponseBodyInterpreter;
import org.junit.Test;

/**
 * 
 * @author Luis Antunes
 */
public class TestResponseBodyInterpreter {

    @Test
    public void testBodyReturnedUnmodifiedWhenNoInstructionsGiven() {
        
        assertEquals("Hello World!", 
                ResponseBodyInterpreter.interpret("Hello World!", "/", 
                        emptyPathParamList(), null));
    }
    
    @Test
    public void testBodyReturnedUnmodifiedWhenNoInstructionsGiven_WithSession() {
        
        Session session = new Session();
        session.set("name", "Tim");
        
        assertEquals("Hello World!", 
                ResponseBodyInterpreter.interpret("Hello World!", "/", 
                        emptyPathParamList(), session));
    }
    
    @Test
    public void testSinglePathParamSubstitued() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :name", "/name/Tim", elements, null);
        
        assertEquals("Hello Tim", interpreted);
    }
    
    @Test
    public void testSinglePathParamSubstituedInPresenceOfOtherColons() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello : :name :", "/name/Tim", elements, null);
        
        assertEquals("Hello : Tim :", interpreted);
    }
    
    @Test
    public void testSinglePathParamDoublyPrefixedIsSubstitued() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello ::name", "/name/Tim", elements, null);
        
        assertEquals("Hello :Tim", interpreted);
    }
    
    @Test
    public void testSinglePathParamEnclosedByColonsIsSubstitued() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello ::name:", "/name/Tim", elements, null);
        
        assertEquals("Hello :Tim:", interpreted);
    }
    
    @Test
    public void testSinglePathParamSubstituedInPresenceOfOtherColonPrefixedTerms() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :someTerm :name", "/name/Tim", elements, null);
        
        assertEquals("Hello :someTerm Tim", interpreted);
    }
    
    @Test
    public void testSinglePathParamSubstituedWithColonContainingValue() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :name", "/name/:Tim", elements, null);
        
        assertEquals("Hello :Tim", interpreted);
    }
    
    @Test
    public void testMultiplePathParamsSubstitued() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1));
        elements.add(new PathParameterElement("lastName", 2));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :firstName :lastName", "/name/John/Doe", elements, null);
        
        assertEquals("Hello John Doe", interpreted);
    }
    
    @Test
    public void testMultiplePathParamsSubstituedInPresenceOfOtherColons() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1));
        elements.add(new PathParameterElement("lastName", 2));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello : :firstName : :lastName :", "/name/John/Doe", elements, null);
        
        assertEquals("Hello : John : Doe :", interpreted);
    }
    
    @Test
    public void testMultiplePathParamsSubstituedWithColonContainingValues() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1));
        elements.add(new PathParameterElement("lastName", 2));
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :firstName :lastName", "/name/:John/:Doe", elements, null);
        
        assertEquals("Hello :John :Doe", interpreted);
    }
    
    @Test
    public void testSingleSessionValueSubstitued() {
        
        Session session = new Session();
        session.set("name", "Tim");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {name}", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello Tim", interpreted);
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraces() {
        
        Session session = new Session();
        session.set("name", "Tim");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello { {name} } {", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello { Tim } {", interpreted);
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraces_NoSpaces() {
        
        Session session = new Session();
        session.set("name", "Tim");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {{name}} {", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello {Tim} {", interpreted);
    }
    
    @Test
    public void testSingleSessionValueSubstituedInPresenceOfOtherBraceEnclosedTerms() {
        
        Session session = new Session();
        session.set("name", "Tim");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {there} {name}", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello {there} Tim", interpreted);
    }
    
    @Test
    public void testSingleSessionValueSubstituedWithBraceEnclosedValue() {
        
        Session session = new Session();
        session.set("name", "{Tim}");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {name}", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello {Tim}", interpreted);
    }
    
    @Test
    public void testMultipleSessionValuesSubstitued() {
        
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {firstName} {lastName}", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello John Doe", interpreted);
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedInPresenceOfOtherBraces() {
        
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello { {firstName} } { {lastName} } {", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello { John } { Doe } {", interpreted);
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedInPresenceOfOtherBraces_NoSpaces() {
        
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {{firstName}} {{lastName}} {", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello {John} {Doe} {", interpreted);
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedInPresenceOfOtherBraceEnclosedTerms() {
        
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {there} {firstName} {lastName}", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello {there} John Doe", interpreted);
    }
    
    @Test
    public void testMultipleSessionValuesSubstituedWithBraceEnclosedValue() {
        
        Session session = new Session();
        session.set("firstName", "{John}");
        session.set("lastName", "{Doe}");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {firstName} {lastName}", 
                "/", emptyPathParamList(), session);
        
        assertEquals("Hello {John} {Doe}", interpreted);
    }
    
    @Test
    public void testPathParamAndSessionValues_PathParamValuesComeFirst() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("salutation", 1));
        
        Session session = new Session();
        session.set("firstName", "John");
        session.set("lastName", "Doe");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :salutation {firstName} {lastName}", 
                "/greet/Mr.", elements, session);
        
        assertEquals("Hello Mr. John Doe", interpreted);
    }
    
    @Test
    public void testPathParamAndSessionValues_SessionValuesComeFirst() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("firstName", 1));
        elements.add(new PathParameterElement("lastName", 2));
        
        Session session = new Session();
        session.set("salutation", "Mr.");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {salutation} :firstName :lastName", 
                "/greet/John/Doe", elements, session);
        
        assertEquals("Hello Mr. John Doe", interpreted);
    }
    
    @Test
    public void testColonPrefixedSessionValueNotHandled() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("{name}", 1));
        
        Session session = new Session();
        session.set("name", "Tim");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello :{name}", 
                "/name/John", elements, session);
        
        assertEquals("Hello John", interpreted);
    }
    
    @Test
    public void testBraceEnclosedPathParamValueDoesNotSubstituteSessionValue() {
        
        List<PathParameterElement> elements = emptyPathParamList();
        elements.add(new PathParameterElement("name", 1));
        
        Session session = new Session();
        session.set(":name", "Tim");
        
        String interpreted = ResponseBodyInterpreter.interpret(
                "Hello {:name}", 
                "/name/John", elements, session);
        
        assertEquals("Hello {John}", interpreted);
    }
    
    /*------------------------------------------------*/
    
    private List<PathParameterElement> emptyPathParamList() {
        return new ArrayList<PathParameterElement>();
    }
}
