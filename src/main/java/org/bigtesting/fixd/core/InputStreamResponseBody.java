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
package org.bigtesting.fixd.core;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.simpleframework.http.Response;

/**
 * 
 * @author Luis Antunes
 */
public class InputStreamResponseBody extends ResponseBody {

    private final InputStream in;
    
    public InputStreamResponseBody(InputStream in) {
        this.in = in;
    }
    
    @Override
    public void send(Response resp, String contentType) {
        try {
            sendContent(resp, contentType);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void sendAndCommit(Response resp, String contentType) {
        try {
            OutputStream out = sendContent(resp, contentType);
            out.close();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private OutputStream sendContent(Response resp, String contentType) throws IOException {
        
        OutputStream out = resp.getOutputStream();
        addStandardHeaders(resp, contentType);
        byte[] buffer = new byte[1024];
        int len;
        while ((len = in.read(buffer)) != -1) {
            out.write(buffer, 0, len);
        }
        out.flush();
        return out;
    }
    
    @Override
    public boolean hasContent() {
        try {
            return in != null && in.available() > 0;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
