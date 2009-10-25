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

import java.util.HashMap;
import java.util.Map;

import raptor.connector.Connector;
import raptor.swt.chat.ChatConsoleController;
import raptor.swt.chess.ChessBoardController;

/**
 * An abstract Raptor Action. Currently all RaptorActions must extend this
 * class. All RaptorActions must also have a no arg contrutor.
 * 
 */
public abstract class AbstractRaptorAction implements RaptorAction {
	protected ChatConsoleController chatConsoleControllerSource;
	protected ChessBoardController chessBoardControllerSource;
	protected Connector connectorSource;
	protected Category category = Category.Misc;
	protected String description;
	protected String name;
	protected String icon;
	protected boolean isSystemAction;
	protected int keyCode;
	protected int modifierKey;
	protected Map<RaptorActionContainer, Integer> containerToOrderMap = new HashMap<RaptorActionContainer, Integer>();

	/**
	 * {@inheritDoc}
	 */
	public void addContainer(RaptorActionContainer container, int order) {
		containerToOrderMap.put(container, order);
	}

	public Category getCategory() {
		return category;
	}

	/**
	 * {@inheritDoc}
	 */
	public ChatConsoleController getChatConsoleControllerSource() {
		return chatConsoleControllerSource;
	}

	/**
	 * {@inheritDoc}
	 */
	public ChessBoardController getChessBoardControllerSource() {
		return chessBoardControllerSource;
	}

	public Connector getConnectorSource() {
		return connectorSource;
	}

	/**
	 * {@inheritDoc}
	 */
	public RaptorActionContainer[] getContainers() {
		return containerToOrderMap.keySet().toArray(
				new RaptorActionContainer[0]);
	}

	/**
	 * {@inheritDoc}
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getIcon() {
		return icon;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getKeyCode() {
		return keyCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getModifierKey() {
		return modifierKey;
	}

	/**
	 * {@inheritDoc}
	 */
	public String getName() {
		return name;
	}

	/**
	 * {@inheritDoc}
	 */
	public int getOrder(RaptorActionContainer container) {
		Integer result = containerToOrderMap.get(container);
		if (result == null) {
			throw new IllegalArgumentException(
					"This RaptorAction is not being showin in container "
							+ container);
		}
		return result;
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isIn(RaptorActionContainer container) {
		return containerToOrderMap.containsKey(container);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean isSystemAction() {
		return isSystemAction;
	}

	/**
	 * {@inheritDoc}
	 */
	public void removeContainer(RaptorActionContainer container) {
		containerToOrderMap.remove(container);
	}

	public void setCategory(Category category) {
		this.category = category;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChatConsoleControllerSource(
			ChatConsoleController chatConsoleControllerSource) {
		this.chatConsoleControllerSource = chatConsoleControllerSource;
		connectorSource = null;
		chessBoardControllerSource = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setChessBoardControllerSource(
			ChessBoardController chessBoardControllerSource) {
		this.chessBoardControllerSource = chessBoardControllerSource;
		connectorSource = null;
		chatConsoleControllerSource = null;
	}

	public void setConnectorSource(Connector connectorSource) {
		this.connectorSource = connectorSource;
		chatConsoleControllerSource = null;
		chessBoardControllerSource = null;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setContainerOrder(RaptorActionContainer container, int order) {
		containerToOrderMap.put(container, order);
	}

	/**
	 * {@inheritDoc}
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setIcon(String icon) {
		this.icon = icon;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setKeyCode(int keyCode) {
		this.keyCode = keyCode;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setModifierKey(int modifierKey) {
		this.modifierKey = modifierKey;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * {@inheritDoc}
	 */
	public void setSystemAction(boolean isSystemAction) {
		this.isSystemAction = isSystemAction;
	}
}
