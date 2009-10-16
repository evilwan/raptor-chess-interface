package raptor.chess;

public enum Result {
	BLACK_WON("0-1"), DRAW("1/2-1/2"), ON_GOING("*"), UNDETERMINED("*"), WHITE_WON(
			"1-0");

	private String description;

	public static Result get(String gameResult) {
		for (Result result : Result.values()) {
			if (result.getDescription().equals(gameResult)) {
				return result;
			}
		}
		return null;
	}

	private Result(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	@Override
	public String toString() {
		return getDescription();
	}

}