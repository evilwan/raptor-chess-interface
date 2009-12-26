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
package raptor.service;

import java.util.HashMap;
import java.util.Map;

import raptor.connector.Connector;
import raptor.connector.bics.BicsConnector;
import raptor.connector.fics.FicsConnector;

/**
 * A service which manages all of the connectors being used by Raptor.
 */
public class ConnectorService {
	private static final ConnectorService instance = new ConnectorService();

	public static ConnectorService getInstance() {
		return instance;
	}

	Map<String, Connector> shortNameToConnector = new HashMap<String, Connector>();

	private ConnectorService() {
		FicsConnector ficsConnector = new FicsConnector();
		BicsConnector bicsConnector = new BicsConnector();
		shortNameToConnector.put(ficsConnector.getShortName(), ficsConnector);
		shortNameToConnector.put(bicsConnector.getShortName(), bicsConnector);
	}

	/**
	 * Disposes of all the connectors being managed.
	 */
	public void dispose() {
		Connector[] connectors = ConnectorService.getInstance().getConnectors();
		for (Connector connector : connectors) {
			try {
				connector.dispose();
			} catch (Throwable t) {
			}
		}
	}

	/**
	 * Returns a connector given its short name.
	 */
	public Connector getConnector(String shortName) {
		return shortNameToConnector.get(shortName);
	}

	/**
	 * Returns an array of all connectors.
	 */
	public Connector[] getConnectors() {
		return new Connector[] { shortNameToConnector.get("fics"),
				shortNameToConnector.get("bics") };

	}
}
