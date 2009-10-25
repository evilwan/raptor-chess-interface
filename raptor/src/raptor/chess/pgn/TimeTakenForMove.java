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
package raptor.chess.pgn;

import java.math.BigDecimal;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.util.RaptorStringTokenizer;
import raptor.util.RaptorStringUtils;

/**
 * These annotations take the form [%emt 1.438]
 */
public class TimeTakenForMove implements MoveAnnotation {
	private static final long serialVersionUID = 3398826247420411970L;
	private static final Log LOG = LogFactory.getLog(TimeTakenForMove.class);
	public String text;

	public TimeTakenForMove(long time) {
		// [%emt 1.438]
		setText(PgnUtils.timeToEMTFormat(time));
	}

	public TimeTakenForMove(String text) {
		setText(text);
	}

	public long getMilliseconds() {
		RaptorStringTokenizer tok = new RaptorStringTokenizer(getText(),
				"[%emt ]", true);
		if (tok.hasMoreTokens()) {
			String elapsedTimeInSeconds = tok.nextToken();
			try {
				BigDecimal decimal = new BigDecimal(elapsedTimeInSeconds);
				decimal = decimal.multiply(new BigDecimal(1000));
				return decimal.longValue();
			} catch (NumberFormatException nfe) {
				if (LOG.isWarnEnabled()) {
					LOG.warn("Invalid Remaining Clock Time detected: "
							+ getText());
				}
			}
		} else {
			if (LOG.isWarnEnabled()) {
				LOG.warn("Invalid Remaining Clock Time detected: " + getText());
			}
		}
		return 0;
	}

	public String getText() {
		return text;
	}

	@Override
	public String toString() {
		return text;
	}

	private void setText(String text) {
		this.text = text;
		this.text = RaptorStringUtils.removeAll(this.text, '\r');
		this.text = RaptorStringUtils.removeAll(this.text, '\n');
		this.text = RaptorStringUtils.removeAll(this.text, '`');
		this.text = RaptorStringUtils.removeAll(this.text, '|');
		this.text = text.trim();
	}
}
