package uk.ac.bris.cs.scotlandyard.model;

public class DestinationVisitor implements Move.Visitor{

    // True if visitor should return the final destination of a move,
    // false if visitor should return an intermediary destination (e.g. destination1 for DoubleMove, -1 for SingleMove)
    private final boolean finalDestination;

    public DestinationVisitor(boolean finalDestination) {
        this.finalDestination = finalDestination;
    }
    @Override
    public Integer visit(Move.SingleMove singleMove) {
        if (finalDestination)
            return singleMove.destination;
        return -1;
    }

    @Override
    public Integer visit(Move.DoubleMove doubleMove) {
        if(finalDestination)
            return doubleMove.destination2;
        return doubleMove.destination1;
    }
}

