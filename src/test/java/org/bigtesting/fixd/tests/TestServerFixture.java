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

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.PathParamSessionHandler;
import org.bigtesting.fixd.ServerFixture;
import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.core.Method;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.gargoylesoftware.htmlunit.HttpMethod;
import com.gargoylesoftware.htmlunit.Page;
import com.gargoylesoftware.htmlunit.WebClient;
import com.gargoylesoftware.htmlunit.WebRequest;
import com.ning.http.client.AsyncCompletionHandler;
import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.HttpResponseBodyPart;
import com.ning.http.client.Response;

/**
 * 
 * @author Luis Antunes
 */
public class TestServerFixture {

    private ServerFixture server;
    
    @Before
    public void beforeEachTest() throws Exception {
        /*
         * Instantiate the server fixture before each test.
         * It could also have been initialized as part of the
         * server field declaration above, as JUnit will 
         * instantiate this Test class for each of its tests.
         */
        server = new ServerFixture(8080);
        server.start();
    }
    
    @Test
    public void testSimpleGet() throws Exception {

        server.handle(Method.GET, "/")
              .with(200, "text/plain", "Hello");
        
        /* 
         * we're using the ning.com AsyncHttpClient, check it out: 
         * https://github.com/AsyncHttpClient/async-http-client 
         */
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/")
                        .execute()
                        .get();
       
        assertEquals("Hello", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithPathParam() throws Exception {

        server.handle(Method.GET, "/name/:name")
              .with(200, "text/plain", "Hello :name");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/name/Tim")
                        .execute()
                        .get();
       
        assertEquals("Hello Tim", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRegexPathParam() throws Exception {

        server.handle(Method.GET, "/name/:name<[A-Za-z]+>")
              .with(200, "text/plain", "Hello :name");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/name/Tim")
                        .execute()
                        .get();
        assertEquals("Hello Tim", resp.getResponseBody().trim());
        
        resp          = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/name/123")
                        .execute()
                        .get();
        assertEquals(404, resp.getStatusCode());
    }
    
    @Test
    public void testSimplePutWithRequestBody() throws Exception {

        server.handle(Method.PUT, "/name")
              .with(200, "text/plain", "Hello [request.body]");
       
        Response resp = new AsyncHttpClient()
                        .preparePut("http://localhost:8080/name")
                        .setBody("Tim")
                        .execute()
                        .get();
       
        assertEquals("Hello Tim", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRequestParameter() throws Exception {

        server.handle(Method.GET, "/greeting")
              .with(200, "text/plain", "Hello [request?name]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/greeting?name=Tim")
                        .execute()
                        .get();
       
        assertEquals("Hello Tim", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimplePostWithRequestParameter() throws Exception {

        server.handle(Method.POST, "/greeting", "application/x-www-form-urlencoded")
              .with(200, "text/plain", "Hello [request?name]");
       
        Response resp = new AsyncHttpClient()
                        .preparePost("http://localhost:8080/greeting")
                        .addParameter("name", "Tim")
                        .execute()
                        .get();
       
        assertEquals("Hello Tim", resp.getResponseBody().trim());
    }
    
    @Test
    public void testStatefulRequests() throws Exception {
        
        server.handle(Method.PUT, "/name/:name")
              .with(200, "text/plain", "OK")
              .withNewSession(new PathParamSessionHandler());
        
        server.handle(Method.GET, "/name")
              .with(200, "text/html", "Name: {name}");
        
        WebClient client = new WebClient();
        
        Page page = client.getPage(new WebRequest(new URL(
                "http://localhost:8080/name/Tim"), 
                HttpMethod.PUT));
        assertEquals(200, page.getWebResponse().getStatusCode());
        
        page      = client.getPage(new WebRequest(new URL(
                "http://localhost:8080/name"), 
                HttpMethod.GET));
        assertEquals("Name: Tim", page.getWebResponse().getContentAsString().trim());
    }
    
    @Test
    public void testDelay() throws Exception {
        
        server.handle(Method.GET, "/suspend")
              .with(200, "text/plain", "OK")
              .after(100, TimeUnit.SECONDS);

        try {
            
            new AsyncHttpClient()
                .prepareGet("http://localhost:8080/suspend")
                .execute()
                .get(1, TimeUnit.SECONDS);
            
            fail("request should have timed out after 1 second");
        
        } catch (Exception e) {}
    }
    
    @Test
    public void testEvery() throws Exception {
        
        server.handle(Method.GET, "/echo/:message")
              .with(200, "text/plain", "message: :message")
              .every(200, TimeUnit.MILLISECONDS, 2);
        
        final List<String> chunks = new ArrayList<String>();
        Future<Integer> f = new AsyncHttpClient()
            .prepareGet("http://localhost:8080/echo/hello")
            .execute(
              new AsyncCompletionHandler<Integer>() {
                  
                public Integer onCompleted(Response r) throws Exception {
                  return r.getStatusCode();
                }
                    
                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {                                  
                  String chunk = new String(bodyPart.getBodyPartBytes()).trim();
                  if (chunk.length() != 0) chunks.add(chunk);
                  return STATE.CONTINUE;
                }
            });
        
        assertEquals(200, (int)f.get());
        assertEquals("[message: hello, message: hello]", chunks.toString());
    }
    
    @Test
    public void testUpon() throws Exception {
        
        server.handle(Method.GET, "/subscribe")
              .with(200, "text/plain", "message: :message")
              .upon(Method.GET, "/broadcast/:message");
        
        final List<String> broadcasts = new ArrayList<String>();
        Future<Integer> f = new AsyncHttpClient()
            .prepareGet("http://localhost:8080/subscribe")
            .execute(
              new AsyncCompletionHandler<Integer>() {
                  
                public Integer onCompleted(Response r) throws Exception {
                  return r.getStatusCode();
                }
                    
                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {                                  
                  String chunk = new String(bodyPart.getBodyPartBytes()).trim();
                  if (chunk.length() != 0) broadcasts.add(chunk);
                  return STATE.CONTINUE;
                }
            });
        
        for (int i = 0; i < 2; i++) {
            
            new AsyncHttpClient()
                .prepareGet("http://localhost:8080/broadcast/hello" + i)
                .execute().get();
            
            /* sometimes the last broadcast request is not
             * finished before f.cancel() is called */
            Thread.sleep(50);
        }
        
        f.cancel(false);
        assertEquals("[message: hello0, message: hello1]", broadcasts.toString());
    }
    
    @Test
    public void testUponUsingRequestBody() throws Exception {
        
        server.handle(Method.GET, "/subscribe")
              .with(200, "text/plain", "message: [request.body]")
              .upon(Method.PUT, "/broadcast");
        
        final List<String> broadcasts = new ArrayList<String>();
        Future<Integer> f = new AsyncHttpClient()
            .prepareGet("http://localhost:8080/subscribe")
            .execute(
              new AsyncCompletionHandler<Integer>() {
                  
                public Integer onCompleted(Response r) throws Exception {
                  return r.getStatusCode();
                }
                    
                public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {                                  
                  String chunk = new String(bodyPart.getBodyPartBytes()).trim();
                  if (chunk.length() != 0) broadcasts.add(chunk);
                  return STATE.CONTINUE;
                }
            });
        
        for (int i = 0; i < 2; i++) {
            
            new AsyncHttpClient()
                .preparePut("http://localhost:8080/broadcast")
                .setBody("hello" + i)
                .execute().get();
            
            /* sometimes the last broadcast request is not
             * finished before f.cancel() is called */
            Thread.sleep(50);
        }
        
        f.cancel(false);
        assertEquals("[message: hello0, message: hello1]", broadcasts.toString());
    }
    
    @Test
    public void recordsRequests() throws Exception {
        
        server.handle(Method.GET, "/say-hello")
              .with(200, "text/plain", "Hello!");
        
        server.handle(Method.PUT, "/name/:name")
              .with(200, "text/plain", "OK");
        
        assertEquals(0, server.capturedRequests().size());
        
        new AsyncHttpClient()
            .prepareGet("http://localhost:8080/say-hello")
            .execute().get();
        
        new AsyncHttpClient()
            .preparePut("http://localhost:8080/name/Tim")
            .execute().get();
        
        assertEquals(2, server.capturedRequests().size());
        
        CapturedRequest firstRequest = server.request();
        assertNotNull(firstRequest);
        assertEquals("GET /say-hello HTTP/1.1", firstRequest.getRequestLine());
        
        CapturedRequest secondRequest = server.request();
        assertNotNull(secondRequest);
        assertEquals("PUT /name/Tim HTTP/1.1", secondRequest.getRequestLine());
    }
    
    @After
    public void afterEachTest() throws Exception {
        server.stop();
    }
}
