import socket
from random import choice
from time import sleep
from Board import Board
from Tile import Tile


class MiniMaxAgent():
    
    HOST = "127.0.0.1"
    PORT = 1234

    def __init__(self, board_size=11):
        self.s = socket.socket(
            socket.AF_INET, socket.SOCK_STREAM
        )

        self.s.connect((self.HOST, self.PORT))

        self.board_size = board_size
        self.board = []
        self.colour = ""
        self.turn_count = 0

    def run(self):
        
        while True:
            data = self.s.recv(1024)
            if not data:
                break
            # print(f"{self.colour} {data.decode('utf-8')}", end="")
            if (self.interpret_data(data)):
                break

        # print(f"Naive agent {self.colour} terminated")

    def interpret_data(self, data):
        
        messages = data.decode("utf-8").strip().split("\n")
        messages = [x.split(";") for x in messages]
        # print(messages)
        for s in messages:
            if s[0] == "START":
                self.board_size = int(s[1])
                self.colour = s[2]
                self.board = [
                    [0]*self.board_size for i in range(self.board_size)]

                if self.colour == "R":
                    self.make_move()

            elif s[0] == "END":
                return True

            elif s[0] == "CHANGE":
                if s[3] == "END":
                    return True

                elif s[1] == "SWAP":
                    self.colour = self.opp_colour()
                    if s[3] == self.colour:
                        self.make_move()

                elif s[3] == self.colour:
                    action = [int(x) for x in s[1].split(",")]
                    self.board[action[0]][action[1]] = self.opp_colour()

                    self.make_move()

        return False

    def make_move(self):
        if self.colour == "B" and self.turn_count == 0:
            if choice([0, 1]) == 1:
                self.s.sendall(bytes("SWAP\n", "utf-8"))
            else:
                move = self.minimax()
                self.s.sendall(bytes(f"{move[0]},{move[1]}\n", "utf-8"))
                self.board[move[0]][move[1]] = self.colour
        else:
            move = self.minimax()
            self.s.sendall(bytes(f"{move[0]},{move[1]}\n", "utf-8"))
            self.board[move[0]][move[1]] = self.colour
        self.turn_count += 1

    def minimax(self):
        #initialise the best score and move variables
        best_score = float('-inf')
        best_move = None

        #for every square in the board
        for i in range(self.board_size):
            for j in range(self.board_size):
                #if possible to make move 
                if self.board[i][j] == 0:
                    #make move, evaluate score
                    self.board[i][j] = self.colour
                    score = self.min_value(float('-inf'), float('inf'), depth=2)
                    #reset board for next iteration
                    self.board[i][j] = 0  

                    #if calculated score is best yet, update variables
                    if score > best_score:
                        best_score = score
                        best_move = (i, j)

        return best_move
    
    def max_value(self, alpha, beta, depth):
        #if reached given depth, use heuristic function
        if depth == 0:
            return self.evaluate_board()

        #initialise max score var
        max_score = float('-inf')

        #traverse tree 
        for i in range(self.board_size):
            for j in range(self.board_size):
                if self.board[i][j] == 0:
                    self.board[i][j] = self.colour
                    score = self.min_value(alpha, beta, depth - 1)
                    self.board[i][j] = 0
                    max_score = max(max_score, score)
                    alpha = max(alpha, score)
                    if beta <= alpha:
                        break
        return max_score
    
    def min_value(self, alpha, beta, depth):
        #if reached given depth, use heuristic function
        if depth == 0:
            return self.evaluate_board()

        #initialise max score var
        min_score = float('inf')

        #traverse tree 
        for i in range(self.board_size):
            for j in range(self.board_size):
                if self.board[i][j] == 0:
                    self.board[i][j] = self.opp_colour()
                    score = self.max_value(alpha, beta, depth - 1)
                    self.board[i][j] = 0
                    min_score = min(min_score, score)
                    beta = min(beta, score)
                    if beta <= alpha:
                        break
        return min_score
    
    def getNeighbours(self, x, y):
        
        neighbours = []

        for i in range(Tile.NEIGHBOUR_COUNT):
            x_n = x + Tile.I_DISPLACEMENTS[i]
            y_n = y + Tile.J_DISPLACEMENTS[i]

            #check if the calculated coordinates are within the bounds of the board
            if 0 <= x_n < self.board_size and 0 <= y_n < self.board_size:
                neighbours.append((x_n, y_n))

        return neighbours

    def checkIfFinished(self):
        
        #check if chain of blues from left to right
        for i in range(self.board_size):
            if self.board[0][i] == "B":
                visited = set()
                if self.DFS_check(0, i, "B", visited):
                    return -1  # Blue wins

        #check if chain of reds from top to bottom
        for i in range(self.board_size):
            if self.board[i][0] == "R":
                visited = set()
                if self.DFS_check(i, 0, "R", visited):
                    return 1  # Red wins

        return 0  #no winner yet
    
    def DFS_check(self, x, y, color, visited):

        visited.add((x, y))

        #if the chain reaches the opposite side, return True
        if (color == "B" and y == self.board_size - 1) or (color == "R" and x == self.board_size - 1):
            return True

        #visit neighbors
        for nx, ny in self.getNeighbours(x, y):
            if (nx, ny) not in visited and self.board[nx][ny] == color:
                if self.DFS_check(nx, ny, color, visited):
                    return True

        return False

    def evaluate_board(self):
    
        #check if there is already a winner
        winner = self.checkIfFinished()
        if winner != 0:
            return winner * float('inf') 

        #number of plays opponent needs to win - Number of plays agent needs to win
        num_plays_opponent = self.count_plays_to_win(self.opp_colour())
        num_plays_agent = self.count_plays_to_win(self.colour)

        return num_plays_opponent - num_plays_agent


    def count_plays_to_win(self, color):
        target_row = self.board_size - 1 if color == "R" else None
        target_col = self.board_size - 1 if color == "B" else None

        plays_needed = 0

        for i in range(self.board_size):
            for j in range(self.board_size):
                if self.board[i][j] == color:
                    #calculate the minimum distance to the target row or column
                    distance_to_target = min(abs(i - target_row), abs(j - target_col))
                    plays_needed = max(plays_needed, distance_to_target)

        return plays_needed



    def opp_colour(self):
        
        if self.colour == "R":
            return "B"
        elif self.colour == "B":
            return "R"
        else:
            return "None"


if (__name__ == "__main__"):
    agent = MiniMaxAgent()
    agent.run()
