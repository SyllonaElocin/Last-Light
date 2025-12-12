package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Door {
    public Vector2 position;
    private Texture closedTexture;
    private Texture openTexture;
    private Texture currentTexture;
    public float size;

    private boolean opening = false;
    private float progress = 0f;
    private final float openTime = 2f; // seconds to open door

    public Door(Vector2 pos, Texture closed, Texture open, float tileSize) {
        this.position = new Vector2(pos);
        this.closedTexture = closed;
        this.openTexture = open;
        this.currentTexture = closed;
        this.size = tileSize;
    }

    public void startOpening(float delta) {
        if (!opening) opening = true;
        if (opening) {
            progress += delta;
            if (progress >= openTime) {
                progress = openTime;
                currentTexture = openTexture;
            }
        }
    }

    public boolean isOpen() {
        return currentTexture == openTexture;
    }

    public void render(SpriteBatch batch) {
        batch.draw(currentTexture, position.x, position.y, size, size);
    }

    public void dispose() {
        // Textures handled in Main
    }
}
