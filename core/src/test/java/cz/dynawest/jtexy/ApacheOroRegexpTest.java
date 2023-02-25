
package cz.dynawest.jtexy;

import junit.framework.TestCase;
import org.apache.oro.text.regex.*;


/**
 *
 * @author Ondrej Zizka
 */
public class ApacheOroRegexpTest extends TestCase {

  public void testApacheOroPatternMatcher() {


    // Input
    String regex = "<[^>]+>(.*)</[^>]+>|U";
    String text = "<b>example: </b><div align=left>this is a test</div>";


    int groups;
    PatternMatcher matcher;
    PatternCompiler compiler;
    Pattern pattern;
    PatternMatcherInput input;
    MatchResult result;

    compiler = new Perl5Compiler();
    matcher = new Perl5Matcher();


    try {
      pattern = compiler.compile( regex );
    }
    catch( MalformedPatternException e ) {
      System.out.println( "Bad pattern." );
      System.out.println( e.getMessage() );
      return;
    }

    input = new PatternMatcherInput( text );

    while( matcher.contains( input, pattern ) ) {
      result = matcher.getMatch();
      // Perform whatever processing on the result you want.
      // Here we just print out all its elements to show how its
      // methods are used.

      System.out.println( "Match: " + result.toString() );
      System.out.println( "Length: " + result.length() );
      groups = result.groups();
      System.out.println( "Groups: " + groups );
      System.out.println( "Begin offset: " + result.beginOffset( 0 ) );
      System.out.println( "End offset: " + result.endOffset( 0 ) );
      System.out.println( "Saved Groups: " );

      // Start at 1 because we just printed out group 0
      for( int group = 1; group < groups; group++ ) {
        System.out.println( group + ": " + result.group( group ) );
        System.out.println( "Begin: " + result.begin( group ) );
        System.out.println( "End: " + result.end( group ) );
      }
    }

  }



}
