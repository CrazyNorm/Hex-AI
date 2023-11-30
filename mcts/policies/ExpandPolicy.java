package mcts.policies;

import mcts.Action;
import mcts.Board;
import mcts.Player;
import mcts.TreeNode;

import java.util.List;

public abstract class ExpandPolicy {
    // abstract superclass for expansion policies (expands a node with a new child)

    public abstract TreeNode expand(TreeNode node, Board board);
    // takes a node & returns the newly expanded child node


    protected List<Action> getNewActions(TreeNode node, Board b) {
        // gets all possible actions which don't already have a child node

        // gets the player for the possible new children
        Player nextPlayer;
        if (node.getAction().getPlayer() == Player.RED) nextPlayer = Player.BLUE;
        else nextPlayer = Player.RED;

        // get possible actions for the next player
        List<Action> newActions = b.getActions(nextPlayer);

        // removes all actions which already have a child node
        for (TreeNode child: node.getChildren())
            newActions.remove(child.getAction());

        return newActions;
    }
}