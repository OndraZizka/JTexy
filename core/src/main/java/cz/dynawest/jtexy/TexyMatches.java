
package cz.dynawest.jtexy;

/**
 * @deprecated
 * Not useful:  Originally coined up for pharse module's matches,
 * but later I decided not to create a class for each pattern.
 * Use List<MatchWithOffset> groups.
 *
 * @see    PatternHandler
 * @author Ondrej Zizka
 */
public class TexyMatches {

	//list(, $mParam, $mMod, $mContent) = $matches;
	//    [1] => code | text | ...
	//    [2] => ... additional parameters
	//    [3] => .(title)[class]{style}<>
	//    [4] => ... content


	public final String code, params, modifiers, content;

	public TexyMatches(String code, String params, String modifiers, String content) {
		this.code = code;
		this.params = params;
		this.modifiers = modifiers;
		this.content = content;
	}


}
