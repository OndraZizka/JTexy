
package cz.dynawest.jtexy.ts;

import java.io.File;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;

/**
 *
 * @author Ondrej Zizka
 */
public class TexyTestCaseInfo {

    public File origSrc;
    
    public File src;
    public File out;
    public File ref;

    public TexyTestCaseInfo(File src, File out, File ref) {
        this.src = src;
        this.out = out;
        this.ref = ref;
    }
    
    private TexyTestCaseInfo(File origSrc, File src, File out, File ref) {
        this.origSrc = origSrc;
        this.src = src;
        this.out = out;
        this.ref = ref;
    }
    
    
    /**
     *  Creates a test case info from a triplet file (taken from Texy tests).
     */
    public static TexyTestCaseInfo fromTripletFile( File triplet, File outputDir )
    {
        if( !outputDir.exists() )  
            throw new RuntimeException("Output dir doesn't exist: \n\t" + outputDir.getAbsolutePath());
        if( !outputDir.isDirectory() )  
            throw new RuntimeException("Output path is not a dir: \n\t" + outputDir.getAbsolutePath());
        
        try {
            ReferenceResult refResult = ReferenceResult.fromFilePath( triplet );

            String fnameBase = StringUtils.substringBeforeLast( triplet.getName(), ".");

            // Write Texy source.
            String fileName = fnameBase + "-src.html";
            File src = new File( outputDir, fileName );
            FileUtils.write( src, refResult.html );

            // Write reference HTML.
            fileName = fnameBase + "-ref.html";
            File ref = new File( outputDir, fileName );
            FileUtils.write( ref, refResult.html );

            // Set output path.
            fileName = fnameBase + "-out.html";
            File out = new File( outputDir, fileName );

            return new TexyTestCaseInfo(triplet, src, out, ref);
        }
        catch ( Exception ex ){
            throw new RuntimeException("Error creating test info from: \n\t"
                    + triplet.getAbsolutePath()
                    + "\n\t" + ex.toString(), ex);
        }
    }

    public File getOrigSrc() { return origSrc; }
    public void setOrigSrc(File origSrc) { this.origSrc = origSrc; }
    public File getSrc() { return src; }
    public void setSrc(File src) { this.src = src; }
    public File getOut() { return out; }
    public void setOut(File out) { this.out = out; }
    public File getRef() { return ref; }
    public void setRef(File ref) { this.ref = ref; }
    
}// class
