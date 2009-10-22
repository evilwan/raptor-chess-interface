package raptor.action;

import raptor.Raptor;
import raptor.script.ScriptContext;
import bsh.Interpreter;

/**
 * A RaptorAction executed from a script.
 */
public class ScriptedAction extends AbstractRaptorAction {
	protected String script;

	public ScriptedAction() {
	}

	/**
	 * Returns the script.
	 * 
	 * @return
	 */
	public String getScript() {
		return script;
	}

	/**
	 * Executes the script.
	 */
	public void run() {
		ScriptContext context = null;
		if (getChatConsoleControllerSource() != null) {
			context = getChatConsoleControllerSource().getConnector()
					.getScriptContext();
		} else if (context == null && getChessBoardControllerSource() != null
				&& getChessBoardControllerSource().getConnector() != null) {
			context = getChessBoardControllerSource().getConnector()
					.getScriptContext();
		} else if (context == null && getConnectorSource() != null) {
			context = getConnectorSource().getScriptContext();
		}

		if (context == null) {
			Raptor
					.getInstance()
					.onError(
							"Could not executed "
									+ getName()
									+ " because a ScriptContext could not be created from a null connector.");
		} else {
			try {
				Interpreter interpeter = new Interpreter();
				interpeter.set("context", context);
				interpeter.eval(getScript());
			} catch (Throwable t) {
				Raptor.getInstance().onError(
						"Error executing script " + getName(), t);
			}
		}
	}

	/**
	 * Sets the script.
	 * 
	 * @param script
	 */
	public void setScript(String script) {
		this.script = script;
	}
}