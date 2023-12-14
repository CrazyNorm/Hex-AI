package mcts.policies;

import mcts.Action;
import mcts.Board;
import mcts.TreeNode;

import java.util.List;
import java.util.Random;

public class RandomExpandShared extends ExpandPolicy {
    // selects a random action to expand

    @Override
    public TreeNode expand(TreeNode node, Board board) {
        // creates a new child node with a random choice of action

        Random rand = new Random();
        boolean nodeLock = false;
        TreeNode newChild = null;
        while (!nodeLock) {
            try {
                nodeLock = node.getLock().tryLock();

                if (nodeLock) {
                    List<Action> newActions = getNewActions(node, board);
                    if (!newActions.isEmpty()) {
                        int randIndex = rand.nextInt(newActions.size());
                        newChild = new TreeNode(node, newActions.get(randIndex));
                    }
                }
            } finally {
                if (nodeLock) node.getLock().unlock();
            }
        }

        if (newChild != null) node.addChild(newChild);

        return newChild;
    }
}
