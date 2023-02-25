/*
 * Copyright 1999-2006 Sun Microsystems, Inc.  All Rights Reserved.
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
 * An engine that performs match operations on a [ &lt;/code&gt;character sequence&lt;code&gt;][java.lang.CharSequence] by interpreting a [Pattern].
 *
 *
 *  A matcher is created from a pattern by invoking the pattern's [ ][Pattern.matcher] method.  Once created, a matcher can be used to
 * perform three different kinds of match operations:
 *
 *
 *
 *  *
 *
 * The [matches][.matches] method attempts to match the entire
 * input sequence against the pattern.
 *
 *  *
 *
 * The [lookingAt][.lookingAt] method attempts to match the
 * input sequence, starting at the beginning, against the pattern.
 *
 *  *
 *
 * The [find][.find] method scans the input sequence looking for
 * the next subsequence that matches the pattern.
 *
 *
 *
 *
 *  Each of these methods returns a boolean indicating success or failure.
 * More information about a successful match can be obtained by querying the
 * state of the matcher.
 *
 *
 *  A matcher finds matches in a subset of its input called the
 * *region*. By default, the region contains all of the matcher's input.
 * The region can be modified via the[region][.region] method and queried
 * via the [regionStart][.regionStart] and [regionEnd][.regionEnd]
 * methods. The way that the region boundaries interact with some pattern
 * constructs can be changed. See [ useAnchoringBounds][.useAnchoringBounds] and [useTransparentBounds][.useTransparentBounds]
 * for more details.
 *
 *
 *  This class also defines methods for replacing matched subsequences with
 * new strings whose contents can, if desired, be computed from the match
 * result.  The [appendReplacement][.appendReplacement] and [ ][.appendTail] methods can be used in tandem in order to collect
 * the result into an existing string buffer, or the more convenient [ ][.replaceAll] method can be used to create a string in which every
 * matching subsequence in the input sequence is replaced.
 *
 *
 *  The explicit state of a matcher includes the start and end indices of
 * the most recent successful match.  It also includes the start and end
 * indices of the input subsequence captured by each [capturing group](Pattern.html#cg) in the pattern as well as a total
 * count of such subsequences.  As a convenience, methods are also provided for
 * returning these captured subsequences in string form.
 *
 *
 *  The explicit state of a matcher is initially undefined; attempting to
 * query any part of it before a successful match will cause an [ ] to be thrown.  The explicit state of a matcher is
 * recomputed by every match operation.
 *
 *
 *  The implicit state of a matcher includes the input character sequence as
 * well as the *append position*, which is initially zero and is updated
 * by the [appendReplacement][.appendReplacement] method.
 *
 *
 *  A matcher may be reset explicitly by invoking its [.reset]
 * method or, if a new input sequence is desired, its [ ][.reset] method.  Resetting a
 * matcher discards its explicit state information and sets the append position
 * to zero.
 *
 *
 *  Instances of this class are not safe for use by multiple concurrent
 * threads.
 *
 *
 * @author      Mike McCloskey
 * @author      Mark Reinhold
 * @author      JSR-51 Expert Group
 * @since       1.4
 * @spec        JSR-51
 */
class Matcher : MatchResult {
    /**
     * The Pattern object that created this Matcher.
     */
    var parentPattern: Pattern? = null

    /**
     * The storage used by groups. They may contain invalid values if
     * a group was skipped during the matching.
     */
    var groups: IntArray

    /**
     * The range within the sequence that is to be matched. Anchors
     * will match at these "hard" boundaries. Changing the region
     * changes these values.
     */
    var from = 0
    var to = 0

    /**
     * Lookbehind uses this value to ensure that the subexpression
     * match ends at the point where the lookbehind was encountered.
     */
    var lookbehindTo = 0

    /**
     * The original string being matched.
     */
    var text: CharSequence? = null
    var acceptMode = NOANCHOR

    /**
     * The range of string that last matched the pattern. If the last
     * match failed then first is -1; last initially holds 0 then it
     * holds the index of the end of the last match (which is where the
     * next search starts).
     */
    var first = -1
    var last = 0

    /**
     * The end index of what matched in the last match operation.
     */
    var oldLast = -1

    /**
     * The index of the last position appended in a substitution.
     */
    var lastAppendPosition = 0

    /**
     * Storage used by nodes to tell what repetition they are on in
     * a pattern, and where groups begin. The nodes themselves are stateless,
     * so they rely on this field to hold state during a match.
     */
    var locals: IntArray

