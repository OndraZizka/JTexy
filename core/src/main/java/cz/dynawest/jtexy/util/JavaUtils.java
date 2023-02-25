
package cz.dynawest.jtexy.util;

import java.io.File;
import java.io.FilenameFilter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;



/**
 *
 * @author Ondrej Zizka
 */
public class JavaUtils
{

	public static Class[] getClassesForPackage(String packName)	throws ClassNotFoundException
	{
		// Get a File object for the package.
		File dir = null;

		ClassLoader loader = Thread.currentThread().getContextClassLoader();
		if( loader == null )
			throw new ClassNotFoundException("Can't get a class loader.");

		String path = '/' + packName.replace('.', '/');
		URL resource = loader.getResource(path);
		if( resource == null )
			throw new ClassNotFoundException("No resource for path: " + path);

		dir = new File( resource.getFile() );
		if( ! dir.exists() )
			throw new ClassNotFoundException("Unknown package: "+packName);


		ArrayList<Class> classes = new ArrayList<Class>();

		// Get the list of the files contained in the package.
		String[] files = dir.list( classExtFilter );
		for( int i = 0;  i < files.length;  i++ ) {
			String name = files[i];
			// Remove the .class extension.
			String classFQN = packName +'.'+ name.substring(0, name.length() - 6);
			classes.add( Class.forName( classFQN ) );
		}

		return (Class[]) classes.toArray();
	}


	/**
	 *  *.class filter.
	 */
	public static final FilenameFilter classExtFilter = new FilenameFilter() {
		@Override	public boolean accept(File dir, String name) {
			boolean isFile = new File(dir.getPath()+File.separator+name).isFile();
			return isFile && name.endsWith(".class");
		}
	};


	/**
	 * Returns a stack trace from the exception.
	 */
  public static String getStackTrace( Throwable aThrowable ) {
    final Writer result = new StringWriter();
    final PrintWriter printWriter = new PrintWriter(result);
    aThrowable.printStackTrace(printWriter);
    return result.toString();
  }

	
}

