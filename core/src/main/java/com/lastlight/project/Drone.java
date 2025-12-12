package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import java.util.Random;

public class Drone {
    public Vector2 position;
    private Texture texture;
    public float size;
    private float radius;
    private float speed = 100f;
    private Vector2 direction;
    private Random random = new Random();

    private final float tileSize = 64f;

    public Drone(Vector2 tilePos, Texture texture, float tileSize) {
        this.size = tileSize * 0.4f; 
        this.radius = size / 2f;

        this.position = new Vector2(
                tilePos.x + (tileSize - size) / 2f,
                tilePos.y + (tileSize - size) / 2f
        );

        this.texture = texture;
        this.direction = new Vector2(
                random.nextBoolean() ? 1 : -1,
                random.nextBoolean() ? 1 : -1
        );
    }

    public void update(float delta, String[][] map) {
        Vector2 movement = new Vector2(direction).scl(speed * delta);

        // Horizontal
        if (isColliding(position.x + movement.x, position.y, map)) {
            direction.x *= -1;
        } else {
            position.x += movement.x;
        }

        // Vertical
        if (isColliding(position.x, position.y + movement.y, map)) {
            direction.y *= -1;
        } else {
            position.y += movement.y;
        }
    }

    private boolean isColliding(float x, float y, String[][] map) {
        float cx = x + radius;
        float cy = y + radius;

        int leftTile = (int)((cx - radius) / tileSize);
        int rightTile = (int)((cx + radius) / tileSize);
        int bottomTile = (int)((cy - radius) / tileSize);
        int topTile = (int)((cy + radius) / tileSize);

        for (int ty = bottomTile; ty <= topTile; ty++) {
            for (int tx = leftTile; tx <= rightTile; tx++) {

                // Out of bounds = collision
                if (tx < 0 || tx >= map[0].length || ty < 0 || ty >= map.length)
                    return true;

                // FIX: Flip Y index to match rendering
                int flippedY = map.length - 1 - ty;

                // Only walls collide
                if (map[flippedY][tx].equals("W")) {

                    float tileX = tx * tileSize;
                    float tileY = ty * tileSize;

                    // Circle-to-AABB collision
                    float nearestX = MathUtils.clamp(cx, tileX, tileX + tileSize);
                    float nearestY = MathUtils.clamp(cy, tileY, tileY + tileSize);

                    float dx = cx - nearestX;
                    float dy = cy - nearestY;

                    if (dx * dx + dy * dy < radius * radius)
                        return true;
                }
            }
        }

        return false;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }
}
