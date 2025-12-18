package com.lastlight.project;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;

public class Item {

    public Vector2 position;
    public float size;
    public ItemType type;
    private Texture texture;
    public boolean collected = false;

    public Item(Vector2 position, Texture texture, ItemType type, float tileSize) {
        this.position = position;
        this.texture = texture;
        this.type = type;
        this.size = tileSize * 0.6f;
    }

    public void render(SpriteBatch batch) {
        if (!collected) {
            batch.draw(texture, position.x, position.y, size, size);
        }
    }
}
