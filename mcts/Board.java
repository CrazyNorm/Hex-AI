package mcts;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Board {
    // representation of the game board

    private final byte BOARD_SIZE = 11;

    private final Player[][] board;


    public Board() {
        board = new Player[BOARD_SIZE][BOARD_SIZE];
        for (int x = 0; x < BOARD_SIZE; x++)
            for (int y = 0; y < BOARD_SIZE; y++)
                board[x][y] = Player.NONE;
    }

    public Board(Board oldBoard) {
        // copy constructor

        board = new Player[BOARD_SIZE][BOARD_SIZE];
        for (int x = 0; x < BOARD_SIZE; x++)
            for (int y = 0; y < BOARD_SIZE; y++)
                board[x][y] = oldBoard.getCell(x, y);
    }

    public Board(String input) {
        // construct board from protocol string

        this();  // create empty board

        String[] inputLines = input.split(",");

        // for each character, set cell value
        for (int x = 0; x < inputLines.length; x++) {
            inputLines[x] = inputLines[x].strip();
            for (int y = 0; y < inputLines[x].length(); y++) {
                if (inputLines[x].charAt(y) == 'R') board[x][y] = Player.RED;
                else if (inputLines[x].charAt(y) == 'B') board[x][y] = Player.BLUE;
            }
        }
        System.out.println();
    }


    public void applyAction(Action a) {
        if (a.isSwap())
            // swaps the colour of every piece on the board
            for (int x = 0; x < BOARD_SIZE; x++)
                for (int y = 0; y < BOARD_SIZE; y++) {
                    if (board[x][y] == Player.RED) board[x][y] = Player.BLUE;
                    else if (board[x][y] == Player.BLUE) board[x][y] = Player.RED;
                }

        else
            board[a.getX()][a.getY()] = a.getPlayer();
    }

    public Player getCell(int x, int y) {
        return board[x][y];
    }

    public List<Action> getActions(Player p) {
        // gets a list of possible actions for the given player (i.e. empty cells)
        List<Action> actions = new ArrayList<>();

        int occupied = 0;
        for (int x = 0; x < BOARD_SIZE; x++)
            for (int y = 0; y < BOARD_SIZE; y++) {
                if (board[x][y] == Player.NONE)
                    actions.add(new Action(p, x, y));
                else
                    occupied++;
            }

        // if only one position has changed from empty, swap is available
        if (occupied == 1) actions.add(new Action(p, true));

        return actions;
    }

    public Player checkWin() {
        // initialise every tile as unvisited
        boolean[][] visited = new boolean[BOARD_SIZE][BOARD_SIZE];
        for (int x = 0; x < BOARD_SIZE; x++)
            for (int y = 0; y < BOARD_SIZE; y++)
                visited[x][y] = false;

        // for each location on the top row / leftmost column
        for (int i = 0; i < BOARD_SIZE; i++) {
            // check for red wins (top to bottom)
            if (board[0][i] == Player.RED)
                if (winSearch(0, i, Player.RED, visited)) return Player.RED;
            // check for blue wins (left to right)
            if (board[i][0] == Player.BLUE)
                if (winSearch(i, 0, Player.BLUE, visited)) return Player.BLUE;
        }

        return Player.NONE;
    }

    public boolean winSearch(int x, int y, Player p, boolean[][] visited) {
        // mark this space as visited
        visited[x][y] = true;

        // return true if reached the other side (i.e. win)
        if (p == Player.RED && x == BOARD_SIZE - 1) return true;
        if (p == Player.BLUE && y == BOARD_SIZE - 1) return true;

        // visit unvisited matching neighbours
        for (int[] neighbour: getNeighbours(x, y)) {
            // check neighbour is unvisited
            if (!visited[neighbour[0]][neighbour[1]])
                // check neighbour is matching
                if (board[neighbour[0]][neighbour[1]] == p)
                    // if the neighbour leads to a win, return true
                    if (winSearch(neighbour[0], neighbour[1], p, visited)) return true;
        }

        // if no neighbours lead to a win, return false
        return false;
    }

    public List<int[]> getNeighbours(int x, int y) {
        // gets the neighbour co-ords for given location

        List<int[]> neighbours = new ArrayList<>(Arrays.asList(
                new int[]{x, y-1},
                new int[]{x+1, y-1},
                new int[]{x-1, y},
                new int[]{x+1, y},
                new int[]{x-1, y+1},
                new int[]{x, y+1}
        ));

        // remove neighbours which fall off board
        List<int[]> off = new ArrayList<>();
        for (int[] co_ords: neighbours) {
            if (co_ords[0] < 0 || co_ords[0] > BOARD_SIZE - 1)
                off.add(co_ords);
            else if (co_ords[1] < 0 || co_ords[1] > BOARD_SIZE - 1)
                off.add(co_ords);
        }
        neighbours.removeAll(off);

        return neighbours;

    }
}
