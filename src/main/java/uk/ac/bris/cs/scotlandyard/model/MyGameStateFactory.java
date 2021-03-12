package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

import com.sun.javafx.geom.AreaOp;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Piece.*;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

/**
 * cw-model
 * Stage 1: Complete this class
 */
public final class MyGameStateFactory implements Factory<GameState> {

	private final class MyGameState implements GameState {

		private MyGameState(
				final GameSetup setup,
				final ImmutableSet<Piece> remaining,
				final ImmutableList<LogEntry> log,
				final Player mrX,
				final List<Player> detectives){
			this.setup = setup;
			this.remaining = remaining;
			this.log = log;
			this.mrX = mrX;
			this.detectives = detectives;
		}
		private GameSetup setup;
		private ImmutableSet<Piece> remaining;
		private ImmutableList<LogEntry> log;
		private Player mrX;
		private List<Player> detectives;
		private ImmutableList<Player> everyone;
		private ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;

		@Override public GameSetup getSetup() {  return setup; }
		@Override public ImmutableSet<Piece> getPlayers() { return null; }
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) { return null; }
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) { return null; }
		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return null; }
		@Override public ImmutableSet<Piece> getWinner() { return winner; }
 		@Override public ImmutableSet<Move> getAvailableMoves() { return moves; }
 		@Override public GameState advance(Move move) {  return null;  }
	}
	@Nonnull @Override public GameState build(
			GameSetup setup,
			Player mrX,
			ImmutableList<Player> detectives) {

		//Throws exceptions on illegal inputs//
		//Rounds//
		if(setup.rounds.isEmpty()) throw new IllegalArgumentException("Rounds is empty!");

		//Mr X//
		boolean mrXInDetectives = detectives.stream().anyMatch(Player::isMrX);
		if(Objects.isNull(mrX)) throw new NullPointerException("Mr X cannot be null!");
		if(!mrXInDetectives && !mrX.isMrX()) throw new IllegalArgumentException("No Mr X!");
		if(mrXInDetectives && !mrX.isMrX()) throw new IllegalArgumentException("Mr X must come first!");
		if(mrXInDetectives && mrX.isMrX()) throw new IllegalArgumentException("There can only be one Mr X!");

		//Detectives//
		if(detectives.isEmpty()) throw new IllegalArgumentException("No detectives!");
		if(detectives.stream().anyMatch(detective -> detective.has(ScotlandYard.Ticket.DOUBLE))) throw new IllegalArgumentException("Detectives can't have doubles!");
		if(detectives.stream().anyMatch(detective -> detective.has(ScotlandYard.Ticket.SECRET))) throw new IllegalArgumentException("Detectives can't have secrets!");

		for (int i = 0; i < detectives.size(); i++) {
			for (int j = i+1; j < detectives.size(); j++) {
				if (detectives.get(i).equals(detectives.get(j))) throw new IllegalArgumentException("Duplicate detective alert!");
			}
		}

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}
}
