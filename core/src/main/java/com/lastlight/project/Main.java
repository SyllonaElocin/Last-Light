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
    private OrthographicCamera camera, hudCamera;

    private Texture wallTexture, floorTexture, trapTexture, droneTexture, playerTexture;
    private Texture generatorTexture, doorClosedTexture, doorOpenTexture;
    private Texture lightMask;
    private Texture victoryTexture, gameOverTexture;

    private Player player;
    private ArrayList<Drone> drones = new ArrayList<>();
    private ArrayList<Trap> traps = new ArrayList<>();
    private ArrayList<Generator> generators = new ArrayList<>();
    private ArrayList<Door> doors = new ArrayList<>();

    private final float tileSize = 64f;
    private final float minLightRadius = 128f;
    private final float maxLightRadius = 512f;
    private float currentLightRadius = 128f;
    private final float lightGrowSpeed = 64f; // pixels per second
    private final float lightShrinkSpeed = 64f; 
    private final float pixelScale = 2f; // how much each pixel is scaled
    private float battery = 100f;           // percentage
    private final float batteryDrainRate = 10f; // % per second when LMB held
    private final float batteryRechargeRate = 20f; // % per second when not using

    private ShapeRenderer shapeRenderer;

    private boolean flashlightLocked = false;
    private float flashlightLockTimer = 0f;
    private final float flashlightLockDuration = 2f;

    private BitmapFont font;
    private boolean gameWon = false;
    private float victoryTimer = 0f;

    private boolean gameOver = false;
    private float gameOverTimer = 0f; // countdown before exit

    String[][] map = { 
        {"W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W"}, 
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"}, 
        {"W","F","F","F","W","W","W","W","F","G","F","F","W","W","W","W","F","F","F","W"}, 
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","G","W"}, 
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"}, 
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

    @Override
    public void create() {
        batch = new SpriteBatch();

        camera = new OrthographicCamera(800, 480);
        camera.zoom = 0.75f;
        camera.update();

        hudCamera = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
        hudCamera.setToOrtho(false);
        hudCamera.update();

        font = new BitmapFont();
        font.getData().setScale(1f);
        font.setColor(1f,1f,0f,1f);

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

        lightMask = createRadialLightMask(8192, minLightRadius);

        shapeRenderer = new ShapeRenderer();

        // Spawn entities
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                String tile = map[y][x];
                Vector2 pos = new Vector2(x*tileSize, (map.length-y-1)*tileSize);
                switch(tile){
                    case "P": player = new Player(pos, playerTexture, tileSize); break;
                    case "D": drones.add(new Drone(pos, droneTexture, tileSize)); break;
                    case "T": traps.add(new Trap(pos, trapTexture, tileSize)); break;
                    case "G": generators.add(new Generator(pos, generatorTexture, tileSize)); break;
                }
            }
        }

        totalGenerators = generators.size();
        doors.add(new Door(new Vector2(1*tileSize, (map.length-2)*tileSize), doorClosedTexture, doorOpenTexture, tileSize));
        doors.add(new Door(new Vector2(18*tileSize, 1*tileSize), doorClosedTexture, doorOpenTexture, tileSize));
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();

        // Update player & drones
        player.update(delta, map, generators, doors);
        for (Drone d : drones) d.update(delta, map, traps, trapTexture);

        // Check for drone-player collisions
        if (!gameOver) {
            for (Drone d : drones) {
                if (playerDroneCollision(player, d)) {
                    gameOver = true;
                    gameOverTimer = 0f;
                    break;
                }
            }
        }

        //Check for trap-player collisions
        if (!gameOver) {
            for (Trap t : traps) {
                if (playerTrapCollision(player, t)) {
                    gameOver = true;
                    gameOverTimer = 0f;
                    break;
                }
            }
        }



        // Camera follow
        camera.position.set(player.position.x+player.size/2f, player.position.y+player.size/2f,0);
        float halfW = camera.viewportWidth*camera.zoom/2f;
        float halfH = camera.viewportHeight*camera.zoom/2f;
        camera.position.x = MathUtils.clamp(camera.position.x, halfW, map[0].length*tileSize-halfW);
        camera.position.y = MathUtils.clamp(camera.position.y, halfH, map.length*tileSize-halfH);
        camera.update();
        batch.setProjectionMatrix(camera.combined);

        // Render world
        batch.begin();
        for(int y=0;y<map.length;y++){
            for(int x=0;x<map[y].length;x++){
                Vector2 pos = new Vector2(x*tileSize,(map.length-y-1)*tileSize);
                Texture tex = map[y][x].equals("W")? wallTexture : floorTexture;
                batch.draw(tex,pos.x,pos.y,tileSize,tileSize);
            }
        }
        for(Generator g: generators) g.render(batch);
        for(Door d: doors) d.render(batch);
        for(Drone d: drones) d.render(batch);
        for(Trap t: traps) t.render(batch);
        player.render(batch);
        batch.end();

        // ======================
        // FLASHLIGHT + BATTERY
        // ======================

        // Update lock timer
        if (flashlightLocked) {
            flashlightLockTimer -= delta;
            if (flashlightLockTimer <= 0f) {
                flashlightLocked = false;
                flashlightLockTimer = 0f;
            }
        }

        boolean lmbHeld = Gdx.input.isButtonPressed(Input.Buttons.LEFT);

        // If flashlight is usable
        if (!flashlightLocked && lmbHeld && battery > 0f) {

            // Grow light
            currentLightRadius += lightGrowSpeed * delta;
            if (currentLightRadius > maxLightRadius)
                currentLightRadius = maxLightRadius;

            // Drain battery
            battery -= batteryDrainRate * delta;

            if (battery <= 0f) {
                battery = 0f;

                // 🔒 Lock flashlight
                flashlightLocked = true;
                flashlightLockTimer = flashlightLockDuration;
            }

        } else {
            // Shrink light
            currentLightRadius -= lightShrinkSpeed * delta;
            if (currentLightRadius < minLightRadius)
                currentLightRadius = minLightRadius;

            // Recharge only if NOT locked
            if (!flashlightLocked) {
                battery += batteryRechargeRate * delta;
                if (battery > 100f)
                    battery = 100f;
            }
        }
        
        // Generator bars
        shapeRenderer.setProjectionMatrix(camera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        for (Generator g : generators) {
            g.renderProgressBar(shapeRenderer);
        }

        shapeRenderer.end();


        // ======================
        // DARKNESS OVERLAY
        // ======================
        batch.setBlendFunction(GL20.GL_ZERO, GL20.GL_ONE_MINUS_SRC_ALPHA);
        batch.begin();

        float scale = currentLightRadius / maxLightRadius;

        // compute final size using pixelScale
        float drawWidth = lightMask.getWidth() * scale * pixelScale;
        float drawHeight = lightMask.getHeight() * scale * pixelScale;

        // center the mask on the player
        float maskX = player.position.x + player.size/2f - drawWidth/2f;
        float maskY = player.position.y + player.size/2f - drawHeight/2f;

        batch.draw(lightMask, maskX, maskY, drawWidth, drawHeight);

        batch.end();
        batch.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA);


        // Doors logic
        boolean allGeneratorsCompleted = getCompletedGenerators()==totalGenerators;
        for(Door d: doors) if(allGeneratorsCompleted) d.setOpenable(true);
        for(Door d: doors) if(d.isOpen() && playerIsOverlapping(player,d.position)) gameWon=true;

        // HUD
        batch.setProjectionMatrix(hudCamera.combined);
        batch.begin();
        String hudText = "Generators: "+getCompletedGenerators()+"/"+totalGenerators;
        GlyphLayout layout = new GlyphLayout(font,hudText);
        float margin = 20f;
        font.draw(batch,hudText,Gdx.graphics.getWidth()-margin-layout.width,Gdx.graphics.getHeight()-margin);
        batch.end();

        // Draw battery bar
        float barWidth = 150f;
        float barHeight = 15f;
        float barX = 20f;
        float barY = 20f;

        shapeRenderer.setProjectionMatrix(hudCamera.combined);
        shapeRenderer.begin(ShapeRenderer.ShapeType.Filled);

        // Background
        shapeRenderer.setColor(0.3f, 0.3f, 0.3f, 1f);
        shapeRenderer.rect(barX, barY, barWidth, barHeight);

        // Battery level
        shapeRenderer.setColor(0.2f, 1f, 0.2f, 1f);
        shapeRenderer.rect(barX, barY, barWidth * (battery / 100f), barHeight);

        shapeRenderer.end();


        // Victory screen
        if(gameWon){
            victoryTimer+=delta;
            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            batch.draw(victoryTexture,0,0,Gdx.graphics.getWidth(),Gdx.graphics.getHeight());
            batch.end();
            if(victoryTimer>=3f) Gdx.app.exit();
        }

        // Game Over screen
        if (gameOver) {
            gameOverTimer += Gdx.graphics.getDeltaTime();

            batch.setProjectionMatrix(hudCamera.combined);
            batch.begin();
            batch.draw(gameOverTexture, 0, 0, Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
            batch.end();

            if (gameOverTimer >= 3f) { // wait 3 seconds
                Gdx.app.exit();
            }
        }

        // Prevent updates after game over
        if (!gameOver) {
            player.update(delta, map, generators, doors);
        }
    }

    private Texture createRadialLightMask(int size, float radius){
        Pixmap pm = new Pixmap(size,size, Pixmap.Format.RGBA8888);
        float center = size/2f;
        for(int y=0;y<size;y++){
            for(int x=0;x<size;x++){
                float dx = x-center;
                float dy = y-center;
                float dist = (float)Math.sqrt(dx*dx + dy*dy);
                float alpha = MathUtils.clamp(dist/radius,0f,1f);
                pm.setColor(0f,0f,0f,alpha);
                pm.drawPixel(x,y);
            }
        }
        Texture tex = new Texture(pm);
        pm.dispose();
        return tex;
    }

    private boolean playerIsOverlapping(Player p, Vector2 objPos){
        return p.position.x+p.size>objPos.x && p.position.x<objPos.x+tileSize &&
               p.position.y+p.size>objPos.y && p.position.y<objPos.y+tileSize;
    }

    private int getCompletedGenerators(){
        int count=0;
        for(Generator g: generators) if(g.isCompleted()) count++;
        return count;
    }

    private boolean playerDroneCollision(Player p, Drone d) {
        float px = p.position.x + p.size/2f;
        float py = p.position.y + p.size/2f;
        float dx = d.position.x + d.size/2f;
        float dy = d.position.y + d.size/2f;

        float distance = Vector2.dst(px, py, dx, dy);
        float collisionDist = (p.size/2f) + (d.size/2f);
        return distance < collisionDist;
    }

    private boolean playerTrapCollision(Player p, Trap t) {
        float px = p.position.x + p.size/2f;
        float py = p.position.y + p.size/2f;
        float tx = t.position.x + t.size/2f;
        float ty = t.position.y + t.size/2f;

        float distance = Vector2.dst(px, py, tx, ty);
        float collisionDist = (p.size/2f) + (t.size/2f);
        return distance < collisionDist;
    }

    @Override
    public void dispose() {
        batch.dispose();
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
    }
}
