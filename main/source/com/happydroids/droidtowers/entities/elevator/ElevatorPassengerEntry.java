/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.entities.elevator;

import com.happydroids.droidtowers.controllers.AvatarSteeringManager;
import com.happydroids.droidtowers.grid.GridPosition;
import com.happydroids.droidtowers.utils.Random;

public class ElevatorPassengerEntry {
  private final AvatarSteeringManager steeringManager;
  private final GridPosition boardingFloor;
  private final GridPosition destinationFloor;
  private Runnable runnable;
  public boolean hasBoarded;
  public boolean isQueued;
  public float offsetX;

  public ElevatorPassengerEntry(AvatarSteeringManager steeringManager, GridPosition boardingFloor, GridPosition destinationFloor) {
    this.steeringManager = steeringManager;
    this.boardingFloor = boardingFloor;
    this.destinationFloor = destinationFloor;

    offsetX = Random.randomInt(8, 36);
  }

  public void addCallback(Runnable runnable) {
    this.runnable = runnable;
  }

  public void runCompleteCallback() {
    runnable.run();
  }

  public GridPosition getBoardingFloor() {
    return boardingFloor;
  }

  public GridPosition getDestinationFloor() {
    return destinationFloor;
  }

  public boolean isHasBoarded() {
    return hasBoarded;
  }

  public boolean isQueued() {
    return isQueued;
  }

  public float getOffsetX() {
    return offsetX;
  }

  public AvatarSteeringManager getSteeringManager() {
    return steeringManager;
  }
}