package raptor.service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;

import raptor.gui.board.Background;
import raptor.gui.board.Set;

public class SWTService {

	private static final SWTService instance = new SWTService();
	Display display = new Display();
	Background squareBackground = null;
	Set set = null;

	List<Shell> shells = Collections.synchronizedList(new ArrayList<Shell>(10));

	public static SWTService getInstance() {
		return instance;
	}
	
	public SWTService() {
		updateFromPrefs();
	}

	public void updateFromPrefs() {

		String newSquareBackgroundName = PreferenceService.getInstance()
				.getConfig().getString(PreferenceService.BACKGROUND_KEY,
						"CrumpledPaper");
		String newSetName = PreferenceService.getInstance().getConfig()
				.getString(PreferenceService.SET_KEY, "WCN");

		if (squareBackground == null
				|| !squareBackground.getName().equals(newSquareBackgroundName)) {
			squareBackground = new Background(display, newSquareBackgroundName);
		}
		if (set == null || !set.getName().equals(newSetName)) {
			set = new Set(display, newSetName);
		}
	}

	public Background getSquareBackground() {
		return squareBackground;
	}

	public void setSquareBackground(Background squareBackground) {
		this.squareBackground = squareBackground;
	}

	public Set getSet() {
		return set;
	}

	public void setSet(Set set) {
		this.set = set;
	}

	public Shell createShell(int style) {
		Shell result =  new Shell(display);
		shells.add(result);
		return result;
	}

	public Shell createShell() {
		Shell result =  new Shell(display);
		shells.add(result);
		return result;
	}

	public void disposeShell(Shell shell) {
		shells.remove(shell);
		shell.dispose();
	}

	public void start() {
		while (!shells.isEmpty()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
