package com.lastlight.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.Input;
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
    private SpriteBatch batch;
    private ShapeRenderer shapeRenderer;
    private OrthographicCamera camera;

    private Texture wallTexture, floorTexture, trapTexture, droneTexture, playerTexture;
    private Texture generatorTexture, doorClosedTexture, doorOpenTexture;
    private Texture lightMask;

    private Texture victoryTexture;
    private boolean gameWon = false;
    private float victoryTimer = 0f;


    private Player player;
    private ArrayList<Drone> drones = new ArrayList<>();
    private ArrayList<Trap> traps = new ArrayList<>();
    private ArrayList<Generator> generators = new ArrayList<>();
    private ArrayList<Door> doors = new ArrayList<>();

    private final float tileSize = 64f;
    private final float lightRadius = 300f;

    private com.badlogic.gdx.graphics.g2d.BitmapFont font;

    String[][] map = {
        {"W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","G","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","G","W"},
        {"W","F","F","F","W","W","W","W","F","F","T","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","P","F","F","F","F","F","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","D","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","G","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","E","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","E","W"},
        {"W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W"}
    };

    private int totalGenerators = 0;
    private boolean playerInteracting = false;
    private OrthographicCamera hudCamera;

    @Override
    public void create() {
        batch = new SpriteBatch();
        shapeRenderer = new ShapeRenderer();
        camera = new OrthographicCamera(800, 480);
        camera.position.set(camera.viewportWidth/2f, camera.viewportHeight/2f, 0);
        camera.zoom = 0.75f;
        camera.update();
        font = new com.badlogic.gdx.graphics.g2d.BitmapFont();
        font.getData().setScale(1f);        // optional: make text bigger
        font.setColor(1f, 1f, 0f, 1f);      // yellow color
        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.setToOrtho(false); // Y-axis points down (top-left origin)
        hudCamera.update();

        // Load textures
        wallTexture = new Texture("Wall.png");
        floorTexture = new Texture("Floor.png");
        trapTexture = new Texture("Trap.png");
        droneTexture = new Texture("Drone.png");
        playerTexture = new Texture("Player.png");
        generatorTexture = new Texture("Generator.png");
        doorClosedTexture = new Texture("DoorClosed.png");
        doorOpenTexture = new Texture("DoorOpen.png");
        victoryTexture = new Texture("VictoryScreen.jpg"); // put your image in assets


        createLightMask();

        // Spawn entities
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                String tile = map[y][x];
                Vector2 pos = new Vector2(x * tileSize, (map.length - y - 1) * tileSize);

                switch (tile) {
                    case "P": player = new Player(pos, playerTexture, tileSize); break;
                    case "D": drones.add(new Drone(pos, droneTexture, tileSize)); break;
                    case "T": traps.add(new Trap(pos, trapTexture, tileSize)); break;
                    case "G": generators.add(new Generator(pos, generatorTexture, tileSize)); break;
                }
            }
        }

        totalGenerators = generators.size(); // total number of generators

        doors.add(new Door(new Vector2(1*tileSize, (map.length-2)*tileSize), doorClosedTexture, doorOpenTexture, tileSize));
        doors.add(new Door(new Vector2(18*tileSize, 1*tileSize), doorClosedTexture, doorOpenTexture, tileSize));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // ======================
        // UPDATE LOGIC
        // ======================
        player.update(delta, map, generators, doors);
        for (Drone d : drones) d.update(delta, map);

        // ======================
        // CAMERA
        // ======================
        camera.position.set(
            player.position.x + player.size / 2f,
            player.position.y + player.size / 2f,
            0
        );

        float halfW = camera.viewportWidth * camera.zoom / 2f;
        float halfH = camera.viewportHeight * camera.zoom / 2f;

        camera.position.x = MathUtils.clamp(
            camera.position.x,
            halfW,
            map[0].length * tileSize - halfW
        );
        camera.position.y = MathUtils.clamp(
            camera.position.y,
            halfH,
            map.length * tileSize - halfH
        );

        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // ======================
        // WORLD RENDER
        // ======================
        batch.begin();
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                Vector2 pos = new Vector2(
                    x * tileSize,
                    (map.length - y - 1) * tileSize
                );
                Texture tex = map[y][x].equals("W") ? wallTexture : floorTexture;
                batch.draw(tex, pos.x, pos.y, tileSize, tileSize);
            }
        }
        for (Generator g : generators) g.render(batch);
        for (Door d : doors) d.render(batch);
        for (Drone d : drones) d.render(batch);
        player.render(batch);
        batch.end();

        // Allow doors to be openable only if all generators completed
        boolean allGeneratorsCompleted = getCompletedGenerators() == totalGenerators;
        for (Door d : doors) {
            if (allGeneratorsCompleted) d.setOpenable(true);
        }

        // Victory check
        for (Door d : doors) {
            if (d.isOpen() && playerIsOverlapping(player, d.position)) {
                gameWon = true;
            }
        }

        // ======================
        // DARKNESS OVERLAY
        // ======================
        batch.begin();
        batch.setColor(0, 0, 0, 0.9f);
        batch.draw(
            floorTexture,
            camera.position.x - camera.viewportWidth,
            camera.position.y - camera.viewportHeight,
            camera.viewportWidth * 2,
            camera.viewportHeight * 2
        );
        batch.setColor(1, 1, 1, 1);
        batch.end();

        // ======================
        // LIGHT MASK
        // ======================
        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();
        batch.draw(
            lightMask,
            camera.position.x - lightRadius,
            camera.position.y - lightRadius,
            lightRadius * 2,
            lightRadius * 2
        );
        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);

        // ======================
        // UI RENDERING (WORLD-DEPENDENT, like generator prompts)
        // ======================
        Generator nearbyGen = null;
        for (Generator g : generators) {
            if (!g.isCompleted() && playerIsOverlapping(player, g.position)) {
                nearbyGen = g;
                break;
            }
        }

        if (nearbyGen != null) {
            // Box behind text
            shapeRenderer.setProjectionMatrix(camera.combined);
            shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
            float boxWidth = 150;
            float boxHeight = 30;
            float boxX = nearbyGen.position.x + tileSize / 2f - boxWidth / 2f;
            float boxY = nearbyGen.position.y + tileSize + 10;
            shapeRenderer.setColor(0f, 0f, 0f, 0.7f);
            shapeRenderer.rect(boxX, boxY, boxWidth, boxHeight);
            shapeRenderer.end();

            // Text above generator
            batch.setProjectionMatrix(camera.combined);
            batch.begin();
            font.draw(batch, "F  Fix Generator", boxX + 10, boxY + boxHeight - 8);
            batch.end();
        }

        // Progress bar for interacting
        if (Gdx.input.isKeyPressed(Input.Keys.F)) {
            for (Generator g : generators) {
                if (!g.isCompleted() && playerIsOverlapping(player, g.position)) {
                    float barWidth = 200;
                    float barHeight = 20;
                    float barX = player.position.x + player.size / 2f - barWidth / 2f;
                    float barY = player.position.y - 30;

                    shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);
                    shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1);
                    shapeRenderer.rect(barX, barY, barWidth, barHeight);

                    shapeRenderer.setColor(0.2f, 1f, 0.2f, 1);
                    shapeRenderer.rect(barX, barY, barWidth * (g.getProgress() / g.getMaxProgress()), barHeight);
                    shapeRenderer.end();
                }
            }
        }

        // ====================================
        // HUD RENDERING (fixed on screen)
        // ====================================
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();

        // Completed generators out of total
        int completed = getCompletedGenerators();
        String hudText = "Generators: " + completed + "/" + totalGenerators;

        GlyphLayout layout = new GlyphLayout(font, hudText);
        float margin = 20f;
        float x = Gdx.graphics.getWidth() - margin - layout.width; // align right
        float y = Gdx.graphics.getHeight() - margin;               // align top
        font.draw(batch, hudText, x, y);

        batch.end();

        // Victory timer
        if (gameWon) {
            victoryTimer += delta;
            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            batch.draw(victoryTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();

            if (victoryTimer >= 3f) {
                Gdx.app.exit(); // end program
            }
        }
    }



    private void createLightMask() {
        int size = 512; // bigger = smoother gradient
        Pixmap pm = new Pixmap(size, size, Pixmap.Format.RGBA8888);

        float radius = size / 2f;
        float innerBrightness = 0.3f; // 0 = fully dark at center, 1 = fully dark at center

        for (int y = 0; y < size; y++) {
            for (int x = 0; x < size; x++) {
                float dx = x - radius;
                float dy = y - radius;
                float dist = (float) Math.sqrt(dx*dx + dy*dy);

                // Smooth radial falloff with brighter center
                float alpha = MathUtils.clamp(dist / radius, 0f, 1f);
                alpha = alpha * alpha * alpha; // keeps edge smooth
                alpha = innerBrightness + alpha * (1 - innerBrightness); // scale so center is less dark

                pm.setColor(0, 0, 0, alpha);
                pm.drawPixel(x, y);
            }
        }

        lightMask = new Texture(pm);
        pm.dispose();
    }


    private boolean playerIsOverlapping(Player p, Vector2 objPos) {
        return p.position.x + p.size > objPos.x && p.position.x < objPos.x + tileSize &&
               p.position.y + p.size > objPos.y && p.position.y < objPos.y + tileSize;
    }

    private int getCompletedGenerators() {
        int count = 0;
        for (Generator g : generators) {
            if (g.isCompleted()) {
                count++;
            }
        }
        return count;
    }

    private boolean isBlocked(Vector2 newPos) {
        // Check walls
        int gridX = (int)(newPos.x / tileSize);
        int gridY = map.length - 1 - (int)(newPos.y / tileSize);
        if (map[gridY][gridX].equals("W")) return true;

        // Check doors
        for (Door d : doors) {
            if (d.isBlocking() &&
                newPos.x + player.size > d.position.x &&
                newPos.x < d.position.x + tileSize &&
                newPos.y + player.size > d.position.y &&
                newPos.y < d.position.y + tileSize) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void dispose() {
        batch.dispose();
        shapeRenderer.dispose();
        playerTexture.dispose();
        wallTexture.dispose();
        floorTexture.dispose();
        trapTexture.dispose();
        droneTexture.dispose();
        generatorTexture.dispose();
        doorClosedTexture.dispose();
        doorOpenTexture.dispose();
        lightMask.dispose();
        font.dispose();
    }
}
