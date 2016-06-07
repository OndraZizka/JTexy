
package cz.dynawest.jtexy;

/**
 *
 ** @author Ondrej Zizka
 */
public class InvalidStateException extends TexyException {

  public InvalidStateException() {
  }


  public InvalidStateException( String arg0 ) {
    super( arg0 );
  }


  public InvalidStateException( String arg0, Throwable arg1 ) {
    super( arg0, arg1 );
  }


  public InvalidStateException( Throwable arg0 ) {
    super( arg0 );
  }


}// class InvalidStateException
