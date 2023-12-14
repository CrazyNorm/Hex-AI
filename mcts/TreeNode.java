package mcts;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class TreeNode {
    // single node in the MCTS partial tree
    // count & payoff currently use shorts to try save on memory use
    // (-32767 < short < 32767)

    // board state is onl needed for selection, so selection uses local board state

    private final Action action;  // corresponding action for the node

    private int count;  // no. of times node visited

    private int payoff;  // cumulative payoff for play-outs passing through this node

    private final TreeNode parent;  // parent node (needed for backpropagation)

    private final List<TreeNode> children;  // child nodes (arraylist? p.queue?)

    private final Lock lock =  new ReentrantLock();

    private final boolean shared;


    public TreeNode(TreeNode parent, Action action) {
        this.action = action;
        this.count = 0;
        this.payoff = 0;

        this.parent = parent;
        if (parent.shared) this.children = new CopyOnWriteArrayList<>();
        else this.children = new ArrayList<>();

        this.shared = parent.shared;
    }

    public TreeNode(Player player, boolean shared) {
        // constructor for root node
        this.action = new Action(player, false);

        this.parent = null;
        if (shared) this.children = new CopyOnWriteArrayList<>();
        else this.children = new ArrayList<>();

        this.shared = shared;
    }

    public TreeNode(Player player) {
        this(player, false);
    }

    public TreeNode(TreeNode parent, TreeNode oldNode) {
        // deep copy constructor
        // copies oldNode as a child of parent

        this.action = oldNode.action;
        this.count = oldNode.count;
        this.payoff = oldNode.payoff;

        this.parent = parent;
        if (oldNode.shared) this.children = new CopyOnWriteArrayList<>();
        else this.children = new ArrayList<>();

        this.shared = oldNode.shared;

        // deep copy every child of the old node
        for (TreeNode child: oldNode.children)
            this.children.add(
                    new TreeNode(this, child)
            );
    }


    public Action getAction() {
        return action;
    }

    public int getCount() {
        return count;
    }

    public synchronized void addCount(int delta) {
        count = count + delta;
    }

    public int getPayoff() {
        return payoff;
    }

    public synchronized void addPayoff(int delta) {
        payoff = payoff + delta;
    }


    public TreeNode getParent() {
        return parent;
    }

    public List<TreeNode> getChildren() {
        return children;
    }

    public synchronized void addChild(TreeNode child) {
        children.add(child);
    }

    public Lock getLock() {
        return this.lock;
    }
}
