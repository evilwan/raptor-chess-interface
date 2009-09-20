/**
 *   Decaf/Decaffeinate ICS server interface
 *   Copyright (C) 2008  Carson Day (carsonday@gmail.com)
 *
 *   This program is free software: you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation, either version 3 of the License, or
 *   (at your option) any later version.
 *
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package raptor.connector.fics.parser;

import java.util.HashMap;


public class G1Parser {
//	private int icsId;
//
//	private Style12Parser style12Parser;
//
//	public G1Parser(int icsId) {
//		this.icsId = icsId;
//		style12Parser = new Style12Parser(icsId);
//	}
//
//	/**
//	 * Orphaned case.
//	 */
//	public GameStartEvent parse(String g1) {
//		HashMap<String, String> g1Hash = g1ToHashMap(g1);
//		GameStartEvent result = new GameStartEvent(icsId, Integer
//				.parseInt(g1Hash.get("gn")), g1Hash);
//		return result;
//	}
//
//	/*
//	 * Setting ivariable gameinfo provides the interface with extra
//	 * notifications when the start starts a game or simul or a game is
//	 * observed.
//	 * 
//	 * Example output: - <g1> 1 p=0 t=blitz r=1 u=1,1 it=5,5 i=8,8 pt=0
//	 * rt=1586E,2100 ts=1,0
//	 * 
//	 * (note the - was added so as not to confuse interfaces displaying this
//	 * helpfile)
//	 * 
//	 * This is in the format: - <g1> game_number p=private(1/0) t=type
//	 * r=rated(1/0) u=white_registered(1/0),black_registered(1/0)
//	 * it=initial_white_time,initial_black_time
//	 * i=initial_white_inc,initial_black_inc pt=partner's_game_number(or 0 if
//	 * none) rt=white_rating(+ provshow character),black_rating(+ provshow
//	 * character) ts=white_uses_timeseal(0/1),black_uses_timeseal(0/1)
//	 * 
//	 * Note any new fields will be appended to the end so the interface must be
//	 * able to handle this.
//	 * 
//	 * See Also: iset ivariables
//	 */
//	public GameStartEvent parse(String g1, String style12) {
//		HashMap<String, String> g1Hash = g1ToHashMap(g1);
//		GameStartEvent result = new GameStartEvent(icsId, Integer
//				.parseInt(g1Hash.get("gn")), g1Hash);
//		result.setFirstEvent(style12Parser.parse(style12));
//		return result;
//	}
//
//	public GameStartEvent parse(String g1, String style12, String b1) {
//		try {
//			HashMap<String, String> g1Hash = g1ToHashMap(g1);
//			GameStartEvent result = new GameStartEvent(icsId, Integer
//					.parseInt(g1Hash.get("gn")), g1Hash);
//			result.setFirstEvent(style12Parser.parse(style12, b1));
//			return result;
//		} catch (NumberFormatException nfe) {
//			throw new RuntimeException(nfe);
//		}
//	}
//
//	private HashMap<String, String> g1ToHashMap(String g1) {
//		String[] params = g1.split(" ");
//		HashMap<String, String> result = new HashMap<String, String>();
//		result.put("gn", params[1]);
//
//		for (int i = 2; i < params.length; i++) {
//			String[] param = params[i].split("=");
//			result.put(param[0], param[1]);
//		}
//		return result;
//	}
}
