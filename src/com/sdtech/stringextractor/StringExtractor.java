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

import com.sdtech.stringextractor.core.MainExtractor;

import java.io.File;

import java.util.regex.Pattern;

/**
 * StringExtractor is used to extracts raw strings from
 * a java and xml files to a optional xml file and link them.
 * <p>
 * StringExtract extracts any non blank strings from java file.
 * and only extracts the strings from supported xml attributes below:
 *
 * <ul>
 *     <li>android:text</li>
 *     <li>android:title</li>
 *     <li>android:summary</li>
 *     <li>android:label</li>
 *     <li>android:hint</li>
 *     <li>android:description</li>
 * </ul>
 *  
 * Note: if one of the above attributes contains the text
 * which starts with '@' or '?' will be ignore even if it is not
 * '@string/*' or '?attr/*'.
 *
 * <p>
 *   Created by sdtech @ 11:54 AM 13 Jan 2025.
 *   Github link <a href="https://github.com/csdtech">https://github.com/csdtech</a>
 * </p>
 */
public abstract class StringExtractor {

    /**
     * prefix for linking strings from java
     */
    protected final String JAVA_CODE="R.string.";

    /**
     * prefix for linking strings from xml
     */
    protected final String XML_CODE="@string/";

    /**
     * pattern for matching any non blank string in java
     */
    protected final Pattern JAVA_STRING_PATTERN = Pattern.compile("\".*?\"", Pattern.CASE_INSENSITIVE);

    /** 
     * pattern for matching supported xml attributes 
     * text,title,label,hint,summary and description
     */
    protected final Pattern XML_STRING_PATTERN = Pattern.compile("(" +
    /** matches android:text="*" which is not starts with '?' or '@' */
    "android\\:text=\"[^@\\?]{1}.*?\"|" +

    /** matches android:title="*" which is not starts with '?' or '@' */
    "android\\:title=\"[^@\\?]{1}.*?\"|" +

    /** matches android:hint="*" which is not starts with '?' or '@' */
    "android\\:hint=\"[^@\\?]{1}.*?\"|" +

    /** matches android:summary="*" which is not starts with '?' or '@' */
    "android\\:summary=\"[^@\\?]{1}.*?\"|" +

    /** matches android:description="*" which is not starts with '? or '@' */
    "android\\:description=\"[^@\\?]{1}.*?\"|" +

    /** matches android:label="*" which is not starts with '?' or '@' */
    "android\\:label=\"[^@\\?]{1}.*?\"" +

    ")", Pattern.CASE_INSENSITIVE);


    protected File fileToRead;
    protected File xmlFile;
    protected boolean extractedString;
    protected String prefix;
    protected String suffix;
    protected boolean backupFile;
    protected boolean modeRecursive;
    protected int extractCount;


    /** create the Extractor */
    public StringExtractor() {}

    /**
     * Set the file to read and extracts strings from.
     * <p> The file must be non-null, valid java or xml and exists.
     *
     * @param fileToRead The java or xml file to read and extracts strings from.
     */
    public void setFileToRead(File fileToRead) {
        this.fileToRead = fileToRead;
    }

    /**
     * @return The given file to read strings from.
     */
    public File getFileToRead() {
        return fileToRead;
    }

    /**
     * Set the file to write the generated xml..
     *
     * <p>This is usually on values folder of project. e.g PROJECT_ROOT/res/values/x_strings.xml
     *
     * @param xmlFile The file to write the generated xml.
     */
    public void setXmlFile(File xmlFile) {
        this.xmlFile = xmlFile;
    }

    /**
     * @return The given file to write the generated xml.
     */
    public File getXmlFile() {
        return xmlFile;
    }

    /**
     * Enable or disable the use of ExtractedString class to get the strings.
     *
     * <p>This should only be set on java file, otherwise will not be used.
     *
     * @param extractedString indicates whether to use ExtractedString class or not.
     * @see {@link ExtractedString} for more info.
     */
    public void setExtractedString(boolean extractedString) {
        this.extractedString = extractedString;
    }

