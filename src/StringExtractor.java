/** Copyright 2025 Suleman 'sdtech' Hamisu
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

/**
 * StringExtractor is used to extracts raw strings from
 * a java and xml files to a optional xml file and link them.
 * <p>
 * StringExtract extracts any non blank strings from java file.
 * and only extracts the strings from supported xml attributes below:
 *
 * <ul>
 *     <li>android:text</li>
 *     <li>android:title
 *     <li>android:summary
 *     <li>android:label
 *     <li>android:hint
 *     <li>android:description
 * </ul>
 *  
 * <p>
 *   Created by sdtech @ 11:54 AM 13 Jan 2025.
 *   Github link <a href="https://github.com/csdtech">https://github.com/csdtech</a>
 * </p>
 */
public final class StringExtractor {
    
    /**
     * prefix for linking strings from java
     */
    private static final String JAVA_CODE="R.string.";
    
    /**
     * prefix for linking strings from xml
     */
    private static final String XML_CODE="@string/";
    
    /**
     * pattern for matching any non blank string in java
     */
    private static final Pattern JAVA_STRING_PATTERN = Pattern.compile("\".*?\"", Pattern.CASE_INSENSITIVE);
    
    /** 
     * pattern for matching supported xml attributes.
     * supported attributes are text,title,label,hint,summary and description
     */
    private static final Pattern XML_STRING_PATTERN = Pattern.compile("("+
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
    "android\\:label=\"[^@\\?]{1}.*?\""+
    /**
     * Note: if one of the following attributes contains the text
     * which starts with '@' or '?' will be ignore even if it is not
     * '@string/*' or '?attr/*'.
     */
    ")", Pattern.CASE_INSENSITIVE);

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
    private static int rcCount = 0;
    
