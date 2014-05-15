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
package org.bigtesting.fixd.response.impl;

import java.io.InputStream;

import org.bigtesting.fixd.core.ByteArrayResponseBody;
import org.bigtesting.fixd.core.InputStreamResponseBody;
import org.bigtesting.fixd.core.InterpolatedResponseBody;
import org.bigtesting.fixd.core.ResponseBody;
import org.bigtesting.fixd.core.StringResponseBody;
import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.response.HttpResponse;
import org.simpleframework.http.Cookie;
import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
public class SimpleHttpResponse implements HttpResponse {

    private final HttpRequest request;
    
    private final Response response;
    
    private ResponseBody body;
    private String contentType;
    private int statusCode;
    
    public SimpleHttpResponse(HttpRequest req, Response response) {
        this.request = req;
        this.response = response;
    }
    
    public void setBody(InputStream in) {
        this.body = new InputStreamResponseBody(in);
    }

    public void setBody(byte[] content) {
        this.body = new ByteArrayResponseBody(content);
    }

    public void setBody(String content) {
        this.body = new StringResponseBody(content);
    }

    public void setInterpolatedBody(String content) {
        this.body = new InterpolatedResponseBody(content, request);
    }
    
    public ResponseBody getBody() {
        return body;
    }
    
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public String getContentType() {
        return contentType;
    }
    
    public void setStatusCode(int statusCode) {
        this.statusCode = statusCode;
    }

    public int getStatusCode() {
        return statusCode;
    }

    public void setCookie(String name, String value) {

        Cookie cookie = new Cookie(name, value);
        response.setCookie(cookie);
    }

    public void addHeader(String name, String value) {

        response.addValue(name, value);
    }
    
    public void redirect(String location) {
        //TODO implement convenient redirects
    }
    
    public void redirect(String location, int statusCode) {
      //TODO implement convenient redirects
    }
}
