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
package org.bigtesting.fixd.tests;

import static org.junit.Assert.*;

import org.bigtesting.fixd.ServerFixture;
import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.internal.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.ning.http.client.AsyncHttpClient;

/**
 * 
 * @author Luis Antunes
 */
public class TestCapturedRequest {

    private ServerFixture server;
    
    @Before
    public void beforeEachTest() throws Exception {
        server = new ServerFixture(8080);
        server.start();
    }
    
    @Test
    public void testGetPath_Root() throws Exception {
    
        server.handle(Method.GET, "/")
              .with(200, "text/plain", "Hello");
  
        new AsyncHttpClient()
              .prepareGet("http://localhost:8080/")
              .execute()
              .get();
        
        CapturedRequest captured = server.request();
        assertEquals("/", captured.getPath());
    }
    
    @Test
    public void testGetPath_SomePath() throws Exception {
    
        server.handle(Method.GET, "/some/path")
              .with(200, "text/plain", "Hello");
  
        new AsyncHttpClient()
              .prepareGet("http://localhost:8080/some/path")
              .execute()
              .get();
        
        CapturedRequest captured = server.request();
        assertEquals("/some/path", captured.getPath());
    }
    
    @Test
    public void testGetPath_SomePathWithParam() throws Exception {
    
        server.handle(Method.GET, "/some/path/:param")
              .with(200, "text/plain", "Hello");
  
        new AsyncHttpClient()
              .prepareGet("http://localhost:8080/some/path/123")
              .execute()
              .get();
        
        CapturedRequest captured = server.request();
        assertEquals("/some/path/123", captured.getPath());
    }
    
    @Test
    public void testGetRequestLine() throws Exception {
        
        server.handle(Method.GET, "/")
              .with(200, "text/plain", "Hello");

        new AsyncHttpClient()
              .prepareGet("http://localhost:8080/")
              .execute()
              .get();

        CapturedRequest captured = server.request();
        assertEquals("GET / HTTP/1.1", captured.getRequestLine());
    }
    
    @Test
    public void testGetMethod() throws Exception {
        
        server.handle(Method.GET, "/")
              .with(200, "text/plain", "Hello");

        new AsyncHttpClient()
              .prepareGet("http://localhost:8080/")
              .execute()
              .get();

        CapturedRequest captured = server.request();
        assertEquals("GET", captured.getMethod());    
    }
    
    @Test
    public void testGetHeaders() throws Exception {
        
        server.handle(Method.GET, "/")
              .with(200, "text/plain", "Hello");

        new AsyncHttpClient()
              .prepareGet("http://localhost:8080/")
              .execute()
              .get();

        CapturedRequest captured = server.request();
        assertEquals("[Host: localhost:8080, " +
        		"Connection: keep-alive, " +
        		"Accept: */*, " +
        		"User-Agent: NING/1.0]", 
        		captured.getHeaders().toString());
    }
    
    @Test
    public void testGetBody() throws Exception {
        
        server.handle(Method.PUT, "/")
              .with(200, "text/plain", "Hello");

        byte[] body = "Hello".getBytes();
        
        new AsyncHttpClient()
              .preparePut("http://localhost:8080/")
              .setBody(body)
              .execute()
              .get();

        CapturedRequest captured = server.request();
        assertArrayEquals(body, captured.getBody());    
    }
    
    @Test
    public void testGetEncodedBody() throws Exception {
        
        server.handle(Method.PUT, "/")
              .with(200, "text/plain", "Hello");

        String unicodeContainingBody = "A\u00ea\u00f1\u00fC";
        
        new AsyncHttpClient()
              .preparePut("http://localhost:8080/")
              .setBody(unicodeContainingBody)
              .execute()
              .get();

        CapturedRequest captured = server.request();
        assertEquals(
                new String(new char[]{'A','\uFFFD','\uFFFD','\uFFFD'}), 
                captured.getBody("US-ASCII"));
    }
    
    @After
    public void afterEachTest() throws Exception {
        server.stop();
    }
}
