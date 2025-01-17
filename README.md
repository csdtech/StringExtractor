# StringExtractor
StringExtractor is used to extracts raw strings from a java and xml files of Android  apps project to a optional xml file and link them
## Usage
This is a sample code to extracts the strings from JavaFile.java and save to myapp_strings.xml
```java
File inputFile = new File("JavaFile.java");
File xmlFile = new File("myapp_strings.xml");
String prefix = "myapp_";
String suffix = "string";
//Exracts the strings
try{
    StringExtractor.extractString(inputFile,xmlFile,/*ExtractedString=*/true,prefix,suffix,/*backupFile=*/true);
}catch (Exception e){
    //handle errors here
}
```
Because we set backupFile to true the file JavaFile.java will be renamed to JavaFile.java.backup before writing the modified code to JavaFile.java<br>the file ```ExtractedString.java``` will be created at the same directory of inputFile.<br>every non blank string will be replaced with ```getResources().getString(R.string.myapp_string%s)``` or ```ExtractedString.getString(R.string.myapp_string%s)``` if ```extractedString=true```.<br>while %s is replaced with the number from 1 to total strings found.
## Command Line
 firstly compile the file using
```
javac StringExtractor.java
```
 and then executes with
<pre>
java StringExtractor -[r|b|c] -i FILE -d PATH -p TEXT -s TEXT -x FILE
</pre>
Options are:
<pre>
-i FILE   the single xml or java file to scan the string. options [-d,-r] are ignore if specified.
-d PATH   directory to scan for xml or java files. this option require -r to work.
-p TEXT   the prefix text to use when genarating xml string name.
-s TEXT   the suffix text to use when genarating xml string name.
-x FILE   the file to write the genarated xml.
-r        search for files recursively and extract their strings. this option require -d to be specified.
-b        backup the original file to filename.backup .
-c        use class ExtractedString for java files. this will generate java file 'ExtractedString.java' in the same directory of input file to access strings from classes that do not have a Context. and you must call ExtractedString.setContext(context) from your application or activity onCreate.
-h        show this usage message.
</pre>

> Note: StringExtractor is only for android apps project. 
