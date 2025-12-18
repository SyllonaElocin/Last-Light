package com.lastlight.project;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import java.util.List;

public class Player {

    public Vector2 position;
    private final Texture texture;
    public final float size;
    private final float radius;

    private Sound shieldSound;
    private Sound sodaSound;
    private Sound batterySound;

    private final float walkSpeed = 80f;
    private final float sprintSpeed = 120f;
    private final float tileSize = 64f;

    private Generator interactingGenerator = null;
    private Door interactingDoor = null;

    private ItemType[] inventory = new ItemType[3];

    //Timers
    private float shieldTimer = 0f;
    private float speedTimer = 0f;

    //Effects
    private boolean shieldActive = false;
    private boolean speedBoostActive = false;

    //Flashlight battery
    private float battery = 100f;
    private static final float MAX_BATTERY = 100f;


    public Player(Vector2 tilePos, Texture texture, float tileSize) {
        this.size = tileSize * 0.4f;
        this.radius = size / 2f;
        this.position = new Vector2(
                tilePos.x + (tileSize - size) / 2f,
                tilePos.y + (tileSize - size) / 2f
        );
        this.texture = texture;
    }

    public void update(float delta, String[][] map,
                       List<Generator> generators,
                       List<Door> doors) {

        boolean actionPressed = Gdx.input.isKeyPressed(Input.Keys.F);

        if (actionPressed) {

            // Acquire interaction ONCE
            if (interactingGenerator == null && interactingDoor == null) {

                for (Generator g : generators) {
                    if (!g.isCompleted() && isNear(g)) {
                        interactingGenerator = g;
                        break;
                    }
                }

                if (interactingGenerator == null) {
                    for (Door d : doors) {
                        if (d.isOpenable() && !d.isOpen() && isNear(d)) {
                            interactingDoor = d;
                            break;
                        }
                    }
                }
            }

            // Progress interaction
            if (interactingGenerator != null) interactingGenerator.progress(delta);
            if (interactingDoor != null) interactingDoor.progress(delta);

        } else {
            // Release interaction
            interactingGenerator = null;
            interactingDoor = null;
        }

        // Use items
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_1)) useItem(0);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_2)) useItem(1);
        if (Gdx.input.isKeyJustPressed(Input.Keys.NUM_3)) useItem(2);

        // Update timers
        if (shieldActive) {
            shieldTimer -= delta;
            if (shieldTimer <= 0) shieldActive = false;
        }

        if (speedBoostActive) {
            speedTimer -= delta;
            if (speedTimer <= 0) speedBoostActive = false;
        }

        // Movement only when NOT interacting
        if (interactingGenerator == null && interactingDoor == null) {
            float baseSpeed = Gdx.input.isKeyPressed(Input.Keys.SHIFT_LEFT)
                    ? sprintSpeed
                    : walkSpeed;

            float currentSpeed = speedBoostActive ? baseSpeed * 2f : baseSpeed;

            Vector2 movement = new Vector2();
            if (Gdx.input.isKeyPressed(Input.Keys.W)) movement.y += currentSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.S)) movement.y -= currentSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.A)) movement.x -= currentSpeed * delta;
            if (Gdx.input.isKeyPressed(Input.Keys.D)) movement.x += currentSpeed * delta;

            move(movement, map, generators, doors);
        }
    }

    private void move(Vector2 delta, String[][] map,
                      List<Generator> generators,
                      List<Door> doors) {

        if (!isColliding(position.x + delta.x, position.y, map, generators, doors))
            position.x += delta.x;

        if (!isColliding(position.x, position.y + delta.y, map, generators, doors))
            position.y += delta.y;
    }

    private boolean isColliding(float x, float y, String[][] map,
                                List<Generator> generators,
                                List<Door> doors) {

        float cx = x + radius;
        float cy = y + radius;

        int leftTile   = (int)((cx - radius) / tileSize);
        int rightTile  = (int)((cx + radius) / tileSize);
        int bottomTile = (int)((cy - radius) / tileSize);
        int topTile    = (int)((cy + radius) / tileSize);

        for (int ty = bottomTile; ty <= topTile; ty++) {
            for (int tx = leftTile; tx <= rightTile; tx++) {

                if (tx < 0 || tx >= map[0].length ||
                    ty < 0 || ty >= map.length)
                    return true;

                int flippedY = map.length - 1 - ty;
                if (map[flippedY][tx].equals("W"))
                    return true;

                for (Generator g : generators) {
                    if (cx + radius > g.position.x &&
                        cx - radius < g.position.x + g.size &&
                        cy + radius > g.position.y &&
                        cy - radius < g.position.y + g.size)
                        return true;
                }

                for (Door d : doors) {
                    if (!d.isOpen() &&
                        cx + radius > d.position.x &&
                        cx - radius < d.position.x + d.size &&
                        cy + radius > d.position.y &&
                        cy - radius < d.position.y + d.size)
                        return true;
                }
            }
        }
        return false;
    }

    public boolean addItem(ItemType item) {
        for (int i = 0; i < inventory.length; i++) {
            if (inventory[i] == null) {
                inventory[i] = item;
                return true;
            }
        }
        return false; // inventory full
    }

    public ItemType getItem(int index) {
        return inventory[index];
    }

    private void useItem(int index) {
        sodaSound = Gdx.audio.newSound(Gdx.files.internal("soda.mp3"));
        shieldSound = Gdx.audio.newSound(Gdx.files.internal("shield.mp3"));
        batterySound = Gdx.audio.newSound(Gdx.files.internal("battery.mp3"));
        ItemType item = inventory[index];
        if (item == null) return;

        switch (item) {
            case SHIELD:
                shieldActive = true;
                shieldTimer = 5f;
                shieldSound.play();
                break;

            case SODA:
                speedBoostActive = true;
                speedTimer = 10f;
                sodaSound.play();
                break;

            case BATTERY:
                refillBattery(); 
                batterySound.play();
                break;
        }

        inventory[index] = null; // consume item
    }

    public void activateShield() {
        shieldTimer = 5f;
    }

    public void activateSpeedBoost() {
        speedTimer = 10f;
    }

    public void drainBattery(float amount) {
        battery = Math.max(0, battery - amount);
    }

    public void rechargeBattery(float amount) {
        battery = Math.min(MAX_BATTERY, battery + amount);
    }


    public void refillBattery() {
        battery = MAX_BATTERY;
    }

    public float getBattery() {
        return battery;
    }

    public boolean isShieldActive() {
        return shieldActive;
    }

    public boolean isSpeedBoostActive() {
        return speedBoostActive;
    }

    public void die(Main main) {
        main.triggerGameOver();
    }


    private boolean isNear(Generator g) {
        return Math.abs((position.x + size/2f) - (g.position.x + g.size/2f)) < tileSize &&
               Math.abs((position.y + size/2f) - (g.position.y + g.size/2f)) < tileSize;
    }

    private boolean isNear(Door d) {
        return Math.abs((position.x + size/2f) - (d.position.x + d.size/2f)) < tileSize &&
               Math.abs((position.y + size/2f) - (d.position.y + d.size/2f)) < tileSize;
    }

    public Generator getInteractingGenerator() {
        return interactingGenerator;
    }

    public Door getInteractingDoor() {
        return interactingDoor;
    }

    public void reset(Vector2 spawnPos) {
        position.set(
            spawnPos.x + (tileSize - size) / 2f,
            spawnPos.y + (tileSize - size) / 2f
        );

        shieldActive = false;
        speedBoostActive = false;
        shieldTimer = 0f;
        speedTimer = 0f;
        battery = 100f;

        // Clear inventory
        for (int i = 0; i < inventory.length; i++) inventory[i] = null;

        interactingGenerator = null;
        interactingDoor = null;
    }

    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }
}
