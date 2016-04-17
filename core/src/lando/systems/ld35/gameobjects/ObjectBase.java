package lando.systems.ld35.gameobjects;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureRegion;
import com.badlogic.gdx.math.Intersector;
import com.badlogic.gdx.math.Rectangle;
import lando.systems.ld35.utils.Assets;

public abstract class ObjectBase {
    static float scaleY = 1f;

    TextureRegion keyframe;
    Pixmap texturePixmap;
    Rectangle bounds;
    Rectangle intersectorRectangle;
    float scaleX = 1;
    float rotation = 0;
    float originX = 0;
    float originY = 0;
    public Rectangle realWorldBounds;


    public ObjectBase(Rectangle bounds, float rotation, boolean flipX) {
        this.keyframe = new TextureRegion(Assets.testTexture);
        this.bounds = bounds;
        this.rotation = rotation;
        realWorldBounds = new Rectangle(bounds);
        if(flipX) {
            scaleX = -1;
            originX = bounds.width / 2;
        }
        intersectorRectangle = new Rectangle();
    }

    public abstract void update(float delta);

    public boolean collision(Balloon balloon) {
        if (texturePixmap != null && balloon.bounds.overlaps(bounds)) {
            if (Intersector.intersectRectangles(bounds, balloon.bounds, intersectorRectangle)){
                Rectangle textureArea = new Rectangle(intersectorRectangle.x - bounds.x + keyframe.getRegionX(),
                    intersectorRectangle.y - bounds.y + keyframe.getRegionY(),
                    intersectorRectangle.width, intersectorRectangle.height);

                int regionY = keyframe.getRegionY();
                // This may need to be <=
                for (int x = 0; x < textureArea.width; x++){
                    for (int y = 0; y <  textureArea.height; y++){
                        int texX = x + (int)textureArea.x;
                        int texY = 32 - (int)(y + intersectorRectangle.y - bounds.y) + regionY;
                        int pix = texturePixmap.getPixel(texX, texY);
                        int index = (int)( intersectorRectangle.x - bounds.x) + x + (int)(intersectorRectangle.y - bounds.y + y) * 32;
                        if((pix & 0xFF) != 0x00) {
                            return true;
                        };
                    }
                }
            }
        }

        return false;
    }

    public void render(SpriteBatch batch) {
        batch.draw(keyframe, bounds.x, bounds.y, originX, originY, bounds.width, bounds.height, scaleX, scaleY, rotation);
    }

    public Rectangle getBounds() { return bounds; }
    public TextureRegion getKeyframe() { return keyframe; }
    public Pixmap getPixmap() {
        Texture t = keyframe.getTexture();
        if (!t.getTextureData().isPrepared()) {
            t.getTextureData().prepare();
        }
        return t.getTextureData().consumePixmap();
    }
}