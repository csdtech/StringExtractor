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
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * The class for writting our generated xml into file.
 * <p>
 * This class open file in append mode by default.
 * so even when the given file to {@link StringExtractor} as xmlFile contains
 * other strings they can be merged without overwriting the file.
 * <p>
 * That means the file passed to this class constructor must be empty file,
 * not exists before or valid strings xml file. otherwise the file may be improperly merged
 * or currupted if not plain text file.
 */
public class XmlWriter {


    /** the writer we use to the xml code to file */
    private FileWriter mWriter;

    /** the file to write the xml code into */
    private File mXmlFile;

    /** indicate whether the given file is already contains the xml code */
    private boolean hasXmlCode = false;

    /** check if file exist before */
    private boolean fileExists = false;

    /** double qoutes version="1.0" */
    private final String xmlStart1 = "<?xml version=\"1.0\" encoding=\"utf-8\"?>";

    /** single qoutes version='1.0' */
    private final String xmlStart2 = "<?xml version='1.0' encoding='utf-8'?>";

    /** resouces tag which is parent for any android item in xml resources like string, id, string-array, etc. */
    private final String resTagStart = "<resources";
    private final String resTagEnd = "</resources>";

    /**
     * Create a new XmlFileWriter with the given file to write the xml code to.
     *
     * @param file The {@link File} to write the xml code.
     * @throws IOException if the given file cannot be written.
     */
    public XmlWriter(File file) throws IOException {
        /** store the given file */
        mXmlFile = file;

        /** check if file exists */
        fileExists = file.exists();

        /** check if file contains string xml code */
        hasXmlCode = isXmlFile(file);

        /** open the file as append mode */
        mWriter = new FileWriter(file,/*append=*/true);
    }

    /**
     * Generate and append the string tag generated from the
     * given name and value to file and save the file to avoid 
     * interruption before calling to {@link #save()} or {@link #close()} method.
     *
     * @param name The name of the string to append to the file.
     * @param value The value of the string.
     * @throws IOException if the file is cannot be written to
     */
    public void write(String name, String value) throws IOException {

        /** append the new line */
        mWriter.append("\r\n");

        /** append the code */
        mWriter.append(String.format("<string name=\"%s\">%s</string>", name, value));

        /** save the changes */
        mWriter.flush();
    }

    /**
     * save the code appended to the filewriter and close the writer.
     *
     * @throws IOException if an Error occur while closing or saving the file.
     */
    public void save() throws IOException {
        /** save file and close the writer */
        close();

        if(!fileExists || hasXmlCode) {
            /** it is new file or valid string xml file we need to merge them. */
            hasXmlCode = true;
            merge();
        }
    }

    /**
     * merge the original xml code inside file if available
     * with new one to avoid xml passing error.
     * 
     * @throws IOException if an error occured while reading from or writing to file when merging.
     */
    public void merge() throws IOException {
        if(!hasXmlCode) {
            /** file is not exist or not valid xml file */
            return;
        }
        /** read the text from the file which merged. */
        StringBuilder sb = new StringBuilder();
        String line;
        BufferedReader reader = new BufferedReader(new InputStreamReader(new FileInputStream(mXmlFile)));
        while((line = reader.readLine()) != null) {
            /** dont add the same line */
            if(sb.toString().contains(line))continue;

            sb.append(sb.toString().isEmpty() ? "" : "\r\n").append(line);
        }
        reader.close();

        String xmlRead = sb.toString();

        /** remove resources open tag <resources> or <resources ...> */
        xmlRead = xmlRead.replaceAll("\\" + resTagStart + ".*?>", "");

        /** remove resources close tag </resources> */
        xmlRead = xmlRead.replace(resTagEnd, "");

        /** remove xml tag with double qoutes. i.e <?xml version="1.0" encoding="utf-8"?> */
        xmlRead = xmlRead.replace(xmlStart1, "");

        /** remove xml tag with single qoutes. i.e <?xml version='1.0' encoding='utf-8'?> */
        xmlRead = xmlRead.replace(xmlStart2, "");


        /** our code is now merged then save */
        byte[] xmlToWrite = sb.toString().getBytes("UTF-8");
        FileOutputStream fout = new FileOutputStream(mXmlFile);
        fout.write(xmlToWrite);
        fout.close();

        /** we need to format the code */
        formatXml();
    }

    /**
     * Formats and indent xml code created or merged.
     *
     * @throws IOException if an exceotion occur while formatting.
     */
    public void formatXml() throws IOException {
        /** create new XmlFormatter with our file */
        XmlFormatter formatter = new XmlFormatter(mXmlFile);
        /** format the xml */
        formatter.formatXml();
    }

    /**
     * Check if the given file is exists and contains valid string xml code.
     * <p>
     * if file is bigger than 2MB in size the negetive value will be returned 
     * even it is a valid xml file.
     *
     * @param xmlFile the {@link File} to check
     * @throws IOException if an error occured while reading the file.
     * @return returns true if is valid xml file. false otherwise.
     */
    public boolean isXmlFile(File xmlFile) throws IOException {

        if(!xmlFile.exists()) {
            /** the file is not exists no need to read */
            return false;
        }

        if(xmlFile.isDirectory()) {
            if(!xmlFile.delete()) {
                /** the directory contains files or is read only */
                return false;
            }
        }

        /**
         * 1024 bytes = 1KB
         * 1024 KB = 1M
         * 1MB x 4 = 4MB
         */
        long four_mb = 1024 * 1024 * 4;
        if(xmlFile.length() >= four_mb) {
            /** the file is bigger than 4MB. not text file? */
            return false;
        }

        /** read the first 2048 Characters from the file. */
        FileInputStream fis = new FileInputStream(xmlFile);
        byte[] data = new byte[2048];
        fis.read(data);
        fis.close();

        String textRead = "";

        /** converting bytes to String. */
        for(byte b : data) {
            textRead += (char) b;
        }

        if((textRead.contains(xmlStart1) || textRead.contains(xmlStart2) || textRead.contains(resTagStart) || textRead.contains(resTagEnd)) && textRead.contains("<string")) {
            /** is valid string xml file */
            return true;
        }

        return false;
    }

    /**
     * Flush and close the writer.
     *
     * @throws IOException when an error occur while closing the writer.
     */
    public void close() throws IOException {
        /** flush the unsaved changes to file. */
        mWriter.flush();
        /** close the writer. */
        mWriter.close();
    }
}
