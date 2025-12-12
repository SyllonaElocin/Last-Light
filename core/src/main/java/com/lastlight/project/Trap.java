package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Trap {

    public Vector2 position;
    private Texture texture;
    private float size;

    public Trap(Vector2 pos, Texture tex, float size) {
        this.position = pos;
        this.texture = tex;
        this.size = size;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }

    public void dispose() {
        texture.dispose();
    }
}
