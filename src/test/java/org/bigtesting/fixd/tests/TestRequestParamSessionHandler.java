package org.bigtesting.fixd.tests;

import static org.junit.Assert.*;
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.session.RequestParamSessionHandler;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
import org.junit.Test;

public class TestRequestParamSessionHandler {

    @Test
    public void onCreateIsHandled() {
        
        HttpRequest request = mock(HttpRequest.class);
        Set<String> params = new HashSet<String>();
        params.add("firstName");
        params.add("lastName");
        when(request.getParameterNames()).thenReturn(params);
        when(request.getParameter("firstName")).thenReturn("John");
        when(request.getParameter("lastName")).thenReturn("Doe");
        
        Session session = new Session();
        
        Route route = new Route("/");
        
        newSessionHandler().onCreate(request, route, session);
        
        List<String> attributeNames = new ArrayList<String>(session.getAttributeNames());
        Collections.sort(attributeNames);
        
        assertEquals("[firstName, lastName]", attributeNames.toString());
        
        assertEquals("John", session.get("firstName"));
        assertEquals("Doe", session.get("lastName"));
    }
    
    @Test
    public void onCreateIsHandledWithNoParams() {
        
        HttpRequest request = mock(HttpRequest.class);
        when(request.getParameterNames()).thenReturn(new HashSet<String>());
        when(request.getParameter(anyString())).thenReturn(null);
        
        Session session = new Session();
        
        Route route = new Route("/");
        
        newSessionHandler().onCreate(request, route, session);
        
        assertEquals("[]", session.getAttributeNames().toString());
    }
    
    private SessionHandler newSessionHandler() {
        return new RequestParamSessionHandler();
    }
}