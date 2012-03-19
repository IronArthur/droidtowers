package com.unhappyrobot.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.OrthographicCamera;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.badlogic.gdx.math.Vector3;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;
import com.badlogic.gdx.scenes.scene2d.ui.*;
import com.badlogic.gdx.scenes.scene2d.ui.tablelayout.Table;
import com.unhappyrobot.entities.CommercialSpace;
import com.unhappyrobot.entities.GridObject;
import com.unhappyrobot.grid.GameGrid;
import com.unhappyrobot.grid.GridPosition;
import com.unhappyrobot.grid.GridPositionCache;
import com.unhappyrobot.input.GestureTool;
import com.unhappyrobot.input.InputCallback;
import com.unhappyrobot.input.InputSystem;
import com.unhappyrobot.math.GridPoint;
import com.unhappyrobot.types.*;

public class HeadsUpDisplay extends WidgetGroup {
  public static final float ONE_MEGABYTE = 1048576.0f;
  private TextureAtlas hudAtlas;
  private Skin guiSkin;
  private OrthographicCamera camera;
  private GameGrid gameGrid;
  private LabelButton addRoomButton;
  private Menu addRoomMenu;
  private Label statusLabel;
  private float updateMoneyLabel;
  private static HeadsUpDisplay instance;
  private BitmapFont menloBitmapFont;
  private Toast toast;
  private LabelButton setOverlayButton;
  private Menu overlayMenu;
  private Table topBar;
  private ToolTip mouseToolTip;
  private GridObjectPurchaseMenu purchaseDialog;
  private InputCallback closeDialogCallback = null;
  private RadialMenu toolMenu;
  private final StackGroup notificationStack;
  private TextButton expandLandButton;

  public static HeadsUpDisplay instance() {
    if (instance == null) {
      instance = new HeadsUpDisplay();
    }

    return instance;
  }

  private HeadsUpDisplay() {
    this.guiSkin = new Skin(Gdx.files.internal("default-skin.ui"), Gdx.files.internal("default-skin.png"));
    notificationStack = new StackGroup();
  }

  public void initialize(final OrthographicCamera camera, final GameGrid gameGrid, final Stage stage, SpriteBatch spriteBatch) {
    this.stage = stage;
    this.camera = camera;
    this.gameGrid = gameGrid;

    ModalOverlay.initialize(this);

    menloBitmapFont = new BitmapFont(Gdx.files.internal("fonts/menlo_14_bold_white.fnt"), false);

    hudAtlas = new TextureAtlas(Gdx.files.internal("hud/buttons.txt"));

    StatusBarPanel statusBarPanel = new StatusBarPanel(guiSkin);
    statusBarPanel.x = -1;
    statusBarPanel.y = stage.height() - statusBarPanel.height + 1;
    addActor(statusBarPanel);

    mouseToolTip = new ToolTip();
    addActor(mouseToolTip);

    toolMenu = new RadialMenu();
    toolMenu.arc = 35f;
    toolMenu.radius = 140f;
    toolMenu.rotation = 3f;

    TextButton connectFacebookButton = new TextButton("Connect to Facebook", guiSkin);
    connectFacebookButton.x = 100;
    connectFacebookButton.y = 100;
    connectFacebookButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        ConnectToFacebook mainMenu = new ConnectToFacebook();
        mainMenu.show().centerOnStage();
      }
    });