    /**
     * Boolean indicating whether or not more input could change
     * the results of the last match.
     *
     * If hitEnd is true, and a match was found, then more input
     * might cause a different match to be found.
     * If hitEnd is true and a match was not found, then more
     * input could cause a match to be found.
     * If hitEnd is false and a match was found, then more input
     * will not change the match.
     * If hitEnd is false and a match was not found, then more
     * input will not cause a match to be found.
     */
    var hitEnd = false

    /**
     * Boolean indicating whether or not more input could change
     * a positive match into a negative one.
     *
     * If requireEnd is true, and a match was found, then more
     * input could cause the match to be lost.
     * If requireEnd is false and a match was found, then more
     * input might change the match but the match won't be lost.
     * If a match was not found, then requireEnd has no meaning.
     */
    var requireEnd = false

    /**
     * If transparentBounds is true then the boundaries of this
     * matcher's region are transparent to lookahead, lookbehind,
     * and boundary matching constructs that try to see beyond them.
     */
    var transparentBounds = false

    /**
     * If anchoringBounds is true then the boundaries of this
     * matcher's region match anchors such as ^ and $.
     */
    var anchoringBounds = true

    /**
     * No default constructor.
     */
    internal constructor()

    /**
     * All matchers have the state used by Pattern during a match.
     */
    internal constructor(parent: Pattern?, text: CharSequence?) {
        parentPattern = parent
        this.text = text

        // Allocate state storage
        val parentGroupCount = Math.max(parent!!.capturingGroupCount, 10)
        groups = IntArray(parentGroupCount * 2)
        locals = IntArray(parent.localCount)

        // Put fields into initial states
        reset()
    }

    /**
     * Returns the pattern that is interpreted by this matcher.
     *
     * @return  The pattern for which this matcher was created
     */
    fun pattern(): Pattern? {
        return parentPattern
    }

    /**
     * Returns the match state of this matcher as a [MatchResult].
     * The result is unaffected by subsequent operations performed upon this
     * matcher.
     *
     * @return  a `MatchResult` with the state of this matcher
     * @since 1.5
     */
    fun toMatchResult(): MatchResult {
        val result = Matcher(parentPattern, text.toString())
        result.first = first
        result.last = last
        result.groups = groups.clone()
        return result
    }

    /**
     * Changes the <tt>Pattern</tt> that this <tt>Matcher</tt> uses to
     * find matches with.
     *
     *
     *  This method causes this matcher to lose information
     * about the groups of the last match that occurred. The
     * matcher's position in the input is maintained and its
     * last append position is unaffected.
     *
     * @param  newPattern
     * The new pattern used by this matcher
     * @return  This matcher
     * @throws  IllegalArgumentException
     * If newPattern is <tt>null</tt>
     * @since 1.5
     */
    fun usePattern(newPattern: Pattern?): Matcher {
        requireNotNull(newPattern) { "Pattern cannot be null" }
        parentPattern = newPattern

        // Reallocate state storage
        val parentGroupCount = Math.max(newPattern.capturingGroupCount, 10)
        groups = IntArray(parentGroupCount * 2)
        locals = IntArray(newPattern.localCount)
        for (i in groups.indices) groups[i] = -1
        for (i in locals.indices) locals[i] = -1
        return this
    }

    /**
     * Resets this matcher.
     *
     *
     *  Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero. The matcher's region is set to the
     * default region, which is its entire character sequence. The anchoring
     * and transparency of this matcher's region boundaries are unaffected.
     *
     * @return  This matcher
     */
    fun reset(): Matcher {
        first = -1
        last = 0
        oldLast = -1
        for (i in groups.indices) groups[i] = -1
        for (i in locals.indices) locals[i] = -1
        lastAppendPosition = 0
        from = 0
        to = getTextLength()
        return this
    }

    /**
     * Resets this matcher with a new input sequence.
     *
     *
     *  Resetting a matcher discards all of its explicit state information
     * and sets its append position to zero.  The matcher's region is set to
     * the default region, which is its entire character sequence.  The
     * anchoring and transparency of this matcher's region boundaries are
     * unaffected.
     *
     * @param  input
     * The new input character sequence
     *
     * @return  This matcher
     */
    fun reset(input: CharSequence?): Matcher {
        text = input
        return reset()
    }

