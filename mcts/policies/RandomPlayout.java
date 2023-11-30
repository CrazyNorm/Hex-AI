package mcts.policies;

import mcts.Action;
import mcts.Board;
import mcts.Player;

import java.util.List;
import java.util.Random;

public class RandomPlayout extends PlayoutPolicy{
    // selects a random available move

    @Override
    public Action nextMove(Board b, Player p) {
        // returns a random available action

        Random rand = new Random();
        List<Action> possibleActions = b.getActions(p);

        int randIndex = rand.nextInt(possibleActions.size());
        return possibleActions.get(randIndex);
    }
}
