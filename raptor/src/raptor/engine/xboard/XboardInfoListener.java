package raptor.engine.xboard;

public interface XboardInfoListener {

	public void engineSentInfo(String ply, String score, String time, String nodes,
			String pv);

}
