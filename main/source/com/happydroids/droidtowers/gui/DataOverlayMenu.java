/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.ui.CheckBox;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Image;
import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.badlogic.gdx.utils.Scaling;
import com.happydroids.droidtowers.Colors;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.graphics.Overlays;
import com.happydroids.droidtowers.grid.GameGridRenderer;

import static com.happydroids.droidtowers.TowerAssetManager.texture;
import static com.happydroids.droidtowers.platform.Display.scale;

class DataOverlayMenu extends Table {
  private Texture triangle;
  private final Texture border;

  DataOverlayMenu(final GameGridRenderer gameGridRenderer) {
    triangle = texture(TowerAssetManager.WHITE_SWATCH_TRIANGLE);
    border = texture(TowerAssetManager.WHITE_SWATCH);
//    visible = false;

    setBackground(TowerAssetManager.ninePatch(TowerAssetManager.WHITE_SWATCH, Colors.TRANSPARENT_BLACK));

    defaults().top().left().pad(6);

    for (final Overlays overlay : Overlays.values()) {
      final CheckBox checkBox = FontManager.Roboto18.makeCheckBox(overlay.toString());
      checkBox.getLabelCell().pad(0).spaceLeft(scale(8));
      checkBox.setClickListener(new ClickListener() {
        public void click(Actor actor, float x, float y) {
          if (checkBox.isChecked()) {
            gameGridRenderer.addActiveOverlay(overlay);
          } else {
            gameGridRenderer.removeActiveOverlay(overlay);
          }
        }
      });

      row().left();
      add(checkBox).pad(0);

      add(new Image(texture("swatches/" + overlay.getSwatchFilename()), Scaling.stretch))
              .width(16)
              .height(16);
    }

    TextButton clearAllButton = FontManager.Roboto18.makeTextButton("Clear All");
    clearAllButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        gameGridRenderer.clearOverlays();

        for (Actor child : getActors()) {
          if (child instanceof CheckBox) {
            ((CheckBox) child).setChecked(false);
          }
        }
      }
    });

    row().colspan(2);
    add(clearAllButton).fillX();
  }

  @Override
  protected void drawBackground(SpriteBatch batch, float parentAlpha) {
    super.drawBackground(batch, parentAlpha);

    batch.setColor(Colors.TRANSPARENT_BLACK);
    batch.draw(triangle, x + width - triangle.getWidth() * 1.3f, y + height);
  }

  public int getOffset() {
    return triangle.getHeight() / 2;
  }
}