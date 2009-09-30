package raptor.game.util;

import raptor.game.GameConstants;
import raptor.game.util.SanUtil.SanValidations;

public class SanTest extends TestCase implements GameConstants {
	public static void main(String args[]) {
		SanTest test = new SanTest();
		test.testPxp();
		System.out.println("All tests passed.");
	}

	public void testPxp() {
		SanValidations validations = SanUtil.getValidations("dxe4");
		System.err.println("is valie epOrAmbigStrict="
				+ validations.isEpOrAmbigPxStrict());
		asserts(validations.isPawnMove() && validations.isEpOrAmbigPxStrict(),
				"Invalid validations '" + validations.getStrictSan() + "'");
	}
}
