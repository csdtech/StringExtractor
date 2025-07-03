/**
 * Copyright 2025 Suleman 'sdtech' Hamisu
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sdtech.stringextractor;

import java.io.File;
import java.io.IOException;
import com.sdtech.stringextractor.core.MainExtractedString;

public abstract class ExtractedString {
    
    /** Create the class instance with no parameters. */
    public ExtractedString(){}
    
    /**
     * Generate and write the ExtractedString class in a given directory
     *
     * @param folder  The directory to create ExtractedString.java file inside.
     * @param pkgName The package name of the class which extracts strings from.
     * @throws IOException if and exception occur while writing the file.
     */
    public abstract void writeTo(File folder, String pkgName) throws IOException;
    
    /**
     * Create the new Instance of ExtractedString and return with it.
     *
     * @return The new created ExtractedString class instance.
     */
     public static ExtractedString getInstance(){
         return new MainExtractedString();
     }
}
