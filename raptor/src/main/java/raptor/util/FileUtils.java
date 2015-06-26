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
package raptor.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import raptor.util.RaptorLogger;
 

import raptor.Raptor;

public class FileUtils {
    private static final RaptorLogger LOG = RaptorLogger.getLog(FileUtils.class);
    /**
     * Everything below this directory in the root dir of the Raptor.jar will be
     * recursively extracted during program startup (i.e. the "install" phase)
     */
    private final static String SRC_RESOURCE_DIR = "resources/";
    
    /**
     * Helper method for printing messages.
     * <p>
     * The printed message is written to <code>System.out</code> and starts with the current
     * class name, followed by two dashes, followed by the specified text.
     * @param s contains the <code>String</code> value to print.
     */
    private static void say(String s) {
	System.out.println("FileUtils -- " + s);
    }
    /**
     * This function will recursively extract files or directories from "/resources" in the application jar to
     * specified destination.
     * 
     * @param src
     *            -- A File object that represents the source for the copy
     * @param dest
     *            -- A File object that represents the destination for the copy.
     * @throws IOException
     *             if unable to copy.
     */
    public static void installFiles(String dest) throws IOException {
	//say("installFiles() -- dest=\"" + dest + "\"");
	try {
	    //
	    // First attempt to create destination directory (if not existing yet)
	    //
	    File df = new File(dest);
	    if(!df.exists()) {
		if (!df.mkdirs()) {
		    throw new IOException("installFiles: Could not create directory: "
					  + df.getAbsolutePath() + ".");
		}
		if (LOG.isDebugEnabled()) {
		    LOG.debug("Created directory " + df.getAbsolutePath());
		}
	    }
	    //
	    // Get contents of application jar
	    //
	    URLClassLoader cl = (URLClassLoader) FileUtils.class.getClassLoader();
	    //
	    // Note: getURLs() should return exactly one entry
	    //
	    for(URL u : cl.getURLs()) {
		JarFile jar = new JarFile(new File(u.toURI()));
		for(Enumeration<JarEntry> entries = jar.entries(); entries.hasMoreElements(); ) {
		    JarEntry e = entries.nextElement();
		    if(e.getName().startsWith(SRC_RESOURCE_DIR) && !e.isDirectory()) {
			String en = e.getName();
			//say("installFiles() -- installing e=\"" + en + "\" ...");
			URL url = new URL("jar", "", "file:" + jar.getName() + "!/" + en);
			//
			// Remove "resources/" prefix from name in Jar
			//
			//say("installFiles() -- installing \"" + en.substring(SRC_RESOURCE_DIR.length()) + "\" ...");
			File of = new File(dest, en.substring(SRC_RESOURCE_DIR.length()));
			if(!of.getParentFile().exists()) {
			    //say("installFiles() -- creating \"" + of.getParentFile().getAbsolutePath() + "\" ...");
			    of.getParentFile().mkdirs();
			    if (LOG.isDebugEnabled()) {
				LOG.debug("Created " + of.getParentFile().getAbsolutePath());
			    }
			}
			InputStream ifs = url.openStream();
			FileOutputStream ofs = new FileOutputStream(of);
			byte[] buf = new byte[4096];
			int n = 0;
			while((n = ifs.read(buf)) >= 0) {
			    ofs.write(buf, 0, n);
			}
			ofs.close();
			ifs.close();
			if (LOG.isDebugEnabled()) {
			    LOG.debug("Installed " + en + " to " + of.getAbsolutePath());
			}
		    }
		}
	    }
	} catch(Exception ex) {
	    Raptor.getInstance().onError("Error installing system resources", ex);
	}
    }
    public static void makeEmptyFile(String filnam) throws IOException {
	File f = new File(filnam);
	String dirnam = f.getParent();
	File df = new File(dirnam);
	if(!df.exists()) {
	    if (!df.mkdirs()) {
		throw new IOException("installFiles: Could not create directory: "
				      + df.getAbsolutePath() + ".");
	    }
	    if (LOG.isDebugEnabled()) {
		LOG.debug("Created new directory " + df.getAbsolutePath());
	    }
	}
	f.createNewFile();
	if (LOG.isDebugEnabled()) {
	    LOG.debug("Created new empty file " + f.getAbsolutePath());
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
				boolean success = deleteDir(new File(dir, children[i]));
				if (!success) {
					LOG.error("Error trying to delete file: "
							+ dir.getAbsolutePath());
				}
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
                result.append(line).append("\n");
			}
			return result.toString();
		} catch (IOException e) {
			Raptor.getInstance().onError(
					"Error reading file: " + f.getAbsolutePath(), e);
			return null;
		} finally {
			try {
				reader.close();
			} catch (Throwable t) {
			}
		}
	}
}
