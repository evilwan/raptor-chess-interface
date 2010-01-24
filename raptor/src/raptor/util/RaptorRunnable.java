/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright (c) 2010, RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import raptor.Raptor;
import raptor.connector.Connector;

/**
 * A runnable with exception handling. Override execute as you would run in a
 * Runnable to use it.
 */
public abstract class RaptorRunnable implements Runnable {
	protected static final Log LOG = LogFactory.getLog(RaptorRunnable.class);

	protected Connector connector;

	public RaptorRunnable() {

	}

	public RaptorRunnable(Connector connector) {
		this.connector = connector;
	}

	/**
	 * Override this to implement run method functionality.
	 */
	public abstract void execute();

	/**
	 * Do not override this. This method calls execute in a try catch to handle
	 * errors.
	 */
	public void run() {
		try {
			if (connector == null || !Raptor.getInstance().isDisposed()) {
				execute();
			} else {
				if (LOG.isInfoEnabled()) {
					LOG
							.info("Execution of runnable vetoed beause raptor is disposed "
									+ this);
				}
			}
		} catch (Throwable t) {
			if (connector != null) {
				connector.onError("Error in exceute", t);
			} else {
				Raptor.getInstance().onError("Error in execute", t);
			}
		}
	}

}