    /**
     * @return Returns whether ExtractedString enabled or not.
     */
    public boolean isExtractedString() {
        return extractedString;
    }

    /**
     * Set prefix string to use when generating extracted string names in xml file.
     *
     * @param prefix The text to use as prefix for generating extracted string names in xml file.
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
    
    /**
     * @return Returns The string used as a prefix if set, null otherwise.
     * @see {@link #setPrefix()}
     */
    public String getPrefix() {
        return prefix;
    }

    /**
     * Set suffix string to use when generating extracted string names in xml file.
     *
     * @param suffix The text to use as suffix for generating extracted string names in xml file.
     */
    public void setSuffix(String suffix) {
        this.suffix = suffix;
    }

    /**
     * @return Returns The string used as a suffix if set, null otherwise.
     * @see {@link #setSuffix()}
     */
    public String getSuffix() {
        return suffix;
    }

    /**
     * Set suffix string to use when generating extracted string names in xml file.
     *
     * @param backupFile The text to use as suffix for generating extracted string names in xml file.
     */
    public void setBackupFile(boolean backupFile) {
        this.backupFile = backupFile;
    }

    /**
     * @return Returns weather to backup the original file before modifying or not.
     * @see {@link #setBackupFile()}
     */
    public boolean isBackupFile() {
        return backupFile;
    }
    
    /**
     * Flag to make the use of {@link #getExtractCount()} when generating the extracted string names in xml file.
     * 
     * <p> <strong>Note:</strong> This wont make the recursive search and extraction of the strings unless implemented.
     *
     * @param modeRecursive flag to let use the extractCount or not.
     */
    public void setModeRecursive(boolean modeRecursive) {
        this.modeRecursive = modeRecursive;
    }

    /**
     * @return Returns true if the given path as input is a directory and recursive flag is applied, false otherwise.
     * @see {@link #setModeRecursive()}
     */
    public boolean isModeRecursive() {
        return modeRecursive;
    }

    /**
     * Set the count to start from and increases instead of starting from 0. This is important only when extracting multiple files at same time.
     *
     * @param extractCount The number to start from.
     */
    public void setExtractCount(int extractCount) {
        this.extractCount = extractCount;
    }

    /**
     * @return Returns The number to begin the count from if set or zero.
     * @see {@link #setExtractCount()}
     */
    public int getExtractCount() {
        return extractCount;
    }

    /**
     * Start the extraction using provided file(s) and option(s).
     *
     * @throws ExtractionException when an Exception occur while extracting strings.*/
    public abstract void startExtraction() throws ExtractionException;

    /**
     * Extracts the strings from single xml or java file,
     *  save to xml file and link them.
     *
     * @param fileToRead      the valid xml or java file to read and extract strings from.
     * @param xmlFile         the file to write the extracted strings.
     * @param extractedString if true and fileToRead is java file the class ExtractedString will be used to get the strings.<p> See the {@link ExtractedString} for more info.
     * @param prefix          the String to use as prefix for generating strings name in xml
     * @param suffix          the String to use as suffix for generating strings name in xml
     * @param backupFile      indicates wether to backup the file before writing the extracted strings to file.
     * @param modeRecursive   this make the extractCount to be used when generating names instead of starting from zero when set to true.
     * @param extractCount    the count to start from, this will be ignore if modeRecursive is false.
     * @throws ExtractionException      when an exception occured during extracting strings from file.
     */
    public static void extractString(File fileToRead, File xmlFile, boolean extractedString, String prefix, String suffix, boolean backupFile, boolean modeRecursive, int extractCount) throws ExtractionException {
        StringExtractor extractor = MainExtractor.getExtractor();
        extractor.setBackupFile(backupFile);
        extractor.setExtractCount(extractCount);
        extractor.setExtractedString(extractedString);
        extractor.setFileToRead(fileToRead);
        extractor.setModeRecursive(modeRecursive);
        extractor.setPrefix(prefix);
        extractor.setSuffix(suffix);
        extractor.setXmlFile(xmlFile);
        extractor.startExtraction();
    }
}

