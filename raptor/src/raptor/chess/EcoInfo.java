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
package raptor.chess;

/**
 * @author John Nahlen (johnthegreat)
 */
public class EcoInfo {

	private String ecoCode;
	private String openingName;
	private String positionOnlyFen;

	/**
	 * 
	 * @param positionOnlyFen
	 *            The fen not containing the move count or half moves since
	 *            unchangeable move.
	 *            rnb1k2r/ppppnpbp/8/6p1/2BPPp1q/2N3P1/PPP4P/R1BQ1KNR b kq -
	 * @param eco
	 *            The eco code.
	 * @param opening
	 *            The opening name.
	 */
	public EcoInfo(String positionOnlyFen, String eco, String opening) {
		this.positionOnlyFen = positionOnlyFen;
		ecoCode = eco.toUpperCase();
		openingName = opening;
	}

	/**
	 * @return The ECO code.
	 */
	public String getEcoCode() {
		return ecoCode;
	}

	/**
	 * @return The name of the opening.
	 */
	public String getOpening() {
		return openingName;
	}

	/**
	 * @return The move sequence required to get to this ECO code.
	 */
	public String getPositionOnlyFen() {
		return positionOnlyFen;
	}

	/**
	 * @return <code>getOpening() + " : " + getVariation()</code>
	 */
	@Override
	public String toString() {
		return getEcoCode() + " " + getOpening();
	}
}
