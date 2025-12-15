package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

import java.util.List;
import java.util.Random;

public class Drone {
    private Main main;
    public Vector2 position;
    private Texture texture;
    public float size;

    private final float tileSize;
    private float speed = 60f;

    // Persistent movement
    private Vector2 direction = new Vector2();
    private float changeDirTimer = 0f;
    private final float changeDirInterval = 1.0f; // seconds

    // Trap laying
    private float trapTimer = 0f;
    private final float minTrapInterval = 10f;
    private final float maxTrapInterval = 20f;
    private int trapsLaid = 0;
    private final int maxTraps = 5;

    private Random random = new Random();

    public Drone(Vector2 pos, Texture texture, float tileSize) {
        this.position = new Vector2(pos);
        this.texture = texture;
        this.tileSize = tileSize;
        this.size = tileSize * 0.8f;
        pickRandomDirection();
        resetTrapTimer();
    }

    private void pickRandomDirection() {
        direction.set((float)Math.random() - 0.5f, (float)Math.random() - 0.5f).nor();
    }

    private void resetTrapTimer() {
        trapTimer = minTrapInterval + random.nextFloat() * (maxTrapInterval - minTrapInterval);
    }

    public void update(float delta, String[][] map, List<Trap> traps, Texture trapTexture, Player player) {
        // Move drone
        changeDirTimer -= delta;
        if (changeDirTimer <= 0f) {
            pickRandomDirection();
            changeDirTimer = changeDirInterval;
        }

        Vector2 move = new Vector2(direction).scl(speed * delta);

        if (!isColliding(position.x + move.x, position.y, map)) position.x += move.x;
        if (!isColliding(position.x, position.y + move.y, map)) position.y += move.y;

        // Trap laying
        if (trapsLaid < maxTraps) {
            trapTimer -= delta;
            if (trapTimer <= 0f) {
                // Lay trap at current position
                traps.add(new Trap(new Vector2(position.x, position.y), trapTexture, tileSize));
                trapsLaid++;
                resetTrapTimer();
            }
        }

    }

    private boolean isTouchingPlayer(Player player) {
        float px = player.position.x;
        float py = player.position.y;
        float pr = player.size / 2f;

        float dx = position.x + size / 2f;
        float dy = position.y + size / 2f;

        return Math.abs(px + pr - dx) < (size / 2f + pr) &&
               Math.abs(py + pr - dy) < (size / 2f + pr);
    }

    private boolean isColliding(float x, float y, String[][] map) {
        int leftTile = (int)(x / tileSize);
        int rightTile = (int)((x + size) / tileSize);
        int bottomTile = (int)(y / tileSize);
        int topTile = (int)((y + size) / tileSize);

        for (int ty = bottomTile; ty <= topTile; ty++) {
            for (int tx = leftTile; tx <= rightTile; tx++) {
                if (tx < 0 || tx >= map[0].length || ty < 0 || ty >= map.length) return true;
                int flippedY = map.length - 1 - ty;
                if (map[flippedY][tx].equals("W")) return true;
            }
        }
        return false;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }

    public void dispose() {
        // Texture handled in Main
    }
}
