package com.lastlight.project;

import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShapeRenderer;
import com.badlogic.gdx.graphics.g2d.GlyphLayout;

public class Button {
    private float x, y, width, height;
    private String text;
    private Runnable action;
    private boolean hovered;

    public Button(float x, float y, float width, float height, String text, Runnable action) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.text = text;
        this.action = action;
    }

    public void render(SpriteBatch batch, BitmapFont font) {
        // Draw button background
        batch.end();
        ShapeRenderer sr = new ShapeRenderer();
        sr.setProjectionMatrix(batch.getProjectionMatrix());
        sr.begin(ShapeRenderer.ShapeType.Filled);
        sr.setColor(hovered ? 0.2f : 0.1f, 0.2f, 0.5f, 1f);  // highlight on hover
        sr.rect(x, y, width, height);
        sr.end();
        batch.begin();

        // Draw button text centered
        GlyphLayout layout = new GlyphLayout(font, text);
        float textX = x + (width - layout.width) / 2f;
        float textY = y + (height + layout.height) / 2f;
        font.draw(batch, layout, textX, textY);
    }

    public void update(float mouseX, float mouseY) {
        hovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    public boolean isClicked(float mouseX, float mouseY) {
        return hovered;
    }

    public void click() {
        if (action != null) action.run();
    }
}
