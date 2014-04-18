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
package org.bigtesting.fixd.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * 
 * @author Luis Antunes
 */
public class RequestUtils {

    public static byte[] readBody(InputStream in) {
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        try {
            
            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer)) != -1) {
                out.write(buffer, 0, len);
            }
            
        } catch (IOException e) {
            throw new RuntimeException("error getting body", e);
        } finally {
            if (in != null) try {in.close();} catch (IOException e2) {}
        }
        
        return out.toByteArray();
    }
}
