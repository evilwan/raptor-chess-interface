package raptor.alias;

import raptor.swt.chat.ChatConsoleController;

/**
 * <p>
 * Aliases are commands applied to text before its sent to a connector.
 * </p>
 * <p>
 * All aliases must end with Alias and they all must be in this package.
 * Reflection is used to load them all.
 * </p>
 */
public abstract class RaptorAlias implements Comparable<RaptorAlias> {
	protected String name;
	protected String description;
	protected String usage;

	public RaptorAlias(String name, String description, String usage) {
		this.name = name;
		this.description = description;
		this.usage = usage;
	}

	/**
	 * Returns null if the alias was'nt applied, otherwise returns a
	 * RaptorAliasResult.
	 * 
	 * @param controller
	 *            The chat console controller applying the alias.
	 * @param command
	 *            The command to apply the alias on.
	 * @return Null if the alias wan't applied, otherwise the results after
	 *         applying the alias.
	 */
	public abstract RaptorAliasResult apply(ChatConsoleController controller,
			String command);

	/**
	 * Compares by name.
	 */
	public int compareTo(RaptorAlias arg0) {
		return name.compareTo(arg0.getName());
	}

	public String getDescription() {
		return description;
	}

	public String getName() {
		return name;
	}

	public String getUsage() {
		return usage;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setUsage(String usage) {
		this.usage = usage;
	}
}
