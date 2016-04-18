package lando.systems.ld35.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Rectangle;
import com.badlogic.gdx.math.Vector2;

public class Door extends TriggerableEntity {

    private static final float ACCELERATION = 220f;
    private static final float MAX_ROTATION_SPEED = 90f;
    private float rotationSpeed = 0f;

    private String tag = this.toString();

    public enum State {OPEN, CLOSED}

    private Vector2 dimensions; // x=width, y=height
    private Vector2 position; // Origin point and point of rotation.  The center of the left side.
    private Vector2 rotationOrigin;

    private float closedRotation;
    private float openedRotation;

    private float targetRotation;

//    private int openDirection;
    private State state;
    private float rotation;
    private TextureRegion textureRegion;

    // -----------------------------------------------------------------------------------------------------------------

    public Door(Rectangle bounds, float closedRotation, float openedRotation, TextureRegion textureRegion) {
        super(bounds, closedRotation, false);
        this.dimensions = new Vector2(bounds.width, bounds.height);
        this.position = new Vector2(bounds.x, bounds.y);
        this.closedRotation = closedRotation;
        this.openedRotation = openedRotation;
        this.textureRegion = textureRegion;
        //

//        this.rotationOrigin = new Vector2(bounds)
        //
        this.rotation = this.closedRotation;
        this.targetRotation = this.closedRotation;
        this.state = State.CLOSED;
    }

    // -----------------------------------------------------------------------------------------------------------------

    @Override
    public void onTrigger() {
        switch(state) {
            case OPEN:
                setState(State.CLOSED);
                targetRotation = closedRotation;
                break;
            case CLOSED:
                setState(State.OPEN);
                targetRotation = openedRotation;
                break;
            default:
                Gdx.app.error(tag, "unrecognized state");
        }
    }


    private void setState(State state) {
        if (this.state != state) {
            this.state = state;
//            Gdx.app.log(tag, "state='" + state.toString() + "'");
        }
    }

    // -----------------------------------------------------------------------------------------------------------------

    public void update(float dt) {

        float amountLeft = targetRotation - rotation;
        if (amountLeft == 0 ) return;

        float dr = Math.signum(amountLeft) * MAX_ROTATION_SPEED * dt;
        if (Math.abs(dr) > Math.abs(amountLeft)){
            rotation = targetRotation;
        } else {
            rotation += dr;
        }

    }

    public void render(SpriteBatch batch){
//        Color c = batch.getColor();
//        batch.setColor(0,0,1,1);
        batch.draw(
                textureRegion,
                position.x, position.y,
                0, (dimensions.y / 2),
                dimensions.x, dimensions.y,
                1, 1, rotation
        );
//        batch.setColor(c);
    }

}
