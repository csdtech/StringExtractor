# StringExtractor
StringExtractor is used to extracts raw strings from a java and xml files of Android  apps project to a optional xml file and link them
## Usage
you can include StringExtractor as library or in your code to extract the strings.

This is a sample code to extracts the strings from JavaFile.java and save to myapp_strings.xml
```java
File inputFile = new File("JavaFile.java");
File xmlFile = new File("myapp_strings.xml");
String prefix = "myapp_";
String suffix = "string";
//Exracts the strings
try{
    StringExtractor.extractString(inputFile,xmlFile,/*ExtractedString=*/true,prefix,suffix,/*backupFile=*/true);
}catch (ExtractionException e){
    //handle errors here
}
```
Because we set backupFile to true the file JavaFile.java will be renamed to JavaFile.java.backup before writing the modified code to JavaFile.java<br>the file ```ExtractedString.java``` will be created at the same directory of inputFile.<br>every non blank string will be replaced with ```getResources().getString(R.string.myapp_string%s)``` or ```ExtractedString.getString(R.string.myapp_string%s)``` if ```extractedString=true```.<br>while %s is replaced with the number from 1 to total strings found.
## Command Line
 firstly compile the file using
```
javac com.sdtech.stringextractor.StringExtractorTerminal.java
```
 and then executes with
<pre>
java com.sdtech.stringextractor.StringExtractorTerminal -[r|b|c] -i FILE -d PATH -p TEXT -s TEXT -x FILE
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
-c        use class ExtractedString for getting strings from java files.
-h        show this usage message.
</pre>
### ExtractedString
when you use option -c from terminal or set ExtractedString to true the file 'ExtractedString.java' will be create in the same directory with first java file found.
 If you are using string in model class which do not have a context instance it is recommended to use ExtractedString, because you can modify the file to use ContextCompat instead of Context.
 for getting the strings.
 
 You must call ```ExtractedString.setContext(context);``` in your application or activity onCreate method.
 
 for example in your Application you can use
 ```java
 @Override
 public void onCreate(){
     super.onCreate();
     ExtractedString.setContext(this);
}
 ```
### Custom Implementation
you can read the documentation [Here](/docs) for custom implementation.
>#### Note: StringExtractor is only for android apps project. 
