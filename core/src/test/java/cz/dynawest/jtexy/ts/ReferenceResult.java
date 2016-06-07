package cz.dynawest.jtexy.ts;

import cz.dynawest.jtexy.TexyException;
import java.io.File;
import java.io.IOException;
import org.apache.commons.io.FileUtils;

/**
 *  Class which reads the PHP Texy's TCK reference .html file
 *  and extracts it's parts into instance's fields.
 */
public class ReferenceResult
{
    public final String html;
    public final String toText;
    public final String source;

    public ReferenceResult(String html, String toText, String source) {
        this.html = html;
        this.toText = toText;
        this.source = source;
    }

    public static ReferenceResult fromFilePath( File refFile ) throws IOException, TexyException {
        String content = FileUtils.readFileToString( refFile );
        String[] parts = content.split("\\n\\n################*\\n\\n");
        if( parts.length != 3 ) {
            throw new TexyException("Reference result file should have 3 "
                    + "parts separated by hashes, see test.php.\n\t"
                    + refFile.getAbsolutePath());
        }
        return new ReferenceResult( parts[0], parts[1], parts[2] );
    }
    
}// class
