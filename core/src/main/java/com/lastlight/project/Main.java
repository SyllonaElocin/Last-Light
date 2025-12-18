package com.lastlight.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
import com.badlogic.gdx.audio.Sound;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.Pixmap;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Vector2;

import java.util.ArrayList;

public class Main extends ApplicationAdapter {

    private GameState gameState = GameState.MAIN_MENU;
    ArrayList<Button> mainMenuButtons = new ArrayList<>();
    ArrayList<Button> pauseMenuButtons = new ArrayList<>();
    ArrayList<Button> endScreenButtons = new ArrayList<>();

    // =====================
    // HUD / Instructions
    // =====================
    private final String controlInstructions = "Controls: LMB - Flashlight | F - Interact | Shift - Sprint | 1-3 - Use Item";
    private String objectiveInstruction = "Finish all generators!";

    // ======================
    // CORE RENDERING
    // ======================
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera, hudCamera;

    // ======================
    // TEXTURES
    // ======================
    private Texture wallTexture, floorTexture, trapTexture, droneTexture, playerTexture;
    private Texture generatorTexture, doorClosedTexture, doorOpenTexture;
    private Texture shieldTexture, sodaTexture, batteryTexture;
    private ArrayList<Item> items = new ArrayList<>();

    private Texture lightMask;
    private Texture victoryTexture, gameOverTexture;

    // ======================
    // AUDIO
    // ======================
    private Sound lightExpandSound, lightShrinkSound, generatorFixSound, doorOpenSound, victorySound, defeatSound, itemPickupSound;

    // ======================
    // ENTITIES
    // ======================
    private Player player;
    private final ArrayList<Drone> drones = new ArrayList<>();
    private final ArrayList<Trap> traps = new ArrayList<>();
    private final ArrayList<Generator> generators = new ArrayList<>();
    private final ArrayList<Door> doors = new ArrayList<>();

    // ======================
    // GAME CONSTANTS
    // ======================
    private final float tileSize = 64f;

    private final float minLightRadius = 128f;
    private final float maxLightRadius = 512f;
    private float currentLightRadius = minLightRadius;
    private final float lightGrowSpeed = 64f;
    private final float lightShrinkSpeed = 64f;

    private static final float batteryDrainRate = 10f;     
    private static final float batteryRechargeRate = 20f;  
    private boolean flashlightLocked = false;
    private float flashlightLockTimer = 0f;
    private final float flashlightLockDuration = 2f;

    // ======================
    // GAME STATE
    // ======================
    private BitmapFont font;
    private boolean wasLightHeld = false;
    private int lastCompletedGenerators = 0;

    // ======================
    // INTRO SEQUENCE
    // ======================
    private String[] introLines = {
        "Escape the facility. Fix all the Generators before opening the door.",
        "Beware, there are traps and drones across the facility, avoid them.",
        "There are items around that you can use to help you."
    };
    private int currentIntroLine = 0;
    private float introTimer = 0f;
    private Sound beepSound;
    private final float lineInterval = 3f;
    private float postIntroTimer = 0f;
    private final float postIntroDelay = 5f;

    // ======================
    // ITEM PICKUP MESSAGE
    // ======================
    private String itemMessage = "";
    private float itemMessageTimer = 0f;
    private final float itemMessageDuration = 3f; // 3 seconds

    // ======================
    // MAP
    // ======================
    private String[][] map = generateMap();

    private int totalGenerators;

    // ======================
    // CREATE
    // ======================
    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();

        camera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        camera.zoom = 1f; // adjust if needed
        camera.update();

        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.setToOrtho(false);
        hudCamera.update();

        font = new BitmapFont();
        font.setColor(1, 1, 0, 1);

        loadTextures();
        loadSounds();
        lightMask = createRadialLightMask(4096, minLightRadius);

        spawnEntities();

        setupMenus();

