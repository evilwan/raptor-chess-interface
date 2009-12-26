package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.chess.Variant;
import raptor.connector.Connector;
import raptor.service.PlayingStatisticsService;
import raptor.swt.chat.ChatConsoleController;

public class PerformanceRatingAlias extends RaptorAlias {
	public PerformanceRatingAlias() {
		super("performance", "Lists all of your performance ratings.",
				"'performance'. " + "Example: 'performance'");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWith(command, "performance")) {
			StringBuilder text = new StringBuilder(400);

			Connector[] connectorsWithStats = PlayingStatisticsService
					.getInstance().getConnetorsWithStats();

			for (Connector connector : connectorsWithStats) {
				text.append("\nPerformance at " + connector.getShortName()
						+ ":\n");
				for (Variant variant : Variant.values()) {
					int[] performance = PlayingStatisticsService.getInstance()
							.getPreformanceRating(connector, variant);
					if (performance != null) {
						text.append(variant.toString() + " (" + performance[0]
								+ " games): " + performance[1] + "\n");
					}
				}
			}
			return new RaptorAliasResult(
					null,
					text.length() == 0 ? "You have not played any games to obtain performance ratings for."
							: text.toString());
		}
		return null;
	}
}