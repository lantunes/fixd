package org.bigtesting.fixd.tests;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.session.PathParamSessionHandler;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.fixd.session.SessionHandler;
import org.junit.Test;
import org.simpleframework.http.Path;
import org.simpleframework.http.Request;

public class TestPathParamSessionHandler {

    @Test
    public void onCreateIsHandled() {
        
        Request request = mock(Request.class);
        Path path = mock(Path.class);
        when(path.getPath()).thenReturn("/first-name/John/last-name/Doe");
        when(request.getPath()).thenReturn(path);
        
        Session session = new Session();
        
        Route route = new Route("/first-name/:firstName/last-name/:lastName");
        
        newSessionHandler().onCreate(request, route, session);
        
        List<String> attributeNames = new ArrayList<String>(session.getAttributeNames());
        Collections.sort(attributeNames);
        
        assertEquals("[firstName, lastName]", attributeNames.toString());
        
        assertEquals("John", session.get("firstName"));
        assertEquals("Doe", session.get("lastName"));
    }
    
    @Test
    public void onCreateIsHandledWithNoParams() {
        
        Request request = mock(Request.class);
        Path path = mock(Path.class);
        when(path.getPath()).thenReturn("/");
        when(request.getPath()).thenReturn(path);
        
        Session session = new Session();
        
        Route route = new Route("/");
        
        newSessionHandler().onCreate(request, route, session);
        
        assertEquals("[]", session.getAttributeNames().toString());
    }
    
    private SessionHandler newSessionHandler() {
        return new PathParamSessionHandler();
    }
}
