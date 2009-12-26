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
package raptor.alias;

import org.apache.commons.lang.StringUtils;

import raptor.Raptor;
import raptor.pref.PreferenceKeys;
import raptor.swt.chat.ChatConsoleController;

public class SetPremoveModeAlias extends RaptorAlias {

	public SetPremoveModeAlias() {
		super(
				"premove",
				"Turns premove on or off.",
				"set premove [on | off | 1 | 0 | queued]. "
						+ "Example: 'set premove queued' will set premove to queued premove mode.");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (StringUtils.startsWith(command, "set premove ")) {
			String whatsLeft = command.substring(12).trim();
			if (whatsLeft.equals("on") || whatsLeft.equals("1")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_PREMOVE_ENABLED, true);
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Non-Queued premove on.");
			} else if (whatsLeft.equals("off") || whatsLeft.equals("0")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_PREMOVE_ENABLED, false);
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED, false);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Premove off.");
			} else if (whatsLeft.equals("queued")) {
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_PREMOVE_ENABLED, true);
				Raptor.getInstance().getPreferences().setValue(
						PreferenceKeys.BOARD_QUEUED_PREMOVE_ENABLED, true);
				Raptor.getInstance().getPreferences().save();
				return new RaptorAliasResult(null, "Queued premove on.");
			}
		}
		return null;
	}

}
