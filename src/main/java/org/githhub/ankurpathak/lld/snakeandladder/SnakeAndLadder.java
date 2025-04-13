package org.githhub.ankurpathak.lld.snakeandladder;


import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;

import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

@RequiredArgsConstructor
class Player {
    final String name;
    int position = 0;
}

@RequiredArgsConstructor
class Dice {
    final int count;
    final int min;
    final int max;

    int roll(){
        int result = 0;
        for(int i = 0; i < count; i++){
            result += ThreadLocalRandom.current().nextInt(min, max + 1);
        }
        return result;
    }
}

@RequiredArgsConstructor
class Jump {
    final int from;
    final int to;

}

@AllArgsConstructor
class Cell {
    Jump jump;
}

class Grid {
    Cell[][] board;
    public Grid(int m, int n, int snake, int ladder){
        initializeGrid(m, n, snake, ladder);
    }

    private void initializeGrid(int m, int n, int snake, int ladder) {
        board = new Cell[m][n];
        for(int i = 0; i < m; i++){
            for(int j = 0; j < n; j++){
                board[i][j] = new Cell(null);
            }
        }

        int i = 0;
        while(i < snake){
            int from = ThreadLocalRandom.current().nextInt(0, m * n);
            int to = ThreadLocalRandom.current().nextInt(0, m * n);
            if(from > to) {
                getCell(from).jump = new Jump(from, to);
                i++;
            }
        }

        i = 0;
        while(i < ladder){
            int from = ThreadLocalRandom.current().nextInt(0, m * n);
            int to = ThreadLocalRandom.current().nextInt(0, m * n);
            if(from < to) {
                getCell(from).jump = new Jump(from, to);
                i++;
            }
        }
    }

    public Cell getCell(int start){
        int m = board.length;
        int n = board[0].length;
        int row = start / n;
        int col = start % n;
        return board[row][col];
    }

    boolean checkWin(int position){
        int m = board.length;
        int n = board[0].length;
        return position >= m * n - 1;
    }
}

public class SnakeAndLadder {
    final Grid grid;
    final Dice dice;
    final Player[] players;
    final Deque<Player> playersQ;

    SnakeAndLadder(int m, int n, int snake, int ladder, int diceCount, int diceMin, int diceMax, Player... players){
        this.grid = new Grid(m, n, snake, ladder);
        this.dice = new Dice(diceCount, diceMin, diceMax);
        this.players = players;
        playersQ = new LinkedList<>(List.of(players));
    }



    void play(){
        int m = grid.board.length;
        int n = grid.board[0].length;

        Player winner = null;
        while(winner == null){
            Player player = playersQ.pollFirst();
            player.position += dice.roll();

            if(player.position >= 0 && player.position < m * n){
                Cell cell = grid.getCell(player.position);
                if(cell.jump != null){
                    player.position = cell.jump.to;
                }
            }

            if(grid.checkWin(player.position)){
                System.out.println(player.name + " wins");
                winner = player;
            }

            playersQ.offerLast(player);
        }
    }

    public static void main(String[] args) {
        Player player1 = new Player("Player 1");
        Player player2 = new Player("Player 2");
        SnakeAndLadder snakeAndLadder = new SnakeAndLadder(10, 10, 10, 10, 1, 1, 6, player1, player2);
        snakeAndLadder.play();
    }


}
