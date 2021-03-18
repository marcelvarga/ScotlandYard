package uk.ac.bris.cs.scotlandyard.model;

public class TicketVisitor implements Move.Visitor<ScotlandYard.Ticket>{

    // True if visitor should return the first ticket of a move,
    // false if visitor should return the second ticket (e.g. ticket2 for DoubleMove, null for SingleMove)
    private final boolean firstTicket;

    public TicketVisitor(boolean firstTicket) {
        this.firstTicket = firstTicket;
    }

    @Override
    public ScotlandYard.Ticket visit(Move.SingleMove singleMove) {
        if (firstTicket)
            return singleMove.ticket;
        return null;
    }

    @Override
    public ScotlandYard.Ticket visit(Move.DoubleMove doubleMove) {
        if(firstTicket)
            return doubleMove.ticket1;
        return doubleMove.ticket2;
    }
}

