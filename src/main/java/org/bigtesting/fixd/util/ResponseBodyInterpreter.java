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

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bigtesting.fixd.Session;
import org.bigtesting.fixd.routing.Route.PathParameterElement;
import org.bigtesting.fixd.routing.RouteHelper;

/**
 * 
 * @author Luis Antunes
 */
public class ResponseBodyInterpreter {
    
    private static final Pattern SESSION_VALUE_PATTERN = Pattern.compile("\\{([^{}]*)\\}");

    public static String interpret(String body, String path, 
            List<PathParameterElement> pathParams, Session session) {
        
        body = interpretPathParamValues(body, path, pathParams);
        
        if (session != null) {
            body = interpretSessionValues(body, session);
        }
        
        return body;
    }
    
    /*
     * handle any values that start with ':'
     */
    private static String interpretPathParamValues(String body, String path, 
            List<PathParameterElement> pathParams) {
        
        String[] pathTokens = RouteHelper.getPathElements(path);
        for (PathParameterElement param : pathParams) {
            String paramName = "\\Q" + param.name() + "\\E";
            body = body.replaceAll(":" + paramName, pathTokens[param.index()]);
        }
        
        return body;
    }
    
    /* 
     * handle any values that are enclosed in '{}'
     * - replacement values can consist of "{}"
     */
    private static String interpretSessionValues(String body, Session session) {
        
        Matcher m = SESSION_VALUE_PATTERN.matcher(body);
        StringBuilder result = new StringBuilder();
        int start = 0;
        while (m.find()) {
            String key = m.group(1);
            Object val = session.get(key);
            if (val != null) {
                String stringVal = val.toString();
                result.append(body.substring(start, m.start()));
                result.append(stringVal);
                start = m.end();
            }
        }
        result.append(body.substring(start));
        return result.toString();
    }
}
