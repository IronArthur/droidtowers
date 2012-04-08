/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.sparky;

import com.google.common.collect.Lists;
import com.happydroids.server.*;
import org.apache.http.HttpResponse;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarFile;

public class GameUpdateChecker {
  private HappyDroidServiceCollection<GameUpdate> updates;
  private final File gameStorage;
  private boolean usePatches;
  private boolean alreadyAtCurrentVersion;
  private String localGameJarVersionSHA;
  private List<GameUpdate> pendingUpdates;

  public GameUpdateChecker(File gameStorage) {
    this.gameStorage = gameStorage;
    pendingUpdates = Lists.newArrayList();
    alreadyAtCurrentVersion = false;
    usePatches = false;
    localGameJarVersionSHA = null;
  }

  public void parseLocalGameJar() {
    File gameJar = new File(gameStorage, "DroidTowers.jar");
    if (gameJar.exists()) {
      try {
        JarFile jarFile = new JarFile(gameJar);
        if (jarFile != null) {
          if (jarFile.getManifest() != null) {
            Attributes mainAttributes = jarFile.getManifest().getMainAttributes();
            if (mainAttributes != null) {
              localGameJarVersionSHA = mainAttributes.getValue("Game-Version-SHA");
            }
          }
        }
      } catch (IOException ignored) {
      }
    }
  }

  public void fetchUpdates() {
    updates = new GameUpdateCollection();
    updates.fetch(new ApiCollectionRunnable<HappyDroidServiceCollection<GameUpdate>>() {
      @Override
      public void onError(HttpResponse response, int statusCode, HappyDroidServiceCollection<GameUpdate> collection) {
//        System.out.println("statusCode = " + statusCode);
      }

      @Override
      public void onSuccess(HttpResponse response, HappyDroidServiceCollection<GameUpdate> collection) {
        for (GameUpdate gameUpdate : collection.getObjects()) {
//          System.out.println("gameUpdate = " + gameUpdate);
        }
      }
    });
  }

  public HappyDroidServiceCollection<GameUpdate> getUpdates() {
    return updates;
  }

  public List<GameUpdate> selectUpdates() throws IOException {
    if (localGameJarVersionSHA != null) {
      if (!updates.isEmpty()) {
        if (updates.getObjects().get(0).gitSha.equals(localGameJarVersionSHA)) {
          alreadyAtCurrentVersion = true;
          return null;
        } else {
          findUpdatesSinceLastValidSha();
        }

        if (!usePatches) {
          pendingUpdates.add(updates.getObjects().get(0));
        }
      }
    } else if (!updates.isEmpty()) {
      pendingUpdates.add(updates.getObjects().get(0));
    }

    return !pendingUpdates.isEmpty() ? Lists.reverse(pendingUpdates) : null;
  }

  private void findUpdatesSinceLastValidSha() {
    if (localGameJarVersionSHA == null) {
      return;
    }

    for (GameUpdate update : updates.getObjects()) {
      if (update.getGitSHA().equals(localGameJarVersionSHA)) {
        usePatches = true;
        break;
      }

      pendingUpdates.add(update);
    }
  }

  public boolean shouldUsePatches() {
    return usePatches;
  }

  public List<GameUpdate> getPendingUpdates() {
    return pendingUpdates;
  }

  public boolean hasCurrentVersion() {
    if (!HappyDroidService.instance().haveNetworkConnection()) {
      return localGameJarVersionSHA != null;
    }

    return alreadyAtCurrentVersion;
  }

  public String getLocalGameJarVersionSHA() {
    return localGameJarVersionSHA;
  }
}
