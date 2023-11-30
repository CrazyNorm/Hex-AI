package mcts;

import java.util.ArrayList;
import java.util.List;

public class TreeNode {
    // single node in the MCTS partial tree
    // count & payoff currently use shorts to try save on memory use
    // (-32767 < short < 32767)

    // board state is onl needed for selection, so selection uses local board state

    private final Action action;  // corresponding action for the node

    private short count;  // no. of times node visited

    private short payoff;  // cumulative payoff for play-outs passing through this node

    private final TreeNode parent;  // parent node (needed for backpropagation)

    private final List<TreeNode> children;  // child nodes (arraylist? p.queue?)


    public TreeNode(TreeNode parent, Action action) {
        this.action = action;
        this.count = 0;
        this.payoff = 0;

        this.parent = parent;
        this.children = new ArrayList<>();
    }

    public TreeNode(Player player) {
        // constructor for root node
        this.action = new Action(player, false);

        this.parent = null;
        this.children = new ArrayList<>();
    }


    public Action getAction() {
        return action;
    }

    public short getCount() {
        return count;
    }

    public void addCount(int delta) {
        count = (short) (count + delta);
    }

    public short getPayoff() {
        return payoff;
    }

    public void addPayoff(int delta) {
        payoff = (short) (payoff + delta);
    }


    public TreeNode getParent() {
        return parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public void addChild(TreeNode child) {
        children.add(child);
    }
}
