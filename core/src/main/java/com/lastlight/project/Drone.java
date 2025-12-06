package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Drone {
    public Vector2 position;
    float size;
    Texture texture;

    public Drone(Vector2 position, float size, Texture texture) {
        this.position = position;
        this.size = size;
        this.texture = texture;
    }

    public void update(float delta) {
        // idle for now (we can add patrol later)
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }

    public void dispose() {
        texture.dispose();
    }
}
