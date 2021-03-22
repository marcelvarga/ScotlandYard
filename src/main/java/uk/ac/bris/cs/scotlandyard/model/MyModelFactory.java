package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	public MyModelFactory() {
		//Assumed parameters
		final Board.GameState game;
		final ImmutableList<Observer> observers;
	}
	Board.GameState game;
	ImmutableList<Observer> observers;

	public void chooseMove(@Nonnull Move move){
		//MOVE!
		game.advance(move);

		//Figure out whether the game has ended
		Event e;
		if (game.getWinner().isEmpty()) e = Event.GAME_OVER;
		else e = Event.MOVE_MADE;

		//Notify each observer the event
		for(Observer observer : observers) {
			observer.onModelChanged(game, e);
		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {

		//Presumably build a gameState
		//This creates recursive loop, so ALL OBSERVER TESTS FAIL
		Model g = build(setup, mrX, detectives);
		return g;
	}
}
