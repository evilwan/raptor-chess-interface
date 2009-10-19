package raptor.chat;

/**
 * This code was adapted from some code johnthegreat for Raptor.
 */
public class BugGame {
	public boolean isRated;
	public String timeControl;
	public String game1Id;
	public String game2Id;
	public Bugger game1White;
	public Bugger game1Black;
	public Bugger game2White;
	public Bugger game2Black;

	public Bugger getGame1Black() {
		return game1Black;
	}

	public String getGame1Id() {
		return game1Id;
	}

	public Bugger getGame1White() {
		return game1White;
	}

	public Bugger getGame2Black() {
		return game2Black;
	}

	public String getGame2Id() {
		return game2Id;
	}

	public Bugger getGame2White() {
		return game2White;
	}

	public String getTimeControl() {
		return timeControl;
	}

	public boolean isRated() {
		return isRated;
	}

	public void setGame1Black(Bugger game1Black) {
		this.game1Black = game1Black;
	}

	public void setGame1Id(String game1Id) {
		this.game1Id = game1Id;
	}

	public void setGame1White(Bugger game1White) {
		this.game1White = game1White;
	}

	public void setGame2Black(Bugger game2Black) {
		this.game2Black = game2Black;
	}

	public void setGame2Id(String game2Id) {
		this.game2Id = game2Id;
	}

	public void setGame2White(Bugger game2White) {
		this.game2White = game2White;
	}

	public void setRated(boolean isRated) {
		this.isRated = isRated;
	}

	public void setTimeControl(String timeControl) {
		this.timeControl = timeControl;
	}
}
