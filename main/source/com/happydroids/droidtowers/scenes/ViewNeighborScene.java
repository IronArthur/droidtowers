/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.scenes;

import aurelienribon.tweenengine.BaseTween;
import aurelienribon.tweenengine.Tween;
import aurelienribon.tweenengine.TweenCallback;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.math.Vector2;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.ui.Label;
import com.google.common.collect.Lists;
import com.happydroids.droidtowers.TowerConsts;
import com.happydroids.droidtowers.TowerGame;
import com.happydroids.droidtowers.entities.GameLayer;
import com.happydroids.droidtowers.entities.GameObject;
import com.happydroids.droidtowers.gamestate.GameSave;
import com.happydroids.droidtowers.gamestate.server.FriendCloudGameSave;
import com.happydroids.droidtowers.gamestate.server.FriendCloudGameSaveCollection;
import com.happydroids.droidtowers.grid.NeighborGameGrid;
import com.happydroids.droidtowers.gui.*;
import com.happydroids.droidtowers.input.CameraController;
import com.happydroids.droidtowers.input.InputCallback;
import com.happydroids.droidtowers.input.InputSystem;
import com.happydroids.droidtowers.math.GridPoint;
import com.happydroids.droidtowers.tween.GameObjectAccessor;
import com.happydroids.droidtowers.tween.TweenSystem;
import com.happydroids.droidtowers.utils.Random;
import com.happydroids.server.ApiCollectionRunnable;
import com.happydroids.server.HappyDroidServiceCollection;
import com.happydroids.utils.BackgroundTask;
import org.apache.http.HttpResponse;

import java.util.List;

public class ViewNeighborScene extends Scene {
  private ViewNeighborHUD neighborHUD;
  private List<GameLayer> gameLayers;
  private Vector3 previousCameraPosition;
  private float previousCameraZoom;
  private Label fetchingLabel;
  private boolean fetchingNeighbors;
  private float timeSinceDroidSpawn;
  private GameObject droid;

  @Override
  public void create(Object... args) {
    previousCameraPosition = getCamera().position.cpy();
    previousCameraZoom = getCamera().zoom;
    CameraController.instance().stopMovement();

    getCamera().position.set(Gdx.graphics.getWidth() / 2, Gdx.graphics.getHeight() / 2, 0);
    getCamera().zoom = CameraController.ZOOM_MIN;

    neighborHUD = new ViewNeighborHUD();
    neighborHUD.x = 0;
    neighborHUD.y = Gdx.graphics.getHeight();

    getStage().addActor(neighborHUD);

    InputSystem.instance().bind(TowerConsts.NEGATIVE_BUTTON_KEYS, goBackHomeCallback);

    gameLayers = Lists.newArrayList();

    fetchingNeighbors = true;
    droid = new GameObject(new Texture("happy-droid.png"));
    droid.setPosition(Random.randomInt(Gdx.graphics.getWidth() / 2), Random.randomInt(Gdx.graphics.getHeight()) / 2);
    Tween.to(droid, GameObjectAccessor.OPACITY, 1000)
            .target(0f)
            .setCallback(new TweenCallback() {
              @Override
              public void onEvent(int type, BaseTween source) {
                droid.setPosition(Random.randomInt(Gdx.graphics.getWidth() / 2), Random.randomInt(Gdx.graphics.getHeight()) / 2);
              }
            })
            .setCallbackTriggers(TweenCallback.END)
            .repeat(Tween.INFINITY, 100)
            .start(TweenSystem.getTweenManager());

    fetchingLabel = FontManager.Roboto64.makeLabel("fetching neighbors :D");
    getStage().addActor(fetchingLabel);


    new BackgroundTask() {
      private FriendCloudGameSaveCollection friendGames;
      public boolean fetchWasSuccessful;

      @Override
      protected void execute() {
        friendGames = new FriendCloudGameSaveCollection();
        friendGames.fetch(new ApiCollectionRunnable<HappyDroidServiceCollection<FriendCloudGameSave>>() {
          @Override
          public void onError(HttpResponse response, int statusCode, HappyDroidServiceCollection<FriendCloudGameSave> collection) {
            System.out.println("collection = " + collection);
          }

          @Override
          public void onSuccess(HttpResponse response, HappyDroidServiceCollection<FriendCloudGameSave> collection) {
            fetchWasSuccessful = true;
          }
        });
      }

      @Override
      public synchronized void afterExecute() {
        if (fetchWasSuccessful) {
          createNeighborTowers(friendGames);
        } else {
          new Dialog()
                  .setTitle("Connection Failed")
                  .setMessage("Sorry, but we were not able to fetch your neighborhood.\n\nPlease check your internet connection and try again.")
                  .addButton("Okay", new OnClickCallback() {
                    @Override
                    public void onClick(Dialog dialog) {
                      dialog.dismiss();
                    }
                  })
                  .setDismissCallback(new Runnable() {
                    @Override
                    public void run() {
                      TowerGame.popScene();
                    }
                  })
                  .show();
        }
      }
    }.run();
  }

