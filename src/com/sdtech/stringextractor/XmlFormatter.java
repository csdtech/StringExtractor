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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.Collections;

/**
 * A class that format the xml code generated with or marged by {@link StringExtractor}.
 *
 * <p>
 * This class can only format simple string xml file with &lt;resources> and &lt;string> tags
 * by removing the empty line fix simple indentation and break tags. for example
 * <p>
 * <code>&lt;resources>&lt;string name="str">String&lt;/string>&lt;/resources></code>
 *
 * <p>
 * will be return to:<br>
 * <code>&lt;resources><br>&nbsp;&nbsp;&nbsp;&nbsp;&lt;string name="str">String&lt;/string><br>&lt;resources></code><br>
 * when formatted.
 */
public class XmlFormatter {

    /** the file to read the xml code from, format the code and save to it. */
    private final File mXmlFile;

    /** double qoutes version="1.0" */
    private final String xmlStart1 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    /** single qoutes version='1.0' */
    private final String xmlStart2 = "<?xml version='1.0' encoding='utf-8'?>";

    /** resouces tag which is parent for any android item in xml resources like string, id, string-array, etc. */
    private final String resTagStart = "<resources";
    private final String resTagEnd = "</resources>";

    /**
     * Construct the Formatter with the given file.
     *
     * <p>
     * The xml code is read from the file first, format the code then save to file.
     *
     * @param xmlFile the {@link File} to read xml from and write to after formatted.
     */
    public XmlFormatter(File xmlFile) {
        mXmlFile = xmlFile;
    }

    /**
     * Do the format operations.
     *
     * @throws IOException if an exception occur when reading from or writing to file.
     */
    public void formatXml() throws IOException {
        /** read xml from file */
        String xmlCode = readCode();

        /** remove resources open tag <resources> or <resources ...> */
        xmlCode = xmlCode.replaceAll("\\" + resTagStart + ".*?>", "");

        /** remove resources close tag </resources> */
        xmlCode = xmlCode.replace(resTagEnd, "");

        /** remove xml tag with double qoutes. <?xml version="1.0" encoding="utf-8"?> */
        xmlCode = xmlCode.replace(xmlStart1, "");

        /** remove xml tag with single qoutes. <?xml version='1.0' encoding='utf-8'?> */
        xmlCode = xmlCode.replace(xmlStart2, "");


        /** replace "><" with ">|<" to split the tags using "|" */
        xmlCode = xmlCode.replaceAll(">[\\s]{0,}\\<", ">|<");

        /** list to store the tags */
        ArrayList<String> tagList = new ArrayList<>();

        /** split and add the tags to list */
        Collections.addAll(tagList, xmlCode.split("\\|"));

        StringBuilder sb = new StringBuilder();

        sb.append(xmlStart1);
        sb.append("\r\n");
        sb.append(resTagStart + ">");
        for(String tag : tagList) {
            /** new line + indent */
            sb.append("\r\n    ");
            sb.append(tag);
        }

        sb.append(resTagEnd);

        /** write the formatted xml to file */
        byte[] xmlToWrite = sb.toString().getBytes("UTF-8");
        FileOutputStream fout = new FileOutputStream(mXmlFile);
        fout.write(xmlToWrite);
        fout.close();
    }

    /** read text from xml file. */
    private String readCode() throws IOException {
        StringBuilder lines = new StringBuilder();
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mXmlFile)));
        String line;
        while((line = reader.readLine()) != null) {
            lines.append(line).append("\r\n");
        }
        return lines.toString();
    }
}
