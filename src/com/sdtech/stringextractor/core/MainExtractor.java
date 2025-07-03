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

package com.sdtech.stringextractor.core;

import com.sdtech.stringextractor.ExtractionException;
import com.sdtech.stringextractor.ExtractedString;
import com.sdtech.stringextractor.StringExtractor;
import com.sdtech.stringextractor.XmlWriter;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/** A class that implements the {@link StringExtractor} methods. */
public class MainExtractor extends StringExtractor {

    /** obtain the instance only from this class */
    private MainExtractor() {}

    /** do the extraction */
    private void extractString(File fileToRead, File xmlFile, boolean extractedString, String prefix, String suffix, boolean backupFile, boolean modeRecursive, int extractCount) throws ExtractionException {
        try {
            String fileString = readFile(fileToRead);
            Matcher stringMatcher = null;
            boolean javaCode = false;
            if(fileToRead.getName().endsWith(".java")) {
                stringMatcher = JAVA_STRING_PATTERN.matcher(fileString);
                javaCode = true;
            } else if(fileToRead.getName().endsWith(".xml")) {
                stringMatcher = XML_STRING_PATTERN.matcher(fileString);
            } else {
                System.out.println("\r\nfileToRead must be valid java or xml file. but got: " + fileToRead.getAbsolutePath());
                return;
            }
            if(prefix == null) {
                prefix = "extracted_string%s";
            } else if(!prefix.endsWith("%s")) {
                prefix += "%s";
            }
            if(suffix == null) {
                suffix = "%s";
            } else if(!suffix.endsWith("%s")) {
                suffix += "%s";
            }
            if(xmlFile == null) {
                xmlFile = new File(fileToRead.getParent(), fileToRead.getName() + "_extracted_strings.xml");
            }

            ArrayList<String> extracted = new ArrayList<String>();
            ArrayList<String> xml_extracted = new ArrayList<String>();
            ArrayList<String> extracted_name = new ArrayList<String>();
            int count = modeRecursive ? extractCount : 0;
            while(stringMatcher.find()) {
                String ext_tmp =  stringMatcher.group();
                if(!javaCode) {
                    ext_tmp = ext_tmp.replaceAll("android\\:.*?=", "");
                }
                // skip empty and blank string 
                if(ext_tmp.matches("(\"\"|\"[ ]{1,}\")"))continue;
                // skip if string is present before.
                if(xml_extracted.contains(ext_tmp))continue;
                String ext = "";
                ++count;
                if(javaCode) {
                    ext = extractedString ?  String.format("ExtractedString.getString(" + JAVA_CODE + prefix + ")", String.format(suffix, count)): 
                    String.format("getResources().getString(" + JAVA_CODE + prefix + ")", String.format(suffix, count));
                } else {
                    ext = String.format(XML_CODE + prefix, String.format(suffix, count));
                }
                extracted_name.add(String.format(prefix, String.format(suffix, count)));
                xml_extracted.add(ext_tmp);
                extracted.add(ext);
            }
            if(modeRecursive)extractCount = count;
            if(xml_extracted.size() > 0 && backupFile) {
                fileToRead.renameTo(new File(fileToRead.getPath() + ".backup"));
            }
            if(extracted.size() <= 0) {
                System.out.printf("\r\nNo strings found on: %s", fileToRead.getAbsolutePath());
                return;
            } else {
                System.out.printf("\r\n%s strings was found on: %s", extracted.size(), fileToRead.getAbsolutePath());
            }
            //write the extracted strings to xml file
            XmlWriter writer = new XmlWriter(xmlFile);
            for(int i =0; i < xml_extracted.size();i++) {
                String str = xml_extracted.get(i);
                // remove the qoutes from string
                str = str.substring(1, str.length() - 1);
                writer.write(extracted_name.get(i), str);
            }

            writer.save();
            //replacing and save the modified code to file
            for(int i =0; i < xml_extracted.size();i++) {
                if(javaCode) {
                    fileString = fileString.replace(xml_extracted.get(i), extracted.get(i));
                } else {
                    fileString = fileString.replace(xml_extracted.get(i), "\"" + extracted.get(i) + "\"");
                }
            }
            writeFile(fileToRead, fileString);
            /**
             * Generate ExtractedString.java file if enable
             */
            if(javaCode && extractedString) {
                String pkgName = "";
                Matcher pkgMatcher = Pattern.compile("package .*?;").matcher(fileString);
                //try to find the package name for the file if possible
                if(pkgMatcher.find()) {
                    pkgName = pkgMatcher.group();
                }
                ExtractedString.getInstance().writeTo(fileToRead.getParentFile(),pkgName);
            }
        } catch(Exception e) {
            throw new ExtractionException(e);
        }
    }

    @Override
    public void startExtraction() throws ExtractionException {
        extractString(fileToRead, xmlFile, extractedString, prefix, suffix, backupFile, modeRecursive, extractCount);
    }

    /**
     * return the string read from the given file.
     */
    private String readFile(File f) throws Exception {
        String str = "";
        InputStream in = new FileInputStream(f);
        byte[] data = new byte[4096];
        
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int read;
        while((read=in.read(data)) != -1){
            out.write(data,0,read);
        }
        str = out.toString();
        in.close();
        out.close();
        
        return str;
    }
    /**
     * saves the given string into a given file
     */
    private void writeFile(File f, String text) throws Exception {
        FileOutputStream fout = new FileOutputStream(f);
        fout.write(text.getBytes());
        fout.flush();
        fout.close();
    }
    /** obtain the StringExtractor instance */
    public static StringExtractor getExtractor() {
        return new MainExtractor();
    }
}
