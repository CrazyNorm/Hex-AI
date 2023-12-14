package mcts;

import mcts.policies.*;

import java.util.ArrayList;
import java.util.List;

public class TreeMCTS {
    // implements tree parallelization for MCTS

    private final int TIMEOUT;
    private final boolean LOG;


    private final ExploitPolicy exploit;
    private List<TreeMCTSThread> threads;


    public TreeMCTS(int timeout) {
        this(timeout, false);
    }

    public TreeMCTS(int timeout, boolean log) {
        this.TIMEOUT = timeout;
        this.exploit = new WinRateExploit();
        this.LOG = log;
    }


    public Action search(Board board, Player p) {
        // find max threads
        int threadCount = Runtime.getRuntime().availableProcessors();
        if (LOG) System.out.println(threadCount + " processors");
        threads = new ArrayList<>();

        // create tree root
        TreeNode root = new TreeNode(p, true);

        // create threads with MCTS instances
        for (int i = 0; i < threadCount; i++) {
            threads.add(new TreeMCTSThread(board, TIMEOUT, root));
            threads.get(i).start();
        }

        // wait for threads to finish
        for (TreeMCTSThread thread: threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }

        // run exploit policy on shared tree
        TreeNode chosen = exploit.exploit(root);
        if (LOG) {
            // get individual thread stats
            int count = 0;
            for (int i = 0; i < threadCount; i++) {
                TreeMCTSThread thread = threads.get(i);
                count += thread.mcts.getCount();
                System.out.println("Thread " + i + ": " + thread.mcts.getCount() + " simulations");
            }

            System.out.println("Chosen:  visited " + chosen.getCount() + " times, won " + chosen.getPayoff());
            System.out.println("Action: " + chosen.getAction().getPlayer() + "; " + chosen.getAction().toString());
            System.out.println(count + " simulations");
        }

        return chosen.getAction();
    }



    private static class TreeMCTSThread extends Thread {

        private final int V_LOSS = 1;  // how many losses to add as virtual loss

        private final Board board;

        private final MCTS mcts;

        private final TreeNode sharedRoot;


        public TreeMCTSThread(Board b, int timeout, TreeNode root) {
            this(b, timeout, false, root);
        }

        public TreeMCTSThread(Board b, int timeout, boolean log, TreeNode root) {
            this.mcts = new MCTS(timeout, log, false, V_LOSS);
            this.mcts.initPolicies(
                    new UCTSelectShared(),
                    new RandomExpandShared(),
                    new RandomPlayout(),
                    new WinRateExploit()
            );

            this.board = b;
            this.sharedRoot = root;
        }


        @Override
        public void run() {
            // run search
            mcts.search(board, sharedRoot);

            // thread terminates
        }
    }
}
