package com.alec.lander.views;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Screen;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer.ShapeType;
import com.badlogic.gdx.math.Interpolation;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.List;
import com.badlogic.gdx.scenes.scene2d.ui.ScrollPane;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.Table;
import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;

public class InterpolationExample implements Screen {

	private Stage stage;
	private Skin skin;
	private Table table;
	private String[] interpolationNames = new String[] {"bounce", "bounceIn", "bounceOut", "circle", "circleIn", "circleOut", "elastic", "elasticIn", "elasticOut", "exp10", "exp10In", "exp10Out", "exp5", "exp5In", "exp5Out", "fade", "linear", "pow2", "pow2In", "pow2Out", "pow3", "pow3In", "pow3Out", "pow4", "pow4In", "pow4Out", "pow5", "pow5In", "pow5Out", "sine", "sineIn", "sineOut", "swing", "swingIn", "swingOut"};
	private String selectedInterpolation = interpolationNames[0];

	private ShapeRenderer renderer;
	private float graphSize = 400, steps = graphSize / 2, time = 0, duration = 2.5f;

	private Vector2 startPosition = new Vector2(), targetPosition = new Vector2(), position = new Vector2();

	private void resetPositions() {
		startPosition.set(stage.getWidth() - stage.getWidth() / 5f, stage.getHeight() - stage.getHeight() / 5f);
		targetPosition.set(stage.getWidth() - stage.getWidth() / 5f, stage.getHeight() / 5f);
	}

	private void updatePosition(float time) {
		position.set(targetPosition);
		position.sub(startPosition);
		position.scl(getInterpolation().apply(time / duration));
		position.add(startPosition);
	}

	private Interpolation getInterpolation() {
		try {
			return (Interpolation) Interpolation.class.getField(selectedInterpolation).get(null);
		} catch(IllegalArgumentException e) {
			e.printStackTrace();
		} catch(IllegalAccessException e) {
			e.printStackTrace();
		} catch(NoSuchFieldException e) {
			e.printStackTrace();
		} catch(SecurityException e) {
			e.printStackTrace();
		}
		return Interpolation.linear;
	}

	@Override
	public void show() {
		Gdx.input.setInputProcessor(stage = new Stage());
		resetPositions();
		table = new Table();
		table.setFillParent(true);

		skin = new Skin(Gdx.files.internal("data/ui/menuSkin.json"));

		final List list = new List(interpolationNames, skin);
		list.addListener(new ChangeListener() {
			@Override
			public void changed(ChangeEvent event, Actor actor) {
				selectedInterpolation = list.getSelection();
			}

		});

		table.add(new ScrollPane(list, skin)).expand().fillY().left();
		stage.addActor(table);

		renderer = new ShapeRenderer();
	}

	@Override
	public void render(float delta) {
		Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

		stage.act(delta);
		stage.draw();

		float bottomLeftX = Gdx.graphics.getWidth() / 2 - graphSize / 2, bottomLeftY = Gdx.graphics.getHeight() / 2 - graphSize / 2;

		renderer.begin(ShapeType.Line);
		// draw graph bounds
		renderer.rect(bottomLeftX, bottomLeftY, graphSize, graphSize);
		// draw graph
		float lastX = bottomLeftX, lastY = bottomLeftY;
		for(int step = 0; step < steps; step++) {
			float percent = step / steps;
			float x = bottomLeftX + graphSize * percent;
			float y = bottomLeftY + graphSize * getInterpolation().apply(percent);
			renderer.line(lastX, lastY, x, y);
			lastX = x;
			lastY = y;
		}
		// draw time marker
		time += delta;
		if(time > duration) {
			time = 0;
			resetPositions();
		}
		float timeMarkerX = bottomLeftX + graphSize * time / duration;
		renderer.line(timeMarkerX, bottomLeftY, timeMarkerX, bottomLeftY + graphSize);
		renderer.end();
		// draw position
		updatePosition(time);
		renderer.begin(ShapeType.Filled);
		renderer.circle(position.x, position.y, 7);
		renderer.end();
	}

	@Override
	public void resize(int width, int height) {
		stage.setViewport(width, height, true);
		table.invalidateHierarchy();

		stage.getCamera().update();
		renderer.setProjectionMatrix(stage.getCamera().combined);
	}

	@Override
	public void hide() {
		dispose();
	}

	@Override
	public void pause() {
	}

	@Override
	public void resume() {
	}

	@Override
	public void dispose() {
		stage.dispose();
		skin.dispose();
		renderer.dispose();
	}

}