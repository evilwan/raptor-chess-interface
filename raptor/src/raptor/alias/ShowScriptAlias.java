package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.script.RegularExpressionScript;
import raptor.service.ScriptService;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.RaptorStringTokenizer;

public class ShowScriptAlias extends RaptorAlias {
	public ShowScriptAlias() {
		super(
				"showscript",
				"Prints out information about a specified regular expression script. ",
				"'showscript scriptName'. Example: 'showScript myRaptorScript'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		if (command.startsWith("showscript")) {
			RaptorStringTokenizer tok = new RaptorStringTokenizer(command, " ",
					true);
			tok.nextToken();
			String scriptName = tok.nextToken();

			if (StringUtils.isBlank(scriptName)) {
				return new RaptorAliasResult(null, "name is required.\n"
						+ getUsage());
			} else {
				RegularExpressionScript script = ScriptService.getInstance()
						.getRegularExpressionScript(scriptName);

				if (script == null) {
					return new RaptorAliasResult(null, "Script " + scriptName
							+ " not found.");
				} else {
					return new RaptorAliasResult(null, "Script " + scriptName
							+ "\n" + "\tDescription: "
							+ script.getDescription() + "\n\tActive: "
							+ script.isActive() + "\n\tRegular Expressoin:"
							+ script.getRegularExpression() + "\n\tScript:\n"
							+ script.getScript());
				}
			}
		}
		return null;
	}
}
