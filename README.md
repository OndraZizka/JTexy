###### TODO: Translate this back to Markdown. Damn Google Code!

*Taken from https://code.google.com/archive/p/jtexy/*


# JTexy - Port of the Texy! project to Java

[Texy!](http://texy.info/en/) is a text tool written in PHP that translates a natural text markup mixed with HTML into a valid (X)HTML code.

JTexy aims to be a fully compatible Java implementation of Texy! markup language.
Also see [my original Java implementation suggestion](http://ondra.zizka.cz/stranky/programovani/java/texy_java_implementation.texy).

## Current release: 1.0.4

**2012-11-12**: JTexy reached the point where it's actually usable, I've decided to release version 1.0.0.
Available in the Maven repository. The Sandbox will be updated later.

Whole 1.x version can be considered as path to compatibility with Texy 2.0.
When that is reached, 2.0.0 will be released and Texy 2.1 improvements porting will begin.

## What does it do?

Converts a concise easy-to-write syntax like this

    Title  .[myHtmlId]{my-css-class1 class2}
    ============================================

    Hello //world!//
    How are you?
    I'm fine. Look at my "blog":<a href="http://www.blog.cz/">http://www.blog.cz/</a>. And my photo: [* img/me.png *]</p>

    Subtitle
    --------

    /--code java
    System.out.println("That's all.");
    \-


to XHTML:

    <p id="myHtmlId" class=my-css-class1 class2">Title</p>

    <p>Hello <em>world!</em> How are you?
      <br>I'm fine. Look at my <a href="http://www.blog.cz/">blog</a>. And my photo: </p>

    <h2>Subtitle</h2>

    <code>System.out.println("That's all.");</code>


Looks like yet-another-wiki-markup, but (J)Texy can do much more - [see here](http://texy.info/en/), [try here](http://texy.info/cs/try/).


## Features


 * Natural syntax, really easy to learn, see here: <a href="http://texy.info/en/syntax">http://texy.info/en/syntax</a>
 * All the usual:
   * Headings, links, lists, images, horizontal lines
   * Strong, emphasis, lower & upper index, strike, citation, inline code, acronyms
   * Images support description, alternative text, onmouseover
   * Tables, allowing syntax inside cells, header cells, spanning cells
   * All constructs support CSS class definition, ID definition, alignment where available
   * Code blocks (optional syntax highlighting by SyntaxHighlighter)
   * Support for nested blocks of various types
   * Typography improvements (dashes, non-breaking spaces, ellipsis, quotes, arrows,...)
 * Supports text mixed with HTML

 * Fixes the embedded HTML to be valid
 * Auto-generated document header and TOC available through API


## Differences from Texy!


 * Faster: Texy: 4 ms vs. JTexy: 1.7 ms on the same input with the same output.
 * Expected to be ~ 2.5 ms after implementing all modules.
 * Configurable patterns separated from code
 * Does not support nested blocks (yet)


## Why is Texy better than other lightweight markup languages?

See [Wikipedia comparison](http://en.wikipedia.org/wiki/Lightweight_markup_language)
(More later).

## News log

 * 2018-07-19: First review after 6 years.
 * 2016-07-07: Moved from Google Code to GitHub
 * 2012-12-16: Version 1.0.4 released. Few bugs fixed, testsuite improved.
 * 2012-12-15: Version 1.0.2 released. Few bugs fixed, testsuite improved.
 * 2012-11-12: Version 1.0.0 released!
 * 2012-10-28: TypographyModule work started.
 * 2012-10-27: LinkModule made work.
 * 2012-10-23: Testsuite rewritten and reviewed.
 * 2010-01-26: Added Java WebStart launcher. GoogleCode bug/feature needs you to download, then launch.
 * 2010-01-26: Fifth snapshot - headings-related fixes.
 * 2010-01-24: Added **JTexy Sandbox** - a simple desktop app to try JTexy
 * 2010-01-21: Fourth snapshot release - lists work.
 * 2010-01-16: Third snapshot release - links work.
 * 2010-01-14: Second snapshot release.
 * 2010-01-12: First snapshot release.

<!-- Gadget: http://www.ohloh.net/p/323685/widgets/project_basic_stats.xml -->

## Implementation progress

Uncommented are done.
Numbers in parens denote expected priority.


    // line parsing
    (8)//module = this.scriptModule = new ScriptModule();
    (1)//module = this.htmlModule = new HtmlModule();
    module = this.imageModule = new ImageModule();      this.registerModule( module );
    module = this.phraseModule = new PhraseModule();    this.registerModule( module );
    module = this.linkModule = new LinkModule();        this.registerModule( module );
    (9)//module = this.emoticonModule = new EmoticonModule();  this.registerModule( module );</p>

    // block parsing
    module = this.paragraphModule = new ParagraphModule();      this.registerModule( module );
    module = this.blockModule = new BlockModule();              this.registerModule( module );
    (6)//module = this.figureModule = new FigureModule();            this.registerModule( module );
    module = this.horizLineModule = new HorizontalLineModule(); this.registerModule( module );
    (3)//module = this.blockQuoteModule = new BlockQuoteModule();    this.registerModule( module );
    (4)//module = this.tableModule = new TableModule();              this.registerModule( module );
    module = this.headingModule = new HeadingModule();          this.registerModule( module );
    module = this.listModule = new ListModule();                this.registerModule( module ); // Almost done.

    // post process
    (5)//module = this.typographyModule = new TypographyModule();              // In progress.
    (7)//module = this.longWordsModule = new LongWordsModule();
    (2)//module = this.htmlOutputModule = new HtmlOutputModule();


## TODOs

 * Create a test suite
 * Support recursive constructs - either by recursive regexs or code own.
 * Implement the rest of the modules (in the order of numbers by the list above)
 * Profile and benchmark.
 * Introduce some "ParsingContext" class to keep information for whole document parsing process.

Maven's settings.xml
--------------------

To run the testsuite, you need to set the `jtexy.phptexy.ts.dir` property in `settings.xml`, pointing to the testsuite dir:

    c:/java/JTexy/JTexy-google/src/phptexy20/testsuite
    c:/java/JTexy/JTexy-google/src/test/resources/cz/dynawest/jtexy/ts
    c:/java/JTexy/JTexy-google/target/ts-output

JTexy Maven repository
----------------------

To add JTexy as a maven dependency, add the repo to your pom.

    <project>
      ...
      <repositories>
         <repository>
            <id>jtexy-google-svn-repo</id>
            <snapshots> <enabled>true</enabled> </snapshots>
            <name>JTexy maven repo at Google Code</name>
            <url>http://jtexy.googlecode.com/svn/maven/</url>
         </repository>
      </repositories>
      ...
      <dependencies>
        <dependency>
          <groupId>cz.dynawest.jtexy</groupId>
          <artifactId>JTexy</artifactId>
          <version>1.0.4</version>
        </dependency>
      </dependencies>
    </project>

<a href="http://navrcholu.cz/Statistika/129225/"><img src="http://c1.navrcholu.cz/hit?site=129225;t=lb14;ref=;jss=0;foo.gif" alt="" title=""></a>
