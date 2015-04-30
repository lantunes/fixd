/*
 * Copyright (C) 2015 BigTesting.org
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

import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.request.HttpRequestHandler;
import org.bigtesting.fixd.session.SessionHandler;

/**
 * 
 * @author Luis Antunes
 */
public interface RequestHandler {

    RequestHandler with(int statusCode, String contentType, String body);
    
    RequestHandler with(int statusCode, String contentType, Object entity);
    
    RequestHandler with(HttpRequestHandler customHandler);

    RequestHandler withSessionHandler(SessionHandler sessionHandler);
    
    RequestHandler withHeader(String name, String value);
    
    RequestHandler after(long delay, TimeUnit delayUnit);
    
    RequestHandler every(long period, TimeUnit periodUnit);
    
    RequestHandler every(long period, TimeUnit periodUnit, int times);
    
    RequestHandler withTimeout(long timeout, TimeUnit timeoutUnit);
    
    RequestHandler upon(Method method, String resource);
    
    RequestHandler upon(Method method, String resource, String contentType);
    
    RequestHandler withRedirect(String location);
    
    RequestHandler withRedirect(String location, int statusCode);
}
