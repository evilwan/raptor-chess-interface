/* Install.java - Install Raptor resources in user home directory.
 *"
 * Copyright (c) 2015 Eddy Vanlerberghe.  All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 * 3. The name of Eddy Vanlerberghe shall not be used to endorse or promote
 *    products derived from this software without specific prior written
 *    permission.
 *
 * THIS SOFTWARE IS PROVIDED BY EDDY VANLERBERGHE ''AS IS'' AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL EDDY VANLERBERGHE BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 * OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT
 * LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY
 * OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
 * SUCH DAMAGE.
 * 
 *****************************************************************************/
package raptor;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


/**
 * Install Raptor resources in user home directory.
 * <p>
 * The main purpose of this application is to split the Raptor
 * resource files from the program distribution. The set of resources
 * is expected to be rather static, even across different releases.
 * <p>
 * Therefore, by using a separate installer for the resources, the
 * program releases will become much smaller.
 * <p>
 * Note that the actual program will still have a mechanism to overwrite
 * resources with a newer version or to add new resources to the set. That
 * means that it will not be necessary to install a new set of resources for
 * every new program release.
 * <p>
 * The set of resources is platform independent, meaning that we only need
 * one version of the resource installer for all supported platforms.
 */
public class Install {
    // {{ Class members }}

    /**
     * Minium required version for Raptor, so keep same requirement here.
     */
    private final static String MINIMUM_JAVA_VERSION = "1.6";
    /**
     * Everything below this directory in the root dir of the enclosing jar will be
     * recursively extracted during program startup and be copied under the Raptor
     * configuration directory for the current user.
     */
    private final static String SRC_RESOURCE_DIR = "resources/";
    /**
     * Configuration directory under user home directory.
     */
    public static final String APP_HOME_DIR = ".raptor/";
    public static final String USER_RAPTOR_DIR = new File(System.getProperty("user.home") +
							  "/" + APP_HOME_DIR).getAbsolutePath();

    // {{ Instance members }}
    
    // {{ Class methods }}
    
    /**
     * Helper method for printing messages.
     * <p>
     * The printed message is written to <code>System.out</code> and starts with the current
     * class name, followed by two dashes, followed by the specified text.
     * @param s contains the <code>String</code> value to print.
     */
    private static void say(String s) {
	System.out.println("Install -- " + s);
    }
    /**
     * Print message and stop application: cannot rely on standard
     * logging and error processing code because the Java version is
     * too old so those facilities themselves might be using features that are
     * not supported by the Java version that was detected.
     */
    private static void croak(String s) {
	System.err.println(s);
	System.exit(1);
    }
    /**
     * Entrypoint for application.
     * <p>
     * Does not use arguments.
     */
    public static void main(String[] args) {
	//
	// Reality check: we really need at least version MINIMUM_JAVA_VERSION
	//
	if(System.getProperty("java.version").compareTo(MINIMUM_JAVA_VERSION) < 0) {
	    croak("Sorry, you are using Java version " + System.getProperty("java.version") +
		  " but Raptor needs version " + MINIMUM_JAVA_VERSION + " or later.");
	}
	try {
	    installFiles();
	} catch (Throwable t) {
	    say("main() -- caught: " + t);
	    t.printStackTrace();
	}
    }

    /**
     * This function will recursively extract files or directories from "/resources" in the application jar to
     * the corresponding location under the user configuration directory.
     */
    public static void installFiles() throws Exception {
	//say("installFiles() -- dest=\"" + dest + "\"");
	//
	// First attempt to create destination directory (if not existing yet)
	//
	File df = new File(USER_RAPTOR_DIR);
	if(!df.exists()) {
	    if (!df.mkdirs()) {
		throw new IOException("installFiles: Could not create directory: "
				      + USER_RAPTOR_DIR + ".");
	    }
	}
	//
	// Get contents of application jar from the current class was loaded.
	//
	URLClassLoader cl = (URLClassLoader) Install.class.getClassLoader();
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
		    File of = new File(USER_RAPTOR_DIR, en.substring(SRC_RESOURCE_DIR.length()));
		    if(!of.getParentFile().exists()) {
			//say("installFiles() -- creating \"" + of.getParentFile().getAbsolutePath() + "\" ...");
			of.getParentFile().mkdirs();
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
		}
	    }
	}
    }

    // {{ Constructors }}
        
    // {{ Instance methods }}
    
}