    /**
     * Returns the start index of the previous match.
     *
     * @return  The index of the first character matched
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     */
    override fun start(): Int {
        check(first >= 0) { "No match available" }
        return first
    }

    /**
     * Returns the start index of the subsequence captured by the given group
     * during the previous match operation.
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
    override fun start(group: Int): Int {
        check(first >= 0) { "No match available" }
        if (group > groupCount()) throw IndexOutOfBoundsException("No group $group")
        return groups[group * 2]
    }

    /**
     * Returns the offset after the last character matched.
     *
     * @return  The offset after the last character matched
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     */
    override fun end(): Int {
        check(first >= 0) { "No match available" }
        return last
    }

    /**
     * Returns the offset after the last character of the subsequence
     * captured by the given group during the previous match operation.
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
    override fun end(group: Int): Int {
        check(first >= 0) { "No match available" }
        if (group > groupCount()) throw IndexOutOfBoundsException("No group $group")
        return groups[group * 2 + 1]
    }

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
    override fun group(): String? {
        return group(0)
    }

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
    override fun group(group: Int): String? {
        check(first >= 0) { "No match found" }
        if (group < 0 || group > groupCount()) throw IndexOutOfBoundsException("No group $group")
        return if (groups[group * 2] == -1 || groups[group * 2 + 1] == -1) null else getSubSequence(
            groups[group * 2],
            groups[group * 2 + 1]
        ).toString()
    }

    /**
     * Returns the number of capturing groups in this matcher's pattern.
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
    override fun groupCount(): Int {
        return parentPattern!!.capturingGroupCount - 1
    }

    /**
     * Attempts to match the entire region against the pattern.
     *
     *
     *  If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.
     *
     * @return  <tt>true</tt> if, and only if, the entire region sequence
     * matches this matcher's pattern
     */
    fun matches(): Boolean {
        return match(from, ENDANCHOR)
    }

    /**
     * Attempts to find the next subsequence of the input sequence that matches
     * the pattern.
     *
     *
     *  This method starts at the beginning of this matcher's region, or, if
     * a previous invocation of the method was successful and the matcher has
     * not since been reset, at the first character not matched by the previous
     * match.
     *
     *
     *  If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.
     *
     * @return  <tt>true</tt> if, and only if, a subsequence of the input
     * sequence matches this matcher's pattern
     */
    fun find(): Boolean {
        var nextSearchIndex = last
        if (nextSearchIndex == first) nextSearchIndex++

        // If next search starts before region, start it at region
        if (nextSearchIndex < from) nextSearchIndex = from

        // If next search starts beyond region then it fails
        if (nextSearchIndex > to) {
            for (i in groups.indices) groups[i] = -1
            return false
        }
        return search(nextSearchIndex)
    }

    /**
     * Resets this matcher and then attempts to find the next subsequence of
     * the input sequence that matches the pattern, starting at the specified
     * index.
     *
     *
     *  If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods, and subsequent
     * invocations of the [.find] method will start at the first
     * character not matched by this match.
     *
     * @throws  IndexOutOfBoundsException
     * If start is less than zero or if start is greater than the
     * length of the input sequence.
     *
     * @return  <tt>true</tt> if, and only if, a subsequence of the input
     * sequence starting at the given index matches this matcher's
     * pattern
     */
    fun find(start: Int): Boolean {
        val limit = getTextLength()
        if (start < 0 || start > limit) throw IndexOutOfBoundsException("Illegal start index")
        reset()
        return search(start)
    }

    /**
     * Attempts to match the input sequence, starting at the beginning of the
     * region, against the pattern.
     *
     *
     *  Like the [matches][.matches] method, this method always starts
     * at the beginning of the region; unlike that method, it does not
     * require that the entire region be matched.
     *
     *
     *  If the match succeeds then more information can be obtained via the
     * <tt>start</tt>, <tt>end</tt>, and <tt>group</tt> methods.
     *
     * @return  <tt>true</tt> if, and only if, a prefix of the input
     * sequence matches this matcher's pattern
     */
    fun lookingAt(): Boolean {
        return match(from, NOANCHOR)
    }

