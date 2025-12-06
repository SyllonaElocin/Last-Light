package com.lastlight.project;

import com.badlogic.gdx.ApplicationAdapter;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import java.util.ArrayList;

public class Main extends ApplicationAdapter {
    SpriteBatch batch;
    OrthographicCamera camera;

    Texture playerTexture;
    Texture wallTexture;
    Texture floorTexture;
    Texture trapTexture;
    Texture droneTexture;

    Player player;
    ArrayList<Drone> drones = new ArrayList<>();
    ArrayList<Trap> traps = new ArrayList<>();

    final float tileSize = 64f;

    String[][] map = {
        {"W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","D","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","T","F","W","W","W","W","F","F","F","W"},
        {"W","F","P","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","F","F","F","W","W","W","W","F","F","F","F","W","W","W","W","F","F","F","W"},
        {"W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W","W"},
    };

    @Override
    public void create() {
        batch = new SpriteBatch();
        camera = new OrthographicCamera(800, 480);
        camera.setToOrtho(false);

        // Load textures
        playerTexture = new Texture("Player.png");
        wallTexture = new Texture("Wall.png");
        floorTexture = new Texture("Floor.png");
        trapTexture = new Texture("Trap.png");
        droneTexture = new Texture("Drone.png");

        // Parse map
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                String tile = map[y][x];
                Vector2 pos = new Vector2(x * tileSize, (map.length - y - 1) * tileSize);

                switch (tile) {
                    case "P": 
                        player = new Player(pos, playerTexture, tileSize); 
                        break;

                    case "D":
                        drones.add(new Drone(pos, tileSize, droneTexture));
                        break;

                    case "T":
                        traps.add(new Trap(pos, trapTexture, tileSize));
                        break;
                }
            }
        }
    }

    @Override
    public void render() {
        float delta = Gdx.graphics.getDeltaTime();
        player.update(delta);

        camera.position.set(player.position.x + tileSize/2, player.position.y + tileSize/2, 0);
        camera.update();

        Gdx.gl.glClearColor(0, 0, 0.2f, 1);
        Gdx.gl.glClear(GL20.GL_COLOR_BUFFER_BIT);

        batch.setProjectionMatrix(camera.combined);
        batch.begin();

        // Draw map
        for (int y = 0; y < map.length; y++) {
            for (int x = 0; x < map[y].length; x++) {
                String tile = map[y][x];
                Vector2 pos = new Vector2(x * tileSize, (map.length - y - 1) * tileSize);

                switch (tile) {
                    case "W": batch.draw(wallTexture, pos.x, pos.y); break;
                    case "F": batch.draw(floorTexture, pos.x, pos.y); break;
                    case "T": batch.draw(trapTexture, pos.x, pos.y); break;
                    default: batch.draw(floorTexture, pos.x, pos.y); break;
                }
            }
        }

        // Render traps
        for (Trap t : traps) t.render(batch);

        // Render drones
        for (Drone d : drones) {
            d.update(delta);
            d.render(batch);
        }

        // Render player
        player.render(batch);

        batch.end();
    }

    @Override
    public void dispose() {
        batch.dispose();
        playerTexture.dispose();
        wallTexture.dispose();
        floorTexture.dispose();
        trapTexture.dispose();
        droneTexture.dispose();

        for (Trap t : traps) t.dispose();
    }
}
