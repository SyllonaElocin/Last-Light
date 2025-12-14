package com.lastlight.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

public class Player {
    public Vector2 position;
    private Texture texture;
    public float size;
    private float radius;

    private float walkSpeed = 100f;
    private float sprintSpeed = 150f;

    private Generator interacting = null;
    private Door interactingDoor = null;


    private final float tileSize = 64f;

    public Player(Vector2 tilePos, Texture texture, float tileSize) {
        this.size = tileSize * 0.4f;
        this.radius = size / 2f;
        this.position = new Vector2(
                tilePos.x + (tileSize - size)/2f,
                tilePos.y + (tileSize - size)/2f
        );
        this.texture = texture;
    }

    public void update(float delta, String[][] map, List<Generator> generators, List<Door> doors) {
        boolean actionPressed = Gdx.input.isKeyPressed(Input.Keys.F);

        // Interaction logic
        if (actionPressed) {
            // Start interacting with generator if not already
            if (interacting == null && interactingDoor == null) {
                for (Generator g : generators) {
                    if (!g.isCompleted() && isNear(g)) {
                        interacting = g;
                        break;
                    }
                }
            }

            // Start interacting with door if not generator
            if (interacting == null && interactingDoor == null) {
                for (Door d : doors) {
                    if (!d.isOpen() && d.isOpenable() && isNear(d)) {
                        interactingDoor = d;
                        break;
                    }
                }
            }

            // Progress interactions
            if (interacting != null) interacting.progress(delta);
            if (interactingDoor != null) interactingDoor.startOpening(delta);

        } else {
            interacting = null;
            interactingDoor = null;
        }

        // Movement only if not interacting
        if (interacting == null && interactingDoor == null) {
            float currentSpeed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT) ? sprintSpeed : walkSpeed;
            Vector2 movement = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) movement.y += currentSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) movement.y -= currentSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.x -= currentSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.x += currentSpeed * delta;
            move(movement, map, generators, doors);
        }
    }


    private void move(Vector2 delta, String[][] map, List<Generator> generators, List<Door> doors) {
        if (!isColliding(position.x + delta.x, position.y, map, generators, doors)) position.x += delta.x;
        if (!isColliding(position.x, position.y + delta.y, map, generators, doors)) position.y += delta.y;
    }

    private boolean isColliding(float x, float y, String[][] map, List<Generator> generators, List<Door> doors) {
        float cx = x + radius;
        float cy = y + radius;

        int leftTile = (int)((cx - radius)/tileSize);
        int rightTile = (int)((cx + radius)/tileSize);
        int bottomTile = (int)((cy - radius)/tileSize);
        int topTile = (int)((cy + radius)/tileSize);

        for (int ty = bottomTile; ty <= topTile; ty++) {
            for (int tx = leftTile; tx <= rightTile; tx++) {
                if (tx < 0 || tx >= map[0].length || ty < 0 || ty >= map.length) return true;

                int flippedY = map.length - 1 - ty;
                if (map[flippedY][tx].equals("W")) return true;

                // Check generator collision
                for (Generator g : generators) {
                    if (g.isCompleted()) continue;
                    if (cx + radius > g.position.x && cx - radius < g.position.x + g.size &&
                        cy + radius > g.position.y && cy - radius < g.position.y + g.size) return true;
                }

                // Check doors collision
                for (Door d : doors) {
                    if (!d.isOpen() && cx + radius > d.position.x && cx - radius < d.position.x + d.size &&
                        cy + radius > d.position.y && cy - radius < d.position.y + d.size) return true;
                }
            }
        }

        return false;
    }


    private boolean isNear(Generator g) {
        float centerX = position.x + size/2f;
        float centerY = position.y + size/2f;
        float genCenterX = g.position.x + g.size/2f;
        float genCenterY = g.position.y + g.size/2f;
        return Math.abs(centerX - genCenterX) < tileSize && Math.abs(centerY - genCenterY) < tileSize;
    }

    private boolean isNear(Door d) {
        float centerX = position.x + size/2f;
        float centerY = position.y + size/2f;
        float doorCenterX = d.position.x + d.size/2f;
        float doorCenterY = d.position.y + d.size/2f;
        return Math.abs(centerX - doorCenterX) < tileSize && Math.abs(centerY - doorCenterY) < tileSize;
    }


    public Generator getInteractingGenerator() {
        return interacting;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }
}
