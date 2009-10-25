/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2009, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.chat;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

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

	public static final Comparator<Seek> TYPE_DESCRIPTION_DESCENDING_COMPARATOR = new Comparator<Seek>() {
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
			if (seek1.getType() == GameType.untimed
					&& seek2.getType() == GameType.untimed) {
				return 0;
			} else if (seek1.getType() == GameType.untimed
					&& seek2.getType() != GameType.untimed) {
				return 1;
			} else if (seek1.getType() != GameType.untimed
					&& seek2.getType() == GameType.untimed) {
				return -1;
			} else if (seek1.getMinutes() < seek2.getMinutes()) {
				return -1;
			} else if (seek1.getMinutes() > seek2.getMinutes()) {
				return 1;
			} else {// seek1.getMinutes() == seek2.getMinutes()
				if (seek1.getIncrement() > seek2.getIncrement()) {
					return 1;
				} else if (seek1.getIncrement() < seek2.getIncrement()) {
					return -1;
				} else {
					return 0;
				}
			}
		}
	};

	public static final Comparator<Seek> TIME_CONTROL_DESCENDING_COMPARATOR = new Comparator<Seek>() {

		public int compare(Seek seek1, Seek seek2) {
			return -1 * TIME_CONTROL_ASCENDING_COMPARATOR.compare(seek1, seek2);
		}
	};

	public static final Comparator<Seek> AD_ASCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			try {
				int ad1 = Integer.parseInt(seek1.getAd());
				int ad2 = Integer.parseInt(seek2.getAd());
				return ad1 > ad2 ? 1 : ad1 < ad2 ? -1 : 0;
			} catch (NumberFormatException nfe) {
				return seek1.getAd().compareTo(seek2.getAd());
			}
		}
	};

	public static final Comparator<Seek> AD_DESCENDING_COMPARATOR = new Comparator<Seek>() {
		public int compare(Seek seek1, Seek seek2) {
			return -1 * AD_ASCENDING_COMPARATOR.compare(seek1, seek2);
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
		untimed, standard, blitz, lightning, wild, crazyhouse, suicide, fischerRandom, losers, atomic
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
			result = Integer.parseInt(StringUtils
					.replaceChars(rating, "PE", ""));
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
