package mcts.policies;

import mcts.Action;
import mcts.Board;
import mcts.Player;

public abstract class PlayoutPolicy {
    // abstract superclass for tree policies (picks the next move to make during play-outs)

    public abstract Action nextMove(Board b, Player p);
    // takes the current board state & player and returns the next action to be taken
}
