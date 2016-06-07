package cz.dynawest.jtexy.perf;

import cz.dynawest.jtexy.Protector;
import cz.dynawest.jtexy.ProtectorArray;
import junit.framework.TestCase;

/**
 *
 * @author Martin Večeřa
 */
public class ProtectorPerformanceTest extends TestCase {

	final protected static String STRING_COMPONENT = "Sed ut perspiciatis unde omnis iste natus " +
					"error sit voluptatem accusantium doloremque laudantium, totam rem aperiam, eaque ipsa " +
					"quae ab illo inventore veritatis et quasi architecto beatae vitae dicta sunt explicabo. " +
					"Nemo enim ipsam voluptatem quia voluptas sit aspernatur aut odit aut fugit, sed quia " +
					"consequuntur magni dolores eos qui ratione voluptatem sequi nesciunt. Neque porro " +
					"quisquam est, qui dolorem ipsum quia dolor sit amet, consectetur, adipisci velit, sed " +
					"quia non numquam eius modi tempora incidunt ut labore et dolore magnam aliquam quaerat " +
					"voluptatem. Ut enim ad minima veniam, quis nostrum exercitationem ullam corporis suscipit " +
					"laboriosam, nisi ut aliquid ex ea commodi consequatur? Quis autem vel eum iure " +
					"reprehenderit qui in ea voluptate velit esse quam nihil molestiae consequatur, vel " +
					"illum qui dolorem eum fugiat quo voluptas nulla pariatur? At vero eos et accusamus " +
					"et iusto odio dignissimos ducimus qui blanditiis praesentium voluptatum deleniti atque " +
					"corrupti quos dolores et quas molestias sint.";
	final private String TEST_STRING;
	final private static int STRING_SIZE = 5; // in kB
	final private static int ITERATIONS = 10000;
	private long start;

	private void startTimer() {
		start = System.currentTimeMillis();
	}

	private double stopTimer(String testMethod) {
		long stop = System.currentTimeMillis();
		double callsPerSec = ITERATIONS * 1000 / (stop - start);
		System.out.println("Speed " + testMethod + " = " + callsPerSec + "calls/sec");

		return callsPerSec;
	}

	public ProtectorPerformanceTest(String testName) {
		super(testName);

		String tmpStr = "";
		for (int i = 0; i < STRING_SIZE; i++) {
			tmpStr += STRING_COMPONENT;
		}
		TEST_STRING = tmpStr;
	}

	@Override
	protected void setUp() throws Exception {
		super.setUp();
	}

	@Override
	protected void tearDown() throws Exception {
		super.tearDown();
	}

	private void doIteration(String text, Protector protector) {
	}


	// Throws java.lang.ArithmeticException: / by zero
	// --> disabling.

	public void XtestArrayProtectorPerformance() {
		startTimer();

		Protector p = new ProtectorArray();
		for (int i = 0; i < ITERATIONS; i++) {
			doIteration(TEST_STRING, p);
		}

		stopTimer("ArrayProtector");
	}

}
