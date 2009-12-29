package raptor.alias;

import java.util.Date;

import org.apache.commons.lang.StringUtils;

import raptor.chat.ChatEvent;
import raptor.service.MemoService;
import raptor.swt.chat.ChatConsoleController;

public class MemosAlias extends RaptorAlias {
	public MemosAlias() {
		super("=memo", "Shows all your memos.", "'=memo'. Example: '=memo'");
		setHidden(false);
	}

	@Override
	public RaptorAliasResult apply(final ChatConsoleController controller,
			String command) {
		command = command.trim();
		if (StringUtils.startsWith(command, "=memo")) {
			ChatEvent[] memos = MemoService.getInstance().getMemos();
			StringBuilder result = new StringBuilder(2000);
			result.append("Memos:\n");
			for (ChatEvent event : memos) {

				result.append(MemoService.FORMAT.format(new Date(event
						.getTime()))
						+ event.getMessage() + "\n");

			}
			return new RaptorAliasResult(null, result.toString());

		}
		return null;
	}
}
