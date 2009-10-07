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
package raptor.connector.ics.chat;

public class SoughtEventParser {

	// private static final Logger logger = Logger
	// .getLogger(SoughtEventParser.class);
	//
	// public SoughtEventParser(int icsId) {
	// super(icsId);
	// }
	//
	// @Override
	// public IcsNonGameEvent parse(String text) {
	// if (text.endsWith(ADS_DISPLAYED) || text.endsWith(AD_DISPLAYED)) {
	//			
	// //Make sure the first word is an integer this
	// //is to make sure events are not running together.
	// //Since its rare if it happens just return null.
	// int firstSpace = text.indexOf(" ");
	// try
	// {
	// if (firstSpace == -1)
	// {
	// return null;
	// }
	// Integer.parseInt(text.substring(0,firstSpace));
	// }
	// catch (Throwable t)
	// {
	// return null;
	// }
	//			
	// String[] lines = text.split("\n\\s*");
	//
	// List<Seek> seeks = new LinkedList<Seek>();
	//
	// for (int i = 0; i < lines.length - 1; i++) { // we don't care
	// // about last line
	// String line = lines[i];
	// if (logger.isDebugEnabled()) {
	// logger.debug("Sought line: " + line);
	// }
	//
	// String[] parts = line.split("\\s+");
	// // for (String part : parts) {
	// // System.err.println("Part: '" + part + "'");
	// // }
	//
	// int rating = -1;
	//
	// try {
	// rating = Integer.parseInt(parts[1].trim());
	// } catch (NumberFormatException e) {
	//
	// }
	//
	// Seek in = new Seek(Integer.parseInt(parts[0].trim()), rating,
	// parts[2].trim(), Integer.parseInt(parts[3].trim()),
	// Integer.parseInt(parts[4].trim()), "rated"
	// .equals(parts[5].trim()) ? true : false);
	// in.setType(parts[6].trim());
	// seeks.add(in);
	// }
	//
	// return new SoughtEvent(getIcsId(), text, seeks);
	// } else {
	// return null;
	// }
	// }

	// private static final String AD_DISPLAYED = "ad displayed.";
	// private static final String ADS_DISPLAYED = "ads displayed.";

}
