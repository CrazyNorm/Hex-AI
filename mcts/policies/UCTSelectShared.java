package mcts.policies;

import mcts.Board;
import mcts.Player;
import mcts.TreeNode;

public class UCTSelectShared extends SelectionPolicy {
    // selects the child node that maximises UCT

    private final double C = 1; // explore - exploit tradeoff

    @Override
    public TreeNode select(TreeNode root, Board board) {
        // returns best node according to UCT

        // get player for any children of current node
        Player nextPlayer;
        if (root.getAction().getPlayer() == Player.RED) nextPlayer = Player.BLUE;
        else nextPlayer = Player.RED;

        boolean nodeLock = false;
        boolean unvisitedChildren = false;
        TreeNode bestChild = null;
        while (!nodeLock) {
            try {
                nodeLock = root.getLock().tryLock();
                if (nodeLock) {
                    // if current node has any unvisited children, expand from this node
                    if (board.getActions(nextPlayer).size() > root.getChildren().size()) unvisitedChildren = true;

                    // otherwise find child node with highest UCT value
                    else bestChild = getBestChild(root);
                }
            } finally {
                if (nodeLock) root.getLock().unlock();
            }
        }

        if (unvisitedChildren) return root; // return this node if any unvisited children
        if (bestChild == null) return null; // if this happens, then something has gone horribly wrong

        // select node from best child
        Board newBoard = new Board(board);
        newBoard.applyAction(bestChild.getAction());
        return select(bestChild, newBoard);
    }

    private TreeNode getBestChild(TreeNode root) {
        TreeNode bestChild = null;
        double bestVal = Double.NEGATIVE_INFINITY;
        for (TreeNode child: root.getChildren()) {
            double exploit = (double) child.getPayoff() / child.getCount();
            double explore = Math.sqrt(2 * Math.log(root.getCount()) / child.getCount());

            double val = exploit + C * explore;
            if (val > bestVal) {
                bestVal = val;
                bestChild = child;
            }
        }
        return bestChild;
    }
}
