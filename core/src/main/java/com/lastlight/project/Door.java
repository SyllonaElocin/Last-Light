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
    private boolean openable = false;  // NEW: can the door be opened
    private boolean interacting = false;

    public Door(Vector2 pos, Texture closed, Texture open, float tileSize) {
        this.position = new Vector2(pos);
        this.closedTexture = closed;
        this.openTexture = open;
        this.currentTexture = closed;
        this.size = tileSize;
    }

    public void startOpening(float delta) {
        if (!openable) return; // can't open yet
        if (!interacting) interacting = true;

        if (interacting) {
            progress += delta;
            if (progress >= openTime) {
                progress = openTime;
                currentTexture = openTexture;
                interacting = false;
            }
        }
    }

    public boolean isOpen() {
        return currentTexture == openTexture;
    }

    public void render(SpriteBatch batch) {
        batch.draw(currentTexture, position.x, position.y, size, size);
    }

    public void setOpenable(boolean value) {
        this.openable = value;
    }

    public boolean isOpenable() {
        return openable;
    }

    public boolean isBlocking() {
        return !isOpen() && !openable; // optional: doors that are openable still block until opened
    }

    public boolean isInteracting() {
        return interacting;
    }

    public void dispose() {
        // Textures handled in Main
    }
}
