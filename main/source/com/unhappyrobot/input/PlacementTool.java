package com.unhappyrobot.input;

import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.math.Vector2;
import com.unhappyrobot.entities.GameGrid;
import com.unhappyrobot.entities.GridObject;
import com.unhappyrobot.entities.GridObjectPlacementState;
import com.unhappyrobot.gui.HeadsUpDisplay;
import com.unhappyrobot.math.GridPoint;
import com.unhappyrobot.money.PurchaseManager;
import com.unhappyrobot.types.GridObjectType;

import static com.unhappyrobot.input.InputSystem.Keys;

public class PlacementTool extends ToolBase {
  private GridObjectType gridObjectType;
  private GridObject gridObject;
  private Vector2 touchDownPointDelta;
  private boolean isDraggingGridObject;
  private PurchaseManager purchaseManager;
  private final InputCallback cancelPlacementInputCallback;

  public PlacementTool(OrthographicCamera camera, GameGrid gameGrid) {
    super(camera, gameGrid);

    cancelPlacementInputCallback = new InputCallback() {
      public boolean run(float timeDelta) {
        InputSystem.getInstance().switchTool(GestureTool.PICKER, null);
        return true;
      }
    };

    InputSystem.getInstance().bind(new int[]{Keys.ESCAPE, Keys.BACK}, cancelPlacementInputCallback);
  }

  public void setup(GridObjectType gridObjectType) {
    this.gridObjectType = gridObjectType;
  }

  public boolean touchDown(int x, int y, int pointer) {
    GridPoint gridPointAtFinger = gridPointAtFinger();
    if (gridObject == null) {
      gridObject = gridObjectType.makeGridObject(gameGrid);
      gridObject.setPosition(gridPointAtFinger);

      gameGrid.addObject(gridObject);
    } else {
      touchDownPointDelta = gridPointAtFinger.cpy().sub(gridObject.getPosition());
    }

    isDraggingGridObject = gridObject.getBounds().containsPoint(gridPointAtFinger);

    return true;
  }

  public boolean pan(int x, int y, int deltaX, int deltaY) {
    if (isDraggingGridObject) {
      GridPoint gridPointAtFinger = gridPointAtFinger();

      if (touchDownPointDelta != null) {
        gridPointAtFinger.sub(touchDownPointDelta);
      }
      if (gridObject != null) {
        gridObject.setPosition(gridPointAtFinger);
      }

      return true;
    }

    return false;
  }

  public boolean tap(int x, int y, int count) {
    if (count >= 2) {
      if (!gameGrid.canObjectBeAt(gridObject)) {
        HeadsUpDisplay.getInstance().showToast("This object cannot be placed here.");
        return false;
      } else {
        gridObject.setPlacementState(GridObjectPlacementState.PLACED);
        gameGrid.updateRenderOrder();
      }

      if (purchaseManager != null) {
        purchaseManager.makePurchase();
      }

      gridObject = null;
      touchDownPointDelta = null;

      verifyAbilityToPurchase();
    }

    return false;
  }

  private void verifyAbilityToPurchase() {
    if (purchaseManager != null && !purchaseManager.canPurchase()) {
      InputSystem.getInstance().switchTool(GestureTool.PICKER, null);
    }
  }

  public void enterPurchaseMode() {
    purchaseManager = new PurchaseManager(gridObjectType);

    verifyAbilityToPurchase();
  }

  @Override
  public void cleanup() {
    if (gridObject != null) {
      gameGrid.removeObject(gridObject);
    }

    InputSystem.getInstance().unbind(new int[]{Keys.ESCAPE, Keys.BACK}, cancelPlacementInputCallback);
  }
}
