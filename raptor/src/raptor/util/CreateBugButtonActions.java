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
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.util.Properties;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.WordUtils;

import raptor.Raptor;
import raptor.action.RaptorActionFactory;
import raptor.action.ScriptedAction;
import raptor.action.RaptorAction.Category;

public class CreateBugButtonActions {
	public static void main(String args[]) throws Exception {
		File file = new File(Raptor.RESOURCES_DIR + "sounds/bughouse");
		String[] bugSounds = file.list(new FilenameFilter() {
			public boolean accept(File dir, String name) {
				return name.endsWith(".wav");
			}
		});

		for (String sound : bugSounds) {
			sound = StringUtils.remove(sound, ".wav");
			String name = WordUtils.capitalizeFully(sound.toLowerCase());
			File scriptFile = new File(Raptor.RESOURCES_DIR + "/actions/"
					+ name + ".properties");
			if (!scriptFile.exists()) {
				ScriptedAction scriptedAction = new ScriptedAction();
				scriptedAction.setCategory(Category.PartnerTells);
				scriptedAction.setName(name);
				scriptedAction.setDescription("Sends the command: ptell "
						+ sound);
				scriptedAction.setScript("context.send(\"ptell " + sound
						+ "\");");
				Properties properties = RaptorActionFactory
						.save(scriptedAction);
				properties.store(new FileOutputStream(scriptFile),
						"Raptor System Action");
				System.err.println("Created " + scriptFile.getAbsolutePath());
			}
		}
	}
}
