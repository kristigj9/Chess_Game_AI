package model.pieces;

import model.Board;
import model.PlayerColor;
import model.Position;

import java.util.List;

public abstract class Piece {
    protected PlayerColor color;
    protected Position position;
    protected boolean moved;

    public Piece(PlayerColor color, Position position) {
        this.color = color;
        this.position = position;
        this.moved = false;
    }

    public PlayerColor getColor() {
        return color;
    }

    public Position getPosition() {
        return position;
    }

    public void setPosition(Position position) {
        this.position = position;
    }

    public boolean hasMoved() {
        return moved;
    }

    public void setMoved(boolean moved) {
        this.moved = moved;
    }

    public abstract String getSymbol();

    public abstract List<Position> getPossibleMoves(Board board);
}