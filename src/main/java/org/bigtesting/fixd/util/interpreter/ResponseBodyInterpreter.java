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
package org.bigtesting.fixd.util.interpreter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigtesting.fixd.routing.Route.PathParameterElement;
import org.bigtesting.fixd.routing.RouteHelper;
import org.bigtesting.fixd.session.Session;
import org.simpleframework.http.Request;

/**
 * 
 * @author Luis Antunes
 */
public class ResponseBodyInterpreter {
    
    private static final Pattern SESSION_VALUE_PATTERN = Pattern.compile("\\{([^{}]*)\\}");
    
    private static final Pattern REQUEST_VALUE_PATTERN = Pattern.compile("\\[([^\\[\\]]*)\\]");
    
    private static final Map<String, RequestValueProvider<?>> requestValueProviders = 
            new HashMap<String, RequestValueProvider<?>>();
    static {
        requestValueProviders.put("request.body", new RequestBodyValueProvider());
        requestValueProviders.put("request.method", new RequestMethodValueProvider());
        requestValueProviders.put("request.time", new RequestTimeValueProvider());
        requestValueProviders.put("request.path", new RequestPathValueProvider());
        requestValueProviders.put("request.query", new RequestQueryValueProvider());
        requestValueProviders.put("request.major", new RequestMajorValueProvider());
        requestValueProviders.put("request.minor", new RequestMinorValueProvider());
        requestValueProviders.put("request.target", new RequestTargetValueProvider());
    }

    public static String interpret(String body, String path, 
            List<PathParameterElement> pathParams, Session session, Request request) {
        
        body = interpretPathParamValues(body, path, pathParams);
        
        if (session != null) {
            body = interpretSessionValues(body, session);
        }
        
        body = interpretRequestValues(body, request);
        
        return body;
    }
    
    /*
     * handle any values that start with ':'
     */
    private static String interpretPathParamValues(String body, String path, 
            List<PathParameterElement> pathParams) {
        
        String[] pathTokens = RouteHelper.getPathElements(path);
        for (PathParameterElement param : pathParams) {
            String paramName = "\\Q" + param.name() + "\\E";
            body = body.replaceAll(":" + paramName, pathTokens[param.index()]);
        }
        
        return body;
    }
    
    /* 
     * handle any values that are enclosed in '{}'
     * - replacement values can consist of "{}"
     */
    private static String interpretSessionValues(String body, final Session session) {
        
        return substituteGroups(body, SESSION_VALUE_PATTERN, new ValueProvider() {
            public Object getValue(String captured) {
                return session.get(captured);
            }
        });
    }
    
    private static String interpretRequestValues(String body, final Request request) {
        
        return substituteGroups(body, REQUEST_VALUE_PATTERN, new ValueProvider() {
            public Object getValue(String captured) {
                
                if (captured.startsWith("request?")) {
                    return request.getParameter(captured.replaceFirst("request\\?", ""));
                }
                
                if (captured.startsWith("request$")) {
                    return request.getValue(captured.replaceFirst("request\\$", ""));
                }
                
                RequestValueProvider<?> requestValueProvider = 
                        requestValueProviders.get(captured);
                if (requestValueProvider != null) {
                    return requestValueProvider.getValue(request);
                }
                return null;
            }
        });
    }
    
    private static String substituteGroups(String body, Pattern pattern, 
            ValueProvider valueProvider) {
        
        Matcher m = pattern.matcher(body);
        StringBuilder result = new StringBuilder();
        int start = 0;
        while (m.find()) {
            String captured = m.group(1);
            Object val = valueProvider.getValue(captured);
            if (val != null) {
                String stringVal = val.toString();
                result.append(body.substring(start, m.start()));
                result.append(stringVal);
                start = m.end();
            }
        }
        result.append(body.substring(start));
        return result.toString();
    }
    
    private static interface ValueProvider {
        Object getValue(String captured);
    }
}