    /**
     * Implements a non-terminal append-and-replace step.
     *
     *
     *  This method performs the following actions:
     *
     *
     *
     *  1.
     *
     * It reads characters from the input sequence, starting at the
     * append position, and appends them to the given string buffer.  It
     * stops after reading the last character preceding the previous match,
     * that is, the character at index [   ][.start]&nbsp;<tt>-</tt>&nbsp;<tt>1</tt>.
     *
     *  1.
     *
     * It appends the given replacement string to the string buffer.
     *
     *
     *  1.
     *
     * It sets the append position of this matcher to the index of
     * the last character matched, plus one, that is, to [.end].
     *
     *
     *
     *
     *
     *  The replacement string may contain references to subsequences
     * captured during the previous match: Each occurrence of
     * <tt>$</tt>*g*<tt></tt> will be replaced by the result of
     * evaluating [group][.group]<tt>(</tt>*g*<tt>)</tt>.
     * The first number after the <tt>$</tt> is always treated as part of
     * the group reference. Subsequent numbers are incorporated into g if
     * they would form a legal group reference. Only the numerals '0'
     * through '9' are considered as potential components of the group
     * reference. If the second group matched the string <tt>"foo"</tt>, for
     * example, then passing the replacement string <tt>"$2bar"</tt> would
     * cause <tt>"foobar"</tt> to be appended to the string buffer. A dollar
     * sign (<tt>$</tt>) may be included as a literal in the replacement
     * string by preceding it with a backslash (<tt>\$</tt>).
     *
     *
     *  Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     *
     *  This method is intended to be used in a loop together with the
     * [appendTail][.appendTail] and [find][.find] methods.  The
     * following code, for example, writes <tt>one dog two dogs in the
     * yard</tt> to the standard-output stream:
     *
     * <blockquote><pre>
     * Pattern p = Pattern.compile("cat");
     * Matcher m = p.matcher("one cat two cats in the yard");
     * StringBuffer sb = new StringBuffer();
     * while (m.find()) {
     * m.appendReplacement(sb, "dog");
     * }
     * m.appendTail(sb);
     * System.out.println(sb.toString());</pre></blockquote>
     *
     * @param  sb
     * The target string buffer
     *
     * @param  replacement
     * The replacement string
     *
     * @return  This matcher
     *
     * @throws  IllegalStateException
     * If no match has yet been attempted,
     * or if the previous match operation failed
     *
     * @throws  IndexOutOfBoundsException
     * If the replacement string refers to a capturing group
     * that does not exist in the pattern
     */
    fun appendReplacement(sb: StringBuffer, replacement: String?): Matcher {

        // If no match, return error
        check(first >= 0) { "No match available" }

        // Process substitution string to replace group references with groups
        var cursor = 0
        val result = StringBuilder()
        while (cursor < replacement!!.length) {
            var nextChar = replacement[cursor]
            if (nextChar == '\\') {
                cursor++
                nextChar = replacement[cursor]
                result.append(nextChar)
                cursor++
            } else if (nextChar == '$') {
                // Skip past $
                cursor++
                // The first number is always a group
                var refNum = replacement[cursor].code - '0'.code
                require(!(refNum < 0 || refNum > 9)) { "Illegal group reference" }
                cursor++

                // Capture the largest legal group string
                var done = false
                while (!done) {
                    if (cursor >= replacement.length) {
                        break
                    }
                    val nextDigit = replacement[cursor].code - '0'.code
                    if (nextDigit < 0 || nextDigit > 9) { // not a number
                        break
                    }
                    val newRefNum = refNum * 10 + nextDigit
                    if (groupCount() < newRefNum) {
                        done = true
                    } else {
                        refNum = newRefNum
                        cursor++
                    }
                }
                // Append group
                if (start(refNum) != -1 && end(refNum) != -1) result.append(text, start(refNum), end(refNum))
            } else {
                result.append(nextChar)
                cursor++
            }
        }
        // Append the intervening text
        sb.append(text, lastAppendPosition, first)
        // Append the match substitution
        sb.append(result)
        lastAppendPosition = last
        return this
    }

    /**
     * Implements a terminal append-and-replace step.
     *
     *
     *  This method reads characters from the input sequence, starting at
     * the append position, and appends them to the given string buffer.  It is
     * intended to be invoked after one or more invocations of the [ ][.appendReplacement] method in order to copy the
     * remainder of the input sequence.
     *
     * @param  sb
     * The target string buffer
     *
     * @return  The target string buffer
     */
    fun appendTail(sb: StringBuffer): StringBuffer {
        sb.append(text, lastAppendPosition, getTextLength())
        return sb
    }

