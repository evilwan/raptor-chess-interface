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

import java.util.Arrays;

import org.apache.commons.lang.StringUtils;

import raptor.service.UserTagService;
import raptor.swt.chat.ChatConsoleController;

public class ShowTagsAlias extends RaptorAlias {

	public ShowTagsAlias() {
		super("=tag", "Shows the all of the users currently tagged.", "=tags");
	}

	@Override
	public RaptorAliasResult apply(ChatConsoleController controller,
			String command) {
		if (command.equalsIgnoreCase("=tag")) {
			StringBuilder builder = new StringBuilder(1000);

			String[] tags = UserTagService.getInstance().getTags();
			Arrays.sort(tags);

			builder.append("Available Tags: ");
			for (String tag : tags) {
				builder.append(tag + " ");
			}

			builder.append("\n\nTagged users:\n");
			for (String tag : tags) {
				String[] users = UserTagService.getInstance()
						.getUsersInTag(tag);
				if (users.length > 0) {
					builder.append(tag + ":\n");
					Arrays.sort(users);
					int counter = 0;
					for (int i = 0; i < users.length; i++) {
						builder.append(StringUtils.rightPad(users[i], 20));
						counter++;
						if (counter == 3) {
							counter = 0;
							builder.append("\n");
						}
					}
					builder.append("\n\n");
				}
			}
			return new RaptorAliasResult(null, builder.toString().trim());
		}
		return null;
	}
}