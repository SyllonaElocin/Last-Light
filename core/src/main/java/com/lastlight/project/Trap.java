package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Trap {
    public Vector2 position;
    float size;
    Texture texture;

    public Trap(Vector2 position, Texture texture, float size) {
        this.position = position;
        this.texture = texture;
        this.size = size;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }

    public void dispose() {
        texture.dispose();
    }
}
