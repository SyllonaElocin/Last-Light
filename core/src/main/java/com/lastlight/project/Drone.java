package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.MathUtils;
import java.util.List;
import java.util.Random;

public class Drone {
    public Vector2 position;
    private Texture texture;
    public float size;
    private float radius;
    private float speed = 90f; // slightly slower than player sprint
    private Vector2 targetTile; // where the drone wants to go
    private Random random = new Random();

    private final float tileSize = 64f;

    // Trap mechanic
    private int trapsPlaced = 0;
    private float trapTimer;
    private final int maxTraps = 5;

    public Drone(Vector2 tilePos, Texture texture, float tileSize) {
        this.size = tileSize * 0.4f; 
        this.radius = size / 2f;

        this.position = new Vector2(
            tilePos.x + (tileSize - size) / 2f,
            tilePos.y + (tileSize - size) / 2f
        );

        this.texture = texture;
        this.targetTile = getCurrentTile();
        trapTimer = getRandomInterval();
    }

    public void update(float delta, String[][] map, List<Trap> traps, Texture trapTexture) {
        Vector2 currentTile = getCurrentTile();

        // If reached target tile, pick a new neighboring "F" tile
        if (currentTile.epsilonEquals(targetTile, 0.1f)) {
            targetTile = getRandomNeighbor(currentTile, map);
        }

        // Move toward target tile
        Vector2 targetPos = new Vector2(
            targetTile.x * tileSize + tileSize/2f - size/2f,
            targetTile.y * tileSize + tileSize/2f - size/2f
        );
        Vector2 direction = targetPos.cpy().sub(position).nor();
        Vector2 movement = direction.scl(speed * delta);

        if (movement.len() > position.dst(targetPos)) {
            position.set(targetPos);
        } else {
            position.add(movement);
        }

        // Trap placement
        trapTimer -= delta;
        if (trapTimer <= 0f && trapsPlaced < maxTraps) {
            Vector2 trapPos = new Vector2(
                (float)Math.floor((position.x + radius) / tileSize) * tileSize,
                (float)Math.floor((position.y + radius) / tileSize) * tileSize
            );
            traps.add(new Trap(trapPos, trapTexture, tileSize));
            trapsPlaced++;
            trapTimer = getRandomInterval();
        }
    }

    private Vector2 getCurrentTile() {
        int x = (int)((position.x + radius) / tileSize);
        int y = (int)((position.y + radius) / tileSize);
        return new Vector2(x, y);
    }

    private Vector2 getRandomNeighbor(Vector2 tile, String[][] map) {
        int x = (int)tile.x;
        int y = (int)tile.y;

        Vector2[] neighbors = {
            new Vector2(x+1, y),
            new Vector2(x-1, y),
            new Vector2(x, y+1),
            new Vector2(x, y-1)
        };

        // Shuffle neighbors
        for (int i = neighbors.length - 1; i > 0; i--) {
            int j = random.nextInt(i+1);
            Vector2 temp = neighbors[i];
            neighbors[i] = neighbors[j];
            neighbors[j] = temp;
        }

        for (Vector2 n : neighbors) {
            int nx = (int)n.x;
            int ny = (int)n.y;
            if (nx < 0 || nx >= map[0].length || ny < 0 || ny >= map.length) continue;

            int flippedY = map.length - 1 - ny;
            if (map[flippedY][nx].equals("F")) {
                return new Vector2(nx, ny);
            }
        }

        // If no free neighbor, stay in place
        return tile.cpy();
    }

    private float getRandomInterval() {
        return 10f + random.nextFloat() * 10f; // 10–20 seconds
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }
}
