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
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.utils.Array;

/**
 * Implements a game loop based on this excellent blog post:
 * http://gafferongames.com/game-physics/fix-your-timestep/
 */
public class GameLoopSystemInvocationStrategy extends SystemInvocationStrategy {
	private float logicTickTime; // step is here 1/25, 25 times per second.

	private float accumulator;

	private Array<BaseSystem> logicMarkedSystems;
	private Array<BaseSystem> otherSystems;


	private boolean systemsSorted = false;

	public GameLoopSystemInvocationStrategy() {
		this(25);
	}

	public GameLoopSystemInvocationStrategy(int logicTicksPerSecond) {
		logicTickTime = 1f / logicTicksPerSecond;

		logicMarkedSystems = new Array<BaseSystem>();
		otherSystems = new Array<BaseSystem>();
	}


	@Override
	protected void process(Bag<BaseSystem> systems) {
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


		float deltaTime = Gdx.graphics.getDeltaTime();

		if (deltaTime > 0.25f) {
			deltaTime = 0.25f; // Note: Avoid spiral of death
		}
		accumulator += deltaTime;

		world.setDelta(logicTickTime);

		while (accumulator >= logicTickTime) {
			/**
			 * processing all entity systems inheriting from {@link LogicRenderMarker}
			 */
			for (int i = 0; i < logicMarkedSystems.size; i++) {
				logicMarkedSystems.get(i).process();
				updateEntityStates();
			}

			accumulator -= logicTickTime;
		}

		/**
		 * processing all NON {@link LogicRenderMarker} inheriting entity systems
		 */
		world.setDelta(deltaTime);
		for (int i = 0; i < otherSystems.size; i++) {
			otherSystems.get(i).process();
			updateEntityStates();
		}

	}
}

