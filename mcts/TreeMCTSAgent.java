package mcts;

public class TreeMCTSAgent extends MCTSAgent {

    protected final int timeout = 5000;

    @Override
    protected Action runSearch(Board b, Player p) {
        // instantiate TreeMCTS class
        TreeMCTS mcts = new TreeMCTS(timeout, true);

        // return result of the search
        return mcts.search(b, p);
    }

    public static void main(String[] args) {
        TreeMCTSAgent agent = new TreeMCTSAgent();
        agent.run();
    }
}
