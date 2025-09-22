package com.gamewerks.blocky.engine;

import java.lang.Math;
import java.util.ArrayList;

import com.gamewerks.blocky.util.Constants;
import com.gamewerks.blocky.util.Position;

public class BlockyGame {
    private static final int LOCK_DELAY_LIMIT = 30;
    
    private Board board;
    private Piece activePiece;
    private Direction movement;

    private int index = 0;
    private PieceKind[] pieces = new PieceKind[7];

    
    private int lockCounter;
    

    public void randomizePieceOrder() {
        ArrayList<PieceKind> pieceArrayList = new ArrayList<PieceKind>();
        for(int i = 0; i<PieceKind.ALL.length; i++) {
            pieceArrayList.add(PieceKind.ALL[i]);
        }
        PieceKind[] pieceArray = PieceKind.ALL;
        for(int i = 0; i < pieceArrayList.size(); i++){
            int rand = (int) (Math.random()*(7-i));
            pieceArray[i] = pieceArrayList.get(rand);
            pieceArrayList.remove(rand);
        }
        pieces = pieceArray;
    }

    public BlockyGame() {
        new java.util.Random();
        board = new Board();
        movement = Direction.NONE;
        lockCounter = 0;
        randomizePieceOrder();
        trySpawnBlock();
    }
    
    private void trySpawnBlock() {
        if (activePiece == null) {
            if(index == pieces.length-1) {
                randomizePieceOrder();
                index = 0;
            }
            PieceKind p = pieces[index];
            //switch(index):
                
            activePiece = new Piece(p, new Position(3, Constants.BOARD_WIDTH / 2 - 2));
            index++;
            if (board.collides(activePiece)) {
                System.exit(0);
            }
        }
    }
    
    public void processMovement() {
        Position nextPos;
        switch(movement) {
        case NONE:
            nextPos = activePiece.getPosition();
            break;
        case LEFT:
            nextPos = activePiece.getPosition().add(0, -1);
            break;
        case RIGHT:
            nextPos = activePiece.getPosition().add(0, 1);
            break;
        default:
            throw new IllegalStateException("Unrecognized direction: " + movement.name());
        }
        if (!board.collides(activePiece.getLayout(), nextPos)) {
            activePiece.moveTo(nextPos);
        }
    }
    
    private void processGravity() {
        Position nextPos = activePiece.getPosition().add(1, 0);
        if (!board.collides(activePiece.getLayout(), nextPos)) {
            lockCounter = 0;
            activePiece.moveTo(nextPos);
        } else {
            if (lockCounter < LOCK_DELAY_LIMIT) {
                lockCounter += 1;
            } else {
                board.addToWell(activePiece);
                lockCounter = 0;
                activePiece = null;
            }
        }
    }
    
    private void processClearedLines() {
        board.deleteRows(board.getCompletedRows());
    }
    
    public void step() {
        trySpawnBlock();
        processGravity();
        processClearedLines();
    }
    
    public boolean[][] getWell() {
        return board.getWell();
    }
    
    public Piece getActivePiece() { return activePiece; }
    public void setDirection(Direction movement) { this.movement = movement; }
    public void rotatePiece(boolean dir) { activePiece.rotate(dir); }
}
