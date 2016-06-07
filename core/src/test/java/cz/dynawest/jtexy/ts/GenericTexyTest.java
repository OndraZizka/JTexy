package cz.dynawest.jtexy.ts;

import cz.dynawest.junit.VerboseTestBase;
import java.io.File;
import junit.framework.ComparisonFailure;
import junit.framework.TestResult;

/**
 * Generic test class to run the test against given file.
 * @author Ondrej Zizka
 */
class GenericTexyTest extends VerboseTestBase {

    private TexyTestCaseInfo tcInfo;

    public GenericTexyTest(File srcFile, File outFile, File refFile) {
        this.tcInfo.setSrc(srcFile);
        this.tcInfo.setOut(outFile);
        this.tcInfo.setRef(refFile);
    }

    public GenericTexyTest(TexyTestCaseInfo tcInfo) {
        super(tcInfo.getSrc().getName());
        this.tcInfo = tcInfo;
    }

    @Override
    public int countTestCases() { return 1; }

    @Override
    public void run(TestResult result) {
        try {
            TestSuiteUtils.runTest(this.tcInfo);
            result.endTest(this);
        } catch (ComparisonFailure ex) {
            result.addFailure(this, ex);
        } catch (Exception ex) {
            result.addError(this, ex);
        }
    }
    
}// class
