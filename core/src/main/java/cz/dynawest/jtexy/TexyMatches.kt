package cz.dynawest.jtexy

import cz.dynawest.jtexy.modules.PatternHandler

/**
 * @see PatternHandler
 *
 * @author Ondrej Zizka
 */
@Deprecated(" Not useful:  Originally coined up for pharse module's matches,\n" + "  but later I decided not to create a class for each pattern.\n" + "  Use List<MatchWithOffset> groups.\n" + " \n" + "  ")
class TexyMatches(//list(, $mParam, $mMod, $mContent) = $matches;
    //    [1] => code | text | ...
    //    [2] => ... additional parameters
    //    [3] => .(title)[class]{style}<>
    //    [4] => ... content
    val code: String, val params: String, val modifiers: String, val content: String
)
