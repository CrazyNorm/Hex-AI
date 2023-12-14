package mcts;

import mcts.policies.ExploitPolicy;
import mcts.policies.WinRateExploit;

import java.util.ArrayList;
import java.util.List;

public class RootMCTS {
    // implements root parallelization for MCTS

    private final int TIMEOUT;
    private final boolean LOG;

    private final boolean SYNC_TREES;

    private final ExploitPolicy exploit;
    private List<RootMCTSThread> threads;


    public RootMCTS(int timeout) {
        this(false, timeout, false);
    }

    public RootMCTS(int timeout, boolean log) {
        this(false, timeout, log);
    }

    public RootMCTS(boolean sync, int timeout) {
        this(sync, timeout, false);
    }

    public RootMCTS(boolean sync, int timeout, boolean log) {
        this.SYNC_TREES = sync;
        this.TIMEOUT = timeout;
        this.exploit = new WinRateExploit();
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

    private void treeDiff(TreeNode root, TreeNode diff) {
        // finds difference between root & diff trees (i.e. root = root - diff)
        // modifies root tree in place

        // subtract statistics for this node
        root.addCount(-diff.getCount());
        root.addPayoff(-diff.getPayoff());

        // for each child node of diff, look for a matching node in root
        for (TreeNode diffChild: diff.getChildren()) {
            boolean matched = false;
            for (TreeNode rootChild: root.getChildren()) {
                if (rootChild.getAction().equals(diffChild.getAction())) {
                    // if the current child is already represented in root, diff these 2 nodes
                    treeDiff(rootChild, diffChild);
                    matched = true;
                    break;
                }
            }

            // if not matched, add a deep copy of the current child to the root tree
            if (!matched) root.addChild(new TreeNode(root, diffChild));
        }
    }


    public Action search(Board board, Player p) {
        // find max threads
        int threadCount = Runtime.getRuntime().availableProcessors();
        if (LOG) System.out.println(threadCount + " processors");
        threads = new ArrayList<>();

        // create threads with MCTS instances
        for (int i = 0; i < threadCount; i++) {
            threads.add(new RootMCTSThread(board, p, TIMEOUT));
            threads.get(i).start();
        }

        TreeNode prevSync = new TreeNode(null);
        if (SYNC_TREES) {
            // sync threads every second
            long endTime = System.currentTimeMillis() + TIMEOUT - 1000;
            while (System.currentTimeMillis() < endTime) {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                prevSync = syncThreads(prevSync, p);
            }
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
        long joinStart = System.currentTimeMillis();
        TreeNode root = new TreeNode(p);
        for (int i = 0; i < threadCount; i++) {
            long threadStart = System.currentTimeMillis();
            RootMCTSThread thread = threads.get(i);
            joinTrees(root, thread.mcts.getTree());
            count += thread.mcts.getCount();
            if (LOG) {
                System.out.print("Thread " + i + ": " + thread.mcts.getCount() + " simulations");
                System.out.println(", " + (System.currentTimeMillis() - threadStart) + " ms joining");
            }
        }

        System.out.println("\nTotal " + (System.currentTimeMillis() - joinStart) + " ms joining");

        // run exploit policy on joined tree
        TreeNode chosen = exploit.exploit(root);
        if (LOG) {
            System.out.println("Chosen:  visited " + chosen.getCount() + " times, won " + chosen.getPayoff());
            System.out.println("Action: " + chosen.getAction().getPlayer() + "; " + chosen.getAction().toString());
            System.out.println(count + " simulations");
        }

        return chosen.getAction();
    }

    public TreeNode syncThreads(TreeNode prevSync, Player p) {
        TreeNode root = new TreeNode(p);
        for (RootMCTSThread thread: threads) {
            // suspend thread
            synchronized (thread.mcts) {
                thread.mcts.suspend();
                try {
                    thread.mcts.wait();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }
            }

            // finds change since last sync
            treeDiff(thread.mcts.getTree(), prevSync);

            // join current partial tree
            joinTrees(root, thread.mcts.getTree());
        }

        // update tree & resume threads
        for (RootMCTSThread thread: threads) {
            synchronized (thread.mcts) {
                thread.mcts.setNewTree(root);
                thread.mcts.resume();
                thread.mcts.notify();
            }
        }

        return root;
    }



    private static class RootMCTSThread extends Thread {

        private final Board board;
        private final Player player;

        private final MCTS mcts;


        public RootMCTSThread(Board b, Player p, int timeout) {
            this(b, p, timeout, false);
        }

        public RootMCTSThread(Board b, Player p, int timeout, boolean log) {
            this.mcts = new MCTS(timeout, log, false);
            this.board = b;
            this.player = p;
        }


        @Override
        public void run() {
            // run search
            mcts.search(board, new TreeNode(player));

            // thread terminates
        }
    }
}
