/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.util;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Comparator;

public class BinarySearchFile {
	final RandomAccessFile file;
	final Comparator<String> test; // tests the element given as search
									// parameter with the line. Insert a
									// PrefixComparator here

	public BinarySearchFile(File f, Comparator<String> test)
			throws FileNotFoundException {
		this.file = new RandomAccessFile(f, "r");
		this.test = test;
	}

	public String search(String element) throws IOException {
		long l = file.length();
		return search(element, -1, l - 1);
	}

	/**
	 * Searches the given element in the range [low,high]. The low value of -1
	 * is a special case to denote the beginning of a file. In contrast to every
	 * other line, a line at the beginning of a file doesn't need a \n directly
	 * before the line
	 */
	private String search(String element, long low, long high)
			throws IOException {
		if (high - low < 1024) {
			// search directly
			long p = low;
			while (p < high) {
				String line = nextLine(p);
				int r = test.compare(line, element);
				if (r > 0) {
					return null;
				} else if (r < 0) {
					p += line.length();
				} else {
					return line;
				}
			}
			return null;
		} else {
			long m = low + ((high - low) / 2);
			String line = nextLine(m);
			int r = test.compare(line, element);
			if (r > 0) {
				return search(element, low, m);
			} else if (r < 0) {
				return search(element, m, high);
			} else {
				return line;
			}
		}
	}

	private String nextLine(long low) throws IOException {
		if (low == -1) { // Beginning of file
			file.seek(0);
		} else {
			file.seek(low);
		}
		int bufferLength = 65 * 1024;
		byte[] buffer = new byte[bufferLength];
		int r = file.read(buffer);
		int lineBeginIndex = -1;

		// search beginning of line
		if (low == -1) { // beginning of file
			lineBeginIndex = 0;
		} else {
			// normal mode
			for (int i = 0; i < 1024; i++) {
				if (buffer[i] == '\n') {
					lineBeginIndex = i + 1;
					break;
				}
			}
		}
		if (lineBeginIndex == -1) {
			// no line begins within next 1024 bytes
			return null;
		}
		int start = lineBeginIndex;
		for (int i = start; i < r; i++) {
			if (buffer[i] == '\n') {
				// Found end of line
				return new String(buffer, lineBeginIndex, i - lineBeginIndex
						+ 1);
				// return line.toString();
			}
		}
		throw new IllegalArgumentException("Line to long");
	}

}