    /**
     * Replaces every subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     *
     *  This method first resets this matcher.  It then scans the input
     * sequence looking for matches of the pattern.  Characters that are not
     * part of any match are appended directly to the result string; each match
     * is replaced in the result by the replacement string.  The replacement
     * string may contain references to captured subsequences as in the [ ][.appendReplacement] method.
     *
     *
     *  Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     *
     *  Given the regular expression <tt>a*b</tt>, the input
     * <tt>"aabfooaabfooabfoob"</tt>, and the replacement string
     * <tt>"-"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"-foo-foo-foo-"</tt>.
     *
     *
     *  Invoking this method changes this matcher's state.  If the matcher
     * is to be used in further matching operations then it should first be
     * reset.
     *
     * @param  replacement
     * The replacement string
     *
     * @return  The string constructed by replacing each matching subsequence
     * by the replacement string, substituting captured subsequences
     * as needed
     */
    fun replaceAll(replacement: String?): String {
        reset()
        var result = find()
        if (result) {
            val sb = StringBuffer()
            do {
                appendReplacement(sb, replacement)
                result = find()
            } while (result)
            appendTail(sb)
            return sb.toString()
        }
        return text.toString()
    }

    /**
     * Replaces the first subsequence of the input sequence that matches the
     * pattern with the given replacement string.
     *
     *
     *  This method first resets this matcher.  It then scans the input
     * sequence looking for a match of the pattern.  Characters that are not
     * part of the match are appended directly to the result string; the match
     * is replaced in the result by the replacement string.  The replacement
     * string may contain references to captured subsequences as in the [ ][.appendReplacement] method.
     *
     *
     * Note that backslashes (<tt>\</tt>) and dollar signs (<tt>$</tt>) in
     * the replacement string may cause the results to be different than if it
     * were being treated as a literal replacement string. Dollar signs may be
     * treated as references to captured subsequences as described above, and
     * backslashes are used to escape literal characters in the replacement
     * string.
     *
     *
     *  Given the regular expression <tt>dog</tt>, the input
     * <tt>"zzzdogzzzdogzzz"</tt>, and the replacement string
     * <tt>"cat"</tt>, an invocation of this method on a matcher for that
     * expression would yield the string <tt>"zzzcatzzzdogzzz"</tt>.
     *
     *
     *  Invoking this method changes this matcher's state.  If the matcher
     * is to be used in further matching operations then it should first be
     * reset.
     *
     * @param  replacement
     * The replacement string
     * @return  The string constructed by replacing the first matching
     * subsequence by the replacement string, substituting captured
     * subsequences as needed
     */
    fun replaceFirst(replacement: String?): String {
        if (replacement == null) throw NullPointerException("replacement")
        reset()
        if (!find()) return text.toString()
        val sb = StringBuffer()
        appendReplacement(sb, replacement)
        appendTail(sb)
        return sb.toString()
    }

    /**
     * Sets the limits of this matcher's region. The region is the part of the
     * input sequence that will be searched to find a match. Invoking this
     * method resets the matcher, and then sets the region to start at the
     * index specified by the `start` parameter and end at the
     * index specified by the `end` parameter.
     *
     *
     * Depending on the transparency and anchoring being used (see
     * [useTransparentBounds][.useTransparentBounds] and
     * [useAnchoringBounds][.useAnchoringBounds]), certain constructs such
     * as anchors may behave differently at or around the boundaries of the
     * region.
     *
     * @param  start
     * The index to start searching at (inclusive)
     * @param  end
     * The index to end searching at (exclusive)
     * @throws  IndexOutOfBoundsException
     * If start or end is less than zero, if
     * start is greater than the length of the input sequence, if
     * end is greater than the length of the input sequence, or if
     * start is greater than end.
     * @return  this matcher
     * @since 1.5
     */
    fun region(start: Int, end: Int): Matcher {
        if (start < 0 || start > getTextLength()) throw IndexOutOfBoundsException("start")
        if (end < 0 || end > getTextLength()) throw IndexOutOfBoundsException("end")
        if (start > end) throw IndexOutOfBoundsException("start > end")
        reset()
        from = start
        to = end
        return this
    }

    /**
     * Reports the start index of this matcher's region. The
     * searches this matcher conducts are limited to finding matches
     * within [regionStart][.regionStart] (inclusive) and
     * [regionEnd][.regionEnd] (exclusive).
     *
     * @return  The starting point of this matcher's region
     * @since 1.5
     */
    fun regionStart(): Int {
        return from
    }

