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
package raptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;

public class FileUtils {
	/**
	 * This code was obtained from:
	 * http://www.dreamincode.net/code/snippet1443.htm
	 * 
	 * This function will copy files or directories from one location to
	 * another. note that the source and the destination must be mutually
	 * exclusive. This function can not be used to copy a directory to a sub
	 * directory of itself. The function will also have problems if the
	 * destination files already exist.
	 * 
	 * @param src
	 *            -- A File object that represents the source for the copy
	 * @param dest
	 *            -- A File object that represents the destination for the copy.
	 * @throws IOException
	 *             if unable to copy.
	 */
	public static void copyFiles(File src, File dest) throws IOException {
		if (src.getName().startsWith(".")) {
			return;
		}

		// Check to ensure that the source is valid...
		if (!src.exists()) {
			throw new IOException("copyFiles: Can not find source: "
					+ src.getAbsolutePath() + ".");

		} else if (!src.canRead()) { // check to ensure we have rights to the
			// source...
			throw new IOException("copyFiles: No right to source: "
					+ src.getAbsolutePath() + ".");
		}

		// is this a directory copy?

		if (src.isDirectory()) {
			if (!dest.exists()) { // does the destination already exist?
				// if not we need to make it exist if possible (note this is
				// mkdirs not mkdir)

				if (!dest.mkdirs()) {
					throw new IOException(
							"copyFiles: Could not create direcotry: "
									+ dest.getAbsolutePath() + ".");
				}
			}
			// get a listing of files...

			String list[] = src.list();

			// copy all the files in the list.
			for (String element : list) {
				File dest1 = new File(dest, element);
				File src1 = new File(src, element);
				copyFiles(src1, dest1);
			}

		} else {
			// This was not a directory, so lets just copy the file

			FileInputStream fin = null;
			FileOutputStream fout = null;
			byte[] buffer = new byte[4096]; // Buffer 4K at a time (you can
			// change this).
			int bytesRead;

			try {

				// open the files for input and output

				fin = new FileInputStream(src);
				fout = new FileOutputStream(dest);

				// while bytesRead indicates a successful read, lets write...

				while ((bytesRead = fin.read(buffer)) >= 0) {

					fout.write(buffer, 0, bytesRead);
				}
			} catch (IOException e) { // Error copying file...

				IOException wrapper = new IOException(
						"copyFiles: Unable to copy file: " +

						src.getAbsolutePath() + "to" + dest.getAbsolutePath()
								+ ".");

				wrapper.initCause(e);
				wrapper.setStackTrace(e.getStackTrace());
				throw wrapper;

			} finally { // Ensure that the files are closed (if they were open).

				if (fin != null) {
					try {
						fin.close();
					} catch (Throwable t) {
					}
				}

				if (fout != null) {
					try {
						fout.close();
					} catch (Throwable t) {
					}
				}
			}
		}
	}

	/**
	 * Deletes all files and subdirectories under "dir".
	 * 
	 * @param dir
	 *            Directory to be deleted
	 * @return boolean Returns "true" if all deletions were successful. If a
	 *         deletion fails, the method stops attempting to delete and returns
	 *         "false".
	 */
	public static boolean deleteDir(File dir) {

		if (dir.isDirectory()) {
			String[] children = dir.list();
			for (int i = 0; i < children.length; i++) {
				deleteDir(new File(dir, children[i]));
			}
		}

		// The directory is now empty so now it can be smoked
		return dir.delete();
	}

	/**
	 * Returns the contents of the specified file as a string.
	 * 
	 * @param fileName
	 *            The fully qualified file name.
	 * @return The contents of the file as a string. Returns null if there was
	 *         an error reading the file.
	 */
	public static String fileAsString(String fileName) {
		File f = null;
		BufferedReader reader = null;
		try {
			f = new File(fileName);

			reader = new BufferedReader(new FileReader(f));
			StringBuilder result = new StringBuilder(10000);
			String line = null;
			while ((line = reader.readLine()) != null) {
				result.append(line + "\n");
			}
			return result.toString();
		} catch (IOException e) {
			throw new RuntimeException("Error reading file: "
					+ f.getAbsolutePath(), e);
		} finally {
			try {
				reader.close();
			} catch (Throwable t) {
			}
		}
	}
}
