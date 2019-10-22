[![APACHE v2 License](https://img.shields.io/badge/license-apachev2-blue.svg?style=flat)](LICENSE-2.0.txt) 
[![Latest Release](https://img.shields.io/maven-central/v/com.github.bbottema/rtf-to-html.svg?style=flat)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.bbottema%22%20AND%20a%3A%22rtf-to-html%22) 
[![Javadocs](http://www.javadoc.io/badge/com.github.bbottema/rtf-to-html.svg)](http://www.javadoc.io/doc/com.github.bbottema/rtf-to-html)
[![Codacy](https://img.shields.io/codacy/grade/2686e9520dcc4833b3205d7bc89c3678.svg?style=flat)](https://www.codacy.com/app/b-bottema/rtf-to-html)

# rtf-to-html #

The world's only RFC compliant RTF to HTML parser.

rtf-to-html is available in Maven Central:

```
<dependency>
    <groupId>com.github.bbottema</groupId>
    <artifactId>rtf-to-html</artifactId>
    <version>1.0.0</version>
</dependency>
```

## Usage

There are **three versions** of RTF to HTML for you to discover:

* RTF2HTMLConverterJEditorPane: A converter that invokes Swing's native RTF support
* RTF2HTMLConverterClassic: The orignal custom (regex-based) built converter with reasonable results
* RTF2HTMLConverterJEditorPane: The improved RFC-compliant parser with the most correct outcome

```java
RTF2HTMLConverter converter = RTF2HTMLConverterJEditorPane.INSTANCE;
RTF2HTMLConverter converter = RTF2HTMLConverterClassic.INSTANCE;
RTF2HTMLConverter converter = RTF2HTMLConverterRFCCompliant.INSTANCE;

String html = converter.rtf2html("RTF text");
```


---


### Latest Progress ###


v1.0.1 (22-October-2019)

- [#1](https://github.com/bbottema/rtf-to-html/issues/1): Missing support for UTF-8's legacy name (cp)65001


v1.0.0 (12-October-2019)

- Initial release, moved from outlook-message-parser