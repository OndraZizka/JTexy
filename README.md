# TODO: Translate this back to Markdown. Damn Google Code!

Taken from https://code.google.com/archive/p/jtexy/


<h1>JTexy - Port of the Texy! project to Java</h1>

<p><a href="http://texy.info/en/">Texy!</a> is a text tool written in PHP that translates a natural text markup mixed with HTML into a valid (X)HTML code.</p>

<p>JTexy aims to be a fully compatible Java implementation of Texy! markup language.
Also see <a href="http://ondra.zizka.cz/stranky/programovani/java/texy_java_implementation.texy">my original Java implementation suggestion</a>.</p>

<h2>Current release: 1.0.4</h2>

<p><strong>2012-11-12</strong>: JTexy reached the point where it's actually usable, I've decided to release version 1.0.0.
Available in the Maven repository. The Sandbox will be updated later.</p>

<p>Whole 1.x version can be considered as path to compatibility with Texy 2.0.
When that is reached, 2.0.0 will be released and Texy 2.1 improvements porting will begin.</p>

<h2>What does it do?</h2>

Converts a concise easy-to-write syntax like this

    Title  .[myHtmlId]{my-css-class1 class2}</p>
    ============================================

    Hello //world!//
    How are you?
    I'm fine. Look at my "blog":<a href="http://www.blog.cz/">http://www.blog.cz/</a>. And my photo: [* img/me.png *]</p>
     
    Subtitle
    --------
     
    /--code java
    System.out.println("That's all.");
    \-
    

<p>to XHTML:</p>

    <p>Title</p>

    <p>Hello <em>world!</em> How are you?
      <br>I'm fine. Look at my <a href="http://www.blog.cz/">blog</a>. And my photo: </p>

    <h2>Subtitle</h2>
    
    <code>System.out.println("That's all.");</code>


