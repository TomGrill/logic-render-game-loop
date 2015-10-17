# logic-render-game-loop

artemis-odb SystemInvocationStrategy to seperate game logic and rendering within the game loop.

### Requires:
https://github.com/junkdog/artemis-odb

https://github.com/libgdx/libgdx


###Usage:
Register `GameLoopSystemInvocationStrategy` with your `WorldConfiguration`


```java 
WorldConfiguration config = new WorldConfigurationBuilder()
.dependsOn(MyPlugin.class)
.with(
		new MySystemA(),
		new MySystemB(),
		new MySystemC(),
)
.register(new GameLoopSystemInvocationStrategy(40))  // millis per logic tick. default: 40 ~ 25ticks/second 
.build();
```

Mark all EntitySystem for processing within the logic part of the game loop like this:
```java 
public class MySuperLogicSystem extends EntityProcessingSystem implements LogicRenderEntitySystem{
	
	public MySuperLogicSystem(Aspect.Builder aspect) {
		super(aspect);
	}

	@Override
	protected void process(Entity e) {
		System.out.println("I will only processed when it's logic time.");
	}
}
```

### License
The logic-render-game-loop
 project is licensed under the Apache 2 License, meaning you can use it free of charge, without strings attached in commercial and non-commercial projects. We love to get (non-mandatory) credit in case you release a game or app using gdx-facebook!
