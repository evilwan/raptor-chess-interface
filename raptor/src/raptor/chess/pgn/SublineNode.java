package raptor.chess.pgn;

import raptor.chess.Move;

/**
 * A class describing a tree like Analysis line in PGN. Analysis lines can
 * contain sub-lines and can contain an SublineNode representing the reply.
 */
public class SublineNode implements MoveAnnotation {
	static final long serialVersionUID = 1;

	private SublineNode parent;

	/**
	 * @deprecated
	 * @param move
	 * @return
	 */
	@Deprecated
	private SublineNode sublineOwner;

	private Move move;

	private SublineNode reply;

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

	/**
	 * @deprecated
	 * @param move
	 * @return
	 */
	@Deprecated
	private void setSublineOwner(SublineNode node) {
		sublineOwner = node;
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
}
