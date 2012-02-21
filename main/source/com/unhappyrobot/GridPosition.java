package com.unhappyrobot;

import com.badlogic.gdx.math.Vector2;
import com.unhappyrobot.entities.*;
import com.unhappyrobot.math.GridPoint;

public class GridPosition {
  public final int x;
  public final int y;
  private GuavaSet<GridObject> objects = new GuavaSet<GridObject>();
  public boolean connectedToTransit;
  public Elevator elevator;
  public Stair stair;

  public GridPosition(int x, int y) {
    this.x = x;
    this.y = y;
  }

  public GuavaSet<GridObject> getObjects() {
    return objects;
  }

  public void add(GridObject gridObject) {
    if (objects.add(gridObject)) {

      if (gridObject instanceof Elevator) {
        elevator = (Elevator) gridObject;
      } else if (gridObject instanceof Stair) {
        stair = (Stair) gridObject;
      }
    }
  }

  public void remove(GridObject gridObject) {
    if (objects.remove(gridObject)) {
      if (gridObject instanceof Elevator) {
        elevator = null;
      } else if (gridObject instanceof Stair) {
        stair = null;
      }

    }
  }

  public int size() {
    return objects.size();
  }

  public Vector2 getBiggestTransitSize() {
    if (elevator == null && stair == null) {
      return null;
    }

    return new Vector2(Math.max(stair.getSize().x, elevator.getSize().x), Math.max(stair.getSize().y, elevator.getSize().y));
  }

  public GridPoint toGridPoint() {
    return new GridPoint(x, y);
  }

  public Vector2 toWorldVector2(GameGrid gameGrid) {
    return toGridPoint().toWorldVector2(gameGrid);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (o == null || getClass() != o.getClass()) return false;

    GridPosition that = (GridPosition) o;

    return x == that.x && y == that.y;
  }

  @Override
  public int hashCode() {
    int result = x;
    result = 31 * result + y;
    return result;
  }

  @Override
  public String toString() {
    return "GridPosition{" +
                   "connectedToTransit=" + connectedToTransit +
                   ", x=" + x +
                   ", y=" + y +
                   ", stair=" + stair +
                   ", elevator=" + elevator +
                   '}';
  }
}
