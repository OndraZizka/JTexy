/*
 * Copyright 2003-2004 Sun Microsystems, Inc.  All Rights Reserved.
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

/**
 * The result of a match operation.
 *
 *
 * This interface contains query methods used to determine the
 * results of a match against a regular expression. The match boundaries,
 * groups and group boundaries can be seen but not modified through
 * a `MatchResult`.
 *
 * @author  Michael McCloskey
 * @see Matcher
 *
 * @since 1.5
 */
interface MatchResult {
    /**
     * Returns the start index of the match.
     *
     * @return  The index of the first character matched
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     */
    fun start(): Int

    /**
     * Returns the start index of the subsequence captured by the given group
     * during this match.
     *
     *
     *  [Capturing groups](Pattern.html#cg) are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression *m.*<tt>start(0)</tt> is equivalent to
     * *m.*<tt>start()</tt>.
     *
     * @param  group
     * The index of a capturing group in this matcher's pattern
     *
     * @return  The index of the first character captured by the group,
     * or <tt>-1</tt> if the match was successful but the group
     * itself did not match anything
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     * If there is no capturing group in the pattern
     * with the given index
     */
    fun start(group: Int): Int

    /**
     * Returns the offset after the last character matched.
     *
     * @return  @return  The offset after the last character matched
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     */
    fun end(): Int

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given group during this match.
     *
     *
     *  [Capturing groups](Pattern.html#cg) are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression *m.*<tt>end(0)</tt> is equivalent to
     * *m.*<tt>end()</tt>.
     *
     * @param  group
     * The index of a capturing group in this matcher's pattern
     *
     * @return  The offset after the last character captured by the group,
     * or <tt>-1</tt> if the match was successful
     * but the group itself did not match anything
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     * If there is no capturing group in the pattern
     * with the given index
     */
    fun end(group: Int): Int

    /**
     * Returns the input subsequence matched by the previous match.
     *
     *
     *  For a matcher *m* with input sequence *s*,
     * the expressions *m.*<tt>group()</tt> and
     * *s.*<tt>substring(</tt>*m.*<tt>start(),</tt>&nbsp;*m.*<tt>end())</tt>
     * are equivalent.
     *
     *
     *  Note that some patterns, for example <tt>a*</tt>, match the empty
     * string.  This method will return the empty string when the pattern
     * successfully matches the empty string in the input.
     *
     * @return The (possibly empty) subsequence matched by the previous match,
     * in string form
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     */
    fun group(): String?

    /**
     * Returns the input subsequence captured by the given group during the
     * previous match operation.
     *
     *
     *  For a matcher *m*, input sequence *s*, and group index
     * *g*, the expressions *m.*<tt>group(</tt>*g*<tt>)</tt> and
     * *s.*<tt>substring(</tt>*m.*<tt>start(</tt>*g*<tt>),</tt>&nbsp;*m.*<tt>end(</tt>*g*<tt>))</tt>
     * are equivalent.
     *
     *
     *  [Capturing groups](Pattern.html#cg) are indexed from left
     * to right, starting at one.  Group zero denotes the entire pattern, so
     * the expression <tt>m.group(0)</tt> is equivalent to <tt>m.group()</tt>.
     *
     *
     *
     *  If the match was successful but the group specified failed to match
     * any part of the input sequence, then <tt>null</tt> is returned. Note
     * that some groups, for example <tt>(a*)</tt>, match the empty string.
     * This method will return the empty string when such a group successfully
     * matches the empty string in the input.
     *
     * @param  group
     * The index of a capturing group in this matcher's pattern
     *
     * @return  The (possibly empty) subsequence captured by the group
     * during the previous match, or <tt>null</tt> if the group
     * failed to match part of the input
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     * If there is no capturing group in the pattern
     * with the given index
     */
    fun group(group: Int): String?

    /**
     * Returns the number of capturing groups in this match result's pattern.
     *
     *
     *  Group zero denotes the entire pattern by convention. It is not
     * included in this count.
     *
     *
     *  Any non-negative integer smaller than or equal to the value
     * returned by this method is guaranteed to be a valid group index for
     * this matcher.
     *
     * @return The number of capturing groups in this matcher's pattern
     */
    fun groupCount(): Int
}
