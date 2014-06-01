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

import org.bigtesting.fixd.interpolation.lib.EnclosureClosingHandler;
import org.bigtesting.fixd.interpolation.lib.EnclosureOpeningHandler;

/**
 * 
 * @author Luis Antunes
 */
public class EnclosureOpeningHandlerImpl implements EnclosureOpeningHandler {

    private final String opening;
    
    private final String characterClass;
    
    private EnclosureClosingHandlerImpl closingHandler;
    
    public EnclosureOpeningHandlerImpl(String opening, String characterClass) {
        
        this.opening = opening;
        this.characterClass = characterClass;
    }

    public EnclosureClosingHandler and(String closing) {
        
        EnclosureClosingHandlerImpl closingHandler = 
                new EnclosureClosingHandlerImpl(opening, closing, characterClass);
        this.closingHandler = closingHandler;
        return closingHandler;
    }
    
    public EnclosureClosingHandlerImpl getEnclosureClosingHandler() {
        
        return closingHandler;
    }
}
