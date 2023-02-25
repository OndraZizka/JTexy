
package cz.dynawest.jtexy;

import java.util.List;

/**
 *
 ** @author Ondrej Zizka
 */
public class TexyException extends Exception {

  public TexyException( Throwable arg0 ) {
    super( arg0 );
  }


  public TexyException( String arg0, Throwable arg1 ) {
    super( arg0, arg1 );
  }


  public TexyException( String arg0 ) {
    super( arg0 );
  }


  public TexyException() {
  }



	public static void throwIfErrors(String string, List<TexyException> exceptions) throws TexyException {
		if( exceptions.size() > 0 )
			throw create(string, exceptions);
	}


	static TexyException create(String string, List<TexyException> exceptions) {
		StringBuilder sb = new StringBuilder( string );
		for (Exception ex : exceptions) {
			sb.append("\n  ").append(ex.getMessage());
		}
		return new TexyException(sb.toString());
	}

  
  
}
