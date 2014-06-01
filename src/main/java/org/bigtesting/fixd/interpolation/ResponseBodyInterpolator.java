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

import org.bigtesting.fixd.interpolation.lib.Interpolator;
import org.bigtesting.fixd.interpolation.lib.Substitutor;
import org.bigtesting.fixd.request.HttpRequest;

/**
 * 
 * @author Luis Antunes
 */
public class ResponseBodyInterpolator {
    
    private static final Interpolator interpolator = new Interpolator();
    
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
        
        interpolator.when("[a-zA-Z0-9_]").prefixedWith(":").handleWith(new Substitutor() {
            public String substitute(String captured, Object arg) {
                
                HttpRequest req = (HttpRequest)arg;
                String path = req.getUndecodedPath();
                return req.getRoute().getNamedParameter(captured, path);
            }
        });
        
        interpolator.when("[0-9]").enclosedBy("*[").and("]").handleWith(new Substitutor() {
            public String substitute(String captured, Object arg) {
                
                HttpRequest req = (HttpRequest)arg;
                String path = req.getUndecodedPath();
                try {
                    int i = Integer.parseInt(captured);
                    String[] splat = req.getRoute().splat(path);
                    //TODO route.getSplatParameter() should return null if index does not exist
                    if (i > splat.length - 1) {
                        return null;
                    }
                    return req.getRoute().splat(path)[i];
                } catch (NumberFormatException e) {
                    return null;
                }
            }
        });
        
        interpolator.when().enclosedBy("{").and("}").handleWith(new Substitutor() {
            public String substitute(String captured, Object arg) {
                
                HttpRequest req = (HttpRequest)arg;
                if (req.getSession() != null) {
                    Object val = req.getSession().get(captured);
                    if (val != null) {
                        return val.toString();
                    }
                }
                return null;
            }
        });
        
        interpolator.when().enclosedBy("[").and("]").handleWith(new Substitutor() {
            public String substitute(String captured, Object arg) {
                
                HttpRequest req = (HttpRequest)arg;
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
    }

    public static String interpolate(String body, HttpRequest request) {
        
        return interpolator.interpolate(body, request);
    }
}
