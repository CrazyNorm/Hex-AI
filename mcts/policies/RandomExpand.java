package mcts.policies;

import mcts.Action;
import mcts.Board;
import mcts.TreeNode;

import java.util.List;
import java.util.Random;

public class RandomExpand extends ExpandPolicy {
    // selects a random action to expand

    @Override
    public TreeNode expand(TreeNode node, Board board) {
        // creates a new child node with a random choice of action

        Random rand = new Random();
        List<Action> newActions = getNewActions(node, board);

        int randIndex = rand.nextInt(newActions.size());
        TreeNode newChild = new TreeNode(node, newActions.get(randIndex));
        node.addChild(newChild);

        return newChild;
    }
}
