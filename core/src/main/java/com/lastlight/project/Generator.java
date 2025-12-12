package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Generator {
    public Vector2 position;
    private Texture texture;
    public float size;

    private float progress = 0f;
    private final float maxProgress = 5f; // seconds to complete
    private boolean completed = false;
    private boolean counted = false;

    public Generator(Vector2 pos, Texture texture, float tileSize) {
        this.position = new Vector2(pos);
        this.texture = texture;
        this.size = tileSize;
    }

    public void progress(float delta) {
        if (!completed) {
            progress += delta;
            if (progress >= maxProgress) {
                progress = maxProgress;
                completed = true;
            }
        }
    }

    public boolean isCompleted() {
        return completed;
    }

    public boolean isCounted() {
        return counted;
    }

    public void setCounted(boolean counted) {
        this.counted = counted;
    }

    public float getProgress() {
        return progress;
    }

    public float getMaxProgress() {
        return maxProgress;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }

    public void dispose() {
        // Texture handled in Main
    }
}
