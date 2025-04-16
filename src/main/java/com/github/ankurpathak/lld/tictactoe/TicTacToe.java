package com.github.ankurpathak.lld.tictactoe;

import java.util.*;

enum PieceType {
    NOT(0, '0'), CROSS(1, 'X'), EMPTY(-1, '_');
    public final int piece;
    public final char symbol;
    PieceType(int piece, char symbol){
        this.piece = piece;
        this.symbol = symbol;
    }

    static PieceType piece(int piece){
        return switch (piece) {
            case 0 -> NOT;
            case 1 -> CROSS;
            default -> EMPTY;
        };
    }

}

abstract class Piece {
    PieceType type;
    Piece(PieceType type) {
        this.type = type;
    }
}

class CrossPiece extends Piece {
    CrossPiece() {
        super(PieceType.CROSS);
    }
}

class NotPiece extends Piece {
    NotPiece() {
        super(PieceType.NOT);
    }
}

class NullPiece extends Piece {
    NullPiece() {
        super(PieceType.EMPTY);
    }
}


record Player(String name, Piece piece) { }

class Grid {

    int n;
    int [][] board;
    int [][] rows;
    int [][] cols;
    int [] diag;
    int [] antiDiag;
    int placed;

    public Grid(int n){
        this.n = n;
        board = new int[n][n];
        rows = new int[2][n];
        cols = new int[2][n];
        diag = new int[2];
        antiDiag = new int[2];
        placed = 0;
        for(int i = 0; i < n; i++){
            Arrays.fill(board[i], -1);
        }
    }

    void display(){
        for(int i = 0; i < n; i++){
            for(int j = 0; j < n; j++){
                System.out.print( PieceType.piece(board[i][j]).symbol + " ");
            }
            System.out.println();
        }
        System.out.println();
    }





    boolean putPiece(int piece, int row, int col){
        if (row < 0 || row >= n || col < 0 || col >= n) {
            return false;
        }
        if(board[row][col] != -1){
            return false;
        }

        board[row][col] = piece;
        rows[piece][row]++;
        cols[piece][col]++;
        if(row == col){
            diag[piece]++;
        }

        if(row + col == n - 1){
            antiDiag[piece]++;
        }

        placed++;

        return true;
    }

    int checkWin(int row, int col){
        int piece = board[row][col];
        if(rows[piece][row] == n || cols[piece][col] == n || diag[piece] == n || antiDiag[piece] == n){
            return piece;
        }

        if(placed == n * n){
            return 2;
        }

        return -1;
    }
}


class TicTacToe {
    Player[] players;
    Queue<Player> q;
    Grid grid;

    TicTacToe(int size, Player not, Player cross){
        reset(size, not, cross);
    }

    void reset(int size, Player not, Player cross){
        grid = new Grid(size);
        players = new Player[2];
        players[not.piece().type.piece] =  not;
        players[cross.piece().type.piece] =  cross;
        q = new LinkedList<>(Arrays.asList(players));
    }

    int play(){
        int result = -1;
        while(result == -1){
            Player player =  q.poll();

            Random random = new Random();
            boolean placed = false;
            int row = -1;
            int col = -1;

            do{
                row = random.nextInt(0, grid.n);
                col = random.nextInt(0, grid.n);
                placed = grid.putPiece(player.piece().type.piece, row, col);
                if(placed){
                    grid.display();
                }
            }while (!placed);

           result = grid.checkWin(row, col);
           if(result == -1){
                q.offer(player);
           }


        }

        return result;
    }


    public static void main(String[] args) {
        Player not = new Player("Trump", new NotPiece());
        Player cross = new Player("Alon", new CrossPiece());
        TicTacToe ticTacToe = new TicTacToe(3, not, cross);
        int result = ticTacToe.play();
        System.out.println(result == 2 ? "Draw" : "Player " + (result == 0 ? ticTacToe.players[0].name() : ticTacToe.players[1].name())  + " wins");
    }

}