Looks like yet-another-wiki-markup, but (J)Texy can do much more - [see here](http://texy.info/en/), [try here](http://texy.info/cs/try/).


<h2>Features</h2>

<ul>
<li>Natural syntax, really easy to learn, see here: <a href="http://texy.info/en/syntax">http://texy.info/en/syntax</a></li>
<li>All the usual:

<ul><li>Headings, links, lists, images, horizontal lines</li>
<li>Strong, emphasis, lower &amp; upper index, strike, citation, inline code, acronyms</li>
<li>Images support description, alternative text, onmouseover</li></ul></li>
<li>Tables, allowing syntax inside cells, header cells, spanning cells</li>
<li>All constructs support CSS class definition, ID definition, alignment where available</li>
<li>Code blocks (optional syntax higlighting by SyntaxHighlighter)</li>
<li>Support for nested blocks of various types</li>
<li>Typography improvements (dashes, non-breaking spaces, ellipsis, quotes, arrows,...)</li>
<li>Supports text mixed with HTML

<ul><li>Fixes the HTML to be valid</li></ul></li>
<li>Auto-generated document header and TOC available through API</li>
</ul>

<h2>Differences from Texy!</h2>

<ul>
<li>Faster: Texy: 4 ms vs. JTexy: 1.7 ms on the same input with the same output.

<ul><li>Expected to be ~ 2.5 ms after implementing all modules.</li></ul></li>
<li>Configurable patterns separated from code</li>
<li>Does not support nested blocks (yet)</li>
</ul>

<h2>Why is Texy better than other lightweight markup languages?</h2>

<p>See <a href="http://en.wikipedia.org/wiki/Lightweight_markup_language">Wikipedia comparison</a>.
(More later).</p>

<h2>News log</h2>

<ul>
<li>2012-12-16: Version 1.0.4 released. Few bugs fixed, testsuite improved.</li>
<li>2012-12-15: Version 1.0.2 released. Few bugs fixed, testsuite improved.</li>
<li>2012-11-12: Version 1.0.0 released!</li>
<li>2012-10-28: TypographyModule work started.</li>
<li>2012-10-27: LinkModule made work.</li>
<li>2012-10-23: Testsuite rewritten and reviewed.</li>
<li>2010-01-26: Added Java WebStart launcher. GoogleCode bug/feature needs you to download, then launch.</li>
<li>2010-01-26: Fifth snapshot - headings-related fixes.</li>
<li>2010-01-24: Added <strong>JTexy Sandbox</strong> - a simple desktop app to try JTexy</li>
<li>2010-01-21: Fourth snapshot release - lists work.</li>
<li>2010-01-16: Third snapshot release - links work.</li>
<li>2010-01-14: Second snapshot release.</li>
<li>2010-01-12: First snapshot release.</li>
</ul>

<p>&lt;wiki:gadget url="http://www.ohloh.net/p/323685/widgets/project_basic_stats.xml" height="220" border="1"/&gt;</p>

<p></p>

<h2>Implementation progress</h2>

<p>Uncommented are done.
Numbers in parens denote expected priority.</p>

<p>```
    // line parsing
    (8)//module = this.scriptModule = new ScriptModule();
    (1)//module = this.htmlModule = new HtmlModule();
    module = this.imageModule = new ImageModule();      this.registerModule( module );
    module = this.phraseModule = new PhraseModule();    this.registerModule( module );
    module = this.linkModule = new LinkModule();        this.registerModule( module );
    (9)//module = this.emoticonModule = new EmoticonModule();  this.registerModule( module );</p>

<pre><code>// block parsing
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
</code></pre>

<p>```</p>

<h2>TODOs</h2>

<ul>
<li>Create a test suite</li>
<li>Support recursive constructs - either by recursive regexs or code own.</li>
<li>Implement the rest of the modules (in the order of numbers by the list above)</li>
<li>Profile and benchmark.</li>
<li>Introduce some "ParsingContext" class to keep information for whole document parsing process.</li>
</ul>

Maven's settings.xml

<p>To run the testsuite, you need to set the <code>jtexy.phptexy.ts.dir</code> property in <code>settings.xml</code>, pointing to the testsuite dir:</p>

<p>```
    
      jtexy
       true 
      
        
        c:/java/JTexy/JTexy-google/src/phptexy20/testsuite
        
        c:/java/JTexy/JTexy-google/src/test/resources/cz/dynawest/jtexy/ts
        
        c:/java/JTexy/JTexy-google/target/ts-output</p>

<pre><code>  &lt;/properties&gt;
&lt;/profile&gt;
</code></pre>

<p>```</p>

JTexy Maven repository

<p>To add JTexy as a maven dependency, add the repo to your pom.</p>

<p><code>
&lt;project&gt;
  ...
  &lt;repositories&gt;
     &lt;repository&gt;
        &lt;id&gt;jtexy-google-svn-repo&lt;/id&gt;
        &lt;snapshots&gt; &lt;enabled&gt;true&lt;/enabled&gt; &lt;/snapshots&gt;
        &lt;name&gt;JTexy maven repo at Google Code&lt;/name&gt;
        &lt;url&gt;http://jtexy.googlecode.com/svn/maven/&lt;/url&gt;
     &lt;/repository&gt;
  &lt;/repositories&gt;
  ...
  &lt;dependencies&gt;
    &lt;dependency&gt;
      &lt;groupId&gt;cz.dynawest.jtexy&lt;/groupId&gt;
      &lt;artifactId&gt;JTexy&lt;/artifactId&gt;
      &lt;version&gt;1.0.4&lt;/version&gt;
    &lt;/dependency&gt;
  &lt;/dependencies&gt;
&lt;/project&gt;
</code></p>

<p><a href="http://navrcholu.cz/Statistika/129225/"><img src="http://c1.navrcholu.cz/hit?site=129225;t=lb14;ref=;jss=0;foo.gif" alt="" title=""></a></p>
