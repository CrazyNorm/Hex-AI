package mcts;

public class RootMCTSAgent extends MCTSAgent{

    @Override
    protected Action runSearch(Board b, Player p) {
        // instantiate RootMCTS class
        RootMCTS mcts = new RootMCTS(timeout, true);

        // return result of the search
        return mcts.search(b, p);
    }

    public static void main(String[] args) {
        RootMCTSAgent agent = new RootMCTSAgent();
        agent.run();
    }
}
