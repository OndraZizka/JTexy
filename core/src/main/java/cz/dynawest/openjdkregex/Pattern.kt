/*
 * Copyright 1999-2007 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Sun designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Sun in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 */
package cz.dynawest.openjdkregex

import java.io.IOException
import java.io.ObjectInputStream
import java.io.Serializable
import java.lang.Character.UnicodeBlock
import java.text.Normalizer
import java.util.*

/**
 * A compiled representation of a regular expression.
 *
 *
 *  A regular expression, specified as a string, must first be compiled into
 * an instance of this class.  The resulting pattern can then be used to create
 * a [Matcher] object that can match arbitrary [ ] against the regular
 * expression.  All of the state involved in performing a match resides in the
 * matcher, so many matchers can share the same pattern.
 *
 *
 *  A typical invocation sequence is thus
 *
 * <blockquote><pre>
 * Pattern p = Pattern.[compile][.compile]("a*b");
 * Matcher m = p.[matcher][.matcher]("aaaaab");
 * boolean b = m.[matches][Matcher.matches]();</pre></blockquote>
 *
 *
 *  A [matches][.matches] method is defined by this class as a
 * convenience for when a regular expression is used just once.  This method
 * compiles an expression and matches an input sequence against it in a single
 * invocation.  The statement
 *
 * <blockquote><pre>
 * boolean b = Pattern.matches("a*b", "aaaaab");</pre></blockquote>
 *
 * is equivalent to the three statements above, though for repeated matches it
 * is less efficient since it does not allow the compiled pattern to be reused.
 *
 *
 *  Instances of this class are immutable and are safe for use by multiple
 * concurrent threads.  Instances of the [Matcher] class are not safe for
 * such use.
 *
 *
 * <a name="sum">
</a> * <h4> Summary of regular-expression constructs </h4>
 *
 * <table border="0" cellpadding="1" cellspacing="0" summary="Regular expression constructs, and what they match">
 *
 * <tr align="left">
 * <th bgcolor="#CCCCFF" align="left" id="construct">Construct</th>
 * <th bgcolor="#CCCCFF" align="left" id="matches">Matches</th>
</tr> *
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="characters">Characters</th></tr>
 *
 * <tr><td valign="top" headers="construct characters">*x*</td>
 * <td headers="matches">The character *x*</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\\</tt></td>
 * <td headers="matches">The backslash character</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\0</tt>*n*</td>
 * <td headers="matches">The character with octal value <tt>0</tt>*n*
 * (0&nbsp;<tt>&lt;=</tt>&nbsp;*n*&nbsp;<tt>&lt;=</tt>&nbsp;7)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\0</tt>*nn*</td>
 * <td headers="matches">The character with octal value <tt>0</tt>*nn*
 * (0&nbsp;<tt>&lt;=</tt>&nbsp;*n*&nbsp;<tt>&lt;=</tt>&nbsp;7)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\0</tt>*mnn*</td>
 * <td headers="matches">The character with octal value <tt>0</tt>*mnn*
 * (0&nbsp;<tt>&lt;=</tt>&nbsp;*m*&nbsp;<tt>&lt;=</tt>&nbsp;3,
 * 0&nbsp;<tt>&lt;=</tt>&nbsp;*n*&nbsp;<tt>&lt;=</tt>&nbsp;7)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\x</tt>*hh*</td>
 * <td headers="matches">The character with hexadecimal&nbsp;value&nbsp;<tt>0x</tt>*hh*</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>&#92;u</tt>*hhhh*</td>
 * <td headers="matches">The character with hexadecimal&nbsp;value&nbsp;<tt>0x</tt>*hhhh*</td></tr>
 * <tr><td valign="top" headers="matches"><tt>\t</tt></td>
 * <td headers="matches">The tab character (<tt>'&#92;u0009'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\n</tt></td>
 * <td headers="matches">The newline (line feed) character (<tt>'&#92;u000A'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\r</tt></td>
 * <td headers="matches">The carriage-return character (<tt>'&#92;u000D'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\f</tt></td>
 * <td headers="matches">The form-feed character (<tt>'&#92;u000C'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\a</tt></td>
 * <td headers="matches">The alert (bell) character (<tt>'&#92;u0007'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\e</tt></td>
 * <td headers="matches">The escape character (<tt>'&#92;u001B'</tt>)</td></tr>
 * <tr><td valign="top" headers="construct characters"><tt>\c</tt>*x*</td>
 * <td headers="matches">The control character corresponding to *x*</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="classes">Character classes</th></tr>
 *
 * <tr><td valign="top" headers="construct classes"><tt>[abc]</tt></td>
 * <td headers="matches"><tt>a</tt>, <tt>b</tt>, or <tt>c</tt> (simple class)</td></tr>
 * <tr><td valign="top" headers="construct classes"><tt>[^abc]</tt></td>
 * <td headers="matches">Any character except <tt>a</tt>, <tt>b</tt>, or <tt>c</tt> (negation)</td></tr>
 * <tr><td valign="top" headers="construct classes"><tt>[a-zA-Z]</tt></td>
 * <td headers="matches"><tt>a</tt> through <tt>z</tt>
 * or <tt>A</tt> through <tt>Z</tt>, inclusive (range)</td></tr>
 * <tr><td valign="top" headers="construct classes"><tt>[a-d[m-p]]</tt></td>
 * <td headers="matches"><tt>a</tt> through <tt>d</tt>,
 * or <tt>m</tt> through <tt>p</tt>: <tt>[a-dm-p]</tt> (union)</td></tr>
 * <tr><td valign="top" headers="construct classes"><tt>[a-z&&[def]]</tt></td>
 * <td headers="matches"><tt>d</tt>, <tt>e</tt>, or <tt>f</tt> (intersection)</td></tr>
 * <tr><td valign="top" headers="construct classes"><tt>[a-z&&[^bc]]</tt></td>
 * <td headers="matches"><tt>a</tt> through <tt>z</tt>,
 * except for <tt>b</tt> and <tt>c</tt>: <tt>[ad-z]</tt> (subtraction)</td></tr>
 * <tr><td valign="top" headers="construct classes"><tt>[a-z&&[^m-p]]</tt></td>
 * <td headers="matches"><tt>a</tt> through <tt>z</tt>,
 * and not <tt>m</tt> through <tt>p</tt>: <tt>[a-lq-z]</tt>(subtraction)</td></tr>
 * <tr><th>&nbsp;</th></tr>
 *
 * <tr align="left"><th colspan="2" id="predef">Predefined character classes</th></tr>
 *
 * <tr><td valign="top" headers="construct predef"><tt>.</tt></td>
 * <td headers="matches">Any character (may or may not match [line terminators](#lt))</td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\d</tt></td>
 * <td headers="matches">A digit: <tt>[0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\D</tt></td>
 * <td headers="matches">A non-digit: <tt>[^0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\s</tt></td>
 * <td headers="matches">A whitespace character: <tt>[ \t\n\x0B\f\r]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\S</tt></td>
 * <td headers="matches">A non-whitespace character: <tt>[^\s]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\w</tt></td>
 * <td headers="matches">A word character: <tt>[a-zA-Z_0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct predef"><tt>\W</tt></td>
 * <td headers="matches">A non-word character: <tt>[^\w]</tt></td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="posix">POSIX character classes (US-ASCII only)****</th></tr>
 *
 * <tr><td valign="top" headers="construct posix"><tt>\p{Lower}</tt></td>
 * <td headers="matches">A lower-case alphabetic character: <tt>[a-z]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Upper}</tt></td>
 * <td headers="matches">An upper-case alphabetic character:<tt>[A-Z]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{ASCII}</tt></td>
 * <td headers="matches">All ASCII:<tt>[\x00-\x7F]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Alpha}</tt></td>
 * <td headers="matches">An alphabetic character:<tt>[\p{Lower}\p{Upper}]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Digit}</tt></td>
 * <td headers="matches">A decimal digit: <tt>[0-9]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Alnum}</tt></td>
 * <td headers="matches">An alphanumeric character:<tt>[\p{Alpha}\p{Digit}]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Punct}</tt></td>
 * <td headers="matches">Punctuation: One of <tt>!"#$%&'()*+,-./:;<=>?@[\]^_`{|}~</tt></td></tr>
 *
 * <tr><td valign="top" headers="construct posix"><tt>\p{Graph}</tt></td>
 * <td headers="matches">A visible character: <tt>[\p{Alnum}\p{Punct}]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Print}</tt></td>
 * <td headers="matches">A printable character: <tt>[\p{Graph}\x20]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Blank}</tt></td>
 * <td headers="matches">A space or a tab: <tt>[ \t]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Cntrl}</tt></td>
 * <td headers="matches">A control character: <tt>[\x00-\x1F\x7F]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{XDigit}</tt></td>
 * <td headers="matches">A hexadecimal digit: <tt>[0-9a-fA-F]</tt></td></tr>
 * <tr><td valign="top" headers="construct posix"><tt>\p{Space}</tt></td>
 * <td headers="matches">A whitespace character: <tt>[ \t\n\x0B\f\r]</tt></td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2">java.lang.Character classes (simple [java character type](#jcc))</th></tr>
 *
 * <tr><td valign="top"><tt>\p{javaLowerCase}</tt></td>
 * <td>Equivalent to java.lang.Character.isLowerCase()</td></tr>
 * <tr><td valign="top"><tt>\p{javaUpperCase}</tt></td>
 * <td>Equivalent to java.lang.Character.isUpperCase()</td></tr>
 * <tr><td valign="top"><tt>\p{javaWhitespace}</tt></td>
 * <td>Equivalent to java.lang.Character.isWhitespace()</td></tr>
 * <tr><td valign="top"><tt>\p{javaMirrored}</tt></td>
 * <td>Equivalent to java.lang.Character.isMirrored()</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="unicode">Classes for Unicode blocks and categories</th></tr>
 *
 * <tr><td valign="top" headers="construct unicode"><tt>\p{InGreek}</tt></td>
 * <td headers="matches">A character in the Greek&nbsp;block (simple [block](#ubc))</td></tr>
 * <tr><td valign="top" headers="construct unicode"><tt>\p{Lu}</tt></td>
 * <td headers="matches">An uppercase letter (simple [category](#ubc))</td></tr>
 * <tr><td valign="top" headers="construct unicode"><tt>\p{Sc}</tt></td>
 * <td headers="matches">A currency symbol</td></tr>
 * <tr><td valign="top" headers="construct unicode"><tt>\P{InGreek}</tt></td>
 * <td headers="matches">Any character except one in the Greek block (negation)</td></tr>
 * <tr><td valign="top" headers="construct unicode"><tt>[\p{L}&&[^\p{Lu}]]&nbsp;</tt></td>
 * <td headers="matches">Any letter except an uppercase letter (subtraction)</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="bounds">Boundary matchers</th></tr>
 *
 * <tr><td valign="top" headers="construct bounds"><tt>^</tt></td>
 * <td headers="matches">The beginning of a line</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>$</tt></td>
 * <td headers="matches">The end of a line</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\b</tt></td>
 * <td headers="matches">A word boundary</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\B</tt></td>
 * <td headers="matches">A non-word boundary</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\A</tt></td>
 * <td headers="matches">The beginning of the input</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\G</tt></td>
 * <td headers="matches">The end of the previous match</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\Z</tt></td>
 * <td headers="matches">The end of the input but for the final
 * [terminator](#lt), if&nbsp;any</td></tr>
 * <tr><td valign="top" headers="construct bounds"><tt>\z</tt></td>
 * <td headers="matches">The end of the input</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="greedy">Greedy quantifiers</th></tr>
 *
 * <tr><td valign="top" headers="construct greedy">*X*<tt>?</tt></td>
 * <td headers="matches">*X*, once or not at all</td></tr>
 * <tr><td valign="top" headers="construct greedy">*X*<tt>*</tt></td>
 * <td headers="matches">*X*, zero or more times</td></tr>
 * <tr><td valign="top" headers="construct greedy">*X*<tt>+</tt></td>
 * <td headers="matches">*X*, one or more times</td></tr>
 * <tr><td valign="top" headers="construct greedy">*X*<tt>{</tt>*n*<tt>}</tt></td>
 * <td headers="matches">*X*, exactly *n* times</td></tr>
 * <tr><td valign="top" headers="construct greedy">*X*<tt>{</tt>*n*<tt>,}</tt></td>
 * <td headers="matches">*X*, at least *n* times</td></tr>
 * <tr><td valign="top" headers="construct greedy">*X*<tt>{</tt>*n*<tt>,</tt>*m*<tt>}</tt></td>
 * <td headers="matches">*X*, at least *n* but not more than *m* times</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="reluc">Reluctant quantifiers</th></tr>
 *
 * <tr><td valign="top" headers="construct reluc">*X*<tt>??</tt></td>
 * <td headers="matches">*X*, once or not at all</td></tr>
 * <tr><td valign="top" headers="construct reluc">*X*<tt>*?</tt></td>
 * <td headers="matches">*X*, zero or more times</td></tr>
 * <tr><td valign="top" headers="construct reluc">*X*<tt>+?</tt></td>
 * <td headers="matches">*X*, one or more times</td></tr>
 * <tr><td valign="top" headers="construct reluc">*X*<tt>{</tt>*n*<tt>}?</tt></td>
 * <td headers="matches">*X*, exactly *n* times</td></tr>
 * <tr><td valign="top" headers="construct reluc">*X*<tt>{</tt>*n*<tt>,}?</tt></td>
 * <td headers="matches">*X*, at least *n* times</td></tr>
 * <tr><td valign="top" headers="construct reluc">*X*<tt>{</tt>*n*<tt>,</tt>*m*<tt>}?</tt></td>
 * <td headers="matches">*X*, at least *n* but not more than *m* times</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="poss">Possessive quantifiers</th></tr>
 *
 * <tr><td valign="top" headers="construct poss">*X*<tt>?+</tt></td>
 * <td headers="matches">*X*, once or not at all</td></tr>
 * <tr><td valign="top" headers="construct poss">*X*<tt>*+</tt></td>
 * <td headers="matches">*X*, zero or more times</td></tr>
 * <tr><td valign="top" headers="construct poss">*X*<tt>++</tt></td>
 * <td headers="matches">*X*, one or more times</td></tr>
 * <tr><td valign="top" headers="construct poss">*X*<tt>{</tt>*n*<tt>}+</tt></td>
 * <td headers="matches">*X*, exactly *n* times</td></tr>
 * <tr><td valign="top" headers="construct poss">*X*<tt>{</tt>*n*<tt>,}+</tt></td>
 * <td headers="matches">*X*, at least *n* times</td></tr>
 * <tr><td valign="top" headers="construct poss">*X*<tt>{</tt>*n*<tt>,</tt>*m*<tt>}+</tt></td>
 * <td headers="matches">*X*, at least *n* but not more than *m* times</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="logical">Logical operators</th></tr>
 *
 * <tr><td valign="top" headers="construct logical">*XY*</td>
 * <td headers="matches">*X* followed by *Y*</td></tr>
 * <tr><td valign="top" headers="construct logical">*X*<tt>|</tt>*Y*</td>
 * <td headers="matches">Either *X* or *Y*</td></tr>
 * <tr><td valign="top" headers="construct logical"><tt>(</tt>*X*<tt>)</tt></td>
 * <td headers="matches">X, as a [capturing group](#cg)</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="backref">Back references</th></tr>
 *
 * <tr><td valign="bottom" headers="construct backref"><tt>\</tt>*n*</td>
 * <td valign="bottom" headers="matches">Whatever the *n*<sup>th</sup>
 * [capturing group](#cg) matched</td></tr>
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="quot">Quotation</th></tr>
 *
 * <tr><td valign="top" headers="construct quot"><tt>\</tt></td>
 * <td headers="matches">Nothing, but quotes the following character</td></tr>
 * <tr><td valign="top" headers="construct quot"><tt>\Q</tt></td>
 * <td headers="matches">Nothing, but quotes all characters until <tt>\E</tt></td></tr>
 * <tr><td valign="top" headers="construct quot"><tt>\E</tt></td>
 * <td headers="matches">Nothing, but ends quoting started by <tt>\Q</tt></td></tr>
 *
 *
 * <tr><th>&nbsp;</th></tr>
 * <tr align="left"><th colspan="2" id="special">Special constructs (non-capturing)</th></tr>
 *
 * <tr><td valign="top" headers="construct special"><tt>(?:</tt>*X*<tt>)</tt></td>
 * <td headers="matches">*X*, as a non-capturing group</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?idmsuxU-idmsuxU)&nbsp;</tt></td>
 * <td headers="matches">Nothing, but turns match flags [i](#CASE_INSENSITIVE)
 * [d](#UNIX_LINES) [m](#MULTILINE) [s](#DOTALL)
 * [u](#UNICODE_CASE) [x](#COMMENTS) [U](#UNGREEDY) on - off</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?idmsuxU-idmsuxU:</tt>*X*<tt>)</tt>&nbsp;&nbsp;</td>
 * <td headers="matches">*X*, as a [non-capturing group](#cg) with the
 * given flags [i](#CASE_INSENSITIVE) [d](#UNIX_LINES)
 * [m](#MULTILINE) [s](#DOTALL) [u](#UNICODE_CASE)
 * [x](#COMMENTS) [U](#UNGREEDY) on - off</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?=</tt>*X*<tt>)</tt></td>
 * <td headers="matches">*X*, via zero-width positive lookahead</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?!</tt>*X*<tt>)</tt></td>
 * <td headers="matches">*X*, via zero-width negative lookahead</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?&lt;=</tt>*X*<tt>)</tt></td>
 * <td headers="matches">*X*, via zero-width positive lookbehind</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?&lt;!</tt>*X*<tt>)</tt></td>
 * <td headers="matches">*X*, via zero-width negative lookbehind</td></tr>
 * <tr><td valign="top" headers="construct special"><tt>(?&gt;</tt>*X*<tt>)</tt></td>
 * <td headers="matches">*X*, as an independent, non-capturing group</td></tr>
 *
</table> *
 *
 * <hr></hr>
 *
 *
 * <a name="bs">
 * <h4> Backslashes, escapes, and quoting </h4>
 *
 *
 *  The backslash character (<tt>'\'</tt>) serves to introduce escaped
 * constructs, as defined in the table above, as well as to quote characters
 * that otherwise would be interpreted as unescaped constructs.  Thus the
 * expression <tt>\\</tt> matches a single backslash and <tt>\{</tt> matches a
 * left brace.
 *
 *
 *  It is an error to use a backslash prior to any alphabetic character that
 * does not denote an escaped construct; these are reserved for future
 * extensions to the regular-expression language.  A backslash may be used
 * prior to a non-alphabetic character regardless of whether that character is
 * part of an unescaped construct.
 *
 *
 *  Backslashes within string literals in Java source code are interpreted
 * as required by the [Java Language
 * Specification](http://java.sun.com/docs/books/jls) as either [Unicode
 * escapes](http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#100850) or other [character
 * escapes](http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#101089).  It is therefore necessary to double backslashes in string
 * literals that represent regular expressions to protect them from
 * interpretation by the Java bytecode compiler.  The string literal
 * <tt>"&#92;b"</tt>, for example, matches a single backspace character when
 * interpreted as a regular expression, while <tt>"&#92;&#92;b"</tt> matches a
 * word boundary.  The string literal <tt>"&#92;(hello&#92;)"</tt> is illegal
 * and leads to a compile-time error; in order to match the string
 * <tt>(hello)</tt> the string literal <tt>"&#92;&#92;(hello&#92;&#92;)"</tt>
 * must be used.
 *
 * <a name="cc">
</a> * <h4> Character Classes </h4>
 *
 *
 *  Character classes may appear within other character classes, and
 * may be composed by the union operator (implicit) and the intersection
 * operator (<tt>&amp;&amp;</tt>).
 * The union operator denotes a class that contains every character that is
 * in at least one of its operand classes.  The intersection operator
 * denotes a class that contains every character that is in both of its
 * operand classes.
 *
 *
 *  The precedence of character-class operators is as follows, from
 * highest to lowest:
 *
 * <blockquote><table border="0" cellpadding="1" cellspacing="0" summary="Precedence of character class operators.">
 * <tr><th>1&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td>Literal escape&nbsp;&nbsp;&nbsp;&nbsp;</td>
 * <td><tt>\x</tt></td></tr>
 * <tr><th>2&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td>Grouping</td>
 * <td><tt>[...]</tt></td></tr>
 * <tr><th>3&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td>Range</td>
 * <td><tt>a-z</tt></td></tr>
 * <tr><th>4&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td>Union</td>
 * <td><tt>[a-e][i-u]</tt></td></tr>
 * <tr><th>5&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td>Intersection</td>
 * <td><tt>[a-z&&[aeiou]]</tt></td></tr>
</table></blockquote> *
 *
 *
 *  Note that a different set of metacharacters are in effect inside
 * a character class than outside a character class. For instance, the
 * regular expression <tt>.</tt> loses its special meaning inside a
 * character class, while the expression <tt>-</tt> becomes a range
 * forming metacharacter.
 *
 * <a name="lt">
</a> * <h4> Line terminators </h4>
 *
 *
 *  A *line terminator* is a one- or two-character sequence that marks
 * the end of a line of the input character sequence.  The following are
 * recognized as line terminators:
 *
 *
 *
 *  *  A newline (line feed) character&nbsp;(<tt>'\n'</tt>),
 *
 *  *  A carriage-return character followed immediately by a newline
 * character&nbsp;(<tt>"\r\n"</tt>),
 *
 *  *  A standalone carriage-return character&nbsp;(<tt>'\r'</tt>),
 *
 *  *  A next-line character&nbsp;(<tt>'&#92;u0085'</tt>),
 *
 *  *  A line-separator character&nbsp;(<tt>'&#92;u2028'</tt>), or
 *
 *  *  A paragraph-separator character&nbsp;(<tt>'&#92;u2029</tt>).
 *
 *
 *
 * If [.UNIX_LINES] mode is activated, then the only line terminators
 * recognized are newline characters.
 *
 *
 *  The regular expression <tt>.</tt> matches any character except a line
 * terminator unless the [.DOTALL] flag is specified.
 *
 *
 *  By default, the regular expressions <tt>^</tt> and <tt>$</tt> ignore
 * line terminators and only match at the beginning and the end, respectively,
 * of the entire input sequence. If [.MULTILINE] mode is activated then
 * <tt>^</tt> matches at the beginning of input and after any line terminator
 * except at the end of input. When in [.MULTILINE] mode <tt>$</tt>
 * matches just before a line terminator or the end of the input sequence.
 *
 * <a name></a>"clo">
 * <h4> Closure greediness </h4>
 *
 *
 *  Closures are greedy by default. If [.UNGREEDY] flag is set, closures are lazy by default.
 *
 * <a name="cg">
 * <h4> Groups and capturing </h4>
 *
 *
 *  Capturing groups are numbered by counting their opening parentheses from
 * left to right.  In the expression <tt>((A)(B(C)))</tt>, for example, there
 * are four such groups:
 *
 * <blockquote><table cellpadding=1 cellspacing=0 summary="Capturing group numberings">
 * <tr><th>1&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td><tt>((A)(B(C)))</tt></td></tr>
 * <tr><th>2&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td><tt>(A)</tt></td></tr>
 * <tr><th>3&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td><tt>(B(C))</tt></td></tr>
 * <tr><th>4&nbsp;&nbsp;&nbsp;&nbsp;</th>
 * <td><tt>(C)</tt></td></tr>
</table></blockquote> *
 *
 *
 *  Group zero always stands for the entire expression.
 *
 *
 *  Capturing groups are so named because, during a match, each subsequence
 * of the input sequence that matches such a group is saved.  The captured
 * subsequence may be used later in the expression, via a back reference, and
 * may also be retrieved from the matcher once the match operation is complete.
 *
 *
 *  The captured input associated with a group is always the subsequence
 * that the group most recently matched.  If a group is evaluated a second time
 * because of quantification then its previously-captured value, if any, will
 * be retained if the second evaluation fails.  Matching the string
 * <tt>"aba"</tt> against the expression <tt>(a(b)?)+</tt>, for example, leaves
 * group two set to <tt>"b"</tt>.  All captured input is discarded at the
 * beginning of each match.
 *
 *
 *  Groups beginning with <tt>(?</tt> are pure, *non-capturing* groups
 * that do not capture text and do not count towards the group total.
 *
 *
 * <h4> Unicode support </h4>
 *
 *
 *  This class is in conformance with Level 1 of [*Unicode Technical
 * Standard #18: Unicode Regular Expression Guidelines*](http://www.unicode.org/reports/tr18/), plus RL2.1
 * Canonical Equivalents.
 *
 *
 *  Unicode escape sequences such as <tt>&#92;u2014</tt> in Java source code
 * are processed as described in [\u00A73.3](http://java.sun.com/docs/books/jls/third_edition/html/lexical.html#100850)
 * of the Java Language Specification.  Such escape sequences are also
 * implemented directly by the regular-expression parser so that Unicode
 * escapes can be used in expressions that are read from files or from the
 * keyboard.  Thus the strings <tt>"&#92;u2014"</tt> and <tt>"\\u2014"</tt>,
 * while not equal, compile into the same pattern, which matches the character
 * with hexadecimal value <tt>0x2014</tt>.
 *
 * <a name="ubc"> </a>
 *
 *Unicode blocks and categories are written with the
 * <tt>\p</tt> and <tt>\P</tt> constructs as in
 * Perl. <tt>\p{</tt>*prop*<tt>}</tt> matches if the input has the
 * property *prop*, while <tt>\P{</tt>*prop*<tt>}</tt> does not match if
 * the input has that property.  Blocks are specified with the prefix
 * <tt>In</tt>, as in <tt>InMongolian</tt>.  Categories may be specified with
 * the optional prefix <tt>Is</tt>: Both <tt>\p{L}</tt> and <tt>\p{IsL}</tt>
 * denote the category of Unicode letters.  Blocks and categories can be used
 * both inside and outside of a character class.
 *
 *
 *  The supported categories are those of
 * [
 * *The Unicode Standard*](http://www.unicode.org/unicode/standard/standard.html) in the version specified by the
 * [Character][java.lang.Character] class. The category names are those
 * defined in the Standard, both normative and informative.
 * The block names supported by `Pattern` are the valid block names
 * accepted and defined by
 * [UnicodeBlock.forName][java.lang.Character.UnicodeBlock.forName].
 *
 * <a name="jcc"> </a>
 *
 *Categories that behave like the java.lang.Character
 * boolean is*methodname* methods (except for the deprecated ones) are
 * available through the same <tt>\p{</tt>*prop*<tt>}</tt> syntax where
 * the specified property has the name <tt>java*methodname*</tt>.
 *
 * <h4> Comparison to Perl 5 </h4>
 *
 *
 * The `Pattern` engine performs traditional NFA-based matching
 * with ordered alternation as occurs in Perl 5.
 *
 *
 *  Perl constructs not supported by this class:
 *
 *
 *
 *  *
 *
 * The conditional constructs <tt>(?{</tt>*X*<tt>})</tt> and
 * <tt>(?(</tt>*condition*<tt>)</tt>*X*<tt>|</tt>*Y*<tt>)</tt>,
 *
 *
 *  *
 *
 * The embedded code constructs <tt>(?{</tt>*code*<tt>})</tt>
 * and <tt>(??{</tt>*code*<tt>})</tt>,
 *
 *  *
 *
 * The embedded comment syntax <tt>(?#comment)</tt>, and
 *
 *  *
 *
 * The preprocessing operations <tt>\l</tt> <tt>&#92;u</tt>,
 * <tt>\L</tt>, and <tt>\U</tt>.
 *
 *
 *
 *
 *  Constructs supported by this class but not by Perl:
 *
 *
 *
 *  *
 *
 * Possessive quantifiers, which greedily match as much as they can
 * and do not back off, even when doing so would allow the overall match to
 * succeed.
 *
 *  *
 *
 * Character-class union and intersection as described
 * [above](#cc).
 *
 *
 *
 *
 *  Notable differences from Perl:
 *
 *
 *
 *  *
 *
 * In Perl, <tt>\1</tt> through <tt>\9</tt> are always interpreted
 * as back references; a backslash-escaped number greater than <tt>9</tt> is
 * treated as a back reference if at least that many subexpressions exist,
 * otherwise it is interpreted, if possible, as an octal escape.  In this
 * class octal escapes must always begin with a zero. In this class,
 * <tt>\1</tt> through <tt>\9</tt> are always interpreted as back
 * references, and a larger number is accepted as a back reference if at
 * least that many subexpressions exist at that point in the regular
 * expression, otherwise the parser will drop digits until the number is
 * smaller or equal to the existing number of groups or it is one digit.
 *
 *
 *  *
 *
 * Perl uses the <tt>g</tt> flag to request a match that resumes
 * where the last match left off.  This functionality is provided implicitly
 * by the [Matcher] class: Repeated invocations of the [    ][Matcher.find] method will resume where the last match left off,
 * unless the matcher is reset.
 *
 *  *
 *
 * In Perl, embedded flags at the top level of an expression affect
 * the whole expression.  In this class, embedded flags always take effect
 * at the point at which they appear, whether they are at the top level or
 * within a group; in the latter case, flags are restored at the end of the
 * group just as in Perl.
 *
 *  *
 *
 * Perl is forgiving about malformed matching constructs, as in the
 * expression <tt>*a</tt>, as well as dangling brackets, as in the
 * expression <tt>abc]</tt>, and treats them as literals.  This
 * class also accepts dangling brackets but is strict about dangling
 * metacharacters like +, ? and *, and will throw a
 * [PatternSyntaxException] if it encounters them.
 *
 *
 *
 *
 *
 *  For a more precise description of the behavior of regular expression
 * constructs, please see [
 * *Mastering Regular Expressions, 3nd Edition*, Jeffrey E. F. Friedl,
 * O'Reilly and Associates, 2006.](http://www.oreilly.com/catalog/regex3/)
 *
 *
 * @see java.lang.String.split
 * @see java.lang.String.split
 * @author      Mike McCloskey
 * @author      Mark Reinhold
 * @author      JSR-51 Expert Group
 * @since       1.4
 * @spec        JSR-51
</a></a> */
class Pattern private constructor(
    /**
     * The original regular-expression pattern string.
     *
     * @serial
     */
    private val pattern: String,
    /**
     * The original pattern flags.
     *
     * @serial
     */
    private var flags: Int
) : Serializable {
    /**
     * Boolean indicating this Pattern is compiled; this is necessary in order
     * to lazily compile deserialized Patterns.
     */
    @Volatile
    @Transient
    private var compiled = false

    /**
     * The normalized pattern string.
     */
    @Transient
    private var normalizedPattern: String? = null

    /**
     * The starting point of state machine for the find operation.  This allows
     * a match to start anywhere in the input.
     */
    @Transient
    var root: Node? = null

    /**
     * The root of object tree for a match operation.  The pattern is matched
     * at the beginning.  This may include a find that uses BnM or a First
     * node.
     */
    @Transient
    var matchRoot: Node? = null

    /**
     * Temporary storage used by parsing pattern slice.
     */
    @Transient
    var buffer: IntArray?

    /**
     * Temporary storage used while parsing group references.
     */
    @Transient
    var groupNodes: Array<GroupHead?>?

    /**
     * Temporary null terminated code point array used by pattern compiling.
     */
    @Transient
    private var temp: IntArray?

    /**
     * The number of capturing groups in this Pattern. Used by matchers to
     * allocate storage needed to perform a match.
     */
    @Transient
    var capturingGroupCount = 1

    /**
     * The local variable count used by parsing tree. Used by matchers to
     * allocate storage needed to perform a match.
     */
    @Transient
    var localCount = 0

    /**
     * Index into the pattern string that keeps track of how much has been
     * parsed.
     */
    @Transient
    private var cursor = 0

    /**
     * Holds the length of the pattern string.
     */
    @Transient
    private var patternLength = 0

    /**
     * Returns the regular expression from which this pattern was compiled.
     *
     *
     * @return  The source of this pattern
     */
    fun pattern(): String {
        return pattern
    }

    /**
     *
     * Returns the string representation of this pattern. This
     * is the regular expression from which this pattern was
     * compiled.
     *
     * @return  The string representation of this pattern
     * @since 1.5
     */
    override fun toString(): String {
        return pattern
    }

    /**
     * Creates a matcher that will match the given input against this pattern.
     *
     *
     * @param  input
     * The character sequence to be matched
     *
     * @return  A new matcher for this pattern
     */
    fun matcher(input: CharSequence?): Matcher {
        if (!compiled) {
            synchronized(this) { if (!compiled) compile() }
        }
        return Matcher(this, input)
    }

    /**
     * Returns this pattern's match flags.
     *
     * @return  The match flags specified when this pattern was compiled
     */
    fun flags(): Int {
        return flags
    }
    /**
     * Splits the given input sequence around matches of this pattern.
     *
     *
     *  The array returned by this method contains each substring of the
     * input sequence that is terminated by another subsequence that matches
     * this pattern or is terminated by the end of the input sequence.  The
     * substrings in the array are in the order in which they occur in the
     * input.  If this pattern does not match any subsequence of the input then
     * the resulting array has just one element, namely the input sequence in
     * string form.
     *
     *
     *  The <tt>limit</tt> parameter controls the number of times the
     * pattern is applied and therefore affects the length of the resulting
     * array.  If the limit *n* is greater than zero then the pattern
     * will be applied at most *n*&nbsp;-&nbsp;1 times, the array's
     * length will be no greater than *n*, and the array's last entry
     * will contain all input beyond the last matched delimiter.  If *n*
     * is non-positive then the pattern will be applied as many times as
     * possible and the array can have any length.  If *n* is zero then
     * the pattern will be applied as many times as possible, the array can
     * have any length, and trailing empty strings will be discarded.
     *
     *
     *  The input <tt>"boo:and:foo"</tt>, for example, yields the following
     * results with these parameters:
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex, limit, and result">
     * <tr><th><P align="left">*Regex&nbsp;&nbsp;&nbsp;&nbsp;*</P></th>
     * <th><P align="left">*Limit&nbsp;&nbsp;&nbsp;&nbsp;*</P></th>
     * <th><P align="left">*Result&nbsp;&nbsp;&nbsp;&nbsp;*</P></th></tr>
     * <tr><td align=center>:</td>
     * <td align=center>2</td>
     * <td><tt>{ "boo", "and:foo" }</tt></td></tr>
     * <tr><td align=center>:</td>
     * <td align=center>5</td>
     * <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>:</td>
     * <td align=center>-2</td>
     * <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>o</td>
     * <td align=center>5</td>
     * <td><tt>{ "b", "", ":and:f", "", "" }</tt></td></tr>
     * <tr><td align=center>o</td>
     * <td align=center>-2</td>
     * <td><tt>{ "b", "", ":and:f", "", "" }</tt></td></tr>
     * <tr><td align=center>o</td>
     * <td align=center>0</td>
     * <td><tt>{ "b", "", ":and:f" }</tt></td></tr>
    </table></blockquote> *
     *
     *
     * @param  input
     * The character sequence to be split
     *
     * @param  limit
     * The result threshold, as described above
     *
     * @return  The array of strings computed by splitting the input
     * around matches of this pattern
     */
    /**
     * Splits the given input sequence around matches of this pattern.
     *
     *
     *  This method works as if by invoking the two-argument [ ][.split] method with the given input
     * sequence and a limit argument of zero.  Trailing empty strings are
     * therefore not included in the resulting array.
     *
     *
     *  The input <tt>"boo:and:foo"</tt>, for example, yields the following
     * results with these expressions:
     *
     * <blockquote><table cellpadding=1 cellspacing=0 summary="Split examples showing regex and result">
     * <tr><th><P align="left">*Regex&nbsp;&nbsp;&nbsp;&nbsp;*</P></th>
     * <th><P align="left">*Result*</P></th></tr>
     * <tr><td align=center>:</td>
     * <td><tt>{ "boo", "and", "foo" }</tt></td></tr>
     * <tr><td align=center>o</td>
     * <td><tt>{ "b", "", ":and:f" }</tt></td></tr>
    </table></blockquote> *
     *
     *
     * @param  input
     * The character sequence to be split
     *
     * @return  The array of strings computed by splitting the input
     * around matches of this pattern
     */
    @JvmOverloads
    fun split(input: CharSequence, limit: Int = 0): Array<String> {
        var index = 0
        val matchLimited = limit > 0
        val matchList = ArrayList<String>()
        val m = matcher(input)

        // Add segments before each match found
        while (m.find()) {
            if (!matchLimited || matchList.size < limit - 1) {
                val match = input.subSequence(index, m.start()).toString()
                matchList.add(match)
                index = m.end()
            } else if (matchList.size == limit - 1) { // last one
                val match = input.subSequence(
                    index,
                    input.length
                ).toString()
                matchList.add(match)
                index = m.end()
            }
        }

        // If no match was found, return this
        if (index == 0) return arrayOf(input.toString())

        // Add remaining segment
        if (!matchLimited || matchList.size < limit) matchList.add(input.subSequence(index, input.length).toString())

        // Construct result
        var resultSize = matchList.size
        if (limit == 0) while (resultSize > 0 && matchList[resultSize - 1] == "") resultSize--
        val result = arrayOfNulls<String>(resultSize)
        return matchList.subList(0, resultSize).toArray(result)
    }

    /**
     * Recompile the Pattern instance from a stream.  The original pattern
     * string is read in and the object tree is recompiled from it.
     */
    @Throws(IOException::class, ClassNotFoundException::class)
    private fun readObject(s: ObjectInputStream) {

        // Read in all fields
        s.defaultReadObject()

        // Initialize counts
        capturingGroupCount = 1
        localCount = 0

        // if length > 0, the Pattern is lazily compiled
        compiled = false
        if (pattern.length == 0) {
            root = Start(lastAccept)
            matchRoot = lastAccept
            compiled = true
        }
    }

    /**
     * The pattern is converted to normalizedD form and then a pure group
     * is constructed to match canonical equivalences of the characters.
     */
    private fun normalize() {
        val inCharClass = false
        var lastCodePoint = -1

        // Convert pattern into normalizedD form
        normalizedPattern = Normalizer.normalize(pattern, Normalizer.Form.NFD)
        patternLength = normalizedPattern.length

        // Modify pattern to match canonical equivalences
        val newPattern = StringBuilder(patternLength)
        var i = 0
        while (i < patternLength) {
            var c = normalizedPattern.codePointAt(i)
            var sequenceBuffer: StringBuilder
            if (Character.getType(c) == Character.NON_SPACING_MARK.toInt() && lastCodePoint != -1) {
                sequenceBuffer = StringBuilder()
                sequenceBuffer.appendCodePoint(lastCodePoint)
                sequenceBuffer.appendCodePoint(c)
                while (Character.getType(c) == Character.NON_SPACING_MARK.toInt()) {
                    i += Character.charCount(c)
                    if (i >= patternLength) break
                    c = normalizedPattern.codePointAt(i)
                    sequenceBuffer.appendCodePoint(c)
                }
                val ea = produceEquivalentAlternation(
                    sequenceBuffer.toString()
                )
                newPattern.setLength(newPattern.length - Character.charCount(lastCodePoint))
                newPattern.append("(?:").append(ea).append(")")
            } else if (c == '['.code && lastCodePoint != '\\'.code) {
                i = normalizeCharClass(newPattern, i)
            } else {
                newPattern.appendCodePoint(c)
            }
            lastCodePoint = c
            i += Character.charCount(c)
        }
        normalizedPattern = newPattern.toString()
    }

    /**
     * Complete the character class being parsed and add a set
     * of alternations to it that will match the canonical equivalences
     * of the characters within the class.
     */
    private fun normalizeCharClass(newPattern: StringBuilder, i: Int): Int {
        var i = i
        val charClass = StringBuilder()
        var eq: StringBuilder? = null
        var lastCodePoint = -1
        val result: String
        i++
        charClass.append("[")
        while (true) {
            var c = normalizedPattern!!.codePointAt(i)
            var sequenceBuffer: StringBuilder
            if (c == ']'.code && lastCodePoint != '\\'.code) {
                charClass.append(c.toChar())
                break
            } else if (Character.getType(c) == Character.NON_SPACING_MARK.toInt()) {
                sequenceBuffer = StringBuilder()
                sequenceBuffer.appendCodePoint(lastCodePoint)
                while (Character.getType(c) == Character.NON_SPACING_MARK.toInt()) {
                    sequenceBuffer.appendCodePoint(c)
                    i += Character.charCount(c)
                    if (i >= normalizedPattern!!.length) break
                    c = normalizedPattern!!.codePointAt(i)
                }
                val ea = produceEquivalentAlternation(
                    sequenceBuffer.toString()
                )
                charClass.setLength(charClass.length - Character.charCount(lastCodePoint))
                if (eq == null) eq = StringBuilder()
                eq.append('|')
                eq.append(ea)
            } else {
                charClass.appendCodePoint(c)
                i++
            }
            if (i == normalizedPattern!!.length) throw error("Unclosed character class")
            lastCodePoint = c
        }
        result = if (eq != null) {
            "(?:$charClass$eq)"
        } else {
            charClass.toString()
        }
        newPattern.append(result)
        return i
    }

    /**
     * Given a specific sequence composed of a regular character and
     * combining marks that follow it, produce the alternation that will
     * match all canonical equivalences of that sequence.
     */
    private fun produceEquivalentAlternation(source: String): String {
        val len = countChars(source, 0, 1)
        if (source.length == len) // source has one character.
            return source
        val base = source.substring(0, len)
        val combiningMarks = source.substring(len)
        val perms = producePermutations(combiningMarks)
        val result = StringBuilder(source)

        // Add combined permutations
        for (x in perms.indices) {
            var next: String? = base + perms[x]
            if (x > 0) result.append("|$next")
            next = composeOneStep(next)
            if (next != null) result.append("|" + produceEquivalentAlternation(next))
        }
        return result.toString()
    }

    /**
     * Returns an array of strings that have all the possible
     * permutations of the characters in the input string.
     * This is used to get a list of all possible orderings
     * of a set of combining marks. Note that some of the permutations
     * are invalid because of combining class collisions, and these
     * possibilities must be removed because they are not canonically
     * equivalent.
     */
    private fun producePermutations(input: String): Array<String?> {
        if (input.length == countChars(input, 0, 1)) return arrayOf(input)
        if (input.length == countChars(input, 0, 2)) {
            val c0 = Character.codePointAt(input, 0)
            val c1 = Character.codePointAt(input, Character.charCount(c0))
            if (getClass(c1) == getClass(c0)) {
                return arrayOf(input)
            }
            val result = arrayOfNulls<String>(2)
            result[0] = input
            val sb = StringBuilder(2)
            sb.appendCodePoint(c1)
            sb.appendCodePoint(c0)
            result[1] = sb.toString()
            return result
        }
        var length = 1
        val nCodePoints = countCodePoints(input)
        for (x in 1 until nCodePoints) length = length * (x + 1)
        val temp = arrayOfNulls<String>(length)
        val combClass = IntArray(nCodePoints)
        run {
            var x = 0
            var i = 0
            while (x < nCodePoints) {
                val c = Character.codePointAt(input, i)
                combClass[x] = getClass(c)
                i += Character.charCount(c)
                x++
            }
        }

        // For each char, take it out and add the permutations
        // of the remaining chars
        var index = 0
        var len: Int
        var x = 0
        var offset = 0
        loop@ while (x < nCodePoints) {
            len = countChars(input, offset, 1)
            val skip = false
            for (y in x - 1 downTo 0) {
                if (combClass[y] == combClass[x]) {
                    x++
                    offset += len
                    continue@loop
                }
            }
            val sb = StringBuilder(input)
            val otherChars = sb.delete(offset, offset + len).toString()
            val subResult = producePermutations(otherChars)
            val prefix = input.substring(offset, offset + len)
            for (y in subResult.indices) temp[index++] = prefix + subResult[y]
            x++
            offset += len
        }
        val result = arrayOfNulls<String>(index)
        for (x in 0 until index) result[x] = temp[x]
        return result
    }

    private fun getClass(c: Int): Int {
        return sun.text.Normalizer.getCombiningClass(c)
    }

    /**
     * Attempts to compose input by combining the first character
     * with the first combining mark following it. Returns a String
     * that is the composition of the leading character with its first
     * combining mark followed by the remaining combining marks. Returns
     * null if the first two characters cannot be further composed.
     */
    private fun composeOneStep(input: String?): String? {
        val len = countChars(input, 0, 2)
        val firstTwoCharacters = input!!.substring(0, len)
        val result = Normalizer.normalize(firstTwoCharacters, Normalizer.Form.NFC)
        return if (result == firstTwoCharacters) null else {
            val remainder = input.substring(len)
            result + remainder
        }
    }

    /**
     * Preprocess any \Q...\E sequences in `temp', meta-quoting them.
     * See the description of `quotemeta' in perlfunc(1).
     */
    private fun RemoveQEQuoting() {
        val pLen = patternLength
        var i = 0
        while (i < pLen - 1) {
            i += if (temp!![i] != '\\'.code) 1 else if (temp!![i + 1] != 'Q'.code) 2 else break
        }
        if (i >= pLen - 1) // No \Q sequence found
            return
        var j = i
        i += 2
        val newtemp = IntArray(j + 2 * (pLen - i) + 2)
        System.arraycopy(temp, 0, newtemp, 0, j)
        var inQuote = true
        while (i < pLen) {
            val c = temp!![i++]
            if (!ASCII.isAscii(c) || ASCII.isAlnum(c)) {
                newtemp[j++] = c
            } else if (c != '\\'.code) {
                if (inQuote) newtemp[j++] = '\\'.code
                newtemp[j++] = c
            } else if (inQuote) {
                if (temp!![i] == 'E'.code) {
                    i++
                    inQuote = false
                } else {
                    newtemp[j++] = '\\'.code
                    newtemp[j++] = '\\'.code
                }
            } else {
                if (temp!![i] == 'Q'.code) {
                    i++
                    inQuote = true
                } else {
                    newtemp[j++] = c
                    if (i != pLen) newtemp[j++] = temp!![i++]
                }
            }
        }
        patternLength = j
        temp = Arrays.copyOf(newtemp, j + 2) // double zero termination
    }

    /**
     * Copies regular expression to an int array and invokes the parsing
     * of the expression which will create the object tree.
     */
    private fun compile() {
        // Handle canonical equivalences
        if (has(CANON_EQ) && !has(LITERAL)) {
            normalize()
        } else {
            normalizedPattern = pattern
        }
        patternLength = normalizedPattern!!.length

        // Copy pattern to int array for convenience
        // Use double zero to terminate pattern
        temp = IntArray(patternLength + 2)
        var hasSupplementary = false
        var c: Int
        var count = 0
        // Convert all chars into code points
        var x = 0
        while (x < patternLength) {
            c = normalizedPattern!!.codePointAt(x)
            if (isSupplementary(c)) {
                hasSupplementary = true
            }
            temp!![count++] = c
            x += Character.charCount(c)
        }
        patternLength = count // patternLength now in code points
        if (!has(LITERAL)) RemoveQEQuoting()

        // Allocate all temporary objects here.
        buffer = IntArray(32)
        groupNodes = arrayOfNulls(10)
        if (has(LITERAL)) {
            // Literal pattern handling
            matchRoot = newSlice(temp, patternLength, hasSupplementary)
            matchRoot!!.next = lastAccept
        } else {
            // Start recursive descent parsing
            matchRoot = expr(lastAccept)
            // Check extra pattern characters
            if (patternLength != cursor) {
                if (peek() == ')'.code) {
                    throw error("Unmatched closing ')'")
                } else {
                    throw error("Unexpected internal error")
                }
            }
        }

        // Peephole optimization
        if (matchRoot is Slice) {
            root = BnM.optimize(matchRoot)
            if (root === matchRoot) {
                root = if (hasSupplementary) StartS(matchRoot) else Start(matchRoot)
            }
        } else if (matchRoot is Begin || matchRoot is First) {
            root = matchRoot
        } else {
            root = if (hasSupplementary) StartS(matchRoot) else Start(matchRoot)
        }

        // Release temporary storage
        temp = null
        buffer = null
        groupNodes = null
        patternLength = 0
        compiled = true
    }

    /**
     * Used to accumulate information about a subtree of the object graph
     * so that optimizations can be applied to the subtree.
     */
    internal class TreeInfo {
        var minLength = 0
        var maxLength = 0
        var maxValid = false
        var deterministic = false

        init {
            reset()
        }

        fun reset() {
            minLength = 0
            maxLength = 0
            maxValid = true
            deterministic = true
        }
    }
    /*
     * The following private methods are mainly used to improve the
     * readability of the code. In order to let the Java compiler easily
     * inline them, we should not put many assertions or error checks in them.
     */
    /**
     * Indicates whether a particular flag is set or not.
     */
    private fun has(f: Int): Boolean {
        return flags and f != 0
    }

    /**
     * Match next character, signal error if failed.
     */
    private fun accept(ch: Int, s: String) {
        var testChar = temp!![cursor++]
        if (has(COMMENTS)) testChar = parsePastWhitespace(testChar)
        if (ch != testChar) {
            throw error(s)
        }
    }

    /**
     * Mark the end of pattern with a specific character.
     */
    private fun mark(c: Int) {
        temp!![patternLength] = c
    }

    /**
     * Peek the next character, and do not advance the cursor.
     */
    private fun peek(): Int {
        var ch = temp!![cursor]
        if (has(COMMENTS)) ch = peekPastWhitespace(ch)
        return ch
    }

    /**
     * Read the next character, and advance the cursor by one.
     */
    private fun read(): Int {
        var ch = temp!![cursor++]
        if (has(COMMENTS)) ch = parsePastWhitespace(ch)
        return ch
    }

    /**
     * Read the next character, and advance the cursor by one,
     * ignoring the COMMENTS setting
     */
    private fun readEscaped(): Int {
        return temp!![cursor++]
    }

    /**
     * Advance the cursor by one, and peek the next character.
     */
    private operator fun next(): Int {
        var ch = temp!![++cursor]
        if (has(COMMENTS)) ch = peekPastWhitespace(ch)
        return ch
    }

    /**
     * Advance the cursor by one, and peek the next character,
     * ignoring the COMMENTS setting
     */
    private fun nextEscaped(): Int {
        return temp!![++cursor]
    }

    /**
     * If in xmode peek past whitespace and comments.
     */
    private fun peekPastWhitespace(ch: Int): Int {
        var ch = ch
        while (ASCII.isSpace(ch) || ch == '#'.code) {
            while (ASCII.isSpace(ch)) ch = temp!![++cursor]
            if (ch == '#'.code) {
                ch = peekPastLine()
            }
        }
        return ch
    }

    /**
     * If in xmode parse past whitespace and comments.
     */
    private fun parsePastWhitespace(ch: Int): Int {
        var ch = ch
        while (ASCII.isSpace(ch) || ch == '#'.code) {
            while (ASCII.isSpace(ch)) ch = temp!![cursor++]
            if (ch == '#'.code) ch = parsePastLine()
        }
        return ch
    }

    /**
     * xmode parse past comment to end of line.
     */
    private fun parsePastLine(): Int {
        var ch = temp!![cursor++]
        while (ch != 0 && !isLineSeparator(ch)) ch = temp!![cursor++]
        return ch
    }

    /**
     * xmode peek past comment to end of line.
     */
    private fun peekPastLine(): Int {
        var ch = temp!![++cursor]
        while (ch != 0 && !isLineSeparator(ch)) ch = temp!![++cursor]
        return ch
    }

    /**
     * Determines if character is a line separator in the current mode
     */
    private fun isLineSeparator(ch: Int): Boolean {
        return if (has(UNIX_LINES)) {
            ch == '\n'.code
        } else {
            ch == '\n'.code || ch == '\r'.code || ch or 1 == '\u2029'.code || ch == '\u0085'.code
        }
    }

    /**
     * Read the character after the next one, and advance the cursor by two.
     */
    private fun skip(): Int {
        val i = cursor
        val ch = temp!![i + 1]
        cursor = i + 2
        return ch
    }

    /**
     * Unread one next character, and retreat cursor by one.
     */
    private fun unread() {
        cursor--
    }

    /**
     * Internal method used for handling all syntax errors. The pattern is
     * displayed with a pointer to aid in locating the syntax error.
     */
    private fun error(s: String): PatternSyntaxException {
        return PatternSyntaxException(s, normalizedPattern, cursor - 1)
    }

    /**
     * Determines if there is any supplementary character or unpaired
     * surrogate in the specified range.
     */
    private fun findSupplementary(start: Int, end: Int): Boolean {
        for (i in start until end) {
            if (isSupplementary(temp!![i])) return true
        }
        return false
    }
    /**
     * The following methods handle the main parsing. They are sorted
     * according to their precedence order, the lowest one first.
     */
    /**
     * The expression is parsed with branch nodes added for alternations.
     * This may be called recursively to parse sub expressions that may
     * contain alternations.
     */
    private fun expr(end: Node?): Node? {
        var prev: Node? = null
        var firstTail: Node? = null
        var branchConn: Node? = null
        while (true) {
            var node = sequence(end)
            val nodeTail = root //double return
            if (prev == null) {
                prev = node
                firstTail = nodeTail
            } else {
                // Branch
                if (branchConn == null) {
                    branchConn = BranchConn()
                    branchConn.next = end
                }
                if (node === end) {
                    // if the node returned from sequence() is "end"
                    // we have an empty expr, set a null atom into
                    // the branch to indicate to go "next" directly.
                    node = null
                } else {
                    // the "tail.next" of each atom goes to branchConn
                    nodeTail!!.next = branchConn
                }
                if (prev is Branch) {
                    prev.add(node)
                } else {
                    if (prev === end) {
                        prev = null
                    } else {
                        // replace the "end" with "branchConn" at its tail.next
                        // when put the "prev" into the branch as the first atom.
                        firstTail!!.next = branchConn
                    }
                    prev = Branch(prev, node, branchConn)
                }
            }
            if (peek() != '|'.code) {
                return prev
            }
            next()
        }
    }

    /**
     * Parsing of sequences between alternations.
     */
    private fun sequence(end: Node?): Node? {
        var head: Node? = null
        var tail: Node? = null
        var node: Node? = null
        LOOP@ while (true) {
            var ch = peek()
            when (ch) {
                '(' -> {
                    // Because group handles its own closure,
                    // we need to treat it differently
                    node = group0()
                    // Check for comment or flag group
                    if (node == null) {
                        continue
                    }
                    if (head == null) head = node else tail!!.next = node
                    // Double return: Tail was returned in root
                    tail = root
                    continue
                }

                '[' -> node = clazz(true)
                '\\' -> {
                    ch = nextEscaped()
                    if (ch == 'p'.code || ch == 'P'.code) {
                        var oneLetter = true
                        val comp = ch == 'P'.code
                        ch = next() // Consume { if present
                        if (ch != '{'.code) {
                            unread()
                        } else {
                            oneLetter = false
                        }
                        node = family(oneLetter).maybeComplement(comp)
                    } else {
                        unread()
                        node = atom()
                    }
                }

                '^' -> {
                    next()
                    node = if (has(MULTILINE)) {
                        if (has(UNIX_LINES)) UnixCaret() else Caret()
                    } else {
                        Begin()
                    }
                }

                '$' -> {
                    next()
                    node =
                        if (has(UNIX_LINES)) UnixDollar(has(MULTILINE)) else Dollar(
                            has(MULTILINE)
                        )
                }

                '.' -> {
                    next()
                    node = if (has(DOTALL)) {
                        All()
                    } else {
                        if (has(UNIX_LINES)) UnixDot() else {
                            Dot()
                        }
                    }
                }

                '|', ')' -> break@LOOP
                ']', '}' -> node = atom()
                '?', '*', '+' -> {
                    next()
                    throw error("Dangling meta character '" + ch.toChar() + "'")
                }

                0 -> {
                    if (cursor >= patternLength) {
                        break@LOOP
                    }
                    node = atom()
                }

                else -> node = atom()
            }
            node = closure(node)
            if (head == null) {
                tail = node
                head = tail
            } else {
                tail!!.next = node
                tail = node
            }
        }
        if (head == null) {
            return end
        }
        tail!!.next = end
        root = tail //double return
        return head
    }

    /**
     * Parse and add a new Single or Slice.
     */
    private fun atom(): Node? {
        var first = 0
        var prev = -1
        var hasSupplementary = false
        var ch = peek()
        while (true) {
            when (ch) {
                '*', '+', '?', '{' -> if (first > 1) {
                    cursor = prev // Unwind one character
                    first--
                }

                '$', '.', '^', '(', '[', '|', ')' -> {}
                '\\' -> {
                    ch = nextEscaped()
                    if (ch == 'p'.code || ch == 'P'.code) { // Property
                        return if (first > 0) { // Slice is waiting; handle it first
                            unread()
                            break
                        } else { // No slice; just return the family node
                            val comp = ch == 'P'.code
                            var oneLetter = true
                            ch = next() // Consume { if present
                            if (ch != '{'.code) unread() else oneLetter = false
                            family(oneLetter).maybeComplement(comp)
                        }
                    }
                    unread()
                    prev = cursor
                    ch = escape(false, first == 0)
                    if (ch >= 0) {
                        append(ch, first)
                        first++
                        if (isSupplementary(ch)) {
                            hasSupplementary = true
                        }
                        ch = peek()
                        continue
                    } else if (first == 0) {
                        return root
                    }
                    // Unwind meta escape sequence
                    cursor = prev
                }

                0 -> {
                    if (cursor >= patternLength) {
                        break
                    }
                    prev = cursor
                    append(ch, first)
                    first++
                    if (isSupplementary(ch)) {
                        hasSupplementary = true
                    }
                    ch = next()
                    continue
                }

                else -> {
                    prev = cursor
                    append(ch, first)
                    first++
                    if (isSupplementary(ch)) {
                        hasSupplementary = true
                    }
                    ch = next()
                    continue
                }
            }
            break
        }
        return if (first == 1) {
            newSingle(buffer!![0])
        } else {
            newSlice(buffer, first, hasSupplementary)
        }
    }

    private fun append(ch: Int, len: Int) {
        if (len >= buffer!!.size) {
            val tmp = IntArray(len + len)
            System.arraycopy(buffer, 0, tmp, 0, len)
            buffer = tmp
        }
        buffer!![len] = ch
    }

    /**
     * Parses a backref greedily, taking as many numbers as it
     * can. The first digit is always treated as a backref, but
     * multi digit numbers are only treated as a backref if at
     * least that many backrefs exist at this point in the regex.
     */
    private fun ref(refNum: Int): Node {
        var refNum = refNum
        var done = false
        while (!done) {
            val ch = peek()
            when (ch) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                    val newRefNum = refNum * 10 + (ch - '0'.code)
                    // Add another number if it doesn't make a group
                    // that doesn't exist
                    if (capturingGroupCount - 1 < newRefNum) {
                        done = true
                        break
                    }
                    refNum = newRefNum
                    read()
                }

                else -> done = true
            }
        }
        return if (has(CASE_INSENSITIVE)) CIBackRef(
            refNum,
            has(UNICODE_CASE)
        ) else BackRef(refNum)
    }

    /**
     * Parses an escape sequence to determine the actual value that needs
     * to be matched.
     * If -1 is returned and create was true a new object was added to the tree
     * to handle the escape sequence.
     * If the returned value is greater than zero, it is the value that
     * matches the escape sequence.
     */
    private fun escape(inclass: Boolean, create: Boolean): Int {
        val ch = skip()
        when (ch) {
            '0' -> return o()
            '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                if (inclass) break
                if (create) {
                    root = ref(ch - '0'.code)
                }
                return -1
            }

            'A' -> {
                if (inclass) break
                if (create) root = Begin()
                return -1
            }

            'B' -> {
                if (inclass) break
                if (create) root = Bound(Bound.NONE)
                return -1
            }

            'C' -> {}
            'D' -> {
                if (create) root = Ctype(ASCII.DIGIT).complement()
                return -1
            }

            'E', 'F' -> {}
            'G' -> {
                if (inclass) break
                if (create) root = LastMatch()
                return -1
            }

            'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R' -> {}
            'S' -> {
                if (create) root = Ctype(ASCII.SPACE).complement()
                return -1
            }

            'T', 'U', 'V' -> {}
            'W' -> {
                if (create) root = Ctype(ASCII.WORD).complement()
                return -1
            }

            'X', 'Y' -> {}
            'Z' -> {
                if (inclass) break
                if (create) {
                    root =
                        if (has(UNIX_LINES)) UnixDollar(false) else Dollar(
                            false
                        )
                }
                return -1
            }

            'a' -> return '\u0007'
            'b' -> {
                if (inclass) break
                if (create) root = Bound(Bound.BOTH)
                return -1
            }

            'c' -> return c()
            'd' -> {
                if (create) root = Ctype(ASCII.DIGIT)
                return -1
            }

            'e' -> return '\u001b'
            'f' -> return '\f'
            'g', 'h', 'i', 'j', 'k', 'l', 'm' -> {}
            'n' -> return '\n'
            'o', 'p', 'q' -> {}
            'r' -> return '\r'
            's' -> {
                if (create) root = Ctype(ASCII.SPACE)
                return -1
            }

            't' -> return '\t'
            'u' -> return u()
            'v' -> return '\u000b'
            'w' -> {
                if (create) root = Ctype(ASCII.WORD)
                return -1
            }

            'x' -> return x()
            'y' -> {}
            'z' -> {
                if (inclass) break
                if (create) root = End()
                return -1
            }

            else -> return ch
        }
        throw error("Illegal/unsupported escape sequence")
    }

    /**
     * Parse a character class, and return the node that matches it.
     *
     * Consumes a ] on the way out if consume is true. Usually consume
     * is true except for the case of [abc&&def] where def is a separate
     * right hand node with "understood" brackets.
     */
    private fun clazz(consume: Boolean): CharProperty {
        var prev: CharProperty? = null
        var node: CharProperty? = null
        val bits = BitClass()
        var include = true
        var firstInClass = true
        var ch = next()
        while (true) {
            when (ch) {
                '^' ->                     // Negates if first char in a class, otherwise literal
                    if (firstInClass) {
                        if (temp!![cursor - 1] != '['.code) break
                        ch = next()
                        include = !include
                        continue
                    } else {
                        // ^ not first in class, treat as literal
                        break
                    }

                '[' -> {
                    firstInClass = false
                    node = clazz(true)
                    prev = if (prev == null) node else union(prev, node)
                    ch = peek()
                    continue
                }

                '&' -> {
                    firstInClass = false
                    ch = next()
                    if (ch == '&'.code) {
                        ch = next()
                        var rightNode: CharProperty? = null
                        while (ch != ']'.code && ch != '&'.code) {
                            rightNode = if (ch == '['.code) {
                                if (rightNode == null) clazz(true) else union(rightNode, clazz(true))
                            } else { // abc&&def
                                unread()
                                clazz(false)
                            }
                            ch = peek()
                        }
                        if (rightNode != null) node = rightNode
                        prev = if (prev == null) {
                            rightNode ?: throw error("Bad class syntax")
                        } else {
                            intersection(prev, node)
                        }
                    } else {
                        // treat as a literal &
                        unread()
                        break
                    }
                    continue
                }

                0 -> {
                    firstInClass = false
                    if (cursor >= patternLength) throw error("Unclosed character class")
                }

                ']' -> {
                    firstInClass = false
                    if (prev != null) {
                        if (consume) next()
                        return prev
                    }
                }

                else -> firstInClass = false
            }
            node = range(bits)
            if (include) {
                if (prev == null) {
                    prev = node
                } else {
                    if (prev !== node) prev = union(prev, node)
                }
            } else {
                if (prev == null) {
                    prev = node!!.complement()
                } else {
                    if (prev !== node) prev = setDifference(prev, node)
                }
            }
            ch = peek()
        }
    }

    private fun bitsOrSingle(bits: BitClass, ch: Int): CharProperty {
        /* Bits can only handle codepoints in [u+0000-u+00ff] range.
           Use "single" node instead of bits when dealing with unicode
           case folding for codepoints listed below.
           (1)Uppercase out of range: u+00ff, u+00b5
              toUpperCase(u+00ff) -> u+0178
              toUpperCase(u+00b5) -> u+039c
           (2)LatinSmallLetterLongS u+17f
              toUpperCase(u+017f) -> u+0053
           (3)LatinSmallLetterDotlessI u+131
              toUpperCase(u+0131) -> u+0049
           (4)LatinCapitalLetterIWithDotAbove u+0130
              toLowerCase(u+0130) -> u+0069
           (5)KelvinSign u+212a
              toLowerCase(u+212a) ==> u+006B
           (6)AngstromSign u+212b
              toLowerCase(u+212b) ==> u+00e5
        */
        var d: Int
        return if (ch < 256 &&
            !(has(CASE_INSENSITIVE) && has(UNICODE_CASE) &&
                    (//I and i
                            //S and s
                            //K and k
                            ch == 0xff || ch == 0xb5 || ch == 0x49 || ch == 0x69 || ch == 0x53 || ch == 0x73 || ch == 0x4b || ch == 0x6b || ch == 0xc5 || ch == 0xe5))
        ) bits.add(ch, flags()) else newSingle(ch)
    }

    /**
     * Parse a single character or a character range in a character class
     * and return its representative node.
     */
    private fun range(bits: BitClass): CharProperty? {
        var ch = peek()
        if (ch == '\\'.code) {
            ch = nextEscaped()
            if (ch == 'p'.code || ch == 'P'.code) { // A property
                val comp = ch == 'P'.code
                var oneLetter = true
                // Consume { if present
                ch = next()
                if (ch != '{'.code) unread() else oneLetter = false
                return family(oneLetter).maybeComplement(comp)
            } else { // ordinary escape
                unread()
                ch = escape(true, true)
                if (ch == -1) return root as CharProperty?
            }
        } else {
            ch = single()
        }
        if (ch >= 0) {
            if (peek() == '-'.code) {
                val endRange = temp!![cursor + 1]
                if (endRange == '['.code) {
                    return bitsOrSingle(bits, ch)
                }
                if (endRange != ']'.code) {
                    next()
                    val m = single()
                    if (m < ch) throw error("Illegal character range")
                    return if (has(CASE_INSENSITIVE)) caseInsensitiveRangeFor(
                        ch,
                        m
                    ) else rangeFor(ch, m)
                }
            }
            return bitsOrSingle(bits, ch)
        }
        throw error("Unexpected character '" + ch.toChar() + "'")
    }

    private fun single(): Int {
        val ch = peek()
        return when (ch) {
            '\\' -> escape(true, false)
            else -> {
                next()
                ch
            }
        }
    }

    /**
     * Parses a Unicode character family and returns its representative node.
     */
    private fun family(singleLetter: Boolean): CharProperty {
        next()
        var name: String
        if (singleLetter) {
            val c = temp!![cursor]
            name = if (!Character.isSupplementaryCodePoint(c)) {
                c.toChar().toString()
            } else {
                kotlin.String(temp, cursor, 1)
            }
            read()
        } else {
            val i = cursor
            mark('}'.code)
            while (read() != '}'.code) {
            }
            mark('\u0000'.code)
            val j = cursor
            if (j > patternLength) throw error("Unclosed character family")
            if (i + 1 >= j) throw error("Empty character family")
            name = kotlin.String(temp, i, j - i - 1)
        }
        return if (name.startsWith("In")) {
            unicodeBlockPropertyFor(name.substring(2))
        } else {
            if (name.startsWith("Is")) name = name.substring(2)
            charPropertyNodeFor(name)
        }
    }

    /**
     * Returns a CharProperty matching all characters in a UnicodeBlock.
     */
    private fun unicodeBlockPropertyFor(name: String): CharProperty {
        val block: UnicodeBlock
        block = try {
            UnicodeBlock.forName(name)
        } catch (iae: IllegalArgumentException) {
            throw error("Unknown character block name {$name}")
        }
        return object : CharProperty() {
            override fun isSatisfiedBy(ch: Int): Boolean {
                return block === UnicodeBlock.of(ch)
            }
        }
    }

    /**
     * Returns a CharProperty matching all characters in a named property.
     */
    private fun charPropertyNodeFor(name: String): CharProperty {
        return CharPropertyNames.charPropertyFor(name) ?: throw error("Unknown character property name {$name}")
    }

    /**
     * Parses a group and returns the head node of a set of nodes that process
     * the group. Sometimes a double return system is used where the tail is
     * returned in root.
     */
    private fun group0(): Node? {
        var capturingGroup = false
        var head: Node? = null
        var tail: Node? = null
        val save = flags
        root = null
        var ch = next()
        if (ch == '?'.code) {
            ch = skip()
            when (ch) {
                ':' -> {
                    head = createGroup(true)
                    tail = root
                    head.next = expr(tail)
                }

                '=', '!' -> {
                    head = createGroup(true)
                    tail = root
                    head.next = expr(tail)
                    if (ch == '='.code) {
                        tail = Pos(head)
                        head = tail
                    } else {
                        tail = Neg(head)
                        head = tail
                    }
                }

                '>' -> {
                    head = createGroup(true)
                    tail = root
                    head.next = expr(tail)
                    run {
                        tail = Ques(head, INDEPENDENT)
                        head = tail
                    }
                }

                '<' -> {
                    ch = read()
                    val start = cursor
                    head = createGroup(true)
                    tail = root
                    head!!.next = expr(tail)
                    tail!!.next = lookbehindEnd
                    val info = TreeInfo()
                    head!!.study(info)
                    if (info.maxValid == false) {
                        throw error(
                            "Look-behind group does not have "
                                    + "an obvious maximum length"
                        )
                    }
                    val hasSupplementary = findSupplementary(start, patternLength)
                    if (ch == '='.code) {
                        tail = if (hasSupplementary) BehindS(
                            head, info.maxLength,
                            info.minLength
                        ) else Behind(
                            head, info.maxLength,
                            info.minLength
                        )
                        head = tail
                    } else if (ch == '!'.code) {
                        tail = if (hasSupplementary) NotBehindS(
                            head, info.maxLength,
                            info.minLength
                        ) else NotBehind(
                            head, info.maxLength,
                            info.minLength
                        )
                        head = tail
                    } else {
                        throw error("Unknown look-behind group")
                    }
                }

                '$', '@' -> throw error("Unknown group type")
                else -> {
                    unread()
                    addFlag()
                    ch = read()
                    if (ch == ')'.code) {
                        return null // Inline modifier only
                    }
                    if (ch != ':'.code) {
                        throw error("Unknown inline modifier")
                    }
                    head = createGroup(true)
                    tail = root
                    head!!.next = expr(tail)
                }
            }
        } else { // (xxx) a regular group
            capturingGroup = true
            head = createGroup(false)
            tail = root
            head!!.next = expr(tail)
        }
        accept(')'.code, "Unclosed group")
        flags = save

        // Check for quantifiers
        val node = closure(head)
        if (node === head) { // No closure
            root = tail
            return node // Dual return
        }
        if (head === tail) { // Zero length assertion
            root = node
            return node // Dual return
        }
        if (node is Ques) {
            val ques = node
            if (ques.type == POSSESSIVE) {
                root = node
                return node
            }
            tail!!.next = BranchConn()
            tail = tail!!.next
            head = if (ques.type == GREEDY) {
                Branch(head, null, tail)
            } else { // Reluctant quantifier
                Branch(null, head, tail)
            }
            root = tail
            return head
        } else if (node is Curly) {
            val curly = node
            if (curly.type == POSSESSIVE) {
                root = node
                return node
            }
            // Discover if the group is deterministic
            val info = TreeInfo()
            return if (head!!.study(info)) { // Deterministic
                val temp = tail as GroupTail?
                root = GroupCurly(
                    head!!.next, curly.cmin,
                    curly.cmax, curly.type,
                    (tail as GroupTail?)!!.localIndex,
                    (tail as GroupTail?)!!.groupIndex,
                    capturingGroup
                )
                head = root
                head
            } else { // Non-deterministic
                val temp = (head as GroupHead?)!!.localIndex
                val loop: Loop
                loop = if (curly.type == GREEDY) Loop(localCount, temp) else  // Reluctant Curly
                    LazyLoop(localCount, temp)
                val prolog = Prolog(loop)
                localCount += 1
                loop.cmin = curly.cmin
                loop.cmax = curly.cmax
                loop.body = head
                tail!!.next = loop
                root = loop
                prolog // Dual return
            }
        }
        throw error("Internal logic error")
    }

    /**
     * Create group head and tail nodes using double return. If the group is
     * created with anonymous true then it is a pure group and should not
     * affect group counting.
     */
    private fun createGroup(anonymous: Boolean): Node {
        val localIndex = localCount++
        var groupIndex = 0
        if (!anonymous) groupIndex = capturingGroupCount++
        val head = GroupHead(localIndex)
        root = GroupTail(localIndex, groupIndex)
        if (!anonymous && groupIndex < 10) groupNodes!![groupIndex] = head
        return head
    }

    /**
     * Parses inlined match flags and set them appropriately.
     */
    private fun addFlag() {
        var ch = peek()
        while (true) {
            when (ch) {
                'U' -> flags = flags or UNGREEDY
                'i' -> flags = flags or CASE_INSENSITIVE
                'm' -> flags = flags or MULTILINE
                's' -> flags = flags or DOTALL
                'd' -> flags = flags or UNIX_LINES
                'u' -> flags = flags or UNICODE_CASE
                'c' -> flags = flags or CANON_EQ
                'x' -> flags = flags or COMMENTS
                '-' -> {
                    ch = next()
                    subFlag()
                    return
                }

                else -> return
            }
            ch = next()
        }
    }

    /**
     * Parses the second part of inlined match flags and turns off
     * flags appropriately.
     */
    private fun subFlag() {
        var ch = peek()
        while (true) {
            flags = when (ch) {
                'U' -> flags and UNGREEDY.inv()
                'i' -> flags and CASE_INSENSITIVE.inv()
                'm' -> flags and MULTILINE.inv()
                's' -> flags and DOTALL.inv()
                'd' -> flags and UNIX_LINES.inv()
                'u' -> flags and UNICODE_CASE.inv()
                'c' -> flags and CANON_EQ.inv()
                'x' -> flags and COMMENTS.inv()
                else -> return
            }
            ch = next()
        }
    }

    /**
     * Processes repetition. If the next character peeked is a quantifier
     * then new nodes must be appended to handle the repetition.
     * Prev could be a single or a group, so it could be a chain of nodes.
     */
    private fun closure(prev: Node?): Node? {
        var atom: Node
        var ch = peek()
        val defaultGreediness = if (flags and UNGREEDY == 0) GREEDY else LAZY
        return when (ch) {
            '?' -> {
                ch = next()
                if (ch == '?'.code) {
                    next()
                    return Ques(prev, LAZY)
                } else if (ch == '+'.code) {
                    next()
                    return Ques(prev, POSSESSIVE)
                }
                Ques(prev, defaultGreediness)
            }

            '*' -> {
                ch = next()
                if (ch == '?'.code) {
                    next()
                    return Curly(prev, 0, MAX_REPS, LAZY)
                } else if (ch == '+'.code) {
                    next()
                    return Curly(prev, 0, MAX_REPS, POSSESSIVE)
                }
                Curly(prev, 0, MAX_REPS, defaultGreediness)
            }

            '+' -> {
                ch = next()
                if (ch == '?'.code) {
                    next()
                    return Curly(prev, 1, MAX_REPS, LAZY)
                } else if (ch == '+'.code) {
                    next()
                    return Curly(prev, 1, MAX_REPS, POSSESSIVE)
                }
                Curly(prev, 1, MAX_REPS, defaultGreediness)
            }

            '{' -> {
                ch = temp!![cursor + 1]
                if (ASCII.isDigit(ch)) {
                    skip()
                    var cmin = 0
                    do {
                        cmin = cmin * 10 + (ch - '0'.code)
                    } while (ASCII.isDigit(read().also { ch = it }))
                    var cmax = cmin
                    if (ch == ','.code) {
                        ch = read()
                        cmax = MAX_REPS
                        if (ch != '}'.code) {
                            cmax = 0
                            while (ASCII.isDigit(ch)) {
                                cmax = cmax * 10 + (ch - '0'.code)
                                ch = read()
                            }
                        }
                    }
                    if (ch != '}'.code) throw error("Unclosed counted closure")
                    if (cmin or cmax or cmax - cmin < 0) throw error("Illegal repetition range")
                    val curly: Curly
                    ch = peek()
                    curly = if (ch == '?'.code) {
                        next()
                        Curly(prev, cmin, cmax, LAZY)
                    } else if (ch == '+'.code) {
                        next()
                        Curly(prev, cmin, cmax, POSSESSIVE)
                    } else {
                        Curly(prev, cmin, cmax, defaultGreediness)
                    }
                    curly
                } else {
                    throw error("Illegal repetition")
                }
            }

            else -> prev
        }
    }

    /**
     * Utility method for parsing control escape sequences.
     */
    private fun c(): Int {
        if (cursor < patternLength) {
            return read() xor 64
        }
        throw error("Illegal control escape sequence")
    }

    /**
     * Utility method for parsing octal escape sequences.
     */
    private fun o(): Int {
        val n = read()
        if (n - '0'.code or '7'.code - n >= 0) {
            val m = read()
            if (m - '0'.code or '7'.code - m >= 0) {
                val o = read()
                if (o - '0'.code or '7'.code - o >= 0 && n - '0'.code or '3'.code - n >= 0) {
                    return (n - '0'.code) * 64 + (m - '0'.code) * 8 + (o - '0'.code)
                }
                unread()
                return (n - '0'.code) * 8 + (m - '0'.code)
            }
            unread()
            return n - '0'.code
        }
        throw error("Illegal octal escape sequence")
    }

    /**
     * Utility method for parsing hexadecimal escape sequences.
     */
    private fun x(): Int {
        val n = read()
        if (ASCII.isHexDigit(n)) {
            val m = read()
            if (ASCII.isHexDigit(m)) {
                return ASCII.toDigit(n) * 16 + ASCII.toDigit(m)
            }
        }
        throw error("Illegal hexadecimal escape sequence")
    }

    /**
     * Utility method for parsing unicode escape sequences.
     */
    private fun u(): Int {
        var n = 0
        for (i in 0..3) {
            val ch = read()
            if (!ASCII.isHexDigit(ch)) {
                throw error("Illegal Unicode escape sequence")
            }
            n = n * 16 + ASCII.toDigit(ch)
        }
        return n
    }

    /**
     * Creates a bit vector for matching Latin-1 values. A normal BitClass
     * never matches values above Latin-1, and a complemented BitClass always
     * matches values above Latin-1.
     */
    private class BitClass : BmpCharProperty {
        val bits: BooleanArray

        internal constructor() {
            bits = BooleanArray(256)
        }

        private constructor(bits: BooleanArray) {
            this.bits = bits
        }

        fun add(c: Int, flags: Int): BitClass {
            assert(c >= 0 && c <= 255)
            if (flags and CASE_INSENSITIVE != 0) {
                if (ASCII.isAscii(c)) {
                    bits[ASCII.toUpper(c)] = true
                    bits[ASCII.toLower(c)] = true
                } else if (flags and UNICODE_CASE != 0) {
                    bits[c.lowercaseChar()] = true
                    bits[c.uppercaseChar()] = true
                }
            }
            bits[c] = true
            return this
        }

        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch < 256 && bits[ch]
        }
    }

    /**
     * Returns a suitably optimized, single character matcher.
     */
    private fun newSingle(ch: Int): CharProperty {
        if (has(CASE_INSENSITIVE)) {
            val lower: Int
            val upper: Int
            if (has(UNICODE_CASE)) {
                upper = ch.uppercaseChar()
                lower = upper.lowercaseChar()
                if (upper != lower) return SingleU(lower)
            } else if (ASCII.isAscii(ch)) {
                lower = ASCII.toLower(ch)
                upper = ASCII.toUpper(ch)
                if (lower != upper) return SingleI(lower, upper)
            }
        }
        return if (isSupplementary(ch)) SingleS(ch) else Single(
            ch
        ) // Match a given Unicode character
        // Match a given BMP character
    }

    /**
     * Utility method for creating a string slice matcher.
     */
    private fun newSlice(buf: IntArray?, count: Int, hasSupplementary: Boolean): Node {
        val tmp = IntArray(count)
        if (has(CASE_INSENSITIVE)) {
            if (has(UNICODE_CASE)) {
                for (i in 0 until count) {
                    tmp[i] = buf!![i].uppercaseChar().lowercaseChar()
                }
                return if (hasSupplementary) SliceUS(tmp) else SliceU(tmp)
            }
            for (i in 0 until count) {
                tmp[i] = ASCII.toLower(buf!![i])
            }
            return if (hasSupplementary) SliceIS(tmp) else SliceI(tmp)
        }
        for (i in 0 until count) {
            tmp[i] = buf!![i]
        }
        return if (hasSupplementary) SliceS(tmp) else Slice(tmp)
    }
    /**
     * The following classes are the building components of the object
     * tree that represents a compiled regular expression. The object tree
     * is made of individual elements that handle constructs in the Pattern.
     * Each type of object knows how to match its equivalent construct with
     * the match() method.
     */
    /**
     * Base class for all node classes. Subclasses should override the match()
     * method as appropriate. This class is an accepting node, so its match()
     * always returns true.
     */
    open class Node : Any() {
        var next: Node?

        init {
            next = accept
        }

        /**
         * This method implements the classic accept node.
         */
        open fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            matcher.last = i
            matcher.groups[0] = matcher.first
            matcher.groups[1] = matcher.last
            return true
        }

        /**
         * This method is good for all zero length assertions.
         */
        open fun study(info: TreeInfo): Boolean {
            return if (next != null) {
                next!!.study(info)
            } else {
                info.deterministic
            }
        }
    }

    internal class LastNode : Node() {
        /**
         * This method implements the classic accept node with
         * the addition of a check to see if the match occurred
         * using all of the input.
         */
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            if (matcher.acceptMode == Matcher.Companion.ENDANCHOR && i != matcher.to) return false
            matcher.last = i
            matcher.groups[0] = matcher.first
            matcher.groups[1] = matcher.last
            return true
        }
    }

    /**
     * Used for REs that can start anywhere within the input string.
     * This basically tries to match repeatedly at each spot in the
     * input string, moving forward after each try. An anchored search
     * or a BnM will bypass this node completely.
     */
    internal open class Start(node: Node?) : Node() {
        var minLength: Int

        init {
            next = node
            val info = TreeInfo()
            next!!.study(info)
            minLength = info.minLength
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            if (i > matcher.to - minLength) {
                matcher.hitEnd = true
                return false
            }
            var ret = false
            val guard = matcher.to - minLength
            while (i <= guard) {
                if (next!!.match(matcher, i, seq).also { ret = it }) break
                if (i == guard) matcher.hitEnd = true
                i++
            }
            if (ret) {
                matcher.first = i
                matcher.groups[0] = matcher.first
                matcher.groups[1] = matcher.last
            }
            return ret
        }

        override fun study(info: TreeInfo): Boolean {
            next!!.study(info)
            info.maxValid = false
            info.deterministic = false
            return false
        }
    }

    /*
     * StartS supports supplementary characters, including unpaired surrogates.
     */
    internal class StartS(node: Node?) : Start(node) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            if (i > matcher.to - minLength) {
                matcher.hitEnd = true
                return false
            }
            var ret = false
            val guard = matcher.to - minLength
            while (i <= guard) {
                if (next!!.match(matcher, i, seq).also { ret = it } || i == guard) break
                // Optimization to move to the next character. This is
                // faster than countChars(seq, i, 1).
                if (Character.isHighSurrogate(seq!![i++])) {
                    if (i < seq.length && Character.isLowSurrogate(seq[i])) {
                        i++
                    }
                }
                if (i == guard) matcher.hitEnd = true
            }
            if (ret) {
                matcher.first = i
                matcher.groups[0] = matcher.first
                matcher.groups[1] = matcher.last
            }
            return ret
        }
    }

    /**
     * Node to anchor at the beginning of input. This object implements the
     * match for a \A sequence, and the caret anchor will use this if not in
     * multiline mode.
     */
    internal class Begin : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val fromIndex = if (matcher.anchoringBounds) matcher.from else 0
            return if (i == fromIndex && next!!.match(matcher, i, seq)) {
                matcher.first = i
                matcher.groups[0] = i
                matcher.groups[1] = matcher.last
                true
            } else {
                false
            }
        }
    }

    /**
     * Node to anchor at the end of input. This is the absolute end, so this
     * should not match at the last newline before the end as $ will.
     */
    internal class End : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val endIndex = if (matcher.anchoringBounds) matcher.to else matcher.textLength
            if (i == endIndex) {
                matcher.hitEnd = true
                return next!!.match(matcher, i, seq)
            }
            return false
        }
    }

    /**
     * Node to anchor at the beginning of a line. This is essentially the
     * object to match for the multiline ^.
     */
    internal class Caret : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var startIndex = matcher.from
            var endIndex = matcher.to
            if (!matcher.anchoringBounds) {
                startIndex = 0
                endIndex = matcher.textLength
            }
            // Perl does not match ^ at end of input even after newline
            if (i == endIndex) {
                matcher.hitEnd = true
                return false
            }
            if (i > startIndex) {
                val ch = seq!![i - 1]
                if (ch != '\n' && ch != '\r' && ch.code or 1 != '\u2029'.code && ch != '\u0085') {
                    return false
                }
                // Should treat /r/n as one newline
                if (ch == '\r' && seq[i] == '\n') return false
            }
            return next!!.match(matcher, i, seq)
        }
    }

    /**
     * Node to anchor at the beginning of a line when in unixdot mode.
     */
    internal class UnixCaret : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var startIndex = matcher.from
            var endIndex = matcher.to
            if (!matcher.anchoringBounds) {
                startIndex = 0
                endIndex = matcher.textLength
            }
            // Perl does not match ^ at end of input even after newline
            if (i == endIndex) {
                matcher.hitEnd = true
                return false
            }
            if (i > startIndex) {
                val ch = seq!![i - 1]
                if (ch != '\n') {
                    return false
                }
            }
            return next!!.match(matcher, i, seq)
        }
    }

    /**
     * Node to match the location where the last match ended.
     * This is used for the \G construct.
     */
    internal class LastMatch : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return if (i != matcher.oldLast) false else next!!.match(matcher, i, seq)
        }
    }

    /**
     * Node to anchor at the end of a line or the end of input based on the
     * multiline mode.
     *
     * When not in multiline mode, the $ can only match at the very end
     * of the input, unless the input ends in a line terminator in which
     * it matches right before the last line terminator.
     *
     * Note that \r\n is considered an atomic line terminator.
     *
     * Like ^ the $ operator matches at a position, it does not match the
     * line terminators themselves.
     */
    internal class Dollar(var multiline: Boolean) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val endIndex = if (matcher.anchoringBounds) matcher.to else matcher.textLength
            if (!multiline) {
                if (i < endIndex - 2) return false
                if (i == endIndex - 2) {
                    var ch = seq!![i]
                    if (ch != '\r') return false
                    ch = seq[i + 1]
                    if (ch != '\n') return false
                }
            }
            // Matches before any line terminator; also matches at the
            // end of input
            // Before line terminator:
            // If multiline, we match here no matter what
            // If not multiline, fall through so that the end
            // is marked as hit; this must be a /r/n or a /n
            // at the very end so the end was hit; more input
            // could make this not match here
            if (i < endIndex) {
                val ch = seq!![i]
                if (ch == '\n') {
                    // No match between \r\n
                    if (i > 0 && seq[i - 1] == '\r') return false
                    if (multiline) return next!!.match(matcher, i, seq)
                } else if (ch == '\r' || ch == '\u0085' || ch.code or 1 == '\u2029'.code) {
                    if (multiline) return next!!.match(matcher, i, seq)
                } else { // No line terminator, no match
                    return false
                }
            }
            // Matched at current end so hit end
            matcher.hitEnd = true
            // If a $ matches because of end of input, then more input
            // could cause it to fail!
            matcher.requireEnd = true
            return next!!.match(matcher, i, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            next!!.study(info)
            return info.deterministic
        }
    }

    /**
     * Node to anchor at the end of a line or the end of input based on the
     * multiline mode when in unix lines mode.
     */
    internal class UnixDollar(var multiline: Boolean) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val endIndex = if (matcher.anchoringBounds) matcher.to else matcher.textLength
            if (i < endIndex) {
                val ch = seq!![i]
                if (ch == '\n') {
                    // If not multiline, then only possible to
                    // match at very end or one before end
                    if (multiline == false && i != endIndex - 1) return false
                    // If multiline return next.match without setting
                    // matcher.hitEnd
                    if (multiline) return next!!.match(matcher, i, seq)
                } else {
                    return false
                }
            }
            // Matching because at the end or 1 before the end;
            // more input could change this so set hitEnd
            matcher.hitEnd = true
            // If a $ matches because of end of input, then more input
            // could cause it to fail!
            matcher.requireEnd = true
            return next!!.match(matcher, i, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            next!!.study(info)
            return info.deterministic
        }
    }

    /**
     * Abstract node class to match one character satisfying some
     * boolean property.
     */
    private abstract class CharProperty : Node() {
        abstract fun isSatisfiedBy(ch: Int): Boolean
        fun complement(): CharProperty {
            return object : CharProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return !this@CharProperty.isSatisfiedBy(ch)
                }
            }
        }

        fun maybeComplement(complement: Boolean): CharProperty {
            return if (complement) complement() else this
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return if (i < matcher.to) {
                val ch = Character.codePointAt(seq, i)
                (isSatisfiedBy(ch)
                        && next!!.match(matcher, i + Character.charCount(ch), seq))
            } else {
                matcher.hitEnd = true
                false
            }
        }

        override fun study(info: TreeInfo): Boolean {
            info.minLength++
            info.maxLength++
            return next!!.study(info)
        }
    }

    /**
     * Optimized version of CharProperty that works only for
     * properties never satisfied by Supplementary characters.
     */
    private abstract class BmpCharProperty : CharProperty() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return if (i < matcher.to) {
                (isSatisfiedBy(seq!![i].code)
                        && next!!.match(matcher, i + 1, seq))
            } else {
                matcher.hitEnd = true
                false
            }
        }
    }

    /**
     * Node class that matches a Supplementary Unicode character
     */
    internal class SingleS(val c: Int) : CharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch == c
        }
    }

    /**
     * Optimization -- matches a given BMP character
     */
    internal class Single(val c: Int) : BmpCharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch == c
        }
    }

    /**
     * Case insensitive matches a given BMP character
     */
    internal class SingleI(val lower: Int, val upper: Int) : BmpCharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch == lower || ch == upper
        }
    }

    /**
     * Unicode case insensitive matches a given Unicode character
     */
    internal class SingleU(val lower: Int) : CharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return lower == ch ||
                    lower == ch.uppercaseChar().lowercaseChar()
        }
    }

    /**
     * Node class that matches a Unicode category.
     */
    internal class Category(val typeMask: Int) : CharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return typeMask and (1 shl Character.getType(ch)) != 0
        }
    }

    /**
     * Node class that matches a POSIX type.
     */
    internal class Ctype(val ctype: Int) : BmpCharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch < 128 && ASCII.isType(ch, ctype)
        }
    }

    /**
     * Base class for all Slice nodes
     */
    internal open class SliceNode(var buffer: IntArray) : Node() {
        override fun study(info: TreeInfo): Boolean {
            info.minLength += buffer.size
            info.maxLength += buffer.size
            return next!!.study(info)
        }
    }

    /**
     * Node class for a case sensitive/BMP-only sequence of literal
     * characters.
     */
    internal class Slice(buf: IntArray) : SliceNode(buf) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val buf = buffer
            val len = buf.size
            for (j in 0 until len) {
                if (i + j >= matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
                if (buf[j] != seq!![i + j].code) return false
            }
            return next!!.match(matcher, i + len, seq)
        }
    }

    /**
     * Node class for a case_insensitive/BMP-only sequence of literal
     * characters.
     */
    internal class SliceI(buf: IntArray) : SliceNode(buf) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val buf = buffer
            val len = buf.size
            for (j in 0 until len) {
                if (i + j >= matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
                val c = seq!![i + j].code
                if (buf[j] != c &&
                    buf[j] != ASCII.toLower(c)
                ) return false
            }
            return next!!.match(matcher, i + len, seq)
        }
    }

    /**
     * Node class for a unicode_case_insensitive/BMP-only sequence of
     * literal characters. Uses unicode case folding.
     */
    internal class SliceU(buf: IntArray) : SliceNode(buf) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val buf = buffer
            val len = buf.size
            for (j in 0 until len) {
                if (i + j >= matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
                val c = seq!![i + j].code
                if (buf[j] != c &&
                    buf[j] != c.uppercaseChar().lowercaseChar()
                ) return false
            }
            return next!!.match(matcher, i + len, seq)
        }
    }

    /**
     * Node class for a case sensitive sequence of literal characters
     * including supplementary characters.
     */
    internal class SliceS(buf: IntArray) : SliceNode(buf) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val buf = buffer
            var x = i
            for (j in buf.indices) {
                if (x >= matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
                val c = Character.codePointAt(seq, x)
                if (buf[j] != c) return false
                x += Character.charCount(c)
                if (x > matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
            }
            return next!!.match(matcher, x, seq)
        }
    }

    /**
     * Node class for a case insensitive sequence of literal characters
     * including supplementary characters.
     */
    internal open class SliceIS(buf: IntArray) : SliceNode(buf) {
        open fun toLower(c: Int): Int {
            return ASCII.toLower(c)
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val buf = buffer
            var x = i
            for (j in buf.indices) {
                if (x >= matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
                val c = Character.codePointAt(seq, x)
                if (buf[j] != c && buf[j] != toLower(c)) return false
                x += Character.charCount(c)
                if (x > matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
            }
            return next!!.match(matcher, x, seq)
        }
    }

    /**
     * Node class for a case insensitive sequence of literal characters.
     * Uses unicode case folding.
     */
    internal class SliceUS(buf: IntArray) : SliceIS(buf) {
        override fun toLower(c: Int): Int {
            return c.uppercaseChar().lowercaseChar()
        }
    }

    /**
     * Returns node for matching characters within an explicit value
     * range in a case insensitive manner.
     */
    private fun caseInsensitiveRangeFor(
        lower: Int,
        upper: Int
    ): CharProperty {
        return if (has(UNICODE_CASE)) object : CharProperty() {
            override fun isSatisfiedBy(ch: Int): Boolean {
                if (inRange(lower, ch, upper)) return true
                val up: Int = ch.uppercaseChar()
                return inRange(lower, up, upper) ||
                        inRange(lower, up.lowercaseChar(), upper)
            }
        } else object : CharProperty() {
            override fun isSatisfiedBy(ch: Int): Boolean {
                return inRange(lower, ch, upper) ||
                        ASCII.isAscii(ch) &&
                        (inRange(lower, ASCII.toUpper(ch), upper) ||
                                inRange(lower, ASCII.toLower(ch), upper))
            }
        }
    }

    /**
     * Implements the Unicode category ALL and the dot metacharacter when
     * in dotall mode.
     */
    internal class All : CharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return true
        }
    }

    /**
     * Node class for the dot metacharacter when dotall is not enabled.
     */
    internal class Dot : CharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch != '\n'.code && ch != '\r'.code && ch or 1 != '\u2029'.code && ch != '\u0085'.code
        }
    }

    /**
     * Node class for the dot metacharacter when dotall is not enabled
     * but UNIX_LINES is enabled.
     */
    internal class UnixDot : CharProperty() {
        override fun isSatisfiedBy(ch: Int): Boolean {
            return ch != '\n'.code
        }
    }

    /**
     * The 0 or 1 quantifier. This one class implements all three types.
     */
    internal class Ques(var atom: Node?, var type: Int) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            return when (type) {
                GREEDY -> (atom!!.match(matcher, i, seq) && next!!.match(matcher, matcher.last, seq)
                        || next!!.match(matcher, i, seq))

                LAZY -> next!!.match(matcher, i, seq) || atom!!.match(matcher, i, seq) && next!!.match(
                    matcher,
                    matcher.last,
                    seq
                )

                POSSESSIVE -> {
                    if (atom!!.match(matcher, i, seq)) i = matcher.last
                    next!!.match(matcher, i, seq)
                }

                else -> atom!!.match(matcher, i, seq) && next!!.match(matcher, matcher.last, seq)
            }
        }

        override fun study(info: TreeInfo): Boolean {
            return if (type != INDEPENDENT) {
                val minL = info.minLength
                atom!!.study(info)
                info.minLength = minL
                info.deterministic = false
                next!!.study(info)
            } else {
                atom!!.study(info)
                next!!.study(info)
            }
        }
    }

    /**
     * Handles the curly-brace style repetition with a specified minimum and
     * maximum occurrences. The * quantifier is handled as a special case.
     * This class handles the three types.
     */
    internal class Curly(var atom: Node?, var cmin: Int, var cmax: Int, var type: Int) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            var j: Int
            j = 0
            while (j < cmin) {
                if (atom!!.match(matcher, i, seq)) {
                    i = matcher.last
                    j++
                    continue
                }
                return false
                j++
            }
            return if (type == GREEDY) match0(
                matcher,
                i,
                j,
                seq
            ) else if (type == LAZY) match1(matcher, i, j, seq) else match2(matcher, i, j, seq)
        }

        // Greedy match.
        // i is the index to start matching at
        // j is the number of atoms that have matched
        fun match0(matcher: Matcher, i: Int, j: Int, seq: CharSequence?): Boolean {
            var i = i
            var j = j
            if (j >= cmax) {
                // We have matched the maximum... continue with the rest of
                // the regular expression
                return next!!.match(matcher, i, seq)
            }
            val backLimit = j
            while (atom!!.match(matcher, i, seq)) {
                // k is the length of this match
                val k = matcher.last - i
                if (k == 0) // Zero length match
                    break
                // Move up index and number matched
                i = matcher.last
                j++
                // We are greedy so match as many as we can
                while (j < cmax) {
                    if (!atom!!.match(matcher, i, seq)) break
                    if (i + k != matcher.last) {
                        if (match0(matcher, matcher.last, j + 1, seq)) return true
                        break
                    }
                    i += k
                    j++
                }
                // Handle backing off if match fails
                while (j >= backLimit) {
                    if (next!!.match(matcher, i, seq)) return true
                    i -= k
                    j--
                }
                return false
            }
            return next!!.match(matcher, i, seq)
        }

        // Reluctant match. At this point, the minimum has been satisfied.
        // i is the index to start matching at
        // j is the number of atoms that have matched
        fun match1(matcher: Matcher, i: Int, j: Int, seq: CharSequence?): Boolean {
            var i = i
            var j = j
            while (true) {

                // Try finishing match without consuming any more
                if (next!!.match(matcher, i, seq)) return true
                // At the maximum, no match found
                if (j >= cmax) return false
                // Okay, must try one more atom
                if (!atom!!.match(matcher, i, seq)) return false
                // If we haven't moved forward then must break out
                if (i == matcher.last) return false
                // Move up index and number matched
                i = matcher.last
                j++
            }
        }

        fun match2(matcher: Matcher, i: Int, j: Int, seq: CharSequence?): Boolean {
            var i = i
            var j = j
            while (j < cmax) {
                if (!atom!!.match(matcher, i, seq)) break
                if (i == matcher.last) break
                i = matcher.last
                j++
            }
            return next!!.match(matcher, i, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            // Save original info
            val minL = info.minLength
            val maxL = info.maxLength
            val maxV = info.maxValid
            val detm = info.deterministic
            info.reset()
            atom!!.study(info)
            var temp = info.minLength * cmin + minL
            if (temp < minL) {
                temp = 0xFFFFFFF // arbitrary large number
            }
            info.minLength = temp
            if (maxV and info.maxValid) {
                temp = info.maxLength * cmax + maxL
                info.maxLength = temp
                if (temp < maxL) {
                    info.maxValid = false
                }
            } else {
                info.maxValid = false
            }
            if (info.deterministic && cmin == cmax) info.deterministic = detm else info.deterministic = false
            return next!!.study(info)
        }
    }

    /**
     * Handles the curly-brace style repetition with a specified minimum and
     * maximum occurrences in deterministic cases. This is an iterative
     * optimization over the Prolog and Loop system which would handle this
     * in a recursive way. The * quantifier is handled as a special case.
     * If capture is true then this class saves group settings and ensures
     * that groups are unset when backing off of a group match.
     */
    internal class GroupCurly(
        var atom: Node?, var cmin: Int, var cmax: Int, var type: Int, var localIndex: Int,
        var groupIndex: Int, var capture: Boolean
    ) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            val groups = matcher.groups
            val locals = matcher.locals
            val save0 = locals!![localIndex]
            var save1 = 0
            var save2 = 0
            if (capture) {
                save1 = groups!![groupIndex]
                save2 = groups[groupIndex + 1]
            }

            // Notify GroupTail there is no need to setup group info
            // because it will be set here
            locals[localIndex] = -1
            var ret = true
            for (j in 0 until cmin) {
                if (atom!!.match(matcher, i, seq)) {
                    if (capture) {
                        groups!![groupIndex] = i
                        groups[groupIndex + 1] = matcher.last
                    }
                    i = matcher.last
                } else {
                    ret = false
                    break
                }
            }
            if (ret) {
                ret = if (type == GREEDY) {
                    match0(matcher, i, cmin, seq)
                } else if (type == LAZY) {
                    match1(matcher, i, cmin, seq)
                } else {
                    match2(matcher, i, cmin, seq)
                }
            }
            if (!ret) {
                locals[localIndex] = save0
                if (capture) {
                    groups!![groupIndex] = save1
                    groups[groupIndex + 1] = save2
                }
            }
            return ret
        }

        // Aggressive group match
        fun match0(matcher: Matcher, i: Int, j: Int, seq: CharSequence?): Boolean {
            var i = i
            var j = j
            val groups = matcher.groups
            var save0 = 0
            var save1 = 0
            if (capture) {
                save0 = groups!![groupIndex]
                save1 = groups[groupIndex + 1]
            }
            while (true) {
                if (j >= cmax) break
                if (!atom!!.match(matcher, i, seq)) break
                val k = matcher.last - i
                if (k <= 0) {
                    if (capture) {
                        groups!![groupIndex] = i
                        groups[groupIndex + 1] = i + k
                    }
                    i = i + k
                    break
                }
                while (true) {
                    if (capture) {
                        groups!![groupIndex] = i
                        groups[groupIndex + 1] = i + k
                    }
                    i = i + k
                    if (++j >= cmax) break
                    if (!atom!!.match(matcher, i, seq)) break
                    if (i + k != matcher.last) {
                        if (match0(matcher, i, j, seq)) return true
                        break
                    }
                }
                while (j > cmin) {
                    if (next!!.match(matcher, i, seq)) {
                        if (capture) {
                            groups!![groupIndex + 1] = i
                            groups[groupIndex] = i - k
                        }
                        i = i - k
                        return true
                    }
                    // backing off
                    if (capture) {
                        groups!![groupIndex + 1] = i
                        groups[groupIndex] = i - k
                    }
                    i = i - k
                    j--
                }
                break
            }
            if (capture) {
                groups!![groupIndex] = save0
                groups[groupIndex + 1] = save1
            }
            return next!!.match(matcher, i, seq)
        }

        // Reluctant matching
        fun match1(matcher: Matcher, i: Int, j: Int, seq: CharSequence?): Boolean {
            var i = i
            var j = j
            while (true) {
                if (next!!.match(matcher, i, seq)) return true
                if (j >= cmax) return false
                if (!atom!!.match(matcher, i, seq)) return false
                if (i == matcher.last) return false
                if (capture) {
                    matcher.groups[groupIndex] = i
                    matcher.groups[groupIndex + 1] = matcher.last
                }
                i = matcher.last
                j++
            }
        }

        // Possessive matching
        fun match2(matcher: Matcher, i: Int, j: Int, seq: CharSequence?): Boolean {
            var i = i
            var j = j
            while (j < cmax) {
                if (!atom!!.match(matcher, i, seq)) {
                    break
                }
                if (capture) {
                    matcher.groups[groupIndex] = i
                    matcher.groups[groupIndex + 1] = matcher.last
                }
                if (i == matcher.last) {
                    break
                }
                i = matcher.last
                j++
            }
            return next!!.match(matcher, i, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            // Save original info
            val minL = info.minLength
            val maxL = info.maxLength
            val maxV = info.maxValid
            val detm = info.deterministic
            info.reset()
            atom!!.study(info)
            var temp = info.minLength * cmin + minL
            if (temp < minL) {
                temp = 0xFFFFFFF // Arbitrary large number
            }
            info.minLength = temp
            if (maxV and info.maxValid) {
                temp = info.maxLength * cmax + maxL
                info.maxLength = temp
                if (temp < maxL) {
                    info.maxValid = false
                }
            } else {
                info.maxValid = false
            }
            if (info.deterministic && cmin == cmax) {
                info.deterministic = detm
            } else {
                info.deterministic = false
            }
            return next!!.study(info)
        }
    }

    /**
     * A Guard node at the end of each atom node in a Branch. It
     * serves the purpose of chaining the "match" operation to
     * "next" but not the "study", so we can collect the TreeInfo
     * of each atom node without including the TreeInfo of the
     * "next".
     */
    internal class BranchConn : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return next!!.match(matcher, i, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            return info.deterministic
        }
    }

    /**
     * Handles the branching of alternations. Note this is also used for
     * the ? quantifier to branch between the case where it matches once
     * and where it does not occur.
     */
    internal class Branch(first: Node?, second: Node?, var conn: Node?) : Node() {
        var atoms = arrayOfNulls<Node>(2)
        var size = 2

        init {
            atoms[0] = first
            atoms[1] = second
        }

        fun add(node: Node?) {
            if (size >= atoms.size) {
                val tmp = arrayOfNulls<Node>(atoms.size * 2)
                System.arraycopy(atoms, 0, tmp, 0, atoms.size)
                atoms = tmp
            }
            atoms[size++] = node
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            for (n in 0 until size) {
                if (atoms[n] == null) {
                    if (conn!!.next!!.match(matcher, i, seq)) return true
                } else if (atoms[n]!!.match(matcher, i, seq)) {
                    return true
                }
            }
            return false
        }

        override fun study(info: TreeInfo): Boolean {
            var minL = info.minLength
            var maxL = info.maxLength
            var maxV = info.maxValid
            var minL2 = Int.MAX_VALUE //arbitrary large enough num
            var maxL2 = -1
            for (n in 0 until size) {
                info.reset()
                if (atoms[n] != null) atoms[n]!!.study(info)
                minL2 = Math.min(minL2, info.minLength)
                maxL2 = Math.max(maxL2, info.maxLength)
                maxV = maxV and info.maxValid
            }
            minL += minL2
            maxL += maxL2
            info.reset()
            conn!!.next!!.study(info)
            info.minLength += minL
            info.maxLength += maxL
            info.maxValid = info.maxValid and maxV
            info.deterministic = false
            return false
        }
    }

    /**
     * The GroupHead saves the location where the group begins in the locals
     * and restores them when the match is done.
     *
     * The matchRef is used when a reference to this group is accessed later
     * in the expression. The locals will have a negative value in them to
     * indicate that we do not want to unset the group if the reference
     * doesn't match.
     */
    class GroupHead(var localIndex: Int) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val save = matcher.locals[localIndex]
            matcher.locals[localIndex] = i
            val ret = next!!.match(matcher, i, seq)
            matcher.locals[localIndex] = save
            return ret
        }

        fun matchRef(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val save = matcher.locals[localIndex]
            matcher.locals[localIndex] = i.inv() // HACK
            val ret = next!!.match(matcher, i, seq)
            matcher.locals[localIndex] = save
            return ret
        }
    }

    /**
     * Recursive reference to a group in the regular expression. It calls
     * matchRef because if the reference fails to match we would not unset
     * the group.
     */
    internal class GroupRef(var head: GroupHead) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return (head.matchRef(matcher, i, seq)
                    && next!!.match(matcher, matcher.last, seq))
        }

        override fun study(info: TreeInfo): Boolean {
            info.maxValid = false
            info.deterministic = false
            return next!!.study(info)
        }
    }

    /**
     * The GroupTail handles the setting of group beginning and ending
     * locations when groups are successfully matched. It must also be able to
     * unset groups that have to be backed off of.
     *
     * The GroupTail node is also used when a previous group is referenced,
     * and in that case no group information needs to be set.
     */
    internal class GroupTail(var localIndex: Int, groupCount: Int) : Node() {
        var groupIndex: Int

        init {
            groupIndex = groupCount + groupCount
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val tmp = matcher.locals[localIndex]
            return if (tmp >= 0) { // This is the normal group case.
                // Save the group so we can unset it if it
                // backs off of a match.
                val groupStart = matcher.groups[groupIndex]
                val groupEnd = matcher.groups[groupIndex + 1]
                matcher.groups[groupIndex] = tmp
                matcher.groups[groupIndex + 1] = i
                if (next!!.match(matcher, i, seq)) {
                    return true
                }
                matcher.groups[groupIndex] = groupStart
                matcher.groups[groupIndex + 1] = groupEnd
                false
            } else {
                // This is a group reference case. We don't need to save any
                // group info because it isn't really a group.
                matcher.last = i
                true
            }
        }
    }

    /**
     * This sets up a loop to handle a recursive quantifier structure.
     */
    internal class Prolog(var loop: Loop) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return loop.matchInit(matcher, i, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            return loop.study(info)
        }
    }

    /**
     * Handles the repetition count for a greedy Curly. The matchInit
     * is called from the Prolog to save the index of where the group
     * beginning is stored. A zero length group check occurs in the
     * normal match but is skipped in the matchInit.
     */
    internal open class Loop(// local count index in matcher locals
        var countIndex: Int, // group beginning index
        var beginIndex: Int
    ) : Node() {
        var body: Node? = null
        var cmin = 0
        var cmax = 0
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            // Avoid infinite loop in zero-length case.
            if (i > matcher.locals[beginIndex]) {
                val count = matcher.locals[countIndex]

                // This block is for before we reach the minimum
                // iterations required for the loop to match
                if (count < cmin) {
                    matcher.locals[countIndex] = count + 1
                    val b = body!!.match(matcher, i, seq)
                    // If match failed we must backtrack, so
                    // the loop count should NOT be incremented
                    if (!b) matcher.locals[countIndex] = count
                    // Return success or failure since we are under
                    // minimum
                    return b
                }
                // This block is for after we have the minimum
                // iterations required for the loop to match
                if (count < cmax) {
                    matcher.locals[countIndex] = count + 1
                    val b = body!!.match(matcher, i, seq)
                    // If match failed we must backtrack, so
                    // the loop count should NOT be incremented
                    if (!b) matcher.locals[countIndex] = count else return true
                }
            }
            return next!!.match(matcher, i, seq)
        }

        open fun matchInit(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val save = matcher.locals[countIndex]
            var ret = false
            if (0 < cmin) {
                matcher.locals[countIndex] = 1
                ret = body!!.match(matcher, i, seq)
            } else if (0 < cmax) {
                matcher.locals[countIndex] = 1
                ret = body!!.match(matcher, i, seq)
                if (ret == false) ret = next!!.match(matcher, i, seq)
            } else {
                ret = next!!.match(matcher, i, seq)
            }
            matcher.locals[countIndex] = save
            return ret
        }

        override fun study(info: TreeInfo): Boolean {
            info.maxValid = false
            info.deterministic = false
            return false
        }
    }

    /**
     * Handles the repetition count for a reluctant Curly. The matchInit
     * is called from the Prolog to save the index of where the group
     * beginning is stored. A zero length group check occurs in the
     * normal match but is skipped in the matchInit.
     */
    internal class LazyLoop(countIndex: Int, beginIndex: Int) : Loop(countIndex, beginIndex) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            // Check for zero length group
            if (i > matcher.locals[beginIndex]) {
                val count = matcher.locals[countIndex]
                if (count < cmin) {
                    matcher.locals[countIndex] = count + 1
                    val result = body!!.match(matcher, i, seq)
                    // If match failed we must backtrack, so
                    // the loop count should NOT be incremented
                    if (!result) matcher.locals[countIndex] = count
                    return result
                }
                if (next!!.match(matcher, i, seq)) return true
                if (count < cmax) {
                    matcher.locals[countIndex] = count + 1
                    val result = body!!.match(matcher, i, seq)
                    // If match failed we must backtrack, so
                    // the loop count should NOT be incremented
                    if (!result) matcher.locals[countIndex] = count
                    return result
                }
                return false
            }
            return next!!.match(matcher, i, seq)
        }

        override fun matchInit(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val save = matcher.locals[countIndex]
            var ret = false
            if (0 < cmin) {
                matcher.locals[countIndex] = 1
                ret = body!!.match(matcher, i, seq)
            } else if (next!!.match(matcher, i, seq)) {
                ret = true
            } else if (0 < cmax) {
                matcher.locals[countIndex] = 1
                ret = body!!.match(matcher, i, seq)
            }
            matcher.locals[countIndex] = save
            return ret
        }

        override fun study(info: TreeInfo): Boolean {
            info.maxValid = false
            info.deterministic = false
            return false
        }
    }

    /**
     * Refers to a group in the regular expression. Attempts to match
     * whatever the group referred to last matched.
     */
    internal class BackRef(groupCount: Int) : Node() {
        var groupIndex: Int

        init {
            groupIndex = groupCount + groupCount
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val j = matcher.groups[groupIndex]
            val k = matcher.groups[groupIndex + 1]
            val groupSize = k - j

            // If the referenced group didn't match, neither can this
            if (j < 0) return false

            // If there isn't enough input left no match
            if (i + groupSize > matcher.to) {
                matcher.hitEnd = true
                return false
            }

            // Check each new char to make sure it matches what the group
            // referenced matched last time around
            for (index in 0 until groupSize) if (seq!![i + index] != seq[j + index]) return false
            return next!!.match(matcher, i + groupSize, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            info.maxValid = false
            return next!!.study(info)
        }
    }

    internal class CIBackRef(groupCount: Int, var doUnicodeCase: Boolean) : Node() {
        var groupIndex: Int

        init {
            groupIndex = groupCount + groupCount
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var j = matcher.groups[groupIndex]
            val k = matcher.groups[groupIndex + 1]
            val groupSize = k - j

            // If the referenced group didn't match, neither can this
            if (j < 0) return false

            // If there isn't enough input left no match
            if (i + groupSize > matcher.to) {
                matcher.hitEnd = true
                return false
            }

            // Check each new char to make sure it matches what the group
            // referenced matched last time around
            var x = i
            for (index in 0 until groupSize) {
                val c1 = Character.codePointAt(seq, x)
                val c2 = Character.codePointAt(seq, j)
                if (c1 != c2) {
                    if (doUnicodeCase) {
                        val cc1: Int = c1.uppercaseChar()
                        val cc2: Int = c2.uppercaseChar()
                        if (cc1 != cc2 &&
                            cc1.lowercaseChar() != cc2.lowercaseChar()
                        ) return false
                    } else {
                        if (ASCII.toLower(c1) != ASCII.toLower(c2)) return false
                    }
                }
                x += Character.charCount(c1)
                j += Character.charCount(c2)
            }
            return next!!.match(matcher, i + groupSize, seq)
        }

        override fun study(info: TreeInfo): Boolean {
            info.maxValid = false
            return next!!.study(info)
        }
    }

    /**
     * Searches until the next instance of its atom. This is useful for
     * finding the atom efficiently without passing an instance of it
     * (greedy problem) and without a lot of wasted search time (reluctant
     * problem).
     */
    internal class First(node: Node) : Node() {
        var atom: Node

        init {
            atom = BnM.optimize(node)
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            if (atom is BnM) {
                return (atom.match(matcher, i, seq)
                        && next!!.match(matcher, matcher.last, seq))
            }
            while (true) {
                if (i > matcher.to) {
                    matcher.hitEnd = true
                    return false
                }
                if (atom.match(matcher, i, seq)) {
                    return next!!.match(matcher, matcher.last, seq)
                }
                i += countChars(seq, i, 1)
                matcher.first++
            }
        }

        override fun study(info: TreeInfo): Boolean {
            atom.study(info)
            info.maxValid = false
            info.deterministic = false
            return next!!.study(info)
        }
    }

    internal class Conditional(var cond: Node, var yes: Node, var not: Node) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return if (cond.match(matcher, i, seq)) {
                yes.match(matcher, i, seq)
            } else {
                not.match(matcher, i, seq)
            }
        }

        override fun study(info: TreeInfo): Boolean {
            val minL = info.minLength
            val maxL = info.maxLength
            val maxV = info.maxValid
            info.reset()
            yes.study(info)
            val minL2 = info.minLength
            val maxL2 = info.maxLength
            val maxV2 = info.maxValid
            info.reset()
            not.study(info)
            info.minLength = minL + Math.min(minL2, info.minLength)
            info.maxLength = maxL + Math.max(maxL2, info.maxLength)
            info.maxValid = maxV and maxV2 and info.maxValid
            info.deterministic = false
            return next!!.study(info)
        }
    }

    /**
     * Zero width positive lookahead.
     */
    internal class Pos(var cond: Node?) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val savedTo = matcher.to
            var conditionMatched = false

            // Relax transparent region boundaries for lookahead
            if (matcher.transparentBounds) matcher.to = matcher.textLength
            conditionMatched = try {
                cond!!.match(matcher, i, seq)
            } finally {
                // Reinstate region boundaries
                matcher.to = savedTo
            }
            return conditionMatched && next!!.match(matcher, i, seq)
        }
    }

    /**
     * Zero width negative lookahead.
     */
    internal class Neg(var cond: Node?) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val savedTo = matcher.to
            var conditionMatched = false

            // Relax transparent region boundaries for lookahead
            if (matcher.transparentBounds) matcher.to = matcher.textLength
            try {
                if (i < matcher.to) {
                    conditionMatched = !cond!!.match(matcher, i, seq)
                } else {
                    // If a negative lookahead succeeds then more input
                    // could cause it to fail!
                    matcher.requireEnd = true
                    conditionMatched = !cond!!.match(matcher, i, seq)
                }
            } finally {
                // Reinstate region boundaries
                matcher.to = savedTo
            }
            return conditionMatched && next!!.match(matcher, i, seq)
        }
    }

    /**
     * Zero width positive lookbehind.
     */
    internal open class Behind(var cond: Node?, var rmax: Int, var rmin: Int) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val savedFrom = matcher.from
            var conditionMatched = false
            val startIndex = if (!matcher.transparentBounds) matcher.from else 0
            val from = Math.max(i - rmax, startIndex)
            // Set end boundary
            val savedLBT = matcher.lookbehindTo
            matcher.lookbehindTo = i
            // Relax transparent region boundaries for lookbehind
            if (matcher.transparentBounds) matcher.from = 0
            var j = i - rmin
            while (!conditionMatched && j >= from) {
                conditionMatched = cond!!.match(matcher, j, seq)
                j--
            }
            matcher.from = savedFrom
            matcher.lookbehindTo = savedLBT
            return conditionMatched && next!!.match(matcher, i, seq)
        }
    }

    /**
     * Zero width positive lookbehind, including supplementary
     * characters or unpaired surrogates.
     */
    internal class BehindS(cond: Node?, rmax: Int, rmin: Int) : Behind(cond, rmax, rmin) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val rmaxChars = countChars(seq, i, -rmax)
            val rminChars = countChars(seq, i, -rmin)
            val savedFrom = matcher.from
            val startIndex = if (!matcher.transparentBounds) matcher.from else 0
            var conditionMatched = false
            val from = Math.max(i - rmaxChars, startIndex)
            // Set end boundary
            val savedLBT = matcher.lookbehindTo
            matcher.lookbehindTo = i
            // Relax transparent region boundaries for lookbehind
            if (matcher.transparentBounds) matcher.from = 0
            var j = i - rminChars
            while (!conditionMatched && j >= from) {
                conditionMatched = cond!!.match(matcher, j, seq)
                j -= if (j > from) countChars(seq, j, -1) else 1
            }
            matcher.from = savedFrom
            matcher.lookbehindTo = savedLBT
            return conditionMatched && next!!.match(matcher, i, seq)
        }
    }

    /**
     * Zero width negative lookbehind.
     */
    internal open class NotBehind(var cond: Node?, var rmax: Int, var rmin: Int) : Node() {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val savedLBT = matcher.lookbehindTo
            val savedFrom = matcher.from
            var conditionMatched = false
            val startIndex = if (!matcher.transparentBounds) matcher.from else 0
            val from = Math.max(i - rmax, startIndex)
            matcher.lookbehindTo = i
            // Relax transparent region boundaries for lookbehind
            if (matcher.transparentBounds) matcher.from = 0
            var j = i - rmin
            while (!conditionMatched && j >= from) {
                conditionMatched = cond!!.match(matcher, j, seq)
                j--
            }
            // Reinstate region boundaries
            matcher.from = savedFrom
            matcher.lookbehindTo = savedLBT
            return !conditionMatched && next!!.match(matcher, i, seq)
        }
    }

    /**
     * Zero width negative lookbehind, including supplementary
     * characters or unpaired surrogates.
     */
    internal class NotBehindS(cond: Node?, rmax: Int, rmin: Int) : NotBehind(cond, rmax, rmin) {
        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            val rmaxChars = countChars(seq, i, -rmax)
            val rminChars = countChars(seq, i, -rmin)
            val savedFrom = matcher.from
            val savedLBT = matcher.lookbehindTo
            var conditionMatched = false
            val startIndex = if (!matcher.transparentBounds) matcher.from else 0
            val from = Math.max(i - rmaxChars, startIndex)
            matcher.lookbehindTo = i
            // Relax transparent region boundaries for lookbehind
            if (matcher.transparentBounds) matcher.from = 0
            var j = i - rminChars
            while (!conditionMatched && j >= from) {
                conditionMatched = cond!!.match(matcher, j, seq)
                j -= if (j > from) countChars(seq, j, -1) else 1
            }
            //Reinstate region boundaries
            matcher.from = savedFrom
            matcher.lookbehindTo = savedLBT
            return !conditionMatched && next!!.match(matcher, i, seq)
        }
    }

    /**
     * Handles word boundaries. Includes a field to allow this one class to
     * deal with the different types of word boundaries we can match. The word
     * characters include underscores, letters, and digits. Non spacing marks
     * can are also part of a word if they have a base character, otherwise
     * they are ignored for purposes of finding word boundaries.
     */
    internal class Bound(var type: Int) : Node() {
        fun check(matcher: Matcher, i: Int, seq: CharSequence?): Int {
            var ch: Int
            var left = false
            var startIndex = matcher.from
            var endIndex = matcher.to
            if (matcher.transparentBounds) {
                startIndex = 0
                endIndex = matcher.textLength
            }
            if (i > startIndex) {
                ch = Character.codePointBefore(seq, i)
                left = ch == '_'.code || Character.isLetterOrDigit(ch) || (Character.getType(ch) == Character.NON_SPACING_MARK.toInt()
                        && hasBaseCharacter(matcher, i - 1, seq))
            }
            var right = false
            if (i < endIndex) {
                ch = Character.codePointAt(seq, i)
                right = ch == '_'.code || Character.isLetterOrDigit(ch) || (Character.getType(ch) == Character.NON_SPACING_MARK.toInt()
                        && hasBaseCharacter(matcher, i, seq))
            } else {
                // Tried to access char past the end
                matcher.hitEnd = true
                // The addition of another char could wreck a boundary
                matcher.requireEnd = true
            }
            return if (left xor right) (if (right) LEFT else RIGHT) else NONE
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            return (check(matcher, i, seq) and type > 0
                    && next!!.match(matcher, i, seq))
        }

        companion object {
            var LEFT = 0x1
            var RIGHT = 0x2
            var BOTH = 0x3
            var NONE = 0x4
        }
    }

    /**
     * Attempts to match a slice in the input using the Boyer-Moore string
     * matching algorithm. The algorithm is based on the idea that the
     * pattern can be shifted farther ahead in the search text if it is
     * matched right to left.
     *
     *
     * The pattern is compared to the input one character at a time, from
     * the rightmost character in the pattern to the left. If the characters
     * all match the pattern has been found. If a character does not match,
     * the pattern is shifted right a distance that is the maximum of two
     * functions, the bad character shift and the good suffix shift. This
     * shift moves the attempted match position through the input more
     * quickly than a naive one position at a time check.
     *
     *
     * The bad character shift is based on the character from the text that
     * did not match. If the character does not appear in the pattern, the
     * pattern can be shifted completely beyond the bad character. If the
     * character does occur in the pattern, the pattern can be shifted to
     * line the pattern up with the next occurrence of that character.
     *
     *
     * The good suffix shift is based on the idea that some subset on the right
     * side of the pattern has matched. When a bad character is found, the
     * pattern can be shifted right by the pattern length if the subset does
     * not occur again in pattern, or by the amount of distance to the
     * next occurrence of the subset in the pattern.
     *
     * Boyer-Moore search methods adapted from code by Amy Yu.
     */
    internal open class BnM(var buffer: IntArray, var lastOcc: IntArray, var optoSft: IntArray, next: Node?) : Node() {
        init {
            this.next = next
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            val src = buffer
            val patternLength = src.size
            val last = matcher.to - patternLength

            // Loop over all possible match positions in text
            NEXT@ while (i <= last) {
                // Loop over pattern from right to left
                for (j in patternLength - 1 downTo 0) {
                    val ch = seq!![i + j].code
                    if (ch != src[j]) {
                        // Shift search to the right by the maximum of the
                        // bad character shift and the good suffix shift
                        i += Math.max(j + 1 - lastOcc[ch and 0x7F], optoSft[j])
                        continue@NEXT
                    }
                }
                // Entire pattern matched starting at i
                matcher.first = i
                val ret = next!!.match(matcher, i + patternLength, seq)
                if (ret) {
                    matcher.first = i
                    matcher.groups[0] = matcher.first
                    matcher.groups[1] = matcher.last
                    return true
                }
                i++
            }
            // BnM is only used as the leading node in the unanchored case,
            // and it replaced its Start() which always searches to the end
            // if it doesn't find what it's looking for, so hitEnd is true.
            matcher.hitEnd = true
            return false
        }

        override fun study(info: TreeInfo): Boolean {
            info.minLength += buffer.size
            info.maxValid = false
            return next!!.study(info)
        }

        companion object {
            /**
             * Pre calculates arrays needed to generate the bad character
             * shift and the good suffix shift. Only the last seven bits
             * are used to see if chars match; This keeps the tables small
             * and covers the heavily used ASCII range, but occasionally
             * results in an aliased match for the bad character shift.
             */
            fun optimize(node: Node): Node {
                if (node !is Slice) {
                    return node
                }
                val src = node.buffer
                val patternLength = src.size
                // The BM algorithm requires a bit of overhead;
                // If the pattern is short don't use it, since
                // a shift larger than the pattern length cannot
                // be used anyway.
                if (patternLength < 4) {
                    return node
                }
                var i: Int
                var j: Int
                var k: Int
                val lastOcc = IntArray(128)
                val optoSft = IntArray(patternLength)
                // Precalculate part of the bad character shift
                // It is a table for where in the pattern each
                // lower 7-bit value occurs
                i = 0
                while (i < patternLength) {
                    lastOcc[src[i] and 0x7F] = i + 1
                    i++
                }
                i = patternLength
                NEXT@ while (i > 0) {

                    // j is the beginning index of suffix being considered
                    j = patternLength - 1
                    while (j >= i) {

                        // Testing for good suffix
                        if (src[j] == src[j - i]) {
                            // src[j..len] is a good suffix
                            optoSft[j - 1] = i
                        } else {
                            // No match. The array has already been
                            // filled up with correct values before.
                            i--
                            continue@NEXT
                        }
                        j--
                    }
                    // This fills up the remaining of optoSft
                    // any suffix can not have larger shift amount
                    // then its sub-suffix. Why???
                    while (j > 0) {
                        optoSft[--j] = i
                    }
                    i--
                }
                // Set the guard value because of unicode compression
                optoSft[patternLength - 1] = 1
                return if (node is SliceS) BnMS(src, lastOcc, optoSft, node.next) else BnM(src, lastOcc, optoSft, node.next)
            }
        }
    }

    /**
     * Supplementary support version of BnM(). Unpaired surrogates are
     * also handled by this class.
     */
    internal class BnMS(src: IntArray, lastOcc: IntArray, optoSft: IntArray, next: Node?) : BnM(src, lastOcc, optoSft, next) {
        var lengthInChars = 0

        init {
            for (x in buffer.indices) {
                lengthInChars += Character.charCount(buffer[x])
            }
        }

        override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
            var i = i
            val src = buffer
            val patternLength = src.size
            val last = matcher.to - lengthInChars

            // Loop over all possible match positions in text
            NEXT@ while (i <= last) {
                // Loop over pattern from right to left
                var ch: Int
                var j = countChars(seq, i, patternLength)
                var x = patternLength - 1
                while (j > 0) {
                    ch = Character.codePointBefore(seq, i + j)
                    if (ch != src[x]) {
                        // Shift search to the right by the maximum of the
                        // bad character shift and the good suffix shift
                        val n = Math.max(x + 1 - lastOcc[ch and 0x7F], optoSft[x])
                        i += countChars(seq, i, n)
                        continue@NEXT
                    }
                    j -= Character.charCount(ch)
                    x--
                }
                // Entire pattern matched starting at i
                matcher.first = i
                val ret = next!!.match(matcher, i + lengthInChars, seq)
                if (ret) {
                    matcher.first = i
                    matcher.groups[0] = matcher.first
                    matcher.groups[1] = matcher.last
                    return true
                }
                i += countChars(seq, i, 1)
            }
            matcher.hitEnd = true
            return false
        }
    }

    /**
     * This private constructor is used to create all Patterns. The pattern
     * string and match flags are all that is needed to completely describe
     * a Pattern. An empty pattern string results in an object tree with
     * only a Start node and a LastNode node.
     */
    init {

        // Reset group index count
        if (pattern.length > 0) {
            compile()
        } else {
            root = Start(lastAccept)
            matchRoot = lastAccept
        }
    }

    private object CharPropertyNames {
        fun charPropertyFor(name: String): CharProperty? {
            val m = map[name]
            return m?.make()
        }

        private fun defCategory(
            name: String,
            typeMask: Int
        ) {
            map[name] = object : CharPropertyFactory() {
                override fun make(): CharProperty {
                    return Category(typeMask)
                }
            }
        }

        private fun defRange(
            name: String,
            lower: Int, upper: Int
        ) {
            map[name] = object : CharPropertyFactory() {
                override fun make(): CharProperty {
                    return rangeFor(lower, upper)
                }
            }
        }

        private fun defCtype(
            name: String,
            ctype: Int
        ) {
            map[name] = object : CharPropertyFactory() {
                override fun make(): CharProperty {
                    return Ctype(ctype)
                }
            }
        }

        private fun defClone(
            name: String,
            p: CloneableProperty
        ) {
            map[name] = object : CharPropertyFactory() {
                override fun make(): CharProperty {
                    return p.clone()
                }
            }
        }

        private val map = HashMap<String, CharPropertyFactory>()

        init {
            // Unicode character property aliases, defined in
            // http://www.unicode.org/Public/UNIDATA/PropertyValueAliases.txt
            defCategory("Cn", 1 shl Character.UNASSIGNED.toInt())
            defCategory("Lu", 1 shl Character.UPPERCASE_LETTER.toInt())
            defCategory("Ll", 1 shl Character.LOWERCASE_LETTER.toInt())
            defCategory("Lt", 1 shl Character.TITLECASE_LETTER.toInt())
            defCategory("Lm", 1 shl Character.MODIFIER_LETTER.toInt())
            defCategory("Lo", 1 shl Character.OTHER_LETTER.toInt())
            defCategory("Mn", 1 shl Character.NON_SPACING_MARK.toInt())
            defCategory("Me", 1 shl Character.ENCLOSING_MARK.toInt())
            defCategory("Mc", 1 shl Character.COMBINING_SPACING_MARK.toInt())
            defCategory("Nd", 1 shl Character.DECIMAL_DIGIT_NUMBER.toInt())
            defCategory("Nl", 1 shl Character.LETTER_NUMBER.toInt())
            defCategory("No", 1 shl Character.OTHER_NUMBER.toInt())
            defCategory("Zs", 1 shl Character.SPACE_SEPARATOR.toInt())
            defCategory("Zl", 1 shl Character.LINE_SEPARATOR.toInt())
            defCategory("Zp", 1 shl Character.PARAGRAPH_SEPARATOR.toInt())
            defCategory("Cc", 1 shl Character.CONTROL.toInt())
            defCategory("Cf", 1 shl Character.FORMAT.toInt())
            defCategory("Co", 1 shl Character.PRIVATE_USE.toInt())
            defCategory("Cs", 1 shl Character.SURROGATE.toInt())
            defCategory("Pd", 1 shl Character.DASH_PUNCTUATION.toInt())
            defCategory("Ps", 1 shl Character.START_PUNCTUATION.toInt())
            defCategory("Pe", 1 shl Character.END_PUNCTUATION.toInt())
            defCategory("Pc", 1 shl Character.CONNECTOR_PUNCTUATION.toInt())
            defCategory("Po", 1 shl Character.OTHER_PUNCTUATION.toInt())
            defCategory("Sm", 1 shl Character.MATH_SYMBOL.toInt())
            defCategory("Sc", 1 shl Character.CURRENCY_SYMBOL.toInt())
            defCategory("Sk", 1 shl Character.MODIFIER_SYMBOL.toInt())
            defCategory("So", 1 shl Character.OTHER_SYMBOL.toInt())
            defCategory("Pi", 1 shl Character.INITIAL_QUOTE_PUNCTUATION.toInt())
            defCategory("Pf", 1 shl Character.FINAL_QUOTE_PUNCTUATION.toInt())
            defCategory(
                "L", 1 shl Character.UPPERCASE_LETTER.toInt() or
                        (1 shl Character.LOWERCASE_LETTER.toInt()) or
                        (1 shl Character.TITLECASE_LETTER.toInt()) or
                        (1 shl Character.MODIFIER_LETTER.toInt()) or
                        (1 shl Character.OTHER_LETTER.toInt())
            )
            defCategory(
                "M", 1 shl Character.NON_SPACING_MARK.toInt() or
                        (1 shl Character.ENCLOSING_MARK.toInt()) or
                        (1 shl Character.COMBINING_SPACING_MARK.toInt())
            )
            defCategory(
                "N", 1 shl Character.DECIMAL_DIGIT_NUMBER.toInt() or
                        (1 shl Character.LETTER_NUMBER.toInt()) or
                        (1 shl Character.OTHER_NUMBER.toInt())
            )
            defCategory(
                "Z", 1 shl Character.SPACE_SEPARATOR.toInt() or
                        (1 shl Character.LINE_SEPARATOR.toInt()) or
                        (1 shl Character.PARAGRAPH_SEPARATOR.toInt())
            )
            defCategory(
                "C", 1 shl Character.CONTROL.toInt() or
                        (1 shl Character.FORMAT.toInt()) or
                        (1 shl Character.PRIVATE_USE.toInt()) or
                        (1 shl Character.SURROGATE.toInt())
            ) // Other
            defCategory(
                "P", 1 shl Character.DASH_PUNCTUATION.toInt() or
                        (1 shl Character.START_PUNCTUATION.toInt()) or
                        (1 shl Character.END_PUNCTUATION.toInt()) or
                        (1 shl Character.CONNECTOR_PUNCTUATION.toInt()) or
                        (1 shl Character.OTHER_PUNCTUATION.toInt()) or
                        (1 shl Character.INITIAL_QUOTE_PUNCTUATION.toInt()) or
                        (1 shl Character.FINAL_QUOTE_PUNCTUATION.toInt())
            )
            defCategory(
                "S", 1 shl Character.MATH_SYMBOL.toInt() or
                        (1 shl Character.CURRENCY_SYMBOL.toInt()) or
                        (1 shl Character.MODIFIER_SYMBOL.toInt()) or
                        (1 shl Character.OTHER_SYMBOL.toInt())
            )
            defCategory(
                "LC", 1 shl Character.UPPERCASE_LETTER.toInt() or
                        (1 shl Character.LOWERCASE_LETTER.toInt()) or
                        (1 shl Character.TITLECASE_LETTER.toInt())
            )
            defCategory(
                "LD", 1 shl Character.UPPERCASE_LETTER.toInt() or
                        (1 shl Character.LOWERCASE_LETTER.toInt()) or
                        (1 shl Character.TITLECASE_LETTER.toInt()) or
                        (1 shl Character.MODIFIER_LETTER.toInt()) or
                        (1 shl Character.OTHER_LETTER.toInt()) or
                        (1 shl Character.DECIMAL_DIGIT_NUMBER.toInt())
            )
            defRange("L1", 0x00, 0xFF) // Latin-1
            map["all"] = object : CharPropertyFactory() {
                override fun make(): CharProperty {
                    return All()
                }
            }

            // Posix regular expression character classes, defined in
            // http://www.unix.org/onlinepubs/009695399/basedefs/xbd_chap09.html
            defRange("ASCII", 0x00, 0x7F) // ASCII
            defCtype("Alnum", ASCII.ALNUM) // Alphanumeric characters
            defCtype("Alpha", ASCII.ALPHA) // Alphabetic characters
            defCtype("Blank", ASCII.BLANK) // Space and tab characters
            defCtype("Cntrl", ASCII.CNTRL) // Control characters
            defRange("Digit", '0'.code, '9'.code) // Numeric characters
            defCtype("Graph", ASCII.GRAPH) // printable and visible
            defRange("Lower", 'a'.code, 'z'.code) // Lower-case alphabetic
            defRange("Print", 0x20, 0x7E) // Printable characters
            defCtype("Punct", ASCII.PUNCT) // Punctuation characters
            defCtype("Space", ASCII.SPACE) // Space characters
            defRange("Upper", 'A'.code, 'Z'.code) // Upper-case alphabetic
            defCtype("XDigit", ASCII.XDIGIT) // hexadecimal digits

            // Java character properties, defined by methods in Character.java
            defClone("javaLowerCase", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isLowerCase(ch)
                }
            })
            defClone("javaUpperCase", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isUpperCase(ch)
                }
            })
            defClone("javaTitleCase", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isTitleCase(ch)
                }
            })
            defClone("javaDigit", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isDigit(ch)
                }
            })
            defClone("javaDefined", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isDefined(ch)
                }
            })
            defClone("javaLetter", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isLetter(ch)
                }
            })
            defClone("javaLetterOrDigit", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isLetterOrDigit(ch)
                }
            })
            defClone("javaJavaIdentifierStart", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isJavaIdentifierStart(ch)
                }
            })
            defClone("javaJavaIdentifierPart", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isJavaIdentifierPart(ch)
                }
            })
            defClone("javaUnicodeIdentifierStart", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isUnicodeIdentifierStart(ch)
                }
            })
            defClone("javaUnicodeIdentifierPart", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isUnicodeIdentifierPart(ch)
                }
            })
            defClone("javaIdentifierIgnorable", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isIdentifierIgnorable(ch)
                }
            })
            defClone("javaSpaceChar", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isSpaceChar(ch)
                }
            })
            defClone("javaWhitespace", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isWhitespace(ch)
                }
            })
            defClone("javaISOControl", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isISOControl(ch)
                }
            })
            defClone("javaMirrored", object : CloneableProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return Character.isMirrored(ch)
                }
            })
        }

        private abstract class CharPropertyFactory {
            abstract fun make(): CharProperty
        }

        private abstract class CloneableProperty : CharProperty(), Cloneable {
            public override fun clone(): CloneableProperty {
                return try {
                    super.clone() as CloneableProperty
                } catch (e: CloneNotSupportedException) {
                    throw AssertionError(e)
                }
            }
        }
    }

    companion object {
        /**
         * Regular expression modifier values.  Instead of being passed as
         * arguments, they can also be passed as inline modifiers.
         * For example, the following statements have the same effect.
         * <pre>
         * RegExp r1 = RegExp.compile("abc", Pattern.I|Pattern.M);
         * RegExp r2 = RegExp.compile("(?im)abc", 0);
        </pre> *
         *
         * The flags are duplicated so that the familiar Perl match flag
         * names are available.
         */
        /**
         * Enables Unix lines mode.
         *
         *
         *  In this mode, only the <tt>'\n'</tt> line terminator is recognized
         * in the behavior of <tt>.</tt>, <tt>^</tt>, and <tt>$</tt>.
         *
         *
         *  Unix lines mode can also be enabled via the embedded flag
         * expression&nbsp;<tt>(?d)</tt>.
         */
        const val UNIX_LINES = 0x01

        /**
         * Enables case-insensitive matching.
         *
         *
         *  By default, case-insensitive matching assumes that only characters
         * in the US-ASCII charset are being matched.  Unicode-aware
         * case-insensitive matching can be enabled by specifying the [ ][.UNICODE_CASE] flag in conjunction with this flag.
         *
         *
         *  Case-insensitive matching can also be enabled via the embedded flag
         * expression&nbsp;<tt>(?i)</tt>.
         *
         *
         *  Specifying this flag may impose a slight performance penalty.
         */
        const val CASE_INSENSITIVE = 0x02

        /**
         * Permits whitespace and comments in pattern.
         *
         *
         *  In this mode, whitespace is ignored, and embedded comments starting
         * with <tt>#</tt> are ignored until the end of a line.
         *
         *
         *  Comments mode can also be enabled via the embedded flag
         * expression&nbsp;<tt>(?x)</tt>.
         */
        const val COMMENTS = 0x04

        /**
         * Enables multiline mode.
         *
         *
         *  In multiline mode the expressions <tt>^</tt> and <tt>$</tt> match
         * just after or just before, respectively, a line terminator or the end of
         * the input sequence.  By default these expressions only match at the
         * beginning and the end of the entire input sequence.
         *
         *
         *  Multiline mode can also be enabled via the embedded flag
         * expression&nbsp;<tt>(?m)</tt>.
         */
        const val MULTILINE = 0x08

        /**
         * Enables literal parsing of the pattern.
         *
         *
         *  When this flag is specified then the input string that specifies
         * the pattern is treated as a sequence of literal characters.
         * Metacharacters or escape sequences in the input sequence will be
         * given no special meaning.
         *
         *
         * The flags CASE_INSENSITIVE and UNICODE_CASE retain their impact on
         * matching when used in conjunction with this flag. The other flags
         * become superfluous.
         *
         *
         *  There is no embedded flag character for enabling literal parsing.
         * @since 1.5
         */
        const val LITERAL = 0x10

        /**
         * Enables dotall mode.
         *
         *
         *  In dotall mode, the expression <tt>.</tt> matches any character,
         * including a line terminator.  By default this expression does not match
         * line terminators.
         *
         *
         *  Dotall mode can also be enabled via the embedded flag
         * expression&nbsp;<tt>(?s)</tt>.  (The <tt>s</tt> is a mnemonic for
         * "single-line" mode, which is what this is called in Perl.)
         */
        const val DOTALL = 0x20

        /**
         * Enables Unicode-aware case folding.
         *
         *
         *  When this flag is specified then case-insensitive matching, when
         * enabled by the [.CASE_INSENSITIVE] flag, is done in a manner
         * consistent with the Unicode Standard.  By default, case-insensitive
         * matching assumes that only characters in the US-ASCII charset are being
         * matched.
         *
         *
         *  Unicode-aware case folding can also be enabled via the embedded flag
         * expression&nbsp;<tt>(?u)</tt>.
         *
         *
         *  Specifying this flag may impose a performance penalty.
         */
        const val UNICODE_CASE = 0x40

        /**
         * Enables canonical equivalence.
         *
         *
         *  When this flag is specified then two characters will be considered
         * to match if, and only if, their full canonical decompositions match.
         * The expression <tt>"a&#92;u030A"</tt>, for example, will match the
         * string <tt>"&#92;u00E5"</tt> when this flag is specified.  By default,
         * matching does not take canonical equivalence into account.
         *
         *
         *  There is no embedded flag character for enabling canonical
         * equivalence.
         *
         *
         *  Specifying this flag may impose a performance penalty.
         */
        const val CANON_EQ = 0x80

        /**
         * Makes the closures ungreedy (a.k.a. lazy a.k.a. reluctant).
         */
        const val UNGREEDY = 0x100
        /* Pattern has only two serialized components: The pattern string
     * and the flags, which are all that is needed to recompile the pattern
     * when it is deserialized.
     */
        /** use serialVersionUID from Merlin b59 for interoperability  */
        private const val serialVersionUID = 5073258162644648461L

        /**
         * Compiles the given regular expression into a pattern.
         *
         * @param  regex
         * The expression to be compiled
         *
         * @throws  PatternSyntaxException
         * If the expression's syntax is invalid
         */
        @JvmStatic
        fun compile(regex: String): Pattern {
            return Pattern(regex, 0)
        }

        /**
         * Compiles the given regular expression into a pattern with the given
         * flags.
         *
         * @param  regex
         * The expression to be compiled
         *
         * @param  flags
         * Match flags, a bit mask that may include
         * [.CASE_INSENSITIVE], [.MULTILINE], [.DOTALL],
         * [.UNICODE_CASE], [.CANON_EQ], [.UNIX_LINES],
         * [.LITERAL], [.UNGREEDY] and [.COMMENTS]
         *
         * @throws  IllegalArgumentException
         * If bit values other than those corresponding to the defined
         * match flags are set in <tt>flags</tt>
         *
         * @throws  PatternSyntaxException
         * If the expression's syntax is invalid
         */
        @JvmStatic
        fun compile(regex: String, flags: Int): Pattern {
            return Pattern(regex, flags)
        }

        /**
         * Compiles the given regular expression and attempts to match the given
         * input against it.
         *
         *
         *  An invocation of this convenience method of the form
         *
         * <blockquote><pre>
         * Pattern.matches(regex, input);</pre></blockquote>
         *
         * behaves in exactly the same way as the expression
         *
         * <blockquote><pre>
         * Pattern.compile(regex).matcher(input).matches()</pre></blockquote>
         *
         *
         *  If a pattern is to be used multiple times, compiling it once and reusing
         * it will be more efficient than invoking this method each time.
         *
         * @param  regex
         * The expression to be compiled
         *
         * @param  input
         * The character sequence to be matched
         *
         * @throws  PatternSyntaxException
         * If the expression's syntax is invalid
         */
        fun matches(regex: String, input: CharSequence?): Boolean {
            val p = compile(regex)
            val m = p.matcher(input)
            return m.matches()
        }

        /**
         * Returns a literal pattern `String` for the specified
         * `String`.
         *
         *
         * This method produces a `String` that can be used to
         * create a `Pattern` that would match the string
         * `s` as if it were a literal pattern. Metacharacters
         * or escape sequences in the input sequence will be given no special
         * meaning.
         *
         * @param  s The string to be literalized
         * @return  A literal string replacement
         * @since 1.5
         */
        fun quote(s: String): String {
            var slashEIndex = s.indexOf("\\E")
            if (slashEIndex == -1) return "\\Q$s\\E"
            val sb = StringBuilder(s.length * 2)
            sb.append("\\Q")
            slashEIndex = 0
            var current = 0
            while (s.indexOf("\\E", current).also { slashEIndex = it } != -1) {
                sb.append(s.substring(current, slashEIndex))
                current = slashEIndex + 2
                sb.append("\\E\\\\E\\Q")
            }
            sb.append(s.substring(current, s.length))
            sb.append("\\E")
            return sb.toString()
        }

        /**
         * Used to print out a subtree of the Pattern to help with debugging.
         */
        private fun printObjectTree(node: Node?) {
            var node = node
            while (node != null) {
                if (node is Prolog) {
                    println(node)
                    printObjectTree(node.loop)
                    println("**** end contents prolog loop")
                } else if (node is Loop) {
                    println(node)
                    printObjectTree(node.body)
                    println("**** end contents Loop body")
                } else if (node is Curly) {
                    println(node)
                    printObjectTree(node.atom)
                    println("**** end contents Curly body")
                } else if (node is GroupCurly) {
                    println(node)
                    printObjectTree(node.atom)
                    println("**** end contents GroupCurly body")
                } else if (node is GroupTail) {
                    println(node)
                    println("Tail next is " + node.next)
                    return
                } else {
                    println(node)
                }
                node = node.next
                if (node != null) println("->next:")
                if (node === accept) {
                    println("Accept Node")
                    node = null
                }
            }
        }

        /**
         * Determines if the specified code point is a supplementary
         * character or unpaired surrogate.
         */
        private fun isSupplementary(ch: Int): Boolean {
            return ch >= Character.MIN_SUPPLEMENTARY_CODE_POINT || isSurrogate(ch)
        }

        const val MAX_REPS = 0x7FFFFFFF
        const val GREEDY = 0
        const val LAZY = 1
        const val POSSESSIVE = 2
        const val INDEPENDENT = 3
        //
        // Utility methods for code point support
        //
        /**
         * Tests a surrogate value.
         */
        private fun isSurrogate(c: Int): Boolean {
            return c >= Character.MIN_HIGH_SURROGATE.code && c <= Character.MAX_LOW_SURROGATE.code
        }

        private fun countChars(
            seq: CharSequence?, index: Int,
            lengthInCodePoints: Int
        ): Int {
            // optimization
            if (lengthInCodePoints == 1 && !Character.isHighSurrogate(seq!![index])) {
                assert(index >= 0 && index < seq.length)
                return 1
            }
            val length = seq!!.length
            var x = index
            if (lengthInCodePoints >= 0) {
                assert(index >= 0 && index < length)
                var i = 0
                while (x < length && i < lengthInCodePoints) {
                    if (Character.isHighSurrogate(seq[x++])) {
                        if (x < length && Character.isLowSurrogate(seq[x])) {
                            x++
                        }
                    }
                    i++
                }
                return x - index
            }
            assert(index >= 0 && index <= length)
            if (index == 0) {
                return 0
            }
            val len = -lengthInCodePoints
            var i = 0
            while (x > 0 && i < len) {
                if (Character.isLowSurrogate(seq[--x])) {
                    if (x > 0 && Character.isHighSurrogate(seq[x - 1])) {
                        x--
                    }
                }
                i++
            }
            return index - x
        }

        private fun countCodePoints(seq: CharSequence): Int {
            val length = seq.length
            var n = 0
            var i = 0
            while (i < length) {
                n++
                if (Character.isHighSurrogate(seq[i++])) {
                    if (i < length && Character.isLowSurrogate(seq[i])) {
                        i++
                    }
                }
            }
            return n
        }

        private fun inRange(lower: Int, ch: Int, upper: Int): Boolean {
            return lower <= ch && ch <= upper
        }

        /**
         * Returns node for matching characters within an explicit value range.
         */
        private fun rangeFor(
            lower: Int,
            upper: Int
        ): CharProperty {
            return object : CharProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return inRange(lower, ch, upper)
                }
            }
        }

        /**
         * For use with lookbehinds; matches the position where the lookbehind
         * was encountered.
         */
        var lookbehindEnd: Node = object : Node() {
            override fun match(matcher: Matcher, i: Int, seq: CharSequence?): Boolean {
                return i == matcher.lookbehindTo
            }
        }

        /**
         * Returns the set union of two CharProperty nodes.
         */
        private fun union(
            lhs: CharProperty,
            rhs: CharProperty?
        ): CharProperty {
            return object : CharProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return lhs.isSatisfiedBy(ch) || rhs!!.isSatisfiedBy(ch)
                }
            }
        }

        /**
         * Returns the set intersection of two CharProperty nodes.
         */
        private fun intersection(
            lhs: CharProperty,
            rhs: CharProperty?
        ): CharProperty {
            return object : CharProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return lhs.isSatisfiedBy(ch) && rhs!!.isSatisfiedBy(ch)
                }
            }
        }

        /**
         * Returns the set difference of two CharProperty nodes.
         */
        private fun setDifference(
            lhs: CharProperty,
            rhs: CharProperty?
        ): CharProperty {
            return object : CharProperty() {
                override fun isSatisfiedBy(ch: Int): Boolean {
                    return !rhs!!.isSatisfiedBy(ch) && lhs.isSatisfiedBy(ch)
                }
            }
        }

        /**
         * Non spacing marks only count as word characters in bounds calculations
         * if they have a base character.
         */
        private fun hasBaseCharacter(
            matcher: Matcher, i: Int,
            seq: CharSequence?
        ): Boolean {
            val start = if (!matcher.transparentBounds) matcher.from else 0
            for (x in i downTo start) {
                val ch = Character.codePointAt(seq, x)
                if (Character.isLetterOrDigit(ch)) return true
                if (Character.getType(ch) == Character.NON_SPACING_MARK.toInt()) continue
                return false
            }
            return false
        }
        ///////////////////////////////////////////////////////////////////////////////
        ///////////////////////////////////////////////////////////////////////////////
        /**
         * This must be the very first initializer.
         */
        var accept = Node()
        var lastAccept: Node = LastNode()
    }
}
