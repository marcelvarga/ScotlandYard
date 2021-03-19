package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;
import uk.ac.bris.cs.scotlandyard.model.Piece.Detective;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;

import javax.annotation.Nonnull;
import java.util.*;
import java.util.stream.Collectors;

import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.TAXI;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.BUS;
import static uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Ticket.UNDERGROUND;

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
			this.currentRound = log.size();
			this.maximumRounds = setup.rounds.size();

			List<Player> allPlayers = new ArrayList<>(detectives);
			allPlayers.add(mrX);
			this.everyone = ImmutableList.copyOf(allPlayers);

			for (Player p : everyone)
				if (p.piece() == remaining.iterator().next()) currentPlayer = p;

			this.winner = getWinner();

			//Moves are not generated if there's a winner
			if (winner.isEmpty()) {
				this.moves = getAvailableMoves();
			} else {
				this.moves = ImmutableSet.copyOf(Collections.emptyList());
			}
		}

		private final GameSetup setup;
		private final ImmutableSet<Piece> remaining;
		private final ImmutableList<LogEntry> log;
		private Player mrX;
		private final List<Player> detectives;
		private final ImmutableList<Player> everyone;
		private final ImmutableSet<Move> moves;
		private ImmutableSet<Piece> winner;
		private Player currentPlayer;
		private int currentRound; // Number of the current round
		private final int maximumRounds; // Maximum number of rounds

		@Nonnull @Override public GameSetup getSetup() {  return setup; }
		@Nonnull @Override public ImmutableSet<Piece> getPlayers() {
			Set<Piece> players = new HashSet<>();
			for (Player player : everyone)
				players.add(player.piece());
			return ImmutableSet.copyOf(players);
		}
		@Nonnull @Override public Optional<Integer> getDetectiveLocation(Detective detective) {
			for (Player player : detectives)
				if (player.piece().equals(detective)) return Optional.of(player.location());
			return Optional.empty();
		}
		@Nonnull @Override public Optional<TicketBoard> getPlayerTickets(Piece piece) {
			for(Player player : everyone)
				if(player.piece() == piece)
					return Optional.of(ticket -> player.tickets().get(ticket));
			return Optional.empty();
		}
		@Nonnull @Override public ImmutableList<LogEntry> getMrXTravelLog() { return log; }

		@Nonnull @Override public ImmutableSet<Piece> getWinner() {
			ArrayList<Piece> winners = new ArrayList<>();

			if (moves != null) System.out.println(moves.toString());
			else System.out.println("NULL MOVES");

			//Detectives win if:
			if (
				//A detective is on the same space as MrX
				detectives.stream().anyMatch(d -> d.location() == mrX.location())

				//MrX cannot move
				|| ((moves != null) && moves.equals(ImmutableSet.of()))

				) winners.addAll(detectives.stream().map(Player::piece).collect(Collectors.toList()));

			//MrX wins if:
			else if (
				//All rounds are completed
				(currentRound == maximumRounds + 1)

				//Detectives are stuck by having no tickets
				|| detectives.stream().noneMatch(d -> d.has(TAXI) || d.has(UNDERGROUND) || d.has(BUS))

				) winners.add(mrX.piece());

			return ImmutableSet.copyOf(winners);
		}

		@Nonnull @Override public ImmutableSet<Move> getAvailableMoves() {

			//Don't get available moves if there's a winner!
			if (!winner.isEmpty()) return ImmutableSet.copyOf(Collections.emptyList());

			ArrayList<Move.SingleMove> singleMoves = new ArrayList<>();
			ArrayList<Move.DoubleMove> doubleMoves = new ArrayList<>();

			for(Player player : everyone)
				if (remaining.contains(player.piece())) {
					singleMoves.addAll(makeSingleMoves(setup, detectives, player, player.location()));

					// If player is mrX, he has a DOUBLE ticket and has enough rounds left in order to perform a double move: find doubleMoves
					if(player.isMrX()
							&& player.has(ScotlandYard.Ticket.DOUBLE)
							&& currentRound < maximumRounds - 1)
						doubleMoves.addAll(makeDoubleMoves(setup, detectives, currentPlayer, currentPlayer.location()));
				}

			// Merge singleMoves and doubleMoves into a list having "MOVE" elements
			ArrayList<Move> possibleMoves = new ArrayList<>();
			possibleMoves.addAll(singleMoves);
			possibleMoves.addAll(doubleMoves);

			return ImmutableSet.copyOf(possibleMoves);
		}

 		@Nonnull @Override public GameState advance(Move move) {
			if(!moves.contains(move)) throw new IllegalArgumentException("Illegal move: "+move);
			for(Player player : everyone) if(player.piece() == move.commencedBy()) currentPlayer = player;

			//Get destination of the move
			int lastDestination = move.visit(new DestinationVisitor(true));
			int intermediaryDestination = move.visit(new DestinationVisitor(false));

			// Get move tickets

			ScotlandYard.Ticket firstTicket = move.visit(new TicketVisitor(true));
			ScotlandYard.Ticket secondTicket = move.visit(new TicketVisitor(false));

			// Current player uses tickets
			// If he's a detective, mrX will get those tickets
			for (ScotlandYard.Ticket ticket : move.tickets()) {
				currentPlayer = currentPlayer.use(ticket);
				if(currentPlayer.isDetective()) mrX = mrX.give(ticket);
			}

			// Current player is moved at destination
			currentPlayer = currentPlayer.at(lastDestination);

			// Update the set of the remaining players
			List<Piece> newRemaining = new ArrayList<>(remaining);

			// If the set is made out of mrX only (first round of the game), add only the detectives
			if (newRemaining.contains(mrX.piece()) && newRemaining.size() == 1) {
				for (Player detective : detectives)
					newRemaining.add(detective.piece());
			}

			// Remove currentPlayer from the "remaining" list as he's making his move now
			newRemaining.remove(currentPlayer.piece());

			// If the remaining set is empty, we're at a new round: add mrX to the remaining Set
			if (newRemaining.isEmpty())
				newRemaining.add(mrX.piece());

			// Return new GameState with updated mrX position and remaining players
			// Add log entries
			if(currentPlayer.piece() == mrX.piece()){
				// Create a new log that will be passed to the MyGameState constructor
				ArrayList<LogEntry> newLog = new ArrayList<>(log);

				// The Move is a SingleMove
				if(secondTicket == null)
					if(setup.rounds.get(currentRound))
						newLog.add(LogEntry.reveal(firstTicket, lastDestination));
					else newLog.add(LogEntry.hidden(firstTicket));

				// The Move is a DoubleMove
				else {
					if(setup.rounds.get(currentRound))
						newLog.add(LogEntry.reveal(firstTicket, intermediaryDestination));
					else newLog.add(LogEntry.hidden(firstTicket));
					currentRound++;

					if(setup.rounds.get(currentRound))
						newLog.add(LogEntry.reveal(secondTicket, lastDestination));
					else newLog.add(LogEntry.hidden(secondTicket));
				}
				return new MyGameState(setup, ImmutableSet.copyOf(newRemaining), ImmutableList.copyOf(newLog), currentPlayer, detectives);
			}

			// Return new GameState
			// List of detectives is updated to match the position of the player that has moved
			else{
				List<Player> newDetectives = new ArrayList<>();
				for(Player detective : detectives)
					if(detective.piece() == currentPlayer.piece()) newDetectives.add(currentPlayer);
					else newDetectives.add(detective);
				return new MyGameState(setup, ImmutableSet.copyOf(newRemaining), log, mrX, ImmutableList.copyOf(newDetectives));
			}
		}

		private ArrayList<LogEntry> updateLog(
				ImmutableList<LogEntry> log,
				ImmutableList<Boolean> rounds,
				int currentRound,
				Move move,
				int intermediaryDestination,
				int lastDestination) {
			ArrayList<LogEntry> newLog= new ArrayList<>(log);

			int ticketsCount = Iterables.size(move.tickets());
			// The Move is a SingleMove
			if(ticketsCount == 1)
				if(setup.rounds.get(currentRound))
					newLog.add(LogEntry.reveal(move.tickets().iterator().next(), lastDestination));
				else newLog.add(LogEntry.hidden(move.tickets().iterator().next()));

				// The Move is a DoubleMove
			else {
				if(setup.rounds.get(currentRound))
					newLog.add(LogEntry.reveal(move.tickets().iterator().next(), intermediaryDestination));
				else newLog.add(LogEntry.hidden(move.tickets().iterator().next()));
				currentRound++;

				if(setup.rounds.get(currentRound))
					newLog.add(LogEntry.reveal(move.tickets().iterator().next(), lastDestination));
				else newLog.add(LogEntry.hidden(move.tickets().iterator().next()));
			}
			return newLog;
		}

	}


	private static ImmutableSet<Move.SingleMove> makeSingleMoves(
			GameSetup setup,
			List<Player> detectives,
			Player player,
			int source){
		List<Move.SingleMove> singleMoves = new ArrayList<>();
		for (int destination : setup.graph.adjacentNodes(source)) {

			// You cannot move onto a detective
			if (detectives.stream().anyMatch(d -> d.location() == destination)) continue;

			// You must have the required ticket
			for (ScotlandYard.Transport transport : Objects.requireNonNull(setup.graph.edgeValueOrDefault(source, destination, ImmutableSet.of()))) {
				if (player.has(transport.requiredTicket()))  //construct SingleMove and add to the list of moves to return
					singleMoves.add(new Move.SingleMove(player.piece(), player.location(), transport.requiredTicket(), destination));
				if (player.has(ScotlandYard.Ticket.SECRET))
					singleMoves.add(new Move.SingleMove(player.piece(), player.location(), ScotlandYard.Ticket.SECRET, destination));
			}
		}
		return ImmutableSet.copyOf(singleMoves);
	}

	private static ImmutableSet<Move.DoubleMove> makeDoubleMoves(
			GameSetup setup,
			List<Player> detectives,
			Player player,
			int source){
		final var doubleMoves = new ArrayList<Move.DoubleMove>();

		ImmutableSet<Move.SingleMove> singleMoves = makeSingleMoves(setup, detectives, player, source);

		for (Move.SingleMove singleMove1: singleMoves) {

			ImmutableSet<Move.SingleMove> MoreSingleMoves = makeSingleMoves(setup, detectives, player.use(singleMove1.ticket), singleMove1.destination);

			for (Move.SingleMove singleMove2 : MoreSingleMoves) {
				doubleMoves.add(new Move.DoubleMove(player.piece(), source, singleMove1.ticket, singleMove1.destination, singleMove2.ticket, singleMove2.destination));
			}
		}

		return ImmutableSet.copyOf(doubleMoves);
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
		if(Objects.isNull(mrX)) throw new NullPointerException("Mr X cannot be null!");
		if(detectives.stream().noneMatch(Player::isMrX) && !mrX.isMrX()) throw new IllegalArgumentException("No Mr X!");

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

		return new MyGameState(setup, ImmutableSet.of(mrX.piece()), ImmutableList.of(), mrX, detectives);
	}
}
