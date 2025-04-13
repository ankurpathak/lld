package org.githhub.ankurpathak.lld.connectfour;



import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Random;

enum PieceType {
    RED(0, 'R'), YELLOW(1, 'Y'), EMPTY(-1, '_');
    final int piece;
    final char symbol;
    PieceType(int piece, char symbol){
        this.piece = piece;
        this.symbol = symbol;
    }

    static PieceType piece(int x){
        return switch (x) {
            case 0 -> RED;
            case 1 -> YELLOW;
            default -> EMPTY;
        };
    }
}

abstract class Piece {
    final PieceType type;
    Piece(PieceType type) {
        this.type = type;
    }
}

class RedPiece extends Piece {
    RedPiece() {
        super(PieceType.RED);
    }
}

class YellowPiece extends Piece {
    YellowPiece() {
        super(PieceType.YELLOW);
    }
}

class Player {
    final String name;
    final Piece piece;
    Player(String name, Piece piece) {
        this.name = name;
        this.piece = piece;
    }

}

class Grid {
    final int m;
    final int n;
    final int[][] board;
    int placed;
    Grid(int m, int n) {
        this.m = m;
        this.n = n;
        this.board = new int[m][n];
        for (int i = 0; i < m; i++) {
            Arrays.fill(board[i], -1);
        }
        placed = 0;
    }

    int place(int piece, int col) {
        if(col < 0 || col >= n) {
            return -1;
        }
        int i = m - 1;
        while(i >= 0 && board[i][col] != -1) i--;

        if(i < 0) {
            return -1;
        }

        board[i][col] = piece;
        return i;
    }

    void display(){
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                System.out.print( PieceType.piece(board[i][j]).symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    int checkWin(int row, int col) {
        int piece = board[row][col];
        if (piece == -1) {
            return -1;
        }
        int count = 0;
        for(int i = row; i >= 0; i--){
            if(board[i][col] == piece){
                count++;
            }else{
                break;
            }
        }

        if(count >= 4) {
            return piece;
        }

        int[][] dirs = new int[][]{
            {0, 1}, // horizontal
            {1, 1}, // diagonal \
            {1, -1} // diagonal /
        };

        for(int[] dir : dirs) {
            count = 1;
            int dx = dir[0];
            int dy = dir[1];

            // Check in the positive direction
            for (int i = 1; i < 4; i++) {
                int newX = row + i * dx;
                int newY = col + i * dy;
                if (newX < 0 || newX >= m || newY < 0 || newY >= n || board[newX][newY] != piece) {
                    break;
                }
                count++;
            }

            // Check in the negative direction
            for (int i = 1; i < 4; i++) {
                int newX = row - i * dx;
                int newY = col - i * dy;
                if (newX < 0 || newX >= m || newY < 0 || newY >= n || board[newX][newY] != piece) {
                    break;
                }
                count++;
            }

            if (count >= 4) {
                return piece;
            }
        }

        if(placed == m * n) {
            return 2; // Draw
        }


        return -1;
    }
}

class ConnectFour {
    Grid grid;
    Player[] players;
    Queue<Player> q;
    ConnectFour(int rows, int cols, Player red, Player yellow) {
        reset(rows, cols, red, yellow);
    }

    void reset(int rows, int cols, Player red, Player yellow){
        grid = new Grid(rows, cols);
        players = new Player[]{red, yellow};
        q = new LinkedList<>();
        q.offer(red);
        q.offer(yellow);
    }


    int play(){
        int result = -1;
        while(result == -1){
            Player player =  q.poll();

            Random random = new Random();
            int placed = -1;
            int row = -1;
            int col = -1;
            do{
                col = random.nextInt(0, grid.n);
                placed = grid.place(player.piece.type.piece, col);
                if(placed != -1){
                    grid.display();
                    row = placed;
                }
            }while (placed == -1);

            result = grid.checkWin(row, col);
            if(result == -1){
                q.offer(player);
            }


        }

        return result;
    }

    public static void main(String[] args) {
        Player red = new Player("Trump", new RedPiece());
        Player yellow = new Player("Alon", new YellowPiece());
        ConnectFour connectFour = new ConnectFour(6, 7, red, yellow);
        int result = connectFour.play();
        System.out.println(result == 2 ? "Draw" : "Player " + (result == 0 ? connectFour.players[0].name : connectFour.players[1].name)  + " wins");
    }
}


