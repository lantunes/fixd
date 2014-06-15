/*
 * Copyright (C) 2014 BigTesting.org
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
package org.bigtesting.fixd.core;

import java.io.IOException;
import java.io.PrintStream;

import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
public class StringResponseBody extends ResponseBody {

    private final String body;
    
    public StringResponseBody(String body) {
        this.body = body;
    }
    
    public boolean hasContent() {
        return body != null && body.trim().length() > 0;
    }
    
    @Override
    public void send(Response response, String contentType) {
        try {
            printBody(response, contentType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendAndCommit(Response response, String contentType) {
        try {
            PrintStream ps = printBody(response, contentType);
            ps.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    
    private PrintStream printBody(Response response, String contentType) throws IOException {
        
        PrintStream ps = response.getPrintStream();
        addStandardHeaders(response, contentType);
        ps.println(body);
        ps.flush();
        return ps;
    }
}