    /**
     * Reports the end index (exclusive) of this matcher's region.
     * The searches this matcher conducts are limited to finding matches
     * within [regionStart][.regionStart] (inclusive) and
     * [regionEnd][.regionEnd] (exclusive).
     *
     * @return  the ending point of this matcher's region
     * @since 1.5
     */
    fun regionEnd(): Int {
        return to
    }

    /**
     * Queries the transparency of region bounds for this matcher.
     *
     *
     *  This method returns <tt>true</tt> if this matcher uses
     * *transparent* bounds, <tt>false</tt> if it uses *opaque*
     * bounds.
     *
     *
     *  See [useTransparentBounds][.useTransparentBounds] for a
     * description of transparent and opaque bounds.
     *
     *
     *  By default, a matcher uses opaque region boundaries.
     *
     * @return <tt>true</tt> iff this matcher is using transparent bounds,
     * <tt>false</tt> otherwise.
     * @see java.util.regex.Matcher.useTransparentBounds
     * @since 1.5
     */
    fun hasTransparentBounds(): Boolean {
        return transparentBounds
    }

    /**
     * Sets the transparency of region bounds for this matcher.
     *
     *
     *  Invoking this method with an argument of <tt>true</tt> will set this
     * matcher to use *transparent* bounds. If the boolean
     * argument is <tt>false</tt>, then *opaque* bounds will be used.
     *
     *
     *  Using transparent bounds, the boundaries of this
     * matcher's region are transparent to lookahead, lookbehind,
     * and boundary matching constructs. Those constructs can see beyond the
     * boundaries of the region to see if a match is appropriate.
     *
     *
     *  Using opaque bounds, the boundaries of this matcher's
     * region are opaque to lookahead, lookbehind, and boundary matching
     * constructs that may try to see beyond them. Those constructs cannot
     * look past the boundaries so they will fail to match anything outside
     * of the region.
     *
     *
     *  By default, a matcher uses opaque bounds.
     *
     * @param  b a boolean indicating whether to use opaque or transparent
     * regions
     * @return this matcher
     * @see java.util.regex.Matcher.hasTransparentBounds
     *
     * @since 1.5
     */
    fun useTransparentBounds(b: Boolean): Matcher {
        transparentBounds = b
        return this
    }

    /**
     * Queries the anchoring of region bounds for this matcher.
     *
     *
     *  This method returns <tt>true</tt> if this matcher uses
     * *anchoring* bounds, <tt>false</tt> otherwise.
     *
     *
     *  See [useAnchoringBounds][.useAnchoringBounds] for a
     * description of anchoring bounds.
     *
     *
     *  By default, a matcher uses anchoring region boundaries.
     *
     * @return <tt>true</tt> iff this matcher is using anchoring bounds,
     * <tt>false</tt> otherwise.
     * @see java.util.regex.Matcher.useAnchoringBounds
     * @since 1.5
     */
    fun hasAnchoringBounds(): Boolean {
        return anchoringBounds
    }

    /**
     * Sets the anchoring of region bounds for this matcher.
     *
     *
     *  Invoking this method with an argument of <tt>true</tt> will set this
     * matcher to use *anchoring* bounds. If the boolean
     * argument is <tt>false</tt>, then *non-anchoring* bounds will be
     * used.
     *
     *
     *  Using anchoring bounds, the boundaries of this
     * matcher's region match anchors such as ^ and $.
     *
     *
     *  Without anchoring bounds, the boundaries of this
     * matcher's region will not match anchors such as ^ and $.
     *
     *
     *  By default, a matcher uses anchoring region boundaries.
     *
     * @param  b a boolean indicating whether or not to use anchoring bounds.
     * @return this matcher
     * @see java.util.regex.Matcher.hasAnchoringBounds
     *
     * @since 1.5
     */
    fun useAnchoringBounds(b: Boolean): Matcher {
        anchoringBounds = b
        return this
    }

    /**
     *
     * Returns the string representation of this matcher. The
     * string representation of a `Matcher` contains information
     * that may be useful for debugging. The exact format is unspecified.
     *
     * @return  The string representation of this matcher
     * @since 1.5
     */
    override fun toString(): String {
        val sb = StringBuilder()
        sb.append("java.util.regex.Matcher")
        sb.append("[pattern=" + pattern())
        sb.append(" region=")
        sb.append(regionStart().toString() + "," + regionEnd())
        sb.append(" lastmatch=")
        if (first >= 0 && group() != null) {
            sb.append(group())
        }
        sb.append("]")
        return sb.toString()
    }

