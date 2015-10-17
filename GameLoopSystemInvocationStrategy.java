/*******************************************************************************
 * Copyright 2015 See AUTHORS file.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 ******************************************************************************/


package de.tomgrill.artemis;

import com.artemis.BaseSystem;
import com.artemis.SystemInvocationStrategy;
import com.artemis.utils.Bag;
import com.badlogic.gdx.utils.Array;

import java.util.concurrent.TimeUnit;

import de.tomgrill.snakerino.Globals;

/**
 * Implements a game loop based on this excellent blog post:
 * http://gafferongames.com/game-physics/fix-your-timestep/
 *
 * To avoid floating point rounding errors we only use fixed point numbers for calculations.
 */
public class GameLoopSystemInvocationStrategy extends SystemInvocationStrategy {

	private final Array<BaseSystem> logicMarkedSystems;
	private final Array<BaseSystem> otherSystems;

	private long nanosPerLogicTick; // ~ dt
	private long currentTime = System.nanoTime();

	private long accumulator;

	private boolean systemsSorted = false;

	public GameLoopSystemInvocationStrategy() {
		this(40);
	}

	public GameLoopSystemInvocationStrategy(int millisPerLogicTick) {
		this.nanosPerLogicTick = TimeUnit.MILLISECONDS.toNanos(millisPerLogicTick);
		logicMarkedSystems = new Array<BaseSystem>();
		otherSystems = new Array<BaseSystem>();
	}

	private void sortSystems(Bag<BaseSystem> systems) {
		if (!systemsSorted) {
			Object[] systemsData = systems.getData();
			for (int i = 0, s = systems.size(); s > i; i++) {
				BaseSystem system = (BaseSystem) systemsData[i];
				if (system instanceof LogicRenderMarker) {
					logicMarkedSystems.add(system);
				} else {
					otherSystems.add(system);
				}
			}
			systemsSorted = true;
		}
	}

	@Override
	protected void process(Bag<BaseSystem> systems) {
		if (!systemsSorted) {
			sortSystems(systems);
		}

		long newTime = System.nanoTime();
		long frameTime = newTime - currentTime;

		if (frameTime > 250000000) {
			frameTime = 250000000;    // Note: Avoid spiral of death
		}

		currentTime = newTime;
		accumulator += frameTime;

		/**
		 * Uncomment this line if you use the world's delta within your systems.
		 * I recommend to use a fixed value for your logic delta like millisPerLogicTick or nanosPerLogicTick
		 */
//		world.setDelta(nanosPerLogicTick * 0.000000001f);

		while (accumulator >= nanosPerLogicTick) {
			/** Process all entity systems inheriting from {@link LogicRenderMarker} */
			for (int i = 0; i < logicMarkedSystems.size; i++) {
				/**
				 * Make sure your systems keep the current state before calculating the new state
				 * else you cannot interpolate later on when rendering
				 */
				logicMarkedSystems.get(i).process();
				updateEntityStates();
			}

			accumulator -= nanosPerLogicTick;
		}

		/**
		 * Uncomment this line if you use the world's delta within your systems.
		 */
//		world.setDelta(frameTime * 0.000000001f);

		/**
		 * When you divide accumulator by nanosPerLogicTick you get your alpha.
		 */
//		float alpha = (float) accumulator / nanosPerLogicTick;


		/** Process all NON {@link LogicRenderMarker} inheriting entity systems */
		for (int i = 0; i < otherSystems.size; i++) {
			/**
			 * Use the kept state from the logic above and interpolate with the current state within your render systems.
			 */
			otherSystems.get(i).process();
			updateEntityStates();
		}

	}
}

