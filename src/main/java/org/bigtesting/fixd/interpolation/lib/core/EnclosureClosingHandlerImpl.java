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
package org.bigtesting.fixd.interpolation.lib.core;

import java.util.regex.Pattern;

import org.bigtesting.fixd.interpolation.lib.EnclosureClosingHandler;

/**
 * 
 * @author Luis Antunes
 */
public class EnclosureClosingHandlerImpl extends SubstitutionHandlerImpl 
    implements EnclosureClosingHandler {

    private final String opening;
    private final String closing;
    
    private final Pattern pattern;
    
    public EnclosureClosingHandlerImpl(String opening, String closing, String characterClass) {
        
        this.opening = opening;
        this.closing = closing;
        
        String quotedOpening = Pattern.quote(opening);
        String quotedClosing = Pattern.quote(closing);
        if (characterClass == null) {
            characterClass = "[^" + quotedOpening + quotedClosing + "\\s]";
        }
        this.pattern = Pattern.compile("(" + quotedOpening + characterClass + "+" + quotedClosing + ")");
    }

    @Override
    protected Pattern getPattern() {
        
        return pattern;
    }

    @Override
    protected String getCaptured(String found) {
        
        return found.substring(opening.length(), found.length() - closing.length());
    }
}
