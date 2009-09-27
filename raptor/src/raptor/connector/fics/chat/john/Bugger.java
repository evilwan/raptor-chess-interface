package raptor.connector.fics.chat.john;

/**
 * 
 * @author John Nahlen (johnthegreat)
 * @since Friday, September 25, 2009
 */
public class Bugger {
	private String rating;
	private char status;
	private String username;

	protected Bugger() {

	}

	/**
	 * @return The bugger's rating.
	 */
	public String getRating() {
		return rating;
	}

	/**
	 * @return The bugger's availability status.
	 */
	public char getStatus() {
		return status;
	}

	/**
	 * @return The bugger's username.
	 */
	public String getUsername() {
		return username;
	}

	/**
	 * @return If this bugger is currently (at the time of parsing) available
	 *         for match requests.
	 */
	public boolean isOpenForMatches() {
		return (status == ' ' || status == '.'); // available or idle
	}

	/**
	 * @return If this bugger is currently (at the time of parsing) playing a
	 *         game.
	 */
	public boolean isPlaying() {
		return (status == '^'); // playing
	}

	protected void setRating(String rating) {
		this.rating = rating;
	}

	protected void setStatus(char status) {
		this.status = status;
	}

	protected void setUsername(String username) {
		this.username = username;
	}

	/**
	 * @return <code>getUsername();</code>
	 */
	@Override
	public String toString() {
		return getUsername();
	}

}