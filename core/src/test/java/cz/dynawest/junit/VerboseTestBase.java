package cz.dynawest.junit;

import cz.dynawest.logging.LoggingUtils;
import java.util.logging.Logger;
import junit.framework.TestCase;

/**
 *
 * @author Ondrej Zizka
 */
public class VerboseTestBase extends TestCase {

    private Logger log = Logger.getLogger(this.getClass().getName());

    static {
        LoggingUtils.initLogging();
    }

    public VerboseTestBase() { }
    public VerboseTestBase(String testName) {
        super(testName);
    }

    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        log.info("\n============================================================\n"
                + "  Setting up:  " + this.getName() + "\n"
                + "============================================================\n");
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        log.info("\n=======  Tearing down:  " + this.getName() + "  ============\n");
    }
}// class TtdTestCase