        totalGenerators = generators.size();
    }

    private void loadTextures() {
        wallTexture = new Texture("Wall.png");
        floorTexture = new Texture("Floor.png");
        trapTexture = new Texture("Trap.png");
        droneTexture = new Texture("Drone.png");
        playerTexture = new Texture("Player.png");
        generatorTexture = new Texture("Generator.png");
        doorClosedTexture = new Texture("DoorClosed.png");
        doorOpenTexture = new Texture("DoorOpen.png");
        victoryTexture = new Texture("VictoryScreen.jpg");
        gameOverTexture = new Texture("GameOverScreen.jpg");
        shieldTexture = new Texture("Shield.png");
        sodaTexture = new Texture("Soda.png");
        batteryTexture = new Texture("Battery.png");

        wallTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        floorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        trapTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        droneTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        playerTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        generatorTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        doorClosedTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        doorOpenTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        shieldTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        sodaTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
        batteryTexture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    }

    private void loadSounds() {
        lightExpandSound = Gdx.audio.newSound(Gdx.files.internal("light_on.mp3"));
        lightShrinkSound = Gdx.audio.newSound(Gdx.files.internal("light_off.mp3"));
        generatorFixSound = Gdx.audio.newSound(Gdx.files.internal("generator_fix.mp3"));
        doorOpenSound = Gdx.audio.newSound(Gdx.files.internal("door_open.mp3"));
        victorySound = Gdx.audio.newSound(Gdx.files.internal("victory.mp3"));
        defeatSound = Gdx.audio.newSound(Gdx.files.internal("defeat.mp3"));
        beepSound = Gdx.audio.newSound(Gdx.files.internal("beep.mp3"));
        itemPickupSound = Gdx.audio.newSound(Gdx.files.internal("pick.mp3"));
    }

    // ======================
    // RENDER LOOP
    // ======================
    @Override
    public void render() {
        Gdx.gl.glClearColor(0, 0, 0, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        float delta = Gdx.graphics.getDeltaTime();

        switch(gameState){
            case MAIN_MENU:
                renderMainMenu();
                updateMenuInput(mainMenuButtons);
                break;

            case INTRO:
                renderIntro(delta);
                break;

            case PLAYING:
                updateGame(delta);
                renderGame();
                if(Gdx.input.isKeyJustPressed(Input.Keys.ESCAPE)){
                    gameState = GameState.PAUSED;
                }
                break;

            case PAUSED:
                renderPauseMenu();
                updateMenuInput(pauseMenuButtons);
                break;

            case WIN:
                renderVictoryScreen();
                updateMenuInput(endScreenButtons);
                break;

            case GAME_OVER:
                renderDefeatScreen();
                updateMenuInput(endScreenButtons);
                break;
            }
        }

    // ======================
    // DOOR LOGIC
    // ======================
    private void updateDoors() {
        boolean allGeneratorsDone = getCompletedGenerators() == totalGenerators;
        float delta = Gdx.graphics.getDeltaTime();

        for (Door d : doors) {
            // Doors become interactable only after all generators done
            d.setOpenable(allGeneratorsDone);

            // Update progress if player is near and pressing action
            if (d.isOpenable() && !d.isOpen() && player.getInteractingDoor() == d) {
                d.progress(delta);

                // Play sound when door just opens
                if (d.isOpen()) {
                    doorOpenSound.play(0.8f);
                }
            }
        }
    }


    private void checkPlayerOnDoors() {
        if (gameState != GameState.PLAYING) return;

        for (Door d : doors) {
            if (d.isOpen() && isPlayerOnDoor(d)) {
                gameState = GameState.WIN;
                victorySound.play(0.8f);
                break;
            }
        }
    }

    private boolean isPlayerOnDoor(Door door) {
        float px = player.position.x + player.size / 2f;
        float py = player.position.y + player.size / 2f;
        float dx = door.position.x;
        float dy = door.position.y;

        return px > dx && px < dx + door.size && py > dy && py < dy + door.size;
    }

    // ======================
    // RENDER HELPERS
    // ======================
    private void renderWorld() {
        batch.setProjectionMatrix(camera.combined);
        batch.begin();
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Texture t = map[y][x].equals("W") ? wallTexture : floorTexture;
                batch.draw(t, x * tileSize, (map.length - y - 1) * tileSize, tileSize, tileSize);
            }
        }
        for (Generator g : generators) g.render(batch);
        for (Door d : doors) d.render(batch);
        for (Trap t : traps) t.render(batch);
        for (Drone d : drones) d.render(batch);
        for (Item item : items) item.render(batch);
        player.render(batch);
        batch.end();
    }

    private void renderProgressBars() {
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        boolean allGeneratorsDone = getCompletedGenerators() == totalGenerators;

        // Generators
        for (Generator g : generators) g.renderProgressBar(shapeRenderer);

        // Doors
        for (Door d : doors) d.renderProgressBar(shapeRenderer);

        shapeRenderer.end();
    }

    private void renderHUD() {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // --- Top Left: Controls ---
        font.setColor(1, 1, 1, 1);
        font.draw(batch, controlInstructions, 20, Gdx.graphics.getHeight() - 20);

        // --- Top Right: Generator Counter ---
        String genText = "Generators: " + getCompletedGenerators() + "/" + totalGenerators;
        GlyphLayout gl = new GlyphLayout(font, genText);
        font.draw(batch, genText, Gdx.graphics.getWidth() - gl.width - 20, Gdx.graphics.getHeight() - 20);

        // --- Top Right: Objective Instructions ---
        if (getCompletedGenerators() < totalGenerators) {
            objectiveInstruction = "Finish all generators!";
        } else {
            objectiveInstruction = "Exits can now be interacted, open them and escape!";
        }

        GlyphLayout gl2 = new GlyphLayout(font, objectiveInstruction);
        font.draw(batch, objectiveInstruction,
                Gdx.graphics.getWidth() - gl2.width - 20,
                Gdx.graphics.getHeight() - 40); // slightly below generator counter

        batch.end();

        // --- Battery bar ---
        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
        shapeRenderer.rect(20, 20, 150, 15);
        shapeRenderer.setColor(0.2f, 1f, 0.2f, 1);
        shapeRenderer.rect(20, 20, 150 * (player.getBattery() / 100f), 15);
        shapeRenderer.end();

        // --- Hotbar ---
        float slotSize = 48f * (Gdx.graphics.getHeight() / 480f); 
        float spacing = 8f * (Gdx.graphics.getHeight() / 480f);
        float startX = (Gdx.graphics.getWidth() - (slotSize * 3 + spacing * 2)) / 2f;
        float hotbarY = 20 * (Gdx.graphics.getHeight() / 480f); // hotbar bottom

        // --- Item message above hotbar ---
        if (!itemMessage.isEmpty()) {
            GlyphLayout layout = new GlyphLayout(font, itemMessage);
            float x = (Gdx.graphics.getWidth() - layout.width) / 2f; // center horizontally
            float y = hotbarY + slotSize + 8f + layout.height; // top of hotbar + padding + text height
            font.setColor(1, 1, 0, 1);
            batch.begin();
            font.draw(batch, layout, x, y);
            batch.end();
        }

        // --- Hotbar slots ---
        shapeRenderer.begin(ShapeRenderer.ShapeType.Line);
        for (int i = 0; i < 3; i++) {
            shapeRenderer.rect(startX + i * (slotSize + spacing), hotbarY, slotSize, slotSize);
        }
        shapeRenderer.end();

        // --- Hotbar numbers & icons inside slots ---
        batch.begin();
        for (int i = 0; i < 3; i++) {
            // Draw slot number near bottom
            font.setColor(1, 1, 1, 1);
            font.draw(batch, String.valueOf(i + 1),
                startX + i * (slotSize + spacing) + slotSize / 2f - 4,
                hotbarY + 15);

            // Draw icon centered in slot
            ItemType item = player.getItem(i);
            Texture icon = null;
            if (item != null) {
                switch (item) { // classic Java 11 switch
                    case SHIELD:
                        icon = shieldTexture;
                        break;
                    case SODA:
                        icon = sodaTexture;
                        break;
                    case BATTERY:
                        icon = batteryTexture;
                        break;
                }
            }

            if (icon != null) {
                float padding = 8f;
                float iconSize = slotSize - padding * 2;
                // Center the icon inside the slot
                float iconX = startX + i * (slotSize + spacing) + (slotSize - iconSize) / 2f;
                float iconY = hotbarY + (slotSize - iconSize) / 2f;
                batch.draw(icon, iconX, iconY, iconSize, iconSize);
            }
        }

        // --- Status Effects (Right side) ---
        float statusX = Gdx.graphics.getWidth() - 20; // margin from right
        float statusY = Gdx.graphics.getHeight() - 80; // starting slightly below top

        font.setColor(1, 1, 0, 1); // yellow for active status

        if (player.isShieldActive()) {
            String shieldText = "Invulnerable";
            GlyphLayout layout = new GlyphLayout(font, shieldText);
            font.draw(batch, shieldText, statusX - layout.width, statusY);
            statusY -= layout.height + 5; // move down for next status
        }

        if (player.isSpeedBoostActive()) {
            String speedText = "Speed";
            GlyphLayout layout = new GlyphLayout(font, speedText);
            font.draw(batch, speedText, statusX - layout.width, statusY);
        }

        batch.end();
    }

    private void renderDarkness() {
        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();

        float scale = currentLightRadius / maxLightRadius;
        float w = lightMask.getWidth() * scale * 2f;
        float h = lightMask.getHeight() * scale * 2f;

        float x = player.position.x + player.size / 2f - w / 2f;
        float y = player.position.y + player.size / 2f - h / 2f;

        batch.draw(lightMask, x, y, w, h);
        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);
    }

    // ======================
    // FLASHLIGHT
    // ======================
    private void updateFlashlight(float delta) {
        if (flashlightLocked) {
            flashlightLockTimer -= delta;
            if (flashlightLockTimer <= 0) flashlightLocked = false;
        }

        boolean held = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        if (!flashlightLocked && held && player.getBattery() > 0) {
            currentLightRadius = Math.min(maxLightRadius, currentLightRadius + lightGrowSpeed * delta);
            player.drainBattery(batteryDrainRate * delta);
            if (player.getBattery() <= 0) {
                flashlightLocked = true;
                flashlightLockTimer = flashlightLockDuration;
            }
        } else {
            currentLightRadius = Math.max(minLightRadius, currentLightRadius - lightShrinkSpeed * delta);
            if (!flashlightLocked)
                player.rechargeBattery(batteryRechargeRate * delta);
        }

        if (held && !wasLightHeld) lightExpandSound.play(0.5f);
        if (!held && wasLightHeld) lightShrinkSound.play(0.4f);
        wasLightHeld = held;
    }

    private void updateProgressSounds() {
        int completed = getCompletedGenerators();
        if (completed > lastCompletedGenerators) generatorFixSound.play(0.7f);
        lastCompletedGenerators = completed;
    }

    // ======================
    // CAMERA
    // ======================
    private void updateCamera() {
        camera.position.set(player.position.x + player.size / 2f, player.position.y + player.size / 2f, 0);
        float hw = camera.viewportWidth * camera.zoom / 2f;
        float hh = camera.viewportHeight * camera.zoom / 2f;
        camera.position.x = MathUtils.clamp(camera.position.x, hw, map[0].length * tileSize - hw);
        camera.position.y = MathUtils.clamp(camera.position.y, hh, map.length * tileSize - hh);
        camera.update();
    }

    // ======================
    // ENTITY SPAWN
    // ======================
    private void spawnEntities() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                String tile = map[y][x];
                Vector2 pos = new Vector2(x * tileSize, (map.length - y - 1) * tileSize);

                switch (tile) {
                    case "P":
                        player = new Player(pos, playerTexture, tileSize);
                        break;
                    case "D":
                        drones.add(new Drone(pos, droneTexture, tileSize));
                        break;
                    case "T":
                        traps.add(new Trap(pos, trapTexture, tileSize));
                        break;
                    case "G":
                        generators.add(new Generator(pos, generatorTexture, tileSize));
                        break;
                    case "I":
                        ItemType randomType = ItemType.values()[MathUtils.random(ItemType.values().length - 1)];
                        items.add(new Item(pos, getItemTexture(randomType), randomType, tileSize));
                        break;
                    case "E":
                        doors.add(new Door(pos, doorClosedTexture, doorOpenTexture, tileSize));
                        break;
                }
            }
        }
    }

    private Texture createRadialLightMask(int size, float radius) {
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);
        float c = size / 2f;
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float d = Vector2.dst(x, y, c, c);
                float a = MathUtils.clamp(d / radius, 0, 1);
                pm.setColor(0, 0, 0, a);
                pm.drawPixel(x, y);
            }
        }
        Texture t = new Texture(pm);
        pm.dispose();
        return t;
    }

    private Texture getItemTexture(ItemType type) {
        switch (type) {
            case SHIELD:
                return shieldTexture;    // make sure you loaded this in create()
            case SODA:
                return sodaTexture;      // or whatever texture you use for speed
            case BATTERY:
                return batteryTexture;
            default:
                return null;
        }
    }

    private int getCompletedGenerators() {
        int c = 0;
        for (Generator g : generators) if (g.isCompleted()) c++;
        return c;
    }

    private void checkPlayerCollisions() {

        if (player.isShieldActive()) return;

        for (Trap t : traps) {
            if (overlaps(t.position.x, t.position.y, t.size, t.size)) {
                triggerGameOver();
                return;
            }
        }

        for (Drone d : drones) {
            if (player.isShieldActive()) continue;
            if (overlaps(d.position.x, d.position.y, d.size, d.size)) {
                triggerGameOver();
                return;
            }
        }
    }

    private Item createRandomItem(Vector2 position) {
        int r = MathUtils.random(2); // 0,1,2

        if (r == 0) {
            return new Item(
                position,
                shieldTexture,
                ItemType.SHIELD,
                tileSize
            );
        } else if (r == 1) {
            return new Item(
                position,
                sodaTexture,
                ItemType.SODA,
                tileSize
            );
        } else {
            return new Item(
                position,
                batteryTexture,
                ItemType.BATTERY,
                tileSize
            );
        }
    }

    public void triggerGameOver() {
        if (gameState == GameState.GAME_OVER) return;

        gameState = GameState.GAME_OVER;
        defeatSound.play(0.8f);
    }

    private void renderVictoryScreen() {
        renderMenuWithTitle("YOU ESCAPED", endScreenButtons);
    }

    private void renderDefeatScreen() {
        renderMenuWithTitle("YOU DIED", endScreenButtons);
    }

    private void updateGame(float delta) {

        player.update(delta, map, generators, doors);

        for (Drone d : drones) {
            d.update(delta, map, traps, trapTexture, player);
        }

        checkPlayerCollisions();
        updateDoors();
        checkPlayerOnDoors();

        // Item pickup
        for (Item item : items) {
            if (!item.collected &&
                player.position.x + player.size > item.position.x &&
                player.position.x < item.position.x + item.size &&
                player.position.y + player.size > item.position.y &&
                player.position.y < item.position.y + item.size) {

                if (player.addItem(item.type)) {
                    item.collected = true;
                    itemPickupSound.play(0.7f, MathUtils.random(0.95f, 1.05f), 0);

                    switch (item.type) {
                        case BATTERY:
                            itemMessage = "Battery: Refills flashlight!";
                            break;
                        case SHIELD:
                            itemMessage = "Shield: Invulnerability for 5 seconds!";
                            break;
                        case SODA:
                            itemMessage = "Soda: Speed boost for 10 seconds!";
                            break;
                    }
                    itemMessageTimer = itemMessageDuration; // reset timer
                }
            }
        }

        updateFlashlight(delta);
        updateProgressSounds();

        if (itemMessageTimer > 0) {
            itemMessageTimer -= delta;
            if (itemMessageTimer <= 0) itemMessage = ""; // clear message when done
        }
    }

    private void renderGame() {
        updateCamera();

        renderWorld();
        renderDarkness();

        renderProgressBars();
        renderHUD();
    }

    private void restartGame() {
        resetWorld();
        gameState = GameState.PLAYING;
    }

    private void returnToMainMenu() {
        gameState = GameState.MAIN_MENU;
    }

    private boolean overlaps(float x, float y, float w, float h) {
        return player.position.x + player.size > x &&
            player.position.x < x + w &&
            player.position.y + player.size > y &&
            player.position.y < y + h;
    }

    private void resetWorld() {
        drones.clear();
        traps.clear();
        items.clear();
        doors.clear();
        generators.clear();

        map = generateMap();
        spawnEntities();

        introTimer = 0f;
        postIntroTimer = 0f;
        currentIntroLine = 0;

        for (Generator g : generators) g.reset();
        for (Door d : doors) d.reset();

        Vector2 spawnPos = findPlayerSpawn();
        player.reset(spawnPos);

        currentLightRadius = minLightRadius;
        flashlightLocked = false;
        flashlightLockTimer = 0f;
        lastCompletedGenerators = 0;
        totalGenerators = generators.size();
    }

    private Vector2 findPlayerSpawn() {
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                if (map[y][x].equals("P")) {
                    // Convert tile coordinates to world position
                    return new Vector2(x * tileSize, (map.length - y - 1) * tileSize);
                }
            }
        }
        // Fallback in case "P" is not found
        return new Vector2(tileSize, tileSize);
    }

    private String[][] generateMap() {
        int size = 20;
        String[][] map = new String[size][size];

        // Step 1: Fill boundaries with walls, inner also initially walls
        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                if (y == 0 || y == size - 1 || x == 0 || x == size - 1) {
                    map[y][x] = "W";
                } else {
                    map[y][x] = "W";
                }
            }
        }

        // Step 2: Carve out floors using randomized growth (hallway width = 4)
        int startX = 1 + MathUtils.random(2);
        int startY = 1 + MathUtils.random(2);

        map[startY][startX] = "F";
        ArrayList<Vector2> frontier = new ArrayList<>();
        frontier.add(new Vector2(startX, startY));

        while (!frontier.isEmpty()) {
            Vector2 pos = frontier.remove(MathUtils.random(frontier.size() - 1));
            int x = (int) pos.x;
            int y = (int) pos.y;

            int[][] dirs = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : dirs) {
                int nx = x + d[0]*2; // wider step
                int ny = y + d[1]*2;

                if (nx > 0 && nx < size-1 && ny > 0 && ny < size-1 && map[ny][nx].equals("W")) {
                    // Carve out 4x4 floor
                    for (int dy = 0; dy <= 3; dy++) {
                        for (int dx = 0; dx <= 3; dx++) {
                            int fx = x + d[0]*dx;
                            int fy = y + d[1]*dy;
                            if (fx > 0 && fx < size-1 && fy > 0 && fy < size-1) {
                                map[fy][fx] = "F";
                            }
                        }
                    }
                    frontier.add(new Vector2(nx, ny));
                }
            }
        }

        // Step 3: Place doors adjacent to walls but not on boundary
        // Guaranteed top door
        ArrayList<Integer> topCandidates = new ArrayList<>();
        for (int x = 1; x < size-1; x++) {
            if (map[1][x].equals("F")) topCandidates.add(x);
        }
        if (!topCandidates.isEmpty()) {
            int topDoorX = topCandidates.get(MathUtils.random(topCandidates.size() - 1));
            map[1][topDoorX] = "E";
        }

        // Guaranteed bottom door
        ArrayList<Integer> bottomCandidates = new ArrayList<>();
        for (int x = 1; x < size-1; x++) {
            if (map[size-2][x].equals("F")) bottomCandidates.add(x);
        }
        if (!bottomCandidates.isEmpty()) {
            int bottomDoorX = bottomCandidates.get(MathUtils.random(bottomCandidates.size() - 1));
            map[size-2][bottomDoorX] = "E";
        }

        // Step 4: Place player near left boundary avoiding door
        for (int y = 1; y < size-1; y++) {
            for (int x = 1; x < size-1; x++) {
                if (map[y][x].equals("F")) {
                    map[y][x] = "P";
                    y = size; // break outer loop
                    break;
                }
            }
        }

        // Step 5: Place drone at opposite end
        for (int y = size-2; y > 0; y--) {
            for (int x = size-2; x > 0; x--) {
                if (map[y][x].equals("F")) {
                    map[y][x] = "D";
                    y = -1; // break outer loop
                    break;
                }
            }
        }

        // Step 6: Place 3 generators adjacent to at least 1 "F"
        int generatorsPlaced = 0;
        while (generatorsPlaced < 3) {
            int x = 1 + MathUtils.random(size-2-1);
            int y = 1 + MathUtils.random(size-2-1);

            if (!map[y][x].equals("F")) continue;

            boolean hasAdjacentFloor = false;
            int[][] adj = {{0,1},{0,-1},{1,0},{-1,0}};
            for (int[] d : adj) {
                int nx = x + d[0];
                int ny = y + d[1];
                if (map[ny][nx].equals("F")) {
                    hasAdjacentFloor = true;
                    break;
                }
            }

            if (hasAdjacentFloor) {
                map[y][x] = "G";
                generatorsPlaced++;
            }
        }

        // Step 7: Place 5 items randomly
        int itemsPlaced = 0;
        while (itemsPlaced < 5) {
            int x = 1 + MathUtils.random(size-2-1);
            int y = 1 + MathUtils.random(size-2-1);
            if (map[y][x].equals("F")) {
                map[y][x] = "I";
                itemsPlaced++;
            }
        }

        // Step 8: Place 5 traps randomly (not adjacent to other traps)
        int trapsPlaced = 0;
        while (trapsPlaced < 5) {
            int x = 1 + MathUtils.random(size-2-1);
            int y = 1 + MathUtils.random(size-2-1);
            if (!map[y][x].equals("F")) continue;

            boolean adjacent = false;
            for (int dy=-1; dy<=1; dy++) {
                for (int dx=-1; dx<=1; dx++) {
                    int nx = x+dx;
                    int ny = y+dy;
                    if (nx > 0 && nx < size-1 && ny > 0 && ny < size-1) {
                        if (map[ny][nx].equals("T")) adjacent = true;
                    }
                }
            }

            if (!adjacent) {
                map[y][x] = "T";
                trapsPlaced++;
            }
        }

        return map;
    }

    private void setupMenus() {
        float w = Gdx.graphics.getWidth();
        float h = Gdx.graphics.getHeight();
        float btnWidth = w * 0.25f;
        float btnHeight = h * 0.08f;
        float centerX = w / 2f - btnWidth / 2f;
        float spacing = btnHeight * 1.2f;

        // --- Main Menu ---
        mainMenuButtons.clear();
        mainMenuButtons.add(new Button(centerX, h/2 + spacing, btnWidth, btnHeight, "PLAY", () -> {
            resetWorld(); 
            gameState = GameState.INTRO;
        }));
        mainMenuButtons.add(new Button(centerX, h/2, btnWidth, btnHeight, "QUIT", () -> Gdx.app.exit()));

        // --- Pause Menu ---
        pauseMenuButtons.clear();
        pauseMenuButtons.add(new Button(centerX, h/2 + spacing, btnWidth, btnHeight, "RESUME", () -> gameState = GameState.PLAYING));
        pauseMenuButtons.add(new Button(centerX, h/2, btnWidth, btnHeight, "RESTART", this::restartGame));
        pauseMenuButtons.add(new Button(centerX, h/2 - spacing, btnWidth, btnHeight, "EXIT TO MENU", this::returnToMainMenu));

        // --- End Screens ---
        endScreenButtons.clear();
        endScreenButtons.add(new Button(centerX, h/2 + spacing, btnWidth, btnHeight, "RESTART", this::restartGame));
        endScreenButtons.add(new Button(centerX, h/2 - spacing, btnWidth, btnHeight, "EXIT TO MENU", this::returnToMainMenu));
    }

    private void renderMenu(ArrayList<Button> buttons) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        for (Button b : buttons) b.render(batch, font);
        batch.end();
    }

    private void updateMenuInput(ArrayList<Button> buttons) {
        float mouseX = Gdx.input.getX();
        float mouseY = Gdx.graphics.getHeight() - Gdx.input.getY(); // flip Y
        for (Button b : buttons) b.update(mouseX, mouseY);

        if (Gdx.input.justTouched()) {
            for (Button b : buttons) if (b.isClicked(mouseX, mouseY)) b.click();
        }
    }

    private void renderMenuWithTitle(String title, ArrayList<Button> buttons) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Draw the title
        BitmapFont titleFont = font; // you can use a separate larger font if you want
        GlyphLayout layout = new GlyphLayout(titleFont, title);
        float x = Gdx.graphics.getWidth() / 2f - layout.width / 2f;
        float y = Gdx.graphics.getHeight() * 0.8f; // 80% from bottom
        titleFont.draw(batch, layout, x, y);

        batch.end();

        // Draw buttons
        renderMenu(buttons);
    }

    private void renderMainMenu() {
        renderMenuWithTitle("LAST LIGHT", mainMenuButtons);
    }

    private void renderPauseMenu() {
        renderMenuWithTitle("Paused", pauseMenuButtons);
    }

    private void renderIntro(float delta) {
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Update timer
        introTimer += delta;

        // Check if we need to display the next line
        if (currentIntroLine < introLines.length && introTimer >= lineInterval * currentIntroLine) {
            beepSound.play(0.5f);
            currentIntroLine++;
        }

        // Draw all lines that should appear so far
        for (int i = 0; i < currentIntroLine; i++) {
            String line = introLines[i];
            GlyphLayout layout = new GlyphLayout(font, line);
            font.draw(batch, line, Gdx.graphics.getWidth()/2f - layout.width/2f,
                    Gdx.graphics.getHeight() * 0.7f - i * 30); // line spacing 30
        }

        batch.end();

        // ⭐ Delay AFTER last line
        if (currentIntroLine >= introLines.length) {
            postIntroTimer += delta;

            if (postIntroTimer >= postIntroDelay) {
                gameState = GameState.PLAYING;
            }
        }
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        wallTexture.dispose();
        floorTexture.dispose();
        trapTexture.dispose();
        droneTexture.dispose();
        playerTexture.dispose();
        generatorTexture.dispose();
        doorClosedTexture.dispose();
        doorOpenTexture.dispose();
        lightMask.dispose();
        font.dispose();
        victoryTexture.dispose();
        gameOverTexture.dispose();
        lightExpandSound.dispose();
        lightShrinkSound.dispose();
        generatorFixSound.dispose();
        doorOpenSound.dispose();
        victorySound.dispose();
        defeatSound.dispose();
        itemPickupSound.dispose();
    }
}
