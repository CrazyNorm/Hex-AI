import socket
from random import choice
from time import sleep


class MiniMaxAgent():
    """This class describes the MiniMax agent.
    """

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
        """Reads data until it receives an END message or the socket closes."""

        while True:
            data = self.s.recv(1024)
            if not data:
                break
            # print(f"{self.colour} {data.decode('utf-8')}", end="")
            if (self.interpret_data(data)):
                break

        # print(f"Naive agent {self.colour} terminated")

    def interpret_data(self, data):
        """Checks the type of message and responds accordingly. Returns True
        if the game ended, False otherwise.
        """

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
    
    def getNeighbours(self,x,y):
        #returns  a list of the neighbours of the given board index 2 to 6 neighbours possible
        

        pass

    def checkIfFinished(self):
        #Check if either player has won

            #Check if chain of blues from left to right, 

            #Check if chain of reds from top to bottom,

        #If blue wins, return -1, red wins return 1, if no winner yet return 0

        pass

    def evaluate_board(self):
        #First check if there is a winner

        #Number of plays opponent needs to win - Number of plays agent needs to win


        pass


    


    def opp_colour(self):
        """Returns the char representation of the colour opposite to the
        current one.
        """
        if self.colour == "R":
            return "B"
        elif self.colour == "B":
            return "R"
        else:
            return "None"


if (__name__ == "__main__"):
    agent = MiniMaxAgent()
    agent.run()
