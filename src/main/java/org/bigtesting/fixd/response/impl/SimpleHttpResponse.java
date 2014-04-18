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

import org.bigtesting.fixd.response.HttpResponse;

/**
 * 
 * @author Luis Antunes
 */
public class SimpleHttpResponse implements HttpResponse {

    /*
     * I don't see a way to restrict the body to
     * the InputStream, byte[], and String types
     * using Bounded Type Parameters and Generics
     */
    private Object body;
    
    private String contentType;
    private int statusCode;
    
    public void setBody(InputStream in) {
        this.body = in;
    }

    public void setBody(byte[] content) {
        this.body = content;
    }

    public void setBody(String content) {
        this.body = content;
    }

    /*
     * must be of the following types: InputStream, byte[], String
     */
    public Object getBody() {
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
}
