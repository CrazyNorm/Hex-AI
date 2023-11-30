package mcts.policies;

import mcts.Board;
import mcts.Player;
import mcts.TreeNode;

import java.util.ArrayList;
import java.util.List;

public abstract class SelectionPolicy {
    // abstract superclass for selection policies (picks a node to be expanded)

    public abstract TreeNode select(TreeNode root, Board board);
    // takes the root node & returns the selected node to be expanded


    protected List<TreeNode> getExpandableNodes(TreeNode node, Board b) {
        // gets all nodes under the given node which are non-terminal & have unvisited children

        List<TreeNode> expandableNodes = new ArrayList<>();

        // create copy of the board with this node's action applied
        Board newBoard = new Board(b);
        newBoard.applyAction(node.getAction());

        // if this node is non-terminal with unvisited children, add it to the list of expandable nodes
        if (newBoard.checkWin() == Player.NONE) {
            Player nextPlayer;
            if (node.getAction().getPlayer() == Player.RED) nextPlayer = Player.BLUE;
            else nextPlayer = Player.RED;

            if (newBoard.getActions(nextPlayer).size() > node.getChildren().size())
                expandableNodes.add(node);
        }

        // explore the node's existing children
        for (TreeNode child: node.getChildren()) {
            List<TreeNode> childExpandable = getExpandableNodes(child, newBoard);
            // add all expandable nodes under the given child to the list of expandable nodes
            expandableNodes.addAll(childExpandable);
        }

        return expandableNodes;
    }
}
