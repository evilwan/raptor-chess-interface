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
package raptor.action;

import org.apache.commons.lang.StringUtils;
import org.eclipse.swt.SWT;

/**
 * Contains utility methods useful when working with RaptorActions.
 */
public class ActionUtils {
	public static final String VALID_ACTION_KEY_CODES = "1234567890-=qwertyuiop[]\\asdfghjkl;'zxcvbnm,./";

	/**
	 * Returns the string to use for the keyCode if there is not a modifier.
	 * These are usually function keys (F1,F2,etc).
	 */
	public static String getNonModifierStringFromKeyCode(int keyCode) {
		switch (keyCode) {
		case 0:
			return "";
		case SWT.ESC:
			return "ESC";
		case SWT.F1:
			return "F1";
		case SWT.F2:
			return "F2";
		case SWT.F3:
			return "F3";
		case SWT.F4:
			return "F4";
		case SWT.F5:
			return "F5";
		case SWT.F6:
			return "F6";
		case SWT.F7:
			return "F7";
		case SWT.F8:
			return "F8";
		case SWT.F9:
			return "F9";
		case SWT.F10:
			return "F10";
		case SWT.F11:
			return "F11";
		case SWT.F12:
			return "F12";
		case SWT.SCROLL_LOCK:
			return "SCRLOCK";
		default:
			return "" + (char) keyCode;
		}
	}

	/**
	 * Returns the modifier key matching the specified keyCode. These are
	 * currently ALT,CTRL,and CMD.
	 */
	public static String getStringFromModifier(int keyCode) {
		switch (keyCode) {
		case SWT.ALT:
			return "ALT";
		case SWT.CONTROL:
			return "CTRL";
		case SWT.COMMAND:
			return "CMD";
		default:
			return "";
		}
	}

	/**
	 * Returns true if the specified keyCode is valid without a modifier.
	 */
	public static boolean isValidKeyCodeWithoutModifier(int keyCode) {
		switch (keyCode) {
		case SWT.ESC:
			return true;
		case SWT.F1:
			return true;
		case SWT.F2:
			return true;
		case SWT.F3:
			return true;
		case SWT.F4:
			return true;
		case SWT.F5:
			return true;
		case SWT.F6:
			return true;
		case SWT.F7:
			return true;
		case SWT.F8:
			return true;
		case SWT.F9:
			return true;
		case SWT.F10:
			return true;
		case SWT.F11:
			return true;
		case SWT.F12:
			return true;
		case SWT.SCROLL_LOCK:
			return true;
		default:
			return false;
		}
	}

	/**
	 * Returns true if the specified keyCode is a valid modifier key. Currently
	 * ALT,COMMAND, and CONTROL.
	 */
	public static boolean isValidModifier(int keyCode) {
		return keyCode == SWT.ALT || keyCode == SWT.CONTROL
				|| keyCode == SWT.COMMAND;
	}

	/**
	 * Returns the key code for the specified key binding description.
	 */
	public static int keyBindingDescriptionToKeyCode(
			String keyBindingDescription) {
		if (StringUtils.isBlank(keyBindingDescription)) {
			return 0;
		} else if (keyBindingDescription.equals("ESC")) {
			return SWT.ESC;
		} else if (keyBindingDescription.equals("F1")) {
			return SWT.F1;
		} else if (keyBindingDescription.equals("F2")) {
			return SWT.F2;
		} else if (keyBindingDescription.equals("F3")) {
			return SWT.F3;
		} else if (keyBindingDescription.equals("F4")) {
			return SWT.F4;
		} else if (keyBindingDescription.equals("F5")) {
			return SWT.F5;
		} else if (keyBindingDescription.equals("F6")) {
			return SWT.F6;
		} else if (keyBindingDescription.equals("F7")) {
			return SWT.F7;
		} else if (keyBindingDescription.equals("F8")) {
			return SWT.F8;
		} else if (keyBindingDescription.equals("F9")) {
			return SWT.F9;
		} else if (keyBindingDescription.equals("F10")) {
			return SWT.F10;
		} else if (keyBindingDescription.equals("F11")) {
			return SWT.F11;
		} else if (keyBindingDescription.equals("F12")) {
			return SWT.F12;
		} else if (keyBindingDescription.equals("SCRLOCK")) {
			return SWT.SCROLL_LOCK;
		} else if (keyBindingDescription.equals("F1")) {
			return SWT.F1;
		} else if (VALID_ACTION_KEY_CODES.indexOf(keyBindingDescription
				.charAt(keyBindingDescription.length() - 1)) != -1) {
			return keyBindingDescription
					.charAt(keyBindingDescription.length() - 1);
		} else {
			return 0;
		}

	}

	/**
	 * Returns the modifier for the specified key binding description.
	 */
	public static int keyBindingDescriptionToKeyModifier(
			String keyBindingDescription) {
		if (StringUtils.isBlank(keyBindingDescription)) {
			return 0;
		} else if (keyBindingDescription.startsWith("ALT")) {
			return SWT.ALT;
		} else if (keyBindingDescription.startsWith("CTRL")) {
			return SWT.CTRL;
		} else if (keyBindingDescription.startsWith("CMD")) {
			return SWT.COMMAND;
		} else {
			return 0;
		}
	}

	/**
	 * Returns a string suitable to show a user describing the key bindings.
	 */
	public static String keyBindingToString(RaptorAction action) {
		String result = "";

		if (action.getModifierKey() != 0) {
			result = getStringFromModifier(action.getModifierKey()) + " "
					+ (char) action.getKeyCode();
		} else if (isValidKeyCodeWithoutModifier(action.getKeyCode())) {
			result = getNonModifierStringFromKeyCode(action.getKeyCode());
		}
		return result;
	}

}
