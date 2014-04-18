package org.bigtesting.fixd.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.session.PathParamSessionHandler;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
import org.junit.Test;

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
