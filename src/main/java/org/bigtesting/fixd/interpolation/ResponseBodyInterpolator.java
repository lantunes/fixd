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

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.interpolatd.Interpolator;
import org.bigtesting.interpolatd.Substitutor;

/**
 * 
 * @author Luis Antunes
 */
public class ResponseBodyInterpolator {
    
    private static final Interpolator<HttpRequest> interpolator = new Interpolator<HttpRequest>();
    
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
        
        interpolator.when("[a-zA-Z0-9_]+").prefixedBy(":")
            .handleWith(new Substitutor<HttpRequest>() {
                public String substitute(String captured, HttpRequest req) {
                    
                    String path = req.getUndecodedPath();
                    return req.getRoute().getNamedParameter(captured, path);
                }
            });
        
        interpolator.when("[0-9]+").enclosedBy("*[").and("]")
            .handleWith(new Substitutor<HttpRequest>() {
                public String substitute(String captured, HttpRequest req) {
                    
                    String path = req.getUndecodedPath();
                    int index = Integer.parseInt(captured);
                    return req.getRoute().getSplatParameter(index, path);
                }
            });
        
        interpolator.when().enclosedBy("{").and("}")
            .handleWith(new Substitutor<HttpRequest>() {
                public String substitute(String captured, HttpRequest req) {
                    
                    if (req.getSession() != null) {
                        Object val = req.getSession().get(captured);
                        if (val != null) {
                            return val.toString();
                        }
                    }
                    return null;
                }
            });
        
        interpolator.when().enclosedBy("[").and("]")
            .handleWith(new Substitutor<HttpRequest>() {
                public String substitute(String captured, HttpRequest req) {
                    
                    if (captured.startsWith("request?")) {
                        return req.getRequestParameter(captured.replaceFirst("request\\?", ""));
                    }
    
                    if (captured.startsWith("request$")) {
                        return req.getHeaderValue(captured.replaceFirst("request\\$", ""));
                    }
    
                    RequestValueProvider<?> requestValueProvider = requestValueProviders.get(captured);
                    if (requestValueProvider != null) {
                        Object val = requestValueProvider.getValue(req);
                        if (val != null) {
                            return val.toString();
                        }
                    }
                    return null;
                }
            });
        
        interpolator.escapeWith("^");
    }

    public static String interpolate(String body, HttpRequest request) {
        
        return interpolator.interpolate(body, request);
    }
}
