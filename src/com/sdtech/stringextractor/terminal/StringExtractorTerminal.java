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

package com.sdtech.stringextractor.terminal;

import com.sdtech.stringextractor.StringExtractor;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;

import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StringExtractorTerminal {
    
    /**
     * indicates weather an error occur or not when extracting single file strings.
     */
    private static boolean allDone = true;

    /**
     * indicates if options -d and -r are specified.
     */
    private static boolean modeRecursive = false;

    /**
     * single count to use on all files if modeMecursive == true
     * to avoid bad resources linking.
     */
    private static int extractCount = 0;

    /** 
     * The main method to pass all the options and arguments.<p>
     *
     * we will use the options and arguments to create new {@link com.sdtech.stringextractor.StringExtractor}.
     *
     * @param args the arguments to parse options and flags
     * @throws Exception if an IOException occured.
     */
    public static void main(String... args)throws Exception {
        if(args.length <= 0) {
            showUsage("");
            return;
        } else if(args.length < 2) {
            if(args[0].matches("[-]{1,2}(h|help|\\?)")) {
                showUsage("");
                return;
            }
            if(args[0].matches("[-]{1,2}((r|b|c|i|d|x|p|s)|([rbc]{3}))")) {
                showUsage("", "option '" + args[0] + "' require one argument.");
                return;
            }
            showUsage(args[0]);
        } else {
            /** parsing options and arguments */
            File inputFile = null;
            File xmlFile = null;
            File pathToScan = null;
            String prefixText=null;
            String suffixText = null;
            boolean useExtractedString=false;
            boolean backupFile = false;
            boolean recursive = false;
            final ArrayList<File> inputFiles = new ArrayList<File>();

            for(int i = 0; i < args.length; i++) {

                String option=args[i].toLowerCase();

                if(option.matches("[-]{1,2}[rbc]{3}")) {
                    backupFile = true;
                    recursive = true;
                    useExtractedString = true;
                } else if(option.matches("[-]{1,2}[br]{2}")) {
                    backupFile = true;
                    recursive = true;
                } else if(option.matches("[-]{1,2}[rc]{2}")) {
                    recursive = true;
                    useExtractedString = true;
                } else if(option.matches("[-]{1,2}[cb]{2}")) {
                    backupFile = true;
                    useExtractedString = true;
                } else if(option.matches("-b")) {
                    backupFile = true;
                } else if(option.matches("-r")) {
                    recursive = true;
                } else if(option.matches("-c")) {
                    useExtractedString = true;
                }

                if(option.matches("-d")) {
                    if(i < args.length - 1) {
                        pathToScan = new File(args[i + 1]);
                    } else {
                        showUsage("", "option -d require one argument.");
                        break;
                    }
                } else if(option.matches("-x")) {
                    if(i < args.length - 1) {
                        xmlFile = new File(args[i + 1]);
                    } else {
                        showUsage("", "option -x require one argument.");
                        break;
                    }
                } else if(option.matches("-i")) {
                    if(i < args.length - 1) {
                        inputFile = new File(args[i + 1]);
                    } else {
                        showUsage("", "option -i require one argument.");
                        break;
                    }
                } else if(option.matches("-s")) {
                    if(i < args.length - 1) {
                        suffixText = args[i + 1];
                    } else {
                        showUsage("", "option -s require one argument.");
                        break;
                    }
                } else if(option.matches("-p")) {
                    if(i < args.length - 1) {
                        prefixText = args[i + 1];
                    } else {
                        showUsage("", "option -p require one argument.");
                        break;
                    }
                }
            }

            if(inputFile != null && pathToScan != null) {
                System.out.println("You provide two options [-d,-i] but only one require.");
                System.out.print("which do you want use [d/i] ? : ");
                recursive = new BufferedReader(new InputStreamReader(System.in)).readLine().toLowerCase().matches("d");
            }
            if(!recursive && inputFile == null && pathToScan != null) {
                System.out.println("You provide option -d which require -r.");
                System.out.print("do you want use -r option [Y/n] ? : ");
                recursive = new BufferedReader(new InputStreamReader(System.in)).readLine().toLowerCase().matches("y");
            }
            if(pathToScan == null) {
                recursive = false;
            }
            /** make variables final for use in anonymous classes. i.e Runnable */
            final String  prx   = prefixText;
            final String  sfx   = suffixText;
            final boolean bkp   = backupFile;
            final boolean esc   = useExtractedString;
            final File    xml   = xmlFile;
            final File    input = inputFile;

            if(recursive) {
                modeRecursive = true;
                extractCount = 0;
                final File path = pathToScan;
                final long startTime = System.currentTimeMillis();

                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Boolean> result = executor.submit(new Runnable(){

                    @Override
                    public void run() {
                        System.out.printf("Finding files on path: %s", path.getAbsolutePath());
                        findFiles(path, inputFiles);
                    }
                }, true);
                if(result.get() && inputFiles.size() > 0) {
                    final ArrayList<File> tempXmlFiles = new ArrayList<File>();
                    Future<Boolean> result2 = executor.submit(new Runnable(){

                        @Override
                        public void run() {
                            if(inputFiles.size() < 2) {
                                try {
                                    System.out.printf("\r\nSearching strings on: %s", inputFiles.get(0).getAbsolutePath());
                                    extractString(inputFiles.get(0), xml, esc, prx, sfx, bkp, true, extractCount);
                                    System.out.printf("\r\nextracted strings from %s was saved to %s in %s ms.", inputFiles.get(0).getAbsolutePath(), xml != null ? xml.getAbsolutePath() : inputFiles.get(0).getAbsolutePath() + ".extracted_strings.xml", System.currentTimeMillis() - startTime);
                                } catch(Exception e) {
                                    e.printStackTrace(System.out);
                                }
                                return;
                            }
                            for(int i =0; i < inputFiles.size(); i++) {
                                try {
                                    File input =  inputFiles.get(i);
                                    //create a temporary xml file
                                    File tempXmlFile = new File(xml != null ? xml.getParent() : input.getParent(), String.format("tmp_ext_str_%s", i + 1));
                                    tempXmlFiles.add(tempXmlFile);
                                    System.out.printf("\r\nSearching strings on: %s", input.getAbsolutePath());
                                    extractString(input, tempXmlFile, esc, prx, sfx, bkp, false, 0);
                                } catch(Exception e) {
                                    e.printStackTrace(System.out);
                                }
                            }
                        }
                    }, true);
                    if(result2.get()) {
                        executor.shutdown();
                        //merge our xml files
                        if(extractCount <= 0) {
                            System.out.printf("\r\n%s files was scanned and no strings found.", inputFiles.size());
                            return;
                        }
                        File finalXml = xml;
                        if(xml == null) {
                            finalXml = new File(inputFiles.get(0).getParent(), "extracted_strings.xml");
                        }
                        StringBuilder strb = new StringBuilder();
                        // read the strings from our temporary files.
                        for(File file : tempXmlFiles) {
                            boolean read = false;
                            try {
                                strb.append(readFile(file));
                                strb.append("\r\n");
                                read = true;
                            } catch(Exception e) {
                                read = false;
                            } finally {
                                //delete the file if exists and read success.
                                if(read && file.delete()) {} else {
                                    if(file.exists()) {
                                        System.out.printf("\r\nunable to %s the temp file: %s", read ? "delete" : "read", file.getAbsoluteFile());
                                    }
                                }
                            }
                        }
                        String xmlStart = "<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<resources>";
                        String xmlEnd = "\r\n</resources>";
                        StringBuilder sb = new StringBuilder();
                        sb.append(xmlStart);
                        String[] lines = strb.toString().split("\r\n");
                        for(String line : lines) {
                            if(line.matches("[\\s]{0,}\\<string.*?")) {
                                sb.append("\r\n" + line);
                            }
                        }
                        sb.append(xmlEnd);
                        //check for duplicate name of strings by
                        //generating new name from prefix and suffix
                        String prefix = prefixText;
                        String suffix = suffixText;
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
                        String name = String.format("name=\"" + prefix + "\">", suffix);

                        String xmlCode = sb.toString();
                        Matcher nameMatcher = Pattern.compile("name=\".*?\">", Pattern.CASE_INSENSITIVE).matcher(xmlCode);
                        StringBuffer strbuffer = new StringBuffer();

                        int count = 0;
                        while(nameMatcher.find()) {
                            // replace any string name with our new one
                            nameMatcher.appendReplacement(strbuffer, String.format(name, ++count));
                        }
                        //append the rest of text
                        nameMatcher.appendTail(strbuffer);
                        xmlCode = strbuffer.toString();
                        //write and save our xml code
                        writeFile(finalXml, xmlCode);
                        System.out.printf("\r\n%s strings was extracted from %s files and saved to %s in %s ms.", extractCount , tempXmlFiles.size(), finalXml.getAbsolutePath(), System.currentTimeMillis() - startTime);
                    }
                } else if(result.get() && pathToScan.exists() && pathToScan.isDirectory()) {
                    System.out.println("\r\nNo java or xml file found on path: " + pathToScan.getAbsolutePath());
                }
            } else {
                modeRecursive = false;
                extractCount = 0;
                final long startTime = System.currentTimeMillis();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Boolean> result = executor.submit(new Runnable(){

                    @Override
                    public void run() {
                        if(input == null) {
                            allDone = false;
                            System.out.println("input file is null.");
                            return;
                        } else if(!input.exists()) {
                            allDone = false;
                            System.out.println("input file is not exists.");
                            return;
                        } else if(input.isDirectory()) {
                            allDone = false;
                            System.out.println("input file is a directory.");
                            return;
                        }
                        System.out.printf("Extracting strings of: %s", input.getAbsolutePath());
                        try {
                            extractString(input, xml, esc, prx, sfx, bkp, false, 0);
                        } catch(Exception e) {
                            e.printStackTrace(System.out);
                        }
                    }
                }, true);
                if(result.get()) {
                    if(allDone) {
                        System.out.printf("\r\nextracted strings from %s was saved to %s in %s ms.", input.getAbsolutePath(), xml != null ? xml.getAbsolutePath() : input.getAbsolutePath() + ".extracted_strings.xml", System.currentTimeMillis() - startTime);
                    }
                    executor.shutdown();
                }
            }
        }   
    }
    /** find the files in folder dir and add to ArrayList found */
    private static void findFiles(File dir, ArrayList<File> found) {
        if(dir.isFile()) {
            System.out.printf("\r\nFile: %s is not a directory", dir.getAbsolutePath());
            return;
        } else if(!dir.exists() || dir.listFiles() == null) {
            System.out.printf("\r\nFile: %s is %s", dir.getAbsolutePath(), dir.exists() ? "not contains any files" : "not exists.");
            return;
        }
        for(File child : dir.listFiles()) {
            if(child.isDirectory()) {
                findFiles(child, found);
            } else if(child.getName().matches(".*?\\.(java|xml)")) {
                found.add(child);
            }
        }
    }
    /** print the usage of this class in terminal */
    private static void showUsage(String option, String... message) {
        String help = "";
        help += option != "" ? " StringExtractor : unknown option '" + option + "'" : "";
        help += message.length >= 1 ? "\r\n" + message[0] : "";
        help += "\r\n  usage: StringExtractor -[r|b|c] -i FILE -d PATH -p TEXT -s TEXT -x FILE";
        help += "\r\n\r\n    Extract raw Strings from xml and java files of android app project to optional xml file and link them.\r\n     for example android:label=\"some text\" will become android:label=\"@string/extracted1\" and ";
        help += " the file extracted.xml will be created with code <string name=\"extracted1\">some text</string>. after extraction of xml file.";
        help += "\r\n  Options are:";
        help += "\r\n    -i FILE        the single xml or java file to scan the string. options [-d,-r] are ignore if specified.";
        help += "\r\n    -d PATH        directory to scan for xml or java files. this option require -r to work.";
        help += "\r\n    -p TEXT        the prefix text to use when genarating xml string name";
        help += "\r\n    -s TEXT        the suffix text to use when genarating xml string name";
        help += "\r\n    -x FILE        the file to write the genarated xml";
        help += "\r\n    -r             search for files recursively and extract their strings. this option require -d to be specified.";
        help += "\r\n    -b             backup the original file to filename.backup";
        help += "\r\n    -c             use class ExtractedString for java files. this will generate java file 'ExtractedString.java' in the same directory of input file to access strings from classes that do not have a Context. and you must call ExtractedString.setContext(context) from your application or activity onCreate. ";
        help += "\r\n    -h             show this usage message.";
        System.out.println(help);
    }

    /** do the extraction */
    private static void extractString(File input, File xmlFile, boolean esc, String prx, String sfx, boolean bkp, boolean recsv, int count) throws Exception {
        StringExtractor.extractString(
        /*fileToRead=*/input,
        /*xmlFile=*/xmlFile,
        /*extractedString=*/esc,
        /*prefix=*/prx,
        /*suffix=*/sfx,
        /*backupFile=*/bkp,
        /*modeRecursive=*/recsv,
        /*extractCount=*/count);
    }

    /**
     * return the string read from the given file.
     */
    private static String readFile(File f) throws Exception {
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
    private static void writeFile(File f, String text) throws Exception {
        FileOutputStream fout = new FileOutputStream(f);
        fout.write(text.getBytes());
        fout.flush();
        fout.close();
    }
}
