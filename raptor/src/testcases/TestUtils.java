package testcases;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

public class TestUtils {

	@Test
	public void testStringReplace() {
		String string = "This\nis a test of the emergency broadcast\nsystem";
		String replaced = RaptorStringUtils.replaceAll(string, "\n", " ");
		assertTrue("Message failed: " + replaced, replaced
				.equals("This is a test of the emergency broadcast system"));
	}

	@Test
	public void testStringTokeizer() {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(
				" 1. e4 (1. g4 g5) (1. g4) 1... e6 2. d4 {[%csl Gg1][%cal Gg2g4,Rg1f3]INSERTED TEXT} d5 3.",
				" \t", true);
		while (tok.hasMoreTokens()) {
			System.out.println("'" + tok.nextToken() + "'");
		}
	}
}
