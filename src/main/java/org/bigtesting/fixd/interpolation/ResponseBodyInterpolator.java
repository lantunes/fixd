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
package org.bigtesting.fixd.interpolation;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.session.Session;
import org.bigtesting.routd.NamedParameterElement;
import org.bigtesting.routd.RouteHelper;

/**
 * 
 * @author Luis Antunes
 */
public class ResponseBodyInterpolator {
    
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

    public static String interpolate(String body, HttpRequest request) {
        
        body = interpolatePathParamValues(body, request);
        
        if (request.getSession() != null) {
            body = interpolateSessionValues(body, request.getSession());
        }
        
        body = interpolateRequestValues(body, request);
        
        return body;
    }
    
    /*
     * handle any values that start with ':'
     */
    private static String interpolatePathParamValues(String body, HttpRequest req) {
        
        String[] pathTokens = RouteHelper.getPathElements(req.getPath());
        for (NamedParameterElement param : req.getRoute().getNamedParameterElements()) {
            String paramName = "\\Q" + param.name() + "\\E";
            body = body.replaceAll(":" + paramName, pathTokens[param.index()]);
        }
        
        return body;
    }
    
    /* 
     * handle any values that are enclosed in '{}'
     * - replacement values can consist of "{}"
     */
    private static String interpolateSessionValues(String body, final Session session) {
        
        return substituteGroups(body, SESSION_VALUE_PATTERN, new ValueProvider() {
            public Object getValue(String captured) {
                return session.get(captured);
            }
        });
    }
    
    private static String interpolateRequestValues(String body, final HttpRequest request) {
        
        return substituteGroups(body, REQUEST_VALUE_PATTERN, new ValueProvider() {
            public Object getValue(String captured) {
                
                if (captured.startsWith("request?")) {
                    return request.getRequestParameter(captured.replaceFirst("request\\?", ""));
                }
                
                if (captured.startsWith("request$")) {
                    return request.getHeaderValue(captured.replaceFirst("request\\$", ""));
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
