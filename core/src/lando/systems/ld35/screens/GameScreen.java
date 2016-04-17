package lando.systems.ld35.screens;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.InputProcessor;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.maps.MapObject;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.utils.Array;
import com.badlogic.gdx.utils.Pool;
import com.badlogic.gdx.utils.Pools;
import lando.systems.ld35.LudumDare35;
import lando.systems.ld35.gameobjects.*;
import lando.systems.ld35.ui.StateButton;
import lando.systems.ld35.utils.Assets;
import lando.systems.ld35.utils.Config;
import lando.systems.ld35.utils.LevelBoundry;
import lando.systems.ld35.utils.Utils;

/**
 * Brian Ploeckelman created on 4/16/2016.
 */
public class GameScreen extends BaseScreen implements InputProcessor {

    LevelInfo level;
    Balloon playerBalloon;
    Array<WindParticle> dustMotes;
    Array<StateButton> stateButtons;
    Pool<Rectangle> rectPool;
    Rectangle buttonTrayRect;

    public GameScreen() {
        super();
        rectPool = Pools.get(Rectangle.class);
        dustMotes = new Array<WindParticle>();
        loadLevel(0);
        updateCamera(1, true);
        Utils.glClearColor(Config.bgColor);
        Gdx.input.setInputProcessor(this);
    }

    // ------------------------------------------------------------------------
    // BaseScreen Implementation ----------------------------------------------
    // ------------------------------------------------------------------------

    @Override
    public void update(float dt) {
        if (Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)) {
            LudumDare35.game.screen = new MenuScreen();
        }

        level.update(dt);
        playerBalloon.update(dt, level);

