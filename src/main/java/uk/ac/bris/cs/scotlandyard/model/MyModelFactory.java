package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

import java.util.ArrayList;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	private final class MyModel implements Model{

		GameState gameState;
		ArrayList<Observer> observers = new ArrayList<>();

		// Constructor
		private MyModel(GameState gameState){
			this.gameState = gameState;
		}

		@Nonnull @Override
		public Board getCurrentBoard() {
			return gameState;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {
			if(observer == null) throw new NullPointerException("Observer is null!");
			if(observers.contains(observer)) throw new IllegalArgumentException("Can't have the same spectator twice!");
			observers.add(observer);
		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {
			if(observer == null) throw new NullPointerException("Observer is null!");
			if(!observers.contains(observer)) throw new IllegalArgumentException("Observer not found!");
			observers.remove(observer);
		}

		@Nonnull @Override
		public ImmutableSet<Observer> getObservers() {
			return ImmutableSet.copyOf(observers);
		}

		@Override
		public void chooseMove(@Nonnull Move move) {
			//Make the move
			gameState = gameState.advance(move);

			//Figure out whether the game has ended
			Event e;
			if (!gameState.getWinner().isEmpty()) e = Event.GAME_OVER;
			else e = Event.MOVE_MADE;

			//Notify each observer the event
			for(Observer observer : observers) {
				observer.onModelChanged(gameState, e);
			}
		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {

		return new MyModel(new MyGameStateFactory().build(setup, mrX, detectives));
	}
}
