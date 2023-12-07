package mcts;

public class RootMCTSSyncAgent extends MCTSAgent{

    @Override
    protected Action runSearch(Board b, Player p) {
        // instantiate RootMCTS class
        RootMCTS mcts = new RootMCTS(true, timeout, true);

        // return result of the search
        return mcts.search(b, p);
    }

    public static void main(String[] args) {
        RootMCTSSyncAgent agent = new RootMCTSSyncAgent();
        agent.run();
    }
}
