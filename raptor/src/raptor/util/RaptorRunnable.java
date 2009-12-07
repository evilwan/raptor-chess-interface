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
				LOG
						.warn("Execution of runnable vetoed beause raptor is disposed "
								+ this);
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
