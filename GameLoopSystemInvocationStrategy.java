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

/**
 * Implements a game loop based on this excellent blog post:
 * http://gafferongames.com/game-physics/fix-your-timestep/
 */
public class GameLoopSystemInvocationStrategy extends SystemInvocationStrategy {
	private float logicTickTime = 1f / 25; // step is here 1/25, 25 times per second.

	private float accumulator;

	public GameLoopSystemInvocationStrategy() {
	}

	public GameLoopSystemInvocationStrategy(int logicTicksPerSecond) {
		logicTickTime = 1f / logicTicksPerSecond;
	}

	@Override
	protected void process(Bag<BaseSystem> systems) {

		Object[] systemsData = systems.getData();

		float deltaTime = Gdx.graphics.getDeltaTime();

		if (deltaTime > 0.25f) {
			deltaTime = 0.25f; // Note: Avoid spiral of death
		}
		accumulator += deltaTime;

		world.setDelta(logicTickTime);

		while (accumulator >= logicTickTime) {
			/**
			 * processing all entity systems inheriting from {@link LogicRenderEntitySystem}
			 */
			for (int i = 0, s = systems.size(); s > i; i++) {
				BaseSystem system = (BaseSystem) systemsData[i];
				if (system instanceof LogicRenderEntitySystem) {
					system.process();
					updateEntityStates();
				}
			}

			accumulator -= logicTickTime;
		}

		/**
		 * processing all NON {@link LogicRenderEntitySystem} inheriting entity systems
		 */
		world.setDelta(deltaTime);
		for (int i = 0, s = systems.size(); s > i; i++) {
			BaseSystem system = (BaseSystem) systemsData[i];
			if (!(system instanceof LogicRenderEntitySystem)) {
				system.process();
				updateEntityStates();
			}
		}


	}
}

