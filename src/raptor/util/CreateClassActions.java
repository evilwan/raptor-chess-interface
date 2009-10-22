package raptor.util;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Properties;

import raptor.Raptor;
import raptor.action.RaptorAction;
import raptor.action.RaptorActionFactory;

public class CreateClassActions {
	@SuppressWarnings("unchecked")
	public static void main(String args[]) throws Exception {
		Class[] classes = ReflectionUtils.getClasses("raptor.action.chat");
		for (Class clazz : classes) {
			RaptorAction action = (RaptorAction) clazz.newInstance();
			Properties properties = RaptorActionFactory.save(action);

			File file = new File(Raptor.RESOURCES_DIR + "/actions/"
					+ action.getName() + ".properties");

			if (!file.exists()) {
				properties.store(new FileOutputStream(file),
						"Raptor System Action");
				System.err.println("Created " + file);
			}
		}

		classes = ReflectionUtils.getClasses("raptor.action.game");
		for (Class clazz : classes) {
			RaptorAction action = (RaptorAction) clazz.newInstance();
			Properties properties = RaptorActionFactory.save(action);

			File file = new File(Raptor.RESOURCES_DIR + "/actions/"
					+ action.getName() + ".properties");

			if (!file.exists()) {
				properties.store(new FileOutputStream(file),
						"Raptor System Action");
				System.err.println("Created " + file);
			}
		}
	}
}
