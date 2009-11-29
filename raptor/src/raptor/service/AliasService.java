package raptor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.alias.RaptorAlias;
import raptor.alias.RaptorAliasResult;
import raptor.swt.chat.ChatConsoleController;
import raptor.util.ReflectionUtils;

public class AliasService {
	private static final Log LOG = LogFactory.getLog(AliasService.class);

	private static final AliasService singletonInstance = new AliasService();
	List<RaptorAlias> aliases = new ArrayList<RaptorAlias>(20);

	public static AliasService getInstance() {
		return singletonInstance;
	}

	@SuppressWarnings("unchecked")
	private AliasService() {
		try {
			long startTime = System.currentTimeMillis();
			Class[] classes = ReflectionUtils.getClasses("raptor.alias");
			for (Class clazz : classes) {
				if (clazz.getName().endsWith("Alias")
						&& !clazz.getName().endsWith("RaptorAlias")) {
					aliases.add((RaptorAlias) clazz.newInstance());
				}
			}
			Collections.sort(aliases);
			LOG
					.info("AliasService initialized " + aliases.size()
							+ " aliases in "
							+ (System.currentTimeMillis() - startTime));
		} catch (Throwable t) {
			Raptor.getInstance().onError("Error loading AliasService", t);
		}
	}

	public void dispose() {
		aliases.clear();
	}

	public RaptorAlias[] getAliases() {
		return aliases.toArray(new RaptorAlias[0]);
	}

	public String getAliasHtml() {
		StringBuilder builder = new StringBuilder(5000);
		builder.append("<html>\n<body>\n");
		builder.append("<h1>Raptor Aliases</h1>\n");
		builder
				.append("<p>Aliases are special commands you can type into a console. "
						+ "Some aliases are preprocessed by Raptor and then sent along to the server."
						+ "Other aliases are Raptor commands which are processed by Raptor and not sent "
						+ "along to the server.</p>\n");

		builder.append("<UL>\n");
		builder.append("<LH>Aliases:</LH>\n");
		for (RaptorAlias alias : getAliases()) {
			builder.append("<LI><a href=\"#" + alias.getName() + "\">"
					+ alias.getName() + "</a></LI>\n");
		}
		builder.append("</UL>\n");
		builder.append("<br/>\n");

		for (RaptorAlias alias : getAliases()) {
			builder.append("<a name=\"" + alias.getName() + "\"><h2>"
					+ alias.getName() + "</h2></a>\n");
			builder.append("<h3>Description:</h3>\n");
			builder.append("<p>" + alias.getDescription() + "</p>\n");
			builder.append("<h3>Usage:</h3>\n");
			builder.append("<p>" + alias.getUsage() + "</p>\n");
			builder.append("<br/>\n");
			builder.append("<br/>\n");
		}
		builder.append("</body></html>\n");
		return builder.toString();
	}

	/**
	 * If null is returned no alias was applied to the command. If a non-null
	 * value was returned an alias was applied.
	 * 
	 * @param command
	 *            The command.
	 * @return The command after applying aliases, or null if on action should
	 *         be taken.
	 */
	public RaptorAliasResult processAlias(ChatConsoleController controller,
			String command) {
		RaptorAliasResult result = null;
		for (RaptorAlias alias : aliases) {
			result = alias.apply(controller, command);
			if (result != null) {
				break;
			}
		}
		return result;
	}
}
