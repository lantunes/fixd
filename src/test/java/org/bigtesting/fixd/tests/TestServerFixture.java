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
import java.util.concurrent.TimeUnit;

import org.bigtesting.fixd.ServerFixture;
import org.bigtesting.fixd.capture.CapturedRequest;
import org.bigtesting.fixd.core.Method;
import org.bigtesting.fixd.session.PathParamSessionHandler;
import org.bigtesting.fixd.session.RequestParamSessionHandler;
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
import com.ning.http.client.ListenableFuture;
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
    public void testSimpleGetWithRequestMethod() throws Exception {

        server.handle(Method.GET, "/say-method")
              .with(200, "text/plain", "Value: [request.method]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-method")
                        .execute()
                        .get();
       
        assertEquals("Value: GET", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRequestPath() throws Exception {

        server.handle(Method.GET, "/say-path")
              .with(200, "text/plain", "Value: [request.path]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-path")
                        .execute()
                        .get();
       
        assertEquals("Value: /say-path", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRequestQuery() throws Exception {

        server.handle(Method.GET, "/say-query")
              .with(200, "text/plain", "Value: [request.query]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-query?a=b")
                        .execute()
                        .get();
       
        assertEquals("Value: a=b", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRequestTime() throws Exception {

        server.handle(Method.GET, "/say-time")
              .with(200, "text/plain", "Value: [request.time]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-time")
                        .execute()
                        .get();
     
        assertTrue(resp.getResponseBody().trim().matches("Value: [0-9]*"));
    }
    
    @Test
    public void testSimpleGetWithRequestMajor() throws Exception {

        server.handle(Method.GET, "/say-major")
              .with(200, "text/plain", "Value: [request.major]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-major")
                        .execute()
                        .get();
       
        assertEquals("Value: 1", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRequestMinor() throws Exception {

        server.handle(Method.GET, "/say-minor")
              .with(200, "text/plain", "Value: [request.minor]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-minor")
                        .execute()
                        .get();
       
        assertEquals("Value: 1", resp.getResponseBody().trim());
    }
    
    @Test
    public void testSimpleGetWithRequestTarget() throws Exception {

        server.handle(Method.GET, "/say-target")
              .with(200, "text/plain", "Value: [request.target]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-target")
                        .execute()
                        .get();
       
        assertEquals("Value: /say-target", resp.getResponseBody().trim());
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
    public void testSimpleGetWithRequestHeader() throws Exception {

        server.handle(Method.GET, "/say-user-agent")
              .with(200, "text/plain", "Value: [request$User-Agent]");
       
        Response resp = new AsyncHttpClient()
                        .prepareGet("http://localhost:8080/say-user-agent")
                        .execute()
                        .get();
       
        assertEquals("Value: NING/1.0", resp.getResponseBody().trim());
    }
    
    @Test
    public void testStatefulRequests() throws Exception {
        
        server.handle(Method.PUT, "/name/:name")
              .with(200, "text/plain", "OK")
              .withSessionHandler(new PathParamSessionHandler());
        
        server.handle(Method.GET, "/name")
              .with(200, "text/plain", "Name: {name}");
        
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
    public void testStatefulRequestsUsingRequestParams() throws Exception {
        
        server.handle(Method.POST, "/", "application/x-www-form-urlencoded")
              .with(200, "text/plain", "OK")
              .withSessionHandler(new RequestParamSessionHandler());
        
        server.handle(Method.GET, "/")
              .with(200, "text/plain", "Name: {name}");
        
        WebClient client = new WebClient();
        
        Page page = client.getPage(new WebRequest(new URL(
                "http://localhost:8080/?name=Tim"), 
                HttpMethod.POST));
        assertEquals(200, page.getWebResponse().getStatusCode());
        
        page      = client.getPage(new WebRequest(new URL(
                "http://localhost:8080/"), 
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
        ListenableFuture<Integer> f = new AsyncHttpClient()
              .prepareGet("http://localhost:8080/echo/hello")
              .execute(new AddToListOnBodyPartReceivedHandler(chunks));
        
        assertEquals(200, (int)f.get());
        assertEquals("[message: hello, message: hello]", chunks.toString());
    }
    
    @Test
    public void testUpon() throws Exception {
        
        server.handle(Method.GET, "/subscribe")
              .with(200, "text/plain", "message: :message")
              .upon(Method.GET, "/broadcast/:message");
        
        final List<String> broadcasts = new ArrayList<String>();
        ListenableFuture<Integer> f = new AsyncHttpClient()
              .prepareGet("http://localhost:8080/subscribe")
              .execute(new AddToListOnBodyPartReceivedHandler(broadcasts));
        
        /* need some time for the above request to complete
         * before the broadcast requests can start */
        Thread.sleep(50);
        
        for (int i = 0; i < 2; i++) {
            
            new AsyncHttpClient()
                .prepareGet("http://localhost:8080/broadcast/hello" + i)
                .execute().get();
            
            /* sometimes the last broadcast request is not
             * finished before f.done() is called */
            Thread.sleep(50);
        }
        
        f.done(null);
        assertEquals("[message: hello0, message: hello1]", broadcasts.toString());
    }
    
    @Test
    public void testUponUsingRequestBody() throws Exception {
        
        server.handle(Method.GET, "/subscribe")
              .with(200, "text/plain", "message: [request.body]")
              .upon(Method.PUT, "/broadcast");
        
        final List<String> broadcasts = new ArrayList<String>();
        ListenableFuture<Integer> f = new AsyncHttpClient()
              .prepareGet("http://localhost:8080/subscribe")
              .execute(new AddToListOnBodyPartReceivedHandler(broadcasts));
        
        /* need some time for the above request to complete
         * before the broadcast requests can start */
        Thread.sleep(50);
        
        for (int i = 0; i < 2; i++) {
            
            new AsyncHttpClient()
                .preparePut("http://localhost:8080/broadcast")
                .setBody("hello" + i)
                .execute().get();
            
            /* sometimes the last broadcast request is not
             * finished before f.done() is called */
            Thread.sleep(50);
        }
        
        f.done(null);
        assertEquals("[message: hello0, message: hello1]", broadcasts.toString());
    }
    
    @Test
    public void testUponWithMultipleScubscribers() throws Exception {
        
        server.handle(Method.GET, "/subscribe")
              .with(200, "text/plain", "message: :message")
              .upon(Method.GET, "/broadcast/:message");
        
        final List<String> client1Broadcasts = new ArrayList<String>();
        ListenableFuture<Integer> f1 = new AsyncHttpClient()
              .prepareGet("http://localhost:8080/subscribe")
              .execute(new AddToListOnBodyPartReceivedHandler(client1Broadcasts));
        
        final List<String> client2Broadcasts = new ArrayList<String>();
        ListenableFuture<Integer> f2 = new AsyncHttpClient()
              .prepareGet("http://localhost:8080/subscribe")
              .execute(new AddToListOnBodyPartReceivedHandler(client2Broadcasts));
        
        /* need some time for the above requests to complete
         * before the broadcast requests can start */
        Thread.sleep(50);
        
        for (int i = 0; i < 2; i++) {
            
            new AsyncHttpClient()
                .prepareGet("http://localhost:8080/broadcast/hello" + i)
                .execute().get();
            
            /* sometimes the last broadcast request is not
             * finished before f.done() is called */
            Thread.sleep(50);
        }
        
        f1.done(null);
        f2.done(null);
        assertEquals("[message: hello0, message: hello1]", client1Broadcasts.toString());
        assertEquals("[message: hello0, message: hello1]", client2Broadcasts.toString());
    }
    
    @Test
    public void testUponWithTimeout() throws Exception {
        
        server.handle(Method.GET, "/subscribe")
              .with(200, "text/plain", "message: :message")
              .upon(Method.GET, "/broadcast/:message")
              .withTimeout(100, TimeUnit.MILLISECONDS);
        
        ListenableFuture<Response> f = new AsyncHttpClient()
              .prepareGet("http://localhost:8080/subscribe")
              .execute();
        
        /*
         * If the process didn't timeout, the subscribe request
         * would wait indefinitely, as no broadcast requests
         * are being made.
         */
        assertEquals(408, f.get().getStatusCode());
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
    
    @Test
    public void addsHeader() throws Exception {
        
        server.handle(Method.GET, "/")
              .with(302, "text/plain", "page moved")
              .withHeader("Location", "http://localhost:8080/new-location");
        
        server.handle(Method.GET, "/new-location")
              .with(200, "text/plain", "OK");
        
        new AsyncHttpClient()
            .prepareGet("http://localhost:8080/")
            .setFollowRedirects(true)
            .execute().get();
        
        assertEquals(2, server.capturedRequests().size());
        
        CapturedRequest firstRequest = server.request();
        assertNotNull(firstRequest);
        assertEquals("GET / HTTP/1.1", firstRequest.getRequestLine());
        
        CapturedRequest secondRequest = server.request();
        assertNotNull(secondRequest);
        assertEquals("GET /new-location HTTP/1.1", secondRequest.getRequestLine());
    }
    
    @After
    public void afterEachTest() throws Exception {
        server.stop();
    }
    
    /*--------------------------------------------*/
    
    private class AddToListOnBodyPartReceivedHandler extends AsyncCompletionHandler<Integer> {

        private final List<String> chunks;

        public AddToListOnBodyPartReceivedHandler(List<String> chunks) {
            this.chunks = chunks;
        }

        public Integer onCompleted(Response r) throws Exception {
            return r.getStatusCode();
        }

        public STATE onBodyPartReceived(HttpResponseBodyPart bodyPart) throws Exception {
            
            String chunk = new String(bodyPart.getBodyPartBytes()).trim();
            if (chunk.length() != 0) {
                chunks.add(chunk);
            }
            return STATE.CONTINUE;
        }
    }
}
