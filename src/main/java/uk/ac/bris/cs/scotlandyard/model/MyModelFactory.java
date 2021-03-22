package uk.ac.bris.cs.scotlandyard.model;

import com.google.common.collect.ImmutableList;

import javax.annotation.Nonnull;

import com.google.common.collect.ImmutableSet;
import uk.ac.bris.cs.scotlandyard.model.ScotlandYard.Factory;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer;
import uk.ac.bris.cs.scotlandyard.model.Model.Observer.Event;
import uk.ac.bris.cs.scotlandyard.model.Board.GameState;

/**
 * cw-model
 * Stage 2: Complete this class
 */
public final class MyModelFactory implements Factory<Model> {

	private final class MyModel implements Model{

		// Constructor
		//Mymodel(){}
		// Attributes


		//

		@Nonnull
		@Override
		public Board getCurrentBoard() {
			return null;
		}

		@Override
		public void registerObserver(@Nonnull Observer observer) {

		}

		@Override
		public void unregisterObserver(@Nonnull Observer observer) {

		}

		@Nonnull
		@Override
		public ImmutableSet<Observer> getObservers() {
			return null;
		}

		@Override
		public void chooseMove(@Nonnull Move move) {

		}
	}

	@Nonnull @Override public Model build(GameSetup setup,
										  Player mrX,
										  ImmutableList<Player> detectives) {

		//return new MyModel(...);
		return null;
	}
}
