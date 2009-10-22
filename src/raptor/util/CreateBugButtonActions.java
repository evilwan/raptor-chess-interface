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
				scriptedAction.setName(name);
				scriptedAction.setDescription("Sends the command: ptell "
						+ sound);
				scriptedAction.setScript("context.sendHidden(\"ptell " + sound
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
