package org.bigtesting.fixd;

import java.util.Set;

import org.bigtesting.fixd.core.Session;
import org.bigtesting.fixd.core.SessionHandler;
import org.bigtesting.fixd.routing.Route;
import org.simpleframework.http.Request;

public class RequestParamSessionHandler implements SessionHandler {

    public void onCreate(Request request, Route route, Session session) {

        Set<String> params = request.getQuery().keySet();
        
        for (String param : params) {
            session.set(param, request.getParameter(param));
        }
    }
}
