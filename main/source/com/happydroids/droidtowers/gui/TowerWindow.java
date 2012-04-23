/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.NinePatch;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.badlogic.gdx.scenes.scene2d.ui.Skin;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.esotericsoftware.tablelayout.Cell;
import com.happydroids.droidtowers.TowerAssetManager;
import com.happydroids.droidtowers.input.InputCallback;
import com.happydroids.droidtowers.input.InputSystem;

import static com.happydroids.droidtowers.platform.Display.scale;

public class TowerWindow {
  private static final int[] DIALOG_CLOSE_KEYCODES = new int[]{InputSystem.Keys.ESCAPE, InputSystem.Keys.BACK};

  private InputCallback closeDialogCallback;
  private Runnable dismissCallback;
  protected final Stage stage;
  protected final Skin skin;
  private final Table content;
  private Table window;
  private final Label titleLabel;
  private final TransparentTextButton closeButton;

  public TowerWindow(String title, Stage stage, Skin skin) {
    this.stage = stage;
    this.skin = skin;

    window = new Table();
    window.touchable = true;
    window.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {

      }
    });

    Texture texture = TowerAssetManager.texture("hud/window-bg.png");
    texture.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
    window.setBackground(new NinePatch(texture));
    window.size((int) stage.width(), (int) stage.height());

    titleLabel = FontManager.Roboto32.makeLabel(title);
    closeButton = FontManager.Roboto18.makeTransparentButton("< back", skin);

    Table topBar = new Table();
    topBar.row().fill();
    topBar.add(closeButton).fill();
    topBar.add(new VerticalRule(scale(2))).fillY();
    topBar.add(titleLabel).center().left().expand().pad(scale(4)).padLeft(scale(12));

    window.add(topBar).fill();

    window.row();
    window.add(new HorizontalRule(scale(2))).expandX();
    window.row().fill().height((int) (stage.height() - scale(46))).padLeft(scale(24)).padRight(scale(24));

    content = new Table();
    content.row().expandX();
    window.add(content).fill();

    closeButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        dismiss();
      }
    });
  }

  public Cell add(Actor actor) {
    return content.add(actor);
  }

  public Cell row() {
    return content.row();
  }

  public TowerWindow show() {
    bindKeys();

    window.invalidate();
    window.pack();
    stage.addActor(window);

    return this;
  }

  public void dismiss() {
    unbindKeys();

    if (dismissCallback != null) {
      dismissCallback.run();
    }

    stage.setScrollFocus(null);
    stage.setKeyboardFocus(null);

    window.markToRemove(true);
  }

  public void setDismissCallback(Runnable dismissCallback) {
    this.dismissCallback = dismissCallback;
  }

  protected void debug() {
    content.debug();
  }

  protected void clear() {
    content.clear();
  }

  protected void bindKeys() {
    closeDialogCallback = new InputCallback() {
      public boolean run(float timeDelta) {
        TowerWindow.this.dismiss();
        return true;
      }
    };

    InputSystem.instance().bind(DIALOG_CLOSE_KEYCODES, closeDialogCallback);
  }

  protected void unbindKeys() {
    if (closeDialogCallback != null) {
      InputSystem.instance().unbind(DIALOG_CLOSE_KEYCODES, closeDialogCallback);
      closeDialogCallback = null;
    }
  }

  public TowerWindow setTitle(String title) {
    titleLabel.setText(title);

    return this;
  }
}