    /** 
     * <p>the main method to pass all the options and arguments.</p>
     *
     * <strong>Note:</strong> this method is intended to use only from command line
     * if you want use recursive extraction out side the comand line
     * you have to define your own implementation.
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
                showUsage("", "a minimum of two options required");
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
            if(pathToScan==null){
                recursive=false;
            }
            
            final String  prx   = prefixText;
            final String  sfx   = suffixText;
            final boolean bkp   = backupFile;
            final boolean esc   = useExtractedString;
            final File    xml   = xmlFile;
            final File    input = inputFile;
            
            if(recursive) {
                modeRecursive=true;
                rcCount=0;
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
                                    extractString(inputFiles.get(0), xml, esc, prx, sfx, bkp);
                                    System.out.printf("\r\nextracted strings from %s was saved to %s in %s ms.", inputFiles.get(0).getAbsolutePath(), xml != null ? xml.getAbsolutePath() : inputFiles.get(0).getAbsolutePath() + ".extracted_strings.xml", System.currentTimeMillis() - startTime);
                                } catch(Exception e) {
                                    e.printStackTrace(System.out);
                                }
                                return;
                            }
                            for(int i =0; i < inputFiles.size(); i++) {
                                try {
                                    File input =  inputFiles.get(i);
                                    File tempXmlFile = new File(xml != null ? xml.getParent() : input.getParent(), String.format("tmp_ext_str_%s", i + 1));
                                    tempXmlFiles.add(tempXmlFile);
                                    System.out.printf("\r\nSearching strings on: %s", input.getAbsolutePath());
                                    extractString(input, tempXmlFile, esc, prx, sfx, bkp);
                                } catch(Exception e) {
                                    e.printStackTrace(System.out);
                                }
                            }
                        }
                    }, true);
                    if(result2.get()) {
                        executor.shutdown();
                        //merge our xml files
                        if(rcCount<=0){
                             System.out.printf("\r\n%s files was scanned and no strings found.",inputFiles.size());
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
                                    if(file.exists()){
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
                        for(String line : lines){
                            if(line.startsWith("    <string")){
                                sb.append("\r\n"+line);
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
                            // replace any string name our new one
                            nameMatcher.appendReplacement(strbuffer, String.format(name, ++count));
                        }
                        //append the rest of text
                        nameMatcher.appendTail(strbuffer);
                        xmlCode = strbuffer.toString();
                        //write and save our xml code
                        writeFile(finalXml, xmlCode);
                        System.out.printf("\r\n%s strings was extracted from %s files and saved to %s in %s ms.",rcCount ,tempXmlFiles.size(), finalXml.getAbsolutePath(), System.currentTimeMillis() - startTime);
                    }
                }else if(result.get() && pathToScan.exists() && pathToScan.isDirectory()){
                    System.out.println("\r\nNo java or xml file found on path: "+pathToScan.getAbsolutePath());
                }
            } else {
                modeRecursive=false;
                rcCount=0;
                final long startTime = System.currentTimeMillis();
                ExecutorService executor = Executors.newSingleThreadExecutor();
                Future<Boolean> result = executor.submit(new Runnable(){

                    @Override
                    public void run() {
                        if(input == null) {
                            allDone=false;
                            System.out.println("input file is null.");
                            return;
                        } else if(!input.exists()) {
                            allDone=false;
                            System.out.println("input file is not exists.");
                            return;
                        }else if(input.isDirectory()){
                            allDone=false;
                            System.out.println("input file is a directory.");
                            return;
                        }
                        System.out.printf("Extracting strings of: %s", input.getAbsolutePath());
                        try {
                            extractString(input, xml, esc, prx, sfx, bkp);
                        } catch(Exception e) {
                            e.printStackTrace(System.out);
                        }
                    }
                }, true);
                if(result.get()) {
                    if(allDone){
                        System.out.printf("\r\nextracted strings from %s was saved to %s in %s ms.", input.getAbsolutePath(), xml != null ? xml.getAbsolutePath() : input.getAbsolutePath() + ".extracted_strings.xml", System.currentTimeMillis() - startTime);
                     }
                    executor.shutdown();
                }
            }
        }   
	}
    private static void findFiles(File dir, ArrayList<File> found) {
        if(dir.isFile()) {
           System.out.printf("\r\nFile: %s is not a directory", dir.getAbsolutePath());
           return;
        }else if(!dir.exists() || dir.listFiles()==null){
            System.out.printf("\r\nFile: %s is %s", dir.getAbsolutePath(),dir.exists() ? "not contains any files" : "not exists.");
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
    private static void showUsage(String option, String... message) {
        String help = "";
        help += option != "" ? " StringExtractor : unknown option '" + option + "'" : "";
        help += message.length >= 1 ? "\r\n" + message[0] : "";
        help += "\r\n  usage: StringExtractor -[r|b|c] -i FILE -d PATH -p TEXT -s TEXT -x FILE";
        help += "\r\n\r\n    Extract raw Strings from xml and java files of android app project to optional xml file and link them for example android:label=\"some text\" will become android:label=\"@string/extracted1\" and ";
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
    /**
     * Extracts the strings from single xml or java file
     * and save to xml file.
     *
     * @param fileToRead      the valid xml or java file to read and extract strings from.
     * @param xmlFile         the file to write the extracted strings.
     * @param extractedString if true and fileToRead is java file the class ExtractedString will be used to get the strings.
     * @param prefix          the String to use as prefix for generating strings name in xml
     * @param suffix          the String to use as suffix for generating strings name in xml
     * @param backupFile      indicates wether to backup the file before writing the extracted strings to file.
     * @throws Exception      if IOException occured.
     */
    public static void extractString(File fileToRead, File xmlFile, boolean extractedString, String prefix, String suffix, boolean backupFile)throws Exception {
        String fileString = readFile(fileToRead);
        Matcher stringMatcher = null;
        boolean javaCode = false;
        if(fileToRead.getName().endsWith(".java")) {
            stringMatcher = JAVA_STRING_PATTERN.matcher(fileString);
            javaCode = true;
        } else if(fileToRead.getName().endsWith(".xml")) {
            stringMatcher = XML_STRING_PATTERN.matcher(fileString);
        } else {
            System.out.println("fileToRead must be valid java or xml file. but got: " + fileToRead.getAbsolutePath());
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
        int count = modeRecursive ? rcCount : 0;
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
        if(modeRecursive)rcCount=count;
        if(xml_extracted.size() > 0 && backupFile) {
            fileToRead.renameTo(new File(fileToRead.getPath() + ".backup"));
        }
        if(extracted.size()<=0){
            System.out.printf("\r\nNo strings found on: %s",fileToRead.getAbsolutePath());
            return;
        }else {
            System.out.printf("\r\n%s strings was found on: %s",extracted.size(),fileToRead.getAbsolutePath());
        }
        //write the extracted strings to xml file
        FileOutputStream fout = new FileOutputStream(xmlFile);
        fout.write("<?xml version=\"1.0\" encoding=\"utf-8\"?>\r\n<resources>".getBytes());
        for(int i =0; i < xml_extracted.size();i++) {
            String str = xml_extracted.get(i);
            // remove the qoutes from string
            str = str.substring(1, str.length() - 1);
            fout.write(String.format("\r\n    <string name=\"%s\">%s</string>", extracted_name.get(i), str).getBytes());
        }
        fout.write("\r\n</resources>".getBytes());
        fout.flush();
        fout.close();
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
            String extStr ="";
            String pkgName = "";
            Matcher pkgMatcher = Pattern.compile("package .*?;").matcher(fileString);
            //try to find the package name for the file if possible
            if(pkgMatcher.find()) {
                pkgName = pkgMatcher.group();
            }
            extStr += pkgName != "" ? pkgName + "\r\n" : "";
            extStr += "\r\nimport android.content.Context;\r\n";
            extStr += "\r\n/** A helper class for retrieving the extracted strings. */\r\n";
            extStr += "\r\npublic final class ExtractedString {\r\n";
            extStr += "\r\n    /** The Context we use to retrieves the strings from xml resources. */\r\n";
            extStr += "\r\n    private static Context mContext;\r\n";
            extStr += "\r\n    /**";
            extStr += "\r\n     * we need to store our context here to retrieve the strings from every class. ";
            extStr += "\r\n     * this method need to called once from {@link Application} or {@link Activity} onCreate.";
            extStr += "\r\n     * @param context      The {@link Context} to store.";
            extStr += "\r\n     */";
            extStr += "\r\n    public static void setContext(Context context){";
            extStr += "\r\n        mContext = context;";
            extStr += "\r\n    }\r\n";
            extStr += "\r\n    /**";
            extStr += "\r\n     * retrieves our extracted string.";
            extStr += "\r\n     * @param strResId the string id generated by StringExtractor.";
            extStr += "\r\n     * @return the extracted string if our {@link Context} not null. \"\" otherwise.";
            extStr += "\r\n     */";
            extStr += "\r\n     public static String getString(int strResId) {";
            extStr += "\r\n         if(mContext != null){";
            extStr += "\r\n             return mContext.getResources().getString(strResId);";
            extStr += "\r\n          }";
            extStr += "\r\n         return \"\";";
            extStr += "\r\n     }";
            extStr += "\r\n}";
            writeFile(new File(fileToRead.getParent(), "ExtractedString.java"), extStr);
        }
    };

    /**
     * return the string read from the given file.
     */
    private static String readFile(File f) throws Exception {
        String str = "";
        int buf = (int) f.length();
        InputStream in = new FileInputStream(f);
        byte[] data = new byte[buf];
        in.read(data);
        in.close();
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        out.write(data);
        str = out.toString();
        out = null;

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

