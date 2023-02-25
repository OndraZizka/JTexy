package cz.dynawest.jtexy.util;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.apache.commons.io.DirectoryWalker;
import org.apache.commons.io.filefilter.AndFileFilter;
import org.apache.commons.io.filefilter.DelegateFileFilter;
import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.io.filefilter.WildcardFileFilter;
import org.apache.commons.lang.StringUtils;

/**
 *  Lists .texy files from a directory.
 *
 *  @author Ondrej Zizka
 */
public class TexyFileLister {

    /**
     *  Lists .texy files from a directory.
     *
     * @param inputDir    Dir to walk through.
     * @param testMask    Either null, or a wildcard mask like "foo.texy" or "*bar*.texy"
     */
    public static List<File> getTexyFiles( File inputDir, String testMask ) {
        return getTexyFiles( inputDir, testMask, true );
    }

    public static List<File> getTexyFiles( File inputDir, String testMask, boolean includeSubdirs ) {

        // Filter
        if( StringUtils.isBlank(testMask) ) testMask = null;

        /*FileFilter filter = (testMask == null)
                ? new SuffixFileFilter(".texy")
                : new WildcardFileFilter(testMask); */
        //new AndFileFilter( Arrays.asList( new WildcardFileFilter(testMask) ) );

        if(testMask == null)
            testMask = "*.texy";
        else if( testMask.endsWith("*") )
            testMask += ".texy";
        FileFilter filter = new WildcardFileFilter(testMask);

		// Get sorted list of .texy files.
        if( ! includeSubdirs ){
            File[] texyFilesA = inputDir.listFiles( filter );
            return sort( Arrays.asList( texyFilesA ) );
        }

        // Get sorted list of .texy files, including subdirectories.
        try {
            return sort( new SimpleDirectoryWalker( filter ).getTexyFiles( inputDir ) );
        } catch( IOException ex ) {
            throw new RuntimeException("Can't read list of files from " + inputDir + ": " + ex.toString(), ex);
        }
    }// getTexyFiles()

    
    private static List sort( List list ) {
        Collections.sort(list);
        return list;
    }
    
}



/**
 * 
 * @author Ondrej Zizka
 */
class SimpleDirectoryWalker extends DirectoryWalker<File> {

    public SimpleDirectoryWalker( FileFilter filter ) {
        //super( new OrFileFilter( DirectoryFileFilter.DIRECTORY, filter ), -1);
        super( null, new DelegateFileFilter( filter ), -1);
    }

    public List getTexyFiles( File startDirectory ) throws IOException {
        List results = new LinkedList();
        walk( startDirectory, results );
        return results;
    }

    @Override
    protected void handleFile( File file, int depth, Collection results ) {
        results.add( file );
    }
}
