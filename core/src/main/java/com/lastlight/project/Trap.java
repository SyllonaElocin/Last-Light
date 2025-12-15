package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Trap {
    public Vector2 position;
    private Texture texture;
    public float size;

    public Trap(Vector2 pos, Texture texture, float tileSize) {
        this.position = new Vector2(pos);
        this.texture = texture;
        this.size = tileSize * 0.9f;
    }

    public boolean isPlayerOnTrap(Player player) {
        float px = player.position.x + player.size / 2f;
        float py = player.position.y + player.size / 2f;

        return px > position.x && px < position.x + size &&
            py > position.y && py < position.y + size;
    }


    public void render(SpriteBatch batch) {
        batch.draw(texture, position.x, position.y, size, size);
    }

    public void dispose() {
        // Texture handled in Main
    }
}
