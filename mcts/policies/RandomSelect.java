package mcts.policies;

import mcts.Board;
import mcts.TreeNode;

import java.util.List;
import java.util.Random;

public class RandomSelect extends SelectionPolicy {
    // selects a random node to expand

    @Override
    public TreeNode select(TreeNode root, Board board) {
        // returns a random expandable node

        Random rand = new Random();
        List<TreeNode> possibleNodes = getExpandableNodes(root, board);

        int randIndex = rand.nextInt(possibleNodes.size());
        return possibleNodes.get(randIndex);
    }
}
