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
package raptor.chess.pgn;

import raptor.chess.Move;

/**
 * A class describing a tree like Analysis line in PGN. Analysis lines can
 * contain sub-lines and can contain an SublineNode representing the reply.
 */
public class SublineNode implements MoveAnnotation {
	static final long serialVersionUID = 1;

	private Move move;

	private SublineNode parent;

	private SublineNode reply;

	/**
	 * @deprecated
	 * @param move
	 * @return
	 */
	@Deprecated
	private SublineNode sublineOwner;

	public SublineNode() {

	}

	public SublineNode(Move move) {
		this.move = move;
	}

	public SublineNode(SublineNode parent, Move move) {
		this(move);
		this.parent = parent;
	}

	public SublineNode createReply(Move move) {
		reply = new SublineNode(this, move);
		return reply;
	}

	/**
	 * @deprecated
	 * @param move
	 * @return
	 */
	@Deprecated
	public SublineNode createSubline(Move move) {
		SublineNode newSubline = new SublineNode(null, move);
		newSubline.setSublineOwner(this);
		getMove().addAnnotation(newSubline);
		return newSubline;
	}

	public SublineNode getGreatestParent() {
		SublineNode result = this;
		while (result.isChild()) {
			result = result.getParent();
		}
		return result;
	}

	public Move getMove() {
		return move;
	}

	public SublineNode getParent() {
		return parent;
	}

	public SublineNode getReply() {
		return reply;
	}

	/**
	 * @deprecated This is different than than the parent. What it returns is
	 *             the SublineNode this SublineNode is a different sub-line of,
	 *             null if its a root SublineNode.
	 */
	@Deprecated
	public SublineNode getSublineOwner() {
		return sublineOwner;
	}

	public SublineNode[] getSublines() {
		return move.getSublines();
	}

	public boolean hasReply() {
		return reply != null;
	}

	/**
	 * This is different than being a parent. What it means is this SublineNode
	 * is a sub-line in another SublineNode.
	 */
	public boolean hasSublineOwner() {
		return sublineOwner != null;
	}

	public boolean hasSublines() {
		return move.hasSubline();
	}

	/**
	 * Returns true if this SublineNode is a child of another SublineNode (e.g.
	 * it is a reply of another line).
	 * 
	 * @return
	 */
	public boolean isChild() {
		return parent != null;
	}

	/**
	 * Returns true if this is the root level analysis line node. (e.g. is not a
	 * reply of another line).
	 */
	public boolean isRootLevel() {
		return parent == null;
	}

	public void setMove(Move move) {
		this.move = move;
	}

	@Override
	public String toString() {
		if (getMove() == null) {
			return "empty";
		} else {
			StringBuilder result = new StringBuilder();
			if (getMove().hasSubline()) {
				for (SublineNode node : getSublines()) {
					result.append("(" + node.toString() + ")");
				}
			}
			result.append(move.toString()
					+ (reply != null ? " " + reply.toString() : ""));
			return result.toString();
		}
	}

	/**
	 * @deprecated
	 * @param move
	 * @return
	 */
	@Deprecated
	private void setSublineOwner(SublineNode node) {
		sublineOwner = node;
	}
}
