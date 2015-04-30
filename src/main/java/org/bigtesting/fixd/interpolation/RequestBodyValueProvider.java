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
package org.bigtesting.fixd.interpolation;

import java.io.IOException;

import org.bigtesting.fixd.request.HttpRequest;
import org.bigtesting.fixd.util.RequestUtils;

/**
 * 
 * @author Luis Antunes
 */
class RequestBodyValueProvider implements RequestValueProvider<String> {

    public String getValue(HttpRequest request) {
        
        try {
            return new String(RequestUtils.readBody(request.getBodyAsStream()));
        } catch (IOException e) {
            throw new RuntimeException("error getting body", e);
        }
    }
}
