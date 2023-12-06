package mcts;

import mcts.policies.ExploitPolicy;
import mcts.policies.WinRateExploit;

import java.util.ArrayList;
import java.util.List;

public class RootMCTS {
    // implements root parallelization for MCTS

    private final int TIMEOUT;
    private boolean LOG = false;

    private final ExploitPolicy exploit;

    public RootMCTS(int timeout) {
        this.TIMEOUT = timeout;
        this.exploit = new WinRateExploit();
    }

    public RootMCTS(int timeout, boolean log) {
        this(timeout);
        this.LOG = log;
    }


    private void joinTrees(TreeNode root, TreeNode join) {
        // aggregates the "join" tree onto the "root" tree
        // modifies root tree in place

        // accumulate statistics for this node
        root.addCount(join.getCount());
        root.addPayoff(join.getPayoff());

        // for each child node of join, look for a matching node in root
        for (TreeNode joinChild: join.getChildren()) {
            boolean joined = false;
            for (TreeNode rootChild: root.getChildren()) {
                if (rootChild.getAction().equals(joinChild.getAction())) {
                    // if the current child is already represented in root, join these 2 nodes
                    joinTrees(rootChild, joinChild);
                    joined = true;
                    break;
                }
            }

            // if not joined, add a deep copy of the current child to the root tree
            if (!joined) root.addChild(new TreeNode(root, joinChild));
        }
    }


    public Action search(Board board, Player p) {
        // find max threads
        int threadCount = Runtime.getRuntime().availableProcessors();
        if (LOG) System.out.println(threadCount + " processors");
        List<RootMCTSThread> threads = new ArrayList<>();

        // create threads with MCTS instances
        for (int i = 0; i < threadCount; i++) {
            threads.add(new RootMCTSThread(board, p, TIMEOUT));
            threads.get(i).start();
        }

        // wait for threads to finish
        for (RootMCTSThread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // join partial trees
        int count = 0;
        TreeNode root = new TreeNode(p);
        for (int i = 0; i < threadCount; i++) {
            RootMCTSThread thread = threads.get(i);
            joinTrees(root, thread.getTree());
            count += thread.getCount();
            if (LOG) System.out.println("Thread " + i + ": " + thread.getCount() + " simulations");
        }

        // run exploit policy on joined tree
        TreeNode chosen = exploit.exploit(root);
        if (LOG) {
            System.out.println("Chosen:  visited " + chosen.getCount() + " times, won " + chosen.getPayoff());
            System.out.println("Action: " + chosen.getAction().getPlayer() + "; " + chosen.getAction().toString());
            System.out.println(count + " simulations");
        }

        return chosen.getAction();
    }



    private static class RootMCTSThread extends Thread {

        private final Board board;

        private final Player player;

        private TreeNode root;

        private int count;

        private final int TIMEOUT;

        private final boolean LOG;


        public RootMCTSThread(Board b, Player p, int timeout) {
            this(b, p, timeout, false);
        }

        public RootMCTSThread(Board b, Player p, int timeout, boolean log) {
            this.board = b;
            this.player = p;
            this.TIMEOUT = timeout;
            this.LOG = log;
        }


        public TreeNode getTree() {
            return root;
        }

        public int getCount() {
            return this.count;
        }


        @Override
        public void run() {
            // run search
            MCTS mcts = new MCTS(TIMEOUT, LOG);
            mcts.search(board, player);

            // get root & count from search
            this.root = mcts.getTree();
            this.count = mcts.getCount();

            // thread terminates
        }
    }
}