        updateMapObjects(dt);
        updateDust(dt);
        updateCamera(dt, false);
    }

    @Override
    public void render(SpriteBatch batch) {
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        level.setView(camera);
        batch.setProjectionMatrix(camera.combined);

        batch.begin();

        level.renderBackground();
        for (WindParticle mote : dustMotes){
            mote.render(batch);
        }
        playerBalloon.render(batch);

        level.renderForeground(batch);
        batch.setProjectionMatrix(hudCamera.combined);
        Assets.trayNinepatch.draw(batch, buttonTrayRect.x, buttonTrayRect.y, buttonTrayRect.width, buttonTrayRect.height);
        for (StateButton stateButton : stateButtons) {
            stateButton.render(batch);
        }
        batch.end();
    }

    // ------------------------------------------------------------------------
    // InputListener Interface ------------------------------------------------
    // ------------------------------------------------------------------------
    private Vector2 touchPosScreen    = new Vector2();
    private Vector3 touchPosUnproject = new Vector3();

    @Override
    public boolean touchDown(int screenX, int screenY, int pointer, int button) {
        touchPosUnproject = hudCamera.unproject(new Vector3(screenX, screenY, 0));
        touchPosScreen.set(touchPosUnproject.x, touchPosUnproject.y);

        if (playerBalloon.currentState == Balloon.State.POP ||
            playerBalloon.currentState == Balloon.State.DEAD)
        {
            resetLevel();
            return false;
        }

        StateButton enabledButton = null;
        for (StateButton stateButton : stateButtons) {
            if (stateButton.checkForTouch(touchPosScreen.x, touchPosScreen.y)) {
                playerBalloon.changeState(stateButton.state);
                enabledButton = stateButton;
            }
        }
        if (enabledButton != null) {
            for (StateButton stateButton : stateButtons) {
                if (stateButton != enabledButton) {
                    stateButton.active = false;
                }
            }
        }

        return false;
    }

    @Override
    public boolean touchUp(int screenX, int screenY, int pointer, int button) {
        return false;
    }

    @Override
    public boolean touchDragged(int screenX, int screenY, int pointer) {
        return false;
    }

    @Override
    public boolean keyDown(int keycode) {
        handleHotkeys(keycode);
        return false;
    }

    @Override
    public boolean keyUp(int keycode) {
        return false;
    }

    @Override
    public boolean keyTyped(char character) {
        return false;
    }

    @Override
    public boolean mouseMoved(int screenX, int screenY) {
//        Vector3 worldPoint = camera.unproject(new Vector3(screenX, screenY, 0));
//        Array<LevelBoundry> cells = level.getTiles((int)worldPoint.x /32, (int)worldPoint.y / 32, (int)worldPoint.x /32, (int)worldPoint.y /32);
//        if (cells.size > 0){
//            for (LevelBoundry boundry: cells) {
//                Texture t = boundry.tile.getTile().getTextureRegion().getTexture();
//                if (!t.getTextureData().isPrepared()) {
//                    t.getTextureData().prepare();
//                }
//                Pixmap tilePixmap = t.getTextureData().consumePixmap();
//                int pxX = (int)(worldPoint.x % 32f) + boundry.tile.getTile().getTextureRegion().getRegionX();
//                int pxY = 32 - (int)(worldPoint.y % 32f) + boundry.tile.getTile().getTextureRegion().getRegionY();
//                Gdx.app.log("Touch", "X:" + pxX + " Y:" + pxY);
//
//                int pix = tilePixmap.getPixel(pxX,  pxY);
//                Gdx.app.log("Touch", pix+"");
//            }
//        }
        return false;
    }

    @Override
    public boolean scrolled(int amount) {
        return false;
    }

    // ------------------------------------------------------------------------
    // Private Implementation -------------------------------------------------
    // ------------------------------------------------------------------------

    private void loadLevel(int levelId){
        level = new LevelInfo(levelId, rectPool);
        playerBalloon = new Balloon(level.details.getStart(), this);
        layoutUI();
    }

    private void resetLevel(){
        // TODO reset the level quickly
        loadLevel(level.levelIndex);
    }

    private void layoutUI() {
        int numButtons = 6;
        float padding = 10f;
        float size = 32f;
        float width = (padding + size) * numButtons;
        float leftMargin = camera.viewportWidth / 2f - width / 2f;

        stateButtons = new Array<StateButton>();
        stateButtons.add(new StateButton(Balloon.State.NORMAL, Assets.balloonTexture,
                                         new Rectangle(leftMargin + 10 * 0f + 32 * 0f, 10, 32, 32)));
        stateButtons.add(new StateButton(Balloon.State.LIFT, Assets.rocketTexture,
                                         new Rectangle(leftMargin + 10 * 1f + 32 * 1f, 10, 32, 32)));
        stateButtons.add(new StateButton(Balloon.State.HEAVY, Assets.weightTexture,
                                         new Rectangle(leftMargin + 10 * 2f + 32 * 2f, 10, 32, 32)));
        stateButtons.add(new StateButton(Balloon.State.SPINNER, Assets.torusTexture,
                                         new Rectangle(leftMargin + 10 * 3f + 32 * 3f, 10, 32, 32)));
        stateButtons.add(new StateButton(Balloon.State.MAGNET, Assets.magnetTexture,
                                         new Rectangle(leftMargin + 10 * 4f + 32 * 4f, 10, 32, 32)));
        stateButtons.add(new StateButton(Balloon.State.BUZZSAW, Assets.buzzsawTexture,
                                         new Rectangle(leftMargin + 10 * 5f + 32 * 5f, 10, 32, 32)));
        stateButtons.get(0).active = true;

        buttonTrayRect = new Rectangle(leftMargin - 10f, 0, width + 10, 52);
   }


    private void updateCamera(float dt, boolean initial){
        Vector2 targetCameraPosition = playerBalloon.position.cpy();
        targetCameraPosition.x = MathUtils.clamp(targetCameraPosition.x, camera.viewportWidth/2f, level.foregroundLayer.getWidth()*32 -camera.viewportWidth/2f );
        targetCameraPosition.y = MathUtils.clamp(targetCameraPosition.y, Math.min(camera.viewportHeight/2f, level.foregroundLayer.getHeight()*16), level.foregroundLayer.getHeight()*32 -camera.viewportHeight/2f );



        Vector2 dir = targetCameraPosition.cpy().sub(camera.position.x, camera.position.y);
        if (initial){
            camera.position.set(targetCameraPosition.x, targetCameraPosition.y, 0);
        } else {
            camera.position.add(dir.x * dt, dir.y * dt, 0);
        }
        camera.update();
    }

    private void updateDust(float dt){
        int newTotal = (int)(level.foregroundLayer.getWidth() * level.foregroundLayer.getHeight() * .1f);
        for (int i = 0; i < newTotal; i++){
            dustMotes.add(new WindParticle(new Vector2(MathUtils.random(level.foregroundLayer.getWidth()*32), MathUtils.random(level.foregroundLayer.getHeight()*32))));
        }

        for (int i = dustMotes.size -1; i >= 0; i--){
            WindParticle mote = dustMotes.get(i);
            for (ObjectBase obj : level.mapObjects){
                if (obj instanceof Fan){
                    Fan f = (Fan) obj;
                    Vector2 force = f.getWindForce(mote.pos);
                    if (!force.epsilonEquals( Vector2.Zero, 1f))
                        mote.vel.add(force.add(MathUtils.random(10f) -5f, MathUtils.random(10f) -5f).scl(dt * 10));
                }
            }
            mote.update(dt);
            if (mote.TTL < 0 || mote.vel.len2() < 50) {
                dustMotes.removeIndex(i);
                continue;
            }
            if (level.getCell((int)mote.pos.x /32, (int)mote.pos.y / 32) != null){
                dustMotes.removeIndex(i);

            }
        }
    }

    private void updateMapObjects(float dt) {
        for (ObjectBase obj : level.mapObjects) {
            // Interact with level exit
            if (obj instanceof Exit) {
                if (playerBalloon.bounds.overlaps(obj.getBounds())) {
                    dustMotes.clear();
                    level.nextLevel();
                    playerBalloon = new Balloon(level.details.getStart(), this);
                    for (StateButton button : stateButtons) {
                        button.active = false;
                    }
                    stateButtons.get(0).active = true;
                    updateCamera(0, true);
                }
            }

            if (obj instanceof Spikes) {
                if (obj.collision(playerBalloon)) {
                    playerBalloon.kill(level);
                }
            }
            // TODO: interact with other stuff
        }
    }

    private void handleHotkeys(int keycode) {
        if (playerBalloon.currentState == Balloon.State.POP ||
            playerBalloon.currentState == Balloon.State.DEAD)
        {
            return;
        }

        switch (keycode) {
            case Input.Keys.NUM_1: activateButton(0); break;
            case Input.Keys.NUM_2: activateButton(1); break;
            case Input.Keys.NUM_3: activateButton(2); break;
            case Input.Keys.NUM_4: activateButton(3); break;
            case Input.Keys.NUM_5: activateButton(4); break;
            case Input.Keys.NUM_6: activateButton(5); break;
            default: break;
        }
    }

    private void activateButton(int index) {
        for (int i = 0; i < stateButtons.size; ++i) {
            final StateButton button = stateButtons.get(i);
            button.active = (index == i);
            if (button.active) {
                playerBalloon.changeState(button.state);
            }
        }
    }

}
