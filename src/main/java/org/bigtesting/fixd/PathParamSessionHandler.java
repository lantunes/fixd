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
package org.bigtesting.fixd;

import java.util.List;

import org.bigtesting.fixd.internal.Session;
import org.bigtesting.fixd.internal.SessionHandler;
import org.bigtesting.fixd.routing.Route;
import org.bigtesting.fixd.routing.Route.PathParameterElement;
import org.bigtesting.fixd.routing.RouteHelper;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class PathParamSessionHandler implements SessionHandler {

    public void onCreate(Request request, Route route, Session session) {
        
        String path = request.getPath().getPath();
        List<PathParameterElement> pathParams = route.pathParameterElements();
        String[] pathTokens = RouteHelper.getPathElements(path);
        
        for (PathParameterElement pathParam : pathParams) {
            session.set(pathParam.name(), pathTokens[pathParam.index()]);
        }
    }
}
