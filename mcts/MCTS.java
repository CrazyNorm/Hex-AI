package mcts;

import mcts.policies.*;

import java.util.Stack;

public class MCTS {
    // main class for MCTS

    private final int TIMEOUT;  // time in ms to spend simulating

    private final boolean LOG;  // whether to log statistics for chosen moves each turn


    private SelectionPolicy select;  // policy for selecting new node to expand

    private ExpandPolicy expand;  // policy for choosing a child node to add to the tree

    private PlayoutPolicy playout;  // policy for choosing moves during play-outs

    private ExploitPolicy exploit;  // policy for choosing a move to make after simulations


    private int count;
    private TreeNode root;  // root of the partial tree


    public MCTS(int timeout) {
        this(timeout, false);
    }

    public MCTS(int timeout, boolean log) {
        this.select = new UCTSelect();
        this.expand = new RandomExpand();
        this.playout = new RandomPlayout();
        this.exploit = new WinRateExploit();
        this.TIMEOUT = timeout;
        this.LOG = log;
    }

    public void initPolicies(
            SelectionPolicy select,
            ExpandPolicy expand,
            PlayoutPolicy playout,
            ExploitPolicy exploit
    ) {
        this.select = select;
        this.expand = expand;
        this.playout = playout;
        this.exploit = exploit;
    }


    public Action search(Board board, Player p) {
        // search for the next move from the current board state
        // uses the given policies for selection, expansion, simulation & exploitation

        // creates tree root
        root = new TreeNode(p);

        // find simulation time threshold
        long endTime = System.currentTimeMillis() + TIMEOUT;

        count = 0;
        while(System.currentTimeMillis() < endTime) {
            // selects the next node to expand
            TreeNode selected = select.select(root, board);

            // generate new board for state at the chosen node
            Board newBoard = new Board(board);
            Stack<Action> actionStack = new Stack<>();
            TreeNode tempNode = selected;
            while (tempNode != root) {
                actionStack.push(tempNode.getAction());
                tempNode = tempNode.getParent();
            }
            for (Action a: actionStack)
                newBoard.applyAction(a);

            // expand selected node with a new child
            TreeNode expanded = expand.expand(selected, newBoard);

            // apply action from expanded node
            newBoard.applyAction(expanded.getAction());

            // simulate playout until the game ends
            Player nextPlayer = expanded.getAction().getPlayer();
            while (newBoard.checkWin() == Player.NONE) {
                // swap player for the next move
                if (nextPlayer == Player.RED) nextPlayer = Player.BLUE;
                else nextPlayer = Player.RED;

                // apply the next playout action to the board
                Action next = playout.nextMove(newBoard, nextPlayer);
                newBoard.applyAction(next);
            }

            // back-propagate from the expanded node back to the root
            Player winner = newBoard.checkWin();
            tempNode = expanded;
            while (tempNode != root) {
                // update statistics for current node
                if (tempNode.getAction().getPlayer() == winner) tempNode.addPayoff(1);
                tempNode.addCount(1);

                tempNode = tempNode.getParent();
            }


            // update statistics for root
            if (tempNode.getAction().getPlayer() == winner) tempNode.addPayoff(1);
            tempNode.addCount(1);

            count++;
        }

        // pick action to take
        TreeNode chosen = exploit.exploit(root);
        if (LOG) {
            System.out.println("Chosen:  visited " + chosen.getCount() + " times, won " + chosen.getPayoff());
            System.out.println("Action: " + chosen.getAction().getPlayer() + "; " + chosen.getAction().toString());
            System.out.println(count + " simulations");
        }

        return chosen.getAction();
    }


    public TreeNode getTree() {
        // returns root of the partial tree
        return this.root;
    }

    public int getCount() {
        return this.count;
    }
}