  private void createNeighborTowers(HappyDroidServiceCollection<FriendCloudGameSave> collection) {
    Vector2 worldSize = new Vector2();
    int gridX = 0;
    for (final FriendCloudGameSave friendCloudGameSave : collection.getObjects()) {
      NeighborGameGrid neighborGameGrid = new NeighborGameGrid(getCamera(), new GridPoint(gridX, TowerConsts.NEIGHBOR_GROUND_HEIGHT));
      neighborGameGrid.setGridScale(1f);
      GameSave gameSave = friendCloudGameSave.getGameSave();

      if (!gameSave.hasGridObjects()) {
        System.out.println("Skipping, no objects! " + friendCloudGameSave);
        continue;
      }

      gameSave.attachToGame(neighborGameGrid, camera);
      neighborGameGrid.findLimits();
      gridX += (neighborGameGrid.getGridSize().x + 2) * TowerConsts.GRID_UNIT_SIZE;

      neighborGameGrid.setOwnerName(friendCloudGameSave.getOwner().getFirstName());
      neighborGameGrid.setClickListener(new Runnable() {
        public void run() {
          HeadsUpDisplay.showToast("HELLO FROM " + friendCloudGameSave.getOwner().getFirstName());
        }
      });

      worldSize.y = Math.max(worldSize.y, neighborGameGrid.getWorldSize().y);
      worldSize.x = gridX + neighborGameGrid.getWorldSize().x;

      gameLayers.add(neighborGameGrid.getRenderer());
      gameLayers.add(neighborGameGrid);

    }

    camera.position.set(worldSize.x / 2 - Gdx.graphics.getWidth() / 2, TowerConsts.GROUND_HEIGHT + Gdx.graphics.getHeight(), 0f);
    fetchingLabel.markToRemove(true);
    fetchingNeighbors = false;
  }

  @Override
  public void pause() {
  }

  @Override
  public void resume() {
  }

  @Override
  public void render(float deltaTime) {
    SpriteBatch batch = getSpriteBatch();
    if (fetchingNeighbors) {
      batch.begin();
      droid.draw(batch);
      batch.end();
    } else {
      for (GameLayer gameLayer : gameLayers) {
        gameLayer.update(deltaTime);
        gameLayer.render(batch);
      }
    }
  }

  @Override
  public void dispose() {
    InputSystem.instance().unbind(TowerConsts.NEGATIVE_BUTTON_KEYS, goBackHomeCallback);
    getCamera().position.set(previousCameraPosition);
    getCamera().zoom = previousCameraZoom;
    CameraController.instance().stopMovement();
  }

  private InputCallback goBackHomeCallback = new InputCallback() {
    @Override
    public boolean run(float timeDelta) {
      TowerGame.popScene();
      return true;
    }
  };
}