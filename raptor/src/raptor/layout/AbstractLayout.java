/**
 * New BSD License
 * http://www.opensource.org/licenses/bsd-license.php
 * Copyright 2009-2011 RaptorProject (http://code.google.com/p/raptor-chess-interface/)
 * All rights reserved.
 * 
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the documentation and/or other materials provided with the distribution.
 * Neither the name of the RaptorProject nor the names of its contributors may be used to endorse or promote products derived from this software without specific prior written permission.
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package raptor.layout;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.Image;

import raptor.Raptor;
import raptor.international.L10n;
import raptor.pref.PreferenceKeys;

public abstract class AbstractLayout implements Layout {
	protected String pathToImage;
	protected String name;
	protected Map<String, String> preferenceAdjustments = new HashMap<String, String>();

	public AbstractLayout(String pathToImage, String name) {
		this.pathToImage = pathToImage;
		this.name = name;
		addAdjusmentsToClearConnectorLayoutInfo();
	}

	protected void addCrossConnectorSetting(String key, String value) {
		preferenceAdjustments.put("fics-" + key, value);
		preferenceAdjustments.put("fics2-" + key, value);
		preferenceAdjustments.put("bics-" + key, value);
		preferenceAdjustments.put("bics2-" + key, value);
	}

	protected void addAdjusmentsToClearConnectorLayoutInfo() {
		preferenceAdjustments.put("fics-"
				+ "Primary" + "-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("fics-"
				+ "Secondary" + "-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("fics-"
				+ "Tertiary" + "-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("bics-"
				+ "Primary" + "-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("bics-"
				+ "Secondary" + "-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
		preferenceAdjustments.put("bics-"
				+ "Tertiary" + "-"
				+ PreferenceKeys.CHANNEL_REGEX_TAB_INFO, null);
	}

	public String getPathToImage() {
		return pathToImage;
	}

	public void setPathToImage(String pathToImage) {
		this.pathToImage = pathToImage;
	}

	@Override
	public Image getImage() {
		if (pathToImage == null) {
			return null;
		} else {
			return Raptor.getInstance().getImage(pathToImage);
		}
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<String, String> getPreferenceAdjustments() {
		return preferenceAdjustments;
	}

	public void setPreferenceAdjustments(
			Map<String, String> preferenceAdjustments) {
		this.preferenceAdjustments = preferenceAdjustments;
	}
	


	public void apply() {
		for (String key : preferenceAdjustments.keySet()) {
			String value = preferenceAdjustments.get(key);
			if (value == null) {
				Raptor.getInstance().getPreferences().setToDefault(key);
			} else {
				Raptor.getInstance().getPreferences().setValue(key,
						preferenceAdjustments.get(key));
			}
		}
		Raptor.getInstance().getWindow().resetLayout();
	}
}
