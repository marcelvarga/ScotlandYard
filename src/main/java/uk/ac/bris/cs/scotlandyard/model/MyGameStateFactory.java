package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import java.util.*;
import javax.annotation.Nonnull;

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
			this.winner = ImmutableSet.of();

			List<Player> allPlayers = new ArrayList<>(detectives);
			allPlayers.add(mrX);
			this.everyone = ImmutableList.copyOf(allPlayers);
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
		@Override public ImmutableSet<Piece> getPlayers() {
			Set<Piece> players = new HashSet<Piece>();
			for (Player player : everyone)
				players.add(player.piece());
			return ImmutableSet.copyOf(players);
		}
		@Override public Optional<Integer> getDetectiveLocation(Detective detective) {
			for (Player player : detectives)
				if (player.piece().equals(detective)) return Optional.of(player.location());
			return Optional.empty();
			}
		@Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			return null;
		} //TODO
		@Override public ImmutableList<LogEntry> getMrXTravelLog() { return log; }
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

		//Graph//
		if(setup.graph.nodes().isEmpty()) throw new IllegalArgumentException("Graph can't be empty!");

		//Mr X//
		boolean mrXInDetectives = detectives.stream().anyMatch(Player::isMrX);
		if(Objects.isNull(mrX)) throw new NullPointerException("Mr X cannot be null!");
		if(!mrXInDetectives && !mrX.isMrX()) throw new IllegalArgumentException("No Mr X!");

		//Detectives//
		if(detectives.isEmpty()) throw new IllegalArgumentException("No detectives!");
		if(detectives.stream().anyMatch(detective -> detective.has(ScotlandYard.Ticket.DOUBLE))) throw new IllegalArgumentException("Detectives can't have doubles!");
		if(detectives.stream().anyMatch(detective -> detective.has(ScotlandYard.Ticket.SECRET))) throw new IllegalArgumentException("Detectives can't have secrets!");

		for (int i = 0; i < detectives.size() - 1; i++)
			for (int j = i+1; j < detectives.size(); j++) {
				if (detectives.get(i).equals(detectives.get(j)))
					throw new IllegalArgumentException("Duplicate detective alert!");
				if (detectives.get(i).location() == detectives.get(j).location())
					throw new IllegalArgumentException("There can't be two detectives at the same location!");
			}

		return new MyGameState(setup, ImmutableSet.of(MrX.MRX), ImmutableList.of(), mrX, detectives);
	}
}
