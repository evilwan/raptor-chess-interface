package testcases;

import org.junit.Assert;
import org.junit.Test;

import raptor.engine.xboard.XboardEngine;
import raptor.engine.xboard.XboardInfoListener;
import raptor.util.RaptorLogger;

public class TestXboardEngine {
	
	@Test
	public void testGnuChessConnectivity() {
		RaptorLogger.initializeLogger();
		XboardEngine engine = new XboardEngine();
		engine.setUsingThreadService(false);
		engine.setProcessPath("gnuchess");
		Assert.assertTrue(engine.connect());
		Assert.assertTrue(engine.getEngineName().equals("GNU Chess 5.07"));
		engine.analyze(new XboardInfoListener() {
			public void engineSentInfo(String ply, String score, String time, String nodes,
					String pv) {
				
			}
		});
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		engine.stop();
	}
}
