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
package raptor.bughouse;

import java.util.Comparator;

import org.apache.commons.lang.StringUtils;

public class Bugger {
	public static Comparator<Bugger> BY_STATUS_ASCENDING = new Comparator<Bugger>() {
		public int compare(Bugger bugger1, Bugger bugger2) {
			return bugger1.getStatus().toString().compareTo(
					bugger2.getStatus().toString());
		}
	};

	public static Comparator<Bugger> BY_STATUS_DESCENDING = new Comparator<Bugger>() {
		public int compare(Bugger bugger1, Bugger bugger2) {
			return -1
					* bugger1.getStatus().toString().compareTo(
							bugger2.getStatus().toString());
		}
	};

	public static Comparator<Bugger> BY_NAME_ASCENDING = new Comparator<Bugger>() {
		public int compare(Bugger bugger1, Bugger bugger2) {
			return bugger1.getName().compareTo(bugger2.getName());
		}
	};

	public static Comparator<Bugger> BY_NAME_DESCENDING = new Comparator<Bugger>() {
		public int compare(Bugger bugger1, Bugger bugger2) {
			return -1 * bugger1.getName().compareTo(bugger2.getName());
		}
	};

	public static Comparator<Bugger> BY_RATING_ASCENDING = new Comparator<Bugger>() {
		public int compare(Bugger bugger1, Bugger bugger2) {
			int rating1 = bugger1.getRatingAsInt();
			int rating2 = bugger2.getRatingAsInt();
			return rating1 < rating2 ? -1 : rating1 == rating2 ? 0 : 1;
		}
	};

	public static Comparator<Bugger> BY_RATING_DESCENDING = new Comparator<Bugger>() {
		public int compare(Bugger bugger1, Bugger bugger2) {
			int rating1 = bugger1.getRatingAsInt();
			int rating2 = bugger2.getRatingAsInt();
			return rating1 < rating2 ? 1 : rating1 == rating2 ? 0 : -1;
		}
	};

	public static enum BuggerStatus {
		Available, Idle, Closed, Playing, Simul, Examining, InTourney
	}

	private String rating;
	private BuggerStatus status;
	private String name;

	public Bugger() {

	}

	/**
	 * @return The bugger's name.
	 */
	public String getName() {
		return name;
	}

	/**
	 * @return The bugger's rating.
	 */
	public String getRating() {
		return rating;
	}

	public int getRatingAsInt() {
		int result = 0;
		try {
			result = Integer.parseInt(StringUtils.replaceChars(getRating(),
					"EP", ""));
		} catch (NumberFormatException nfe) {
		}
		return result;
	}

	/**
	 * @return The bugger's availability status.
	 */
	public BuggerStatus getStatus() {
		return status;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setRating(String rating) {
		this.rating = rating;
	}

	public void setStatus(BuggerStatus status) {
		this.status = status;
	}

	/**
	 * @return <code>getUsername();</code>
	 */
	@Override
	public String toString() {
		return getName() + "(" + rating + ")";
	}

}