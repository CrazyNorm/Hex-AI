package mcts;

public class Action {
    // data structure for representing a possible action choice

    private final Player player;

    private final byte x;

    private final byte y;

    private final boolean swap;


    public Action(Player player, int x, int y) {
        // constructor for a regular move
        this.player = player;
        this.x = (byte)x;
        this.y = (byte)y;
        this.swap = false;
    }

    public Action(Player player, boolean swap) {
        // constructor for a swap move (co-ords are irrelevant, set swap to true)
        this.player = player;
        this.x = 0;
        this.y = 0;
        this.swap = swap;
    }


    public Player getPlayer() {
        return player;
    }

    public byte getX() {
        return x;
    }

    public byte getY() {
        return y;
    }

    public boolean isSwap() {
        return swap;
    }


    @Override
    public String toString() {
        if (swap) return "SWAP\n";
        else return x + "," + y + "\n";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Action action = (Action) o;

        if (x != action.x) return false;
        if (y != action.y) return false;
        if (swap != action.swap) return false;
        return player == action.player;
    }
}
