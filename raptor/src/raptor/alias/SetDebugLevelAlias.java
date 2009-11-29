package raptor.alias;

import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;

import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class SetDebugLevelAlias extends RaptorAlias {

	public SetDebugLevelAlias() {
		super(
				"debuglevel",
				"Sets the current debug level of raptor. This will effect the "
						+ "debug messages being stored in $RAPTOR_HOME_DIR/logs/error.log.",
				"'debuglevel [loggerName] [DEBUG | INFO | WARN | ERROR]'. Example: 'debuglevel rootLogger DEBUG'");
	}

	@SuppressWarnings("deprecation")
	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.startsWith("debuglevel")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			String logLevel = null;
			String loggerName = null;

			if (tok.hasMoreTokens()) {
				loggerName = tok.nextToken();
			} else {
				return new RaptorAliasResult(null, "Invalid syntax: " + command
						+ "\n" + getUsage());
			}

			if (tok.hasMoreTokens()) {
				logLevel = tok.nextToken();
			} else {
				return new RaptorAliasResult(null, "Invalid syntax: " + command
						+ "\n" + getUsage());
			}

			Logger logger = loggerName.equals("rootLogger") ? Logger
					.getRootLogger() : Logger.getLogger(loggerName);

			if (logLevel.equals("DEBUG")) {
				logger.setLevel(Level.DEBUG);
				((FileAppender) Logger.getRootLogger().getAppender("file"))
						.setThreshold(Priority.DEBUG);
			} else if (logLevel.equals("INFO")) {
				logger.setLevel(Level.DEBUG);
				((FileAppender) Logger.getRootLogger().getAppender("file"))
						.setThreshold(Priority.INFO);
			} else if (logLevel.equals("WARN")) {
				logger.setLevel(Level.DEBUG);
				((FileAppender) Logger.getRootLogger().getAppender("file"))
						.setThreshold(Priority.WARN);
			} else if (logLevel.equals("ERROR")) {
				logger.setLevel(Level.DEBUG);
				((FileAppender) Logger.getRootLogger().getAppender("file"))
						.setThreshold(Priority.ERROR);
			} else {
				return new RaptorAliasResult(null, "Invalid syntax: " + command
						+ "\n" + getUsage());
			}

			return new RaptorAliasResult(null, "Log level set to " + logLevel
					+ " for logger " + loggerName);
		}
		return null;
	}
}