//    addActor(connectFacebookButton);
    addActor(new ExpandLandOverlay(gameGrid, guiSkin));

    ImageButton housingButton = new ImageButton(hudAtlas.findRegion("tool-housing"));
    housingButton.setClickListener(makePurchaseButtonClickListener("Housing", RoomTypeFactory.instance()));
    toolMenu.addActor(housingButton);

    ImageButton transitButton = new ImageButton(hudAtlas.findRegion("tool-transit"));
    transitButton.setClickListener(makePurchaseButtonClickListener("Transit", TransitTypeFactory.instance()));
    toolMenu.addActor(transitButton);

    ImageButton commerceButton = new ImageButton(hudAtlas.findRegion("tool-commerce"));
    commerceButton.setClickListener(makePurchaseButtonClickListener("Commerce", CommercialTypeFactory.instance()));
    toolMenu.addActor(commerceButton);

    ImageButton servicesButton = new ImageButton(hudAtlas.findRegion("tool-services"));
    servicesButton.setClickListener(makePurchaseButtonClickListener("Services", ServiceRoomTypeFactory.instance()));
    toolMenu.addActor(servicesButton);

    ImageButton sellButton = new ImageButton(hudAtlas.findRegion("tool-sell"));
    sellButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        toolMenu.hide();
        InputSystem.instance().switchTool(GestureTool.SELL, null);
      }
    });

    toolMenu.addActor(sellButton);

    final ImageButton toolButton = new ImageButton(hudAtlas.findRegion("tool-sprite"));
    toolButton.x = stage.width() - toolButton.width - 5;
    toolButton.y = 5;
    addActor(toolButton);
    toolButton.setClickListener(new ClickListener() {
      public void click(Actor actor, float x, float y) {
        if (InputSystem.instance().getCurrentTool() != GestureTool.PLACEMENT) {
          if (purchaseDialog != null) {
            purchaseDialog.dismiss();
            purchaseDialog = null;
          }
          InputSystem.instance().switchTool(GestureTool.PICKER, null);
        }

        if (!toolMenu.visible) {
          stage.addActor(toolMenu);
          toolMenu.x = toolButton.x + 20f;
          toolMenu.y = toolButton.y;
          toolMenu.show();
        } else {
          toolMenu.hide();
          toolMenu.markToRemove(true);
        }
      }
    });

    AudioControl audioControl = new AudioControl(hudAtlas);
    audioControl.x = stage.width() - audioControl.width - 5;
    audioControl.y = stage.height() - audioControl.height - 5;
    addActor(audioControl);

    OverlayControl overlayControl = new OverlayControl(hudAtlas, guiSkin);
    overlayControl.x = audioControl.x - audioControl.width - 5;
    overlayControl.y = stage.height() - overlayControl.height - 5;
    addActor(overlayControl);

    notificationStack.pad(10);
    notificationStack.x = 0;
    notificationStack.y = 0;
    addActor(notificationStack);

    stage.addActor(this);
  }

  private ClickListener makePurchaseButtonClickListener(final String dialogTitle, final GridObjectTypeFactory typeFactory) {
    return new ClickListener() {
      public void click(Actor actor, float x, float y) {
        toolMenu.hide();

        if (purchaseDialog == null) {
          makePurchaseDialog(dialogTitle, typeFactory);
        } else {
          purchaseDialog.dismiss();
          purchaseDialog = null;
        }
      }
    };
  }

  private void makePurchaseDialog(String title, GridObjectTypeFactory typeFactory) {
    purchaseDialog = new GridObjectPurchaseMenu(title, typeFactory);

    purchaseDialog.setDismissCallback(new Runnable() {
      public void run() {
        purchaseDialog = null;
      }
    });

    stage.addActor(purchaseDialog);

    purchaseDialog.centerOnStage().modal(true).show();
  }

  public Skin getGuiSkin() {
    return guiSkin;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    super.draw(batch, parentAlpha);

    float javaHeapInBytes = Gdx.app.getJavaHeap() / ONE_MEGABYTE;
    float nativeHeapInBytes = Gdx.app.getNativeHeap() / ONE_MEGABYTE;

    String infoText = String.format("fps: %02d, camera(%.1f, %.1f, %.1f), grid(%.0f, %.0f)\nmem: (java %.1f Mb, native %.1f Mb)", Gdx.graphics.getFramesPerSecond(), camera.position.x, camera.position.y, camera.zoom, gameGrid.getGridSize().x, gameGrid.getGridSize().y, javaHeapInBytes, nativeHeapInBytes);
    menloBitmapFont.drawMultiLine(batch, infoText, 5, 35);
  }

  @Override
  public boolean touchMoved(float x, float y) {
    Actor hit = hit(x, y);
    if (hit == null || hit == mouseToolTip) {
      updateGridPointTooltip(x, y);
    } else {
      mouseToolTip.visible = false;
    }

    return super.touchMoved(x, y);
  }

  private void updateGridPointTooltip(float x, float y) {
    Vector3 worldPoint = camera.getPickRay(Gdx.input.getX(), Gdx.input.getY()).getEndPoint(1);

    GridPoint gridPointAtMouse = gameGrid.closestGridPoint(worldPoint.x, worldPoint.y);
    GridPosition gridPosition = GridPositionCache.instance().getPosition(gridPointAtMouse);
    if (gridPosition != null) {
      int totalVisitors = 0;
      for (GridObject gridObject : gridPosition.getObjects()) {
        if (gridObject instanceof CommercialSpace) {
          totalVisitors = ((CommercialSpace) gridObject).getNumVisitors();
        }
      }

      mouseToolTip.visible = true;
      mouseToolTip.setText(String.format("%s\nobjects: %s\nelevator: %s\nstairs: %s\nvisitors: %d", gridPointAtMouse, gridPosition.size(), gridPosition.elevator != null, gridPosition.stair != null, totalVisitors));
      mouseToolTip.x = x + 5;
      mouseToolTip.y = y + 5;
    } else {
      mouseToolTip.visible = false;
    }
  }

  public void showToast(String message, Object... objects) {
    if (toast == null) {
      toast = new Toast();
      addActor(toast);
    }

    toast.setMessage(String.format(message, objects));
    toast.show();
  }

  public float getPrefWidth() {
    return 0;
  }

  public float getPrefHeight() {
    return 0;
  }

  public void showTipBubble(GridObject gridObject, String message) {
    SpeechBubble bubble = new SpeechBubble();
    bubble.setText(message);
    bubble.followObject(gridObject);
    bubble.show();

    addActor(bubble);
  }

  public StackGroup getNotificationStack() {
    return notificationStack;
  }
}
