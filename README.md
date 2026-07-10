[![APACHE v2 License](https://img.shields.io/badge/license-apachev2-blue.svg?style=flat)](LICENSE-2.0.txt) 
[![Latest Release](https://img.shields.io/maven-central/v/com.github.bbottema/rtf-to-html.svg?style=flat)](http://search.maven.org/#search%7Cgav%7C1%7Cg%3A%22com.github.bbottema%22%20AND%20a%3A%22rtf-to-html%22) 
[![Javadocs](http://www.javadoc.io/badge/com.github.bbottema/rtf-to-html.svg)](http://www.javadoc.io/doc/com.github.bbottema/rtf-to-html)
[![Codacy](https://img.shields.io/codacy/grade/2686e9520dcc4833b3205d7bc89c3678.svg?style=flat)](https://www.codacy.com/app/b-bottema/rtf-to-html)

# rtf-to-html

General-purpose RTF parsing and HTML conversion for Java, with first-class Outlook/Exchange
encapsulated RTF support.

This library started life inside
[outlook-message-parser](https://github.com/bbottema/outlook-message-parser), where the main problem is
turning Outlook/Exchange RTF body content into usable HTML. The core is now a normal RTF parser and
renderer; Outlook handling is an extension on top of that core.

rtf-to-html is available in Maven Central. The current branch contains unreleased breaking API changes;
the latest published release is still:

```
<dependency>
    <groupId>com.github.bbottema</groupId>
    <artifactId>rtf-to-html</artifactId>
    <version>1.1.1</version>
</dependency>
```

## Usage

There are several converters available:

* `StandardRtfToHtmlConverter`: the general-purpose RTF-to-HTML converter
* `OutlookRtfToHtmlConverter`: the Outlook/MS-OXRTFEX-aware converter used by `outlook-message-parser`
* `legacy.ClassicRtfToHtmlConverter`: the inherited regex-based converter, kept for comparison
* `legacy.JEditorPaneRtfToHtmlConverter`: Swing's built-in limited RTF parser, kept for comparison

```java
RtfToHtmlConverter converter = StandardRtfToHtmlConverter.INSTANCE;

String html = converter.toHtml(rtf);
```

For Outlook `.msg` bodies, callers that receive raw RTF bytes, or callers that want to extract embedded
images:

```java
RtfToHtmlConverter converter = new OutlookRtfToHtmlConverter(RtfToHtmlOptions.builder()
    .imageHandler(image -> saveImageAndReturnSrc(image))
    .build());

String html = converter.toHtml(rtfBytes);
```

The parser is also public:

```java
RtfDocument document = new RtfParser().parse(rtfBytes);
```

Outlook `\fromhtml` RTF extracts the original HTML. Outlook `\fromtext` RTF returns escaped HTML using
a `<div style="white-space:pre-wrap">` wrapper so plain-text email line breaks survive without imposing
browser defaults such as monospace fonts.

See [docs/rtf-architecture-and-standards.md](docs/rtf-architecture-and-standards.md) for the RTF,
MS-OXRTFEX, parser, and renderer rules used by the converters.


---


### Latest Progress ###

Unreleased

- 10-July-2026: Fixed Outlook `\fromtext` HTML output to preserve whitespace without a `<pre>` wrapper,
  preventing browser default monospace styling in downstream renderers such as Simple Java Mail
  ([simple-java-mail#651](https://github.com/bbottema/simple-java-mail/issues/651)).
- 06-July-2026: [#15](https://github.com/bbottema/rtf-to-html/issues/15): Published a valid
  JPMS automatic module name, `org.bbottema.rtftohtml`, for `module-info.java` consumers.
- 06-July-2026: Breaking overhaul: replaced the old `RTF2HTMLConverter` API with `RtfToHtmlConverter`,
  `StandardRtfToHtmlConverter`, and `OutlookRtfToHtmlConverter`.
- 06-July-2026: Added a public `RtfParser` and document model for groups, control words, control
  symbols, text, escaped bytes, binary payloads, and source offsets.
- 06-July-2026: Reframed the project as a general-purpose RTF parser/renderer with an Outlook
  MS-OXRTFEX extension, instead of an Outlook-only converter.
- 06-July-2026: Removed the misleading RFC-compliant converter and moved the classic regex and
  JEditorPane converters to `org.bbottema.rtftohtml.legacy`.
- 06-July-2026: Added standards and architecture notes in
  [docs/rtf-architecture-and-standards.md](docs/rtf-architecture-and-standards.md).
- 06-July-2026: Expanded coverage for parser edge cases, standard rendering, Outlook encapsulation,
  image extraction, charset handling, Unicode fallback, byte input, and public API invariants.


v1.1.0 - v1.1.1

- 08-June-2024: [#14](https://github.com/bbottema/rtf-to-html/issues/14): Bullet numbers in list items have double numbers
- 25-May-2024: [#13](https://github.com/bbottema/rtf-to-html/issues/13): Charset should be determined based on the RTF's ansicpg control word


v1.0.1 (22-October-2019)

- [#1](https://github.com/bbottema/rtf-to-html/issues/1): Missing support for UTF-8's legacy name (cp)65001


v1.0.0 (12-October-2019)

- Initial release, moved from outlook-message-parser
