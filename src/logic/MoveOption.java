package logic;

import model.Position;

public class MoveOption {
    private final Position from;
    private final Position to;

    public MoveOption(Position from, Position to) {
        this.from = from;
        this.to = to;
    }

    public Position getFrom() {
        return from;
    }

    public Position getTo() {
        return to;
    }
}