/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
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
