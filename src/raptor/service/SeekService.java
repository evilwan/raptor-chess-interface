package raptor.service;

import java.util.ArrayList;
import java.util.List;

import raptor.chat.Seek;
import raptor.connector.Connector;

public class SeekService {
	public static interface SeekServiceListener {
		public void seeksChanged(Seek[] newSeeks);
	}

	private Seek[] seeks = new Seek[0];
	private Connector connector;
	private List<SeekServiceListener> listeners = new ArrayList<SeekServiceListener>(
			10);

	public SeekService(Connector connector) {
		this.connector = connector;
	}

	public void adSeekServiceListener(SeekServiceListener listener) {
		listeners.add(listener);
	}

	public Connector getConnector() {
		return connector;
	}

	public Seek[] getSeeks() {
		return seeks;
	}

	public void refreshSeeks() {
		connector.sendGetSeeksMessage();
	}

	public void removeSeekServiceLisetner(SeekServiceListener listener) {
		listeners.remove(listener);
	}

	public void setSeeks(Seek[] seeks) {
		this.seeks = seeks;
		fireSeeksChanged();
	}

	protected void fireSeeksChanged() {
		for (SeekServiceListener listener : listeners) {
			listener.seeksChanged(seeks);
		}
	}
}