    /**
     *
     * Returns true if the end of input was hit by the search engine in
     * the last match operation performed by this matcher.
     *
     *
     * When this method returns true, then it is possible that more input
     * would have changed the result of the last search.
     *
     * @return  true iff the end of input was hit in the last match; false
     * otherwise
     * @since 1.5
     */
    fun hitEnd(): Boolean {
        return hitEnd
    }

    /**
     *
     * Returns true if more input could change a positive match into a
     * negative one.
     *
     *
     * If this method returns true, and a match was found, then more
     * input could cause the match to be lost. If this method returns false
     * and a match was found, then more input might change the match but the
     * match won't be lost. If a match was not found, then requireEnd has no
     * meaning.
     *
     * @return  true iff more input could change a positive match into a
     * negative one.
     * @since 1.5
     */
    fun requireEnd(): Boolean {
        return requireEnd
    }

    /**
     * Initiates a search to find a Pattern within the given bounds.
     * The groups are filled with default values and the match of the root
     * of the state machine is called. The state machine will hold the state
     * of the match as it proceeds in this matcher.
     *
     * Matcher.from is not set here, because it is the "hard" boundary
     * of the start of the search which anchors will set to. The from param
     * is the "soft" boundary of the start of the search, meaning that the
     * regex tries to match at that index but ^ won't match there. Subsequent
     * calls to the search methods start at a new "soft" boundary which is
     * the end of the previous match.
     */
    fun search(from: Int): Boolean {
        var from = from
        hitEnd = false
        requireEnd = false
        from = if (from < 0) 0 else from
        first = from
        oldLast = if (oldLast < 0) from else oldLast
        for (i in groups.indices) groups[i] = -1
        acceptMode = NOANCHOR
        val result = parentPattern!!.root!!.match(this, from, text)
        if (!result) first = -1
        oldLast = last
        return result
    }

    /**
     * Initiates a search for an anchored match to a Pattern within the given
     * bounds. The groups are filled with default values and the match of the
     * root of the state machine is called. The state machine will hold the
     * state of the match as it proceeds in this matcher.
     */
    fun match(from: Int, anchor: Int): Boolean {
        var from = from
        hitEnd = false
        requireEnd = false
        from = if (from < 0) 0 else from
        first = from
        oldLast = if (oldLast < 0) from else oldLast
        for (i in groups.indices) groups[i] = -1
        acceptMode = anchor
        val result = parentPattern!!.matchRoot!!.match(this, from, text)
        if (!result) first = -1
        oldLast = last
        return result
    }

    /**
     * Returns the end index of the text.
     *
     * @return the index after the last character in the text
     */
    fun getTextLength(): Int {
        return text!!.length
    }

    /**
     * Generates a String from this Matcher's input in the specified range.
     *
     * @param  beginIndex   the beginning index, inclusive
     * @param  endIndex     the ending index, exclusive
     * @return A String generated from this Matcher's input
     */
    fun getSubSequence(beginIndex: Int, endIndex: Int): CharSequence {
        return text!!.subSequence(beginIndex, endIndex)
    }

    /**
     * Returns this Matcher's input character at index i.
     *
     * @return A char from the specified index
     */
    fun charAt(i: Int): Char {
        return text!![i]
    }

    companion object {
        /**
         * Matcher state used by the last node. NOANCHOR is used when a
         * match does not have to consume all of the input. ENDANCHOR is
         * the mode used for matching all the input.
         */
        const val ENDANCHOR = 1
        const val NOANCHOR = 0

        /**
         * Returns a literal replacement `String` for the specified
         * `String`.
         *
         * This method produces a `String` that will work
         * as a literal replacement `s` in the
         * `appendReplacement` method of the [Matcher] class.
         * The `String` produced will match the sequence of characters
         * in `s` treated as a literal sequence. Slashes ('\') and
         * dollar signs ('$') will be given no special meaning.
         *
         * @param  s The string to be literalized
         * @return  A literal string replacement
         * @since 1.5
         */
        fun quoteReplacement(s: String): String {
            if (s.indexOf('\\') == -1 && s.indexOf('$') == -1) return s
            val sb = StringBuilder()
            for (i in 0 until s.length) {
                val c = s[i]
                if (c == '\\' || c == '$') {
                    sb.append('\\')
                }
                sb.append(c)
            }
            return sb.toString()
        }
    }
}
