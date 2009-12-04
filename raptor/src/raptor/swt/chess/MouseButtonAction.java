package raptor.swt.chess;

import org.eclipse.swt.events.MouseEvent;

import raptor.pref.PreferenceKeys;

public enum MouseButtonAction {
	Left, Middle, Right, Misc1, Misc2, LeftDoubleClick;

	public static MouseButtonAction buttonFromEvent(MouseEvent e) {
		switch (e.button) {
		case 1:
			return Left;
		case 2:
			return Middle;
		case 3:
			return Right;
		case 4:
			return Misc1;
		case 5:
			return Misc2;
		default:
			return null;
		}
	}

	public String getPreferenceSuffix() {
		switch (this) {
		case Left:
			return PreferenceKeys.LEFT_MOUSE_BUTTON_ACTION;
		case Right:
			return PreferenceKeys.RIGHT_MOUSE_BUTTON_ACTION;
		case Middle:
			return PreferenceKeys.MIDDLE_MOUSE_BUTTON_ACTION;
		case Misc1:
			return PreferenceKeys.MISC1_MOUSE_BUTTON_ACTION;
		case Misc2:
			return PreferenceKeys.MISC2_MOUSE_BUTTON_ACTION;
		case LeftDoubleClick:
			return PreferenceKeys.LEFT_DOUBLE_CLICK_MOUSE_BUTTON_ACTION;
		default:
			return null;
		}
	}
}
