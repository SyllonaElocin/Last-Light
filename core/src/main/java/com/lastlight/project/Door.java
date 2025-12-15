package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.Vector2;

public class Door {

    public Vector2 position;
    private Texture closedTexture;
    private Texture openTexture;
    private Texture currentTexture;
    public float size;

    private float progress = 0f;
    private final float maxProgress = 2f; // seconds to open
    private boolean completed = false;

    private boolean openable = false; // set by Main when all generators done

    public Door(Vector2 pos, Texture closed, Texture open, float tileSize) {
        this.position = new Vector2(pos);
        this.closedTexture = closed;
        this.openTexture = open;
        this.currentTexture = closed;
        this.size = tileSize;
    }

    // ===== SAME AS GENERATOR =====
    public void progress(float delta) {
        if (!openable || completed) return;

        progress += delta;
        if (progress >= maxProgress) {
            progress = maxProgress;
            completed = true;
            currentTexture = openTexture;
        }
    }

    // ===== PROGRESS BAR =====
    public void renderProgressBar(ShapeRenderer sr) {
        if (!openable || completed) return;

        float barWidth = size;
        float barHeight = 6f;
        float x = position.x;
        float y = position.y + size + 6f;

        float ratio = progress / maxProgress;

        sr.setColor(0.15f, 0.15f, 0.15f, 1f);
        sr.rect(x, y, barWidth, barHeight);

        sr.setColor(0.2f, 0.9f, 0.2f, 1f);
        sr.rect(x, y, barWidth * ratio, barHeight);
    }

    // ===== STATE =====
    public boolean isOpen() {
        return completed;
    }

    public void setOpenable(boolean value) {
        openable = value;
    }

    public boolean isOpenable() {
        return openable;
    }

    // ===== RENDER =====
    public void render(SpriteBatch batch) {
        batch.draw(currentTexture, position.x, position.y, size, size);
    }
}
