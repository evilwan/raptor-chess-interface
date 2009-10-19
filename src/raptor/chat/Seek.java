package raptor.chat;

import java.util.Comparator;

/**
 * This code was adapted from code written for Decaf by kozyr.
 */
public class Seek {
	public static final Comparator<Seek> NAME_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getName().compareTo(seek2.getName());
		}
	};

	public static final Comparator<Seek> NAME_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getName().compareTo(seek2.getName()) * -1;
		}
	};

	public static final Comparator<Seek> TYPE_DESCRIPTION_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getTypeDescription().compareTo(
					seek2.getTypeDescription());
		}
	};

	public static final Comparator<Seek> TYPE_DESCRIPTIONE_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getTypeDescription().compareTo(
					seek2.getTypeDescription())
					* -1;
		}
	};

	public static final Comparator<Seek> RATING_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getRatingAsInt() > seek2.getRatingAsInt() ? 1 : seek1
					.getRatingAsInt() == seek2.getRatingAsInt() ? 0 : -1;
		}
	};

	public static final Comparator<Seek> RATING_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getRatingAsInt() > seek2.getRatingAsInt() ? -1 : seek1
					.getRatingAsInt() == seek2.getRatingAsInt() ? 0 : 1;
		}
	};

	public static final Comparator<Seek> TIME_CONTROL_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getTimeControl().compareTo(seek2.getTimeControl());
		}
	};

	public static final Comparator<Seek> TIME_CONTROL_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getTimeControl().compareTo(seek2.getTimeControl())
					* -1;
		}
	};

	public static final Comparator<Seek> AD_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getAd().compareTo(seek2.getAd());
		}
	};

	public static final Comparator<Seek> AD_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getAd().compareTo(seek2.getAd()) * -1;
		}
	};

	public static final Comparator<Seek> RATING_RANGE_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getRatingRange().compareTo(seek2.getRatingRange());
		}
	};

	public static final Comparator<Seek> RATING_RANGE_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getRatingRange().compareTo(seek2.getRatingRange())
					* -1;
		}
	};

	public static final Comparator<Seek> FLAGS_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getRatingRange().compareTo(seek2.getRatingRange());
		}
	};

	public static final Comparator<Seek> FLAGS_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return seek1.getRatingRange().compareTo(seek2.getRatingRange())
					* -1;
		}
	};

	public static enum GameColor {
		white, black
	}

	public static enum GameType {
		untimed,standard, blitz, lightning, wild, crazyhouse, suicide, fischerRandom, losers, atomic
	}

	protected String ad;
	protected String rating;
	protected int minRating;
	protected int maxRating;
	protected String name;
	protected int minutes;
	protected int increment;
	protected boolean isRated;
	protected boolean isManual;
	protected boolean isFormula;
	protected GameType type;
	protected String typeDescription;
	protected GameColor color;

	public Seek() {
	}

	public Seek(String ad, String rating, String name, int minutes,
			int increment, boolean isRated, String typeDescription,
			GameType gameType, GameColor color, int minRating, int maxRating,
			boolean isManual, boolean isFormula) {
		this.ad = ad;
		this.rating = rating;
		this.name = name;
		this.minutes = minutes;
		this.increment = increment;
		this.isRated = isRated;
		this.typeDescription = typeDescription;
		type = gameType;
		this.color = color;
		this.minRating = minRating;
		this.maxRating = maxRating;
		this.isManual = isManual;
		this.isFormula = isFormula;
	}

	public String getAd() {
		return ad;
	}

	public GameColor getColor() {
		return color;
	}

	public String getFlags() {
		String result = "";
		if (isManual) {
			result += "m";
		}
		if (isFormula) {
			result += "f";
		}
		return result;
	}

	public int getIncrement() {
		return increment;
	}

	public int getMaxRating() {
		return maxRating;
	}

	public int getMinRating() {
		return minRating;
	}

	public int getMinutes() {
		return minutes;
	}

	public String getName() {
		return name;
	}

	public String getRating() {
		return rating;
	}

	public int getRatingAsInt() {
		int result = 0;
		try {
			result = Integer.parseInt(rating);
		} catch (NumberFormatException nfe) {
		}
		return result;
	}

	public String getRatingRange() {
		return minRating + "-" + maxRating;
	}

	public String getTimeControl() {
		return getMinutes() + " " + getIncrement() + " "
				+ (isRated ? "r" : "u");
	}

	public GameType getType() {
		return type;
	}

	public String getTypeDescription() {
		return typeDescription;
	}

	public boolean isComputer() {
		return getName().endsWith("(C)");
	}

	public boolean isFormula() {
		return isFormula;
	}

	public boolean isManual() {
		return isManual;
	}

	public boolean isRated() {
		return isRated;
	}

	public void setAd(String ad) {
		this.ad = ad;
	}

	public void setColor(GameColor color) {
		this.color = color;
	}

	public void setFormula(boolean isFormula) {
		this.isFormula = isFormula;
	}

	public void setIncrement(int increment) {
		this.increment = increment;
	}

	public void setManual(boolean isManual) {
		this.isManual = isManual;
	}

	public void setMaxRating(int maxRating) {
		this.maxRating = maxRating;
	}

	public void setMinRating(int minRating) {
		this.minRating = minRating;
	}

	public void setMinutes(int minutes) {
		this.minutes = minutes;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRated(boolean isRated) {
		this.isRated = isRated;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public void setType(GameType type) {
		this.type = type;
	}

	public void setTypeDescription(String typeDescription) {
		this.typeDescription = typeDescription;
	}

	@Override
	public String toString() {
		return ad + " " + minRating + " " + maxRating + " " + name + " "
				+ minutes + " " + increment;
	}
}
