/*
 * Copyright (c) 2012. HappyDroids LLC, All rights reserved.
 */

package com.happydroids.droidtowers.gui;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.GL10;
import com.badlogic.gdx.graphics.Mesh;
import com.badlogic.gdx.graphics.VertexAttribute;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.badlogic.gdx.graphics.glutils.ShaderProgram;
import com.badlogic.gdx.math.MathUtils;
import com.badlogic.gdx.math.Matrix4;
import com.badlogic.gdx.scenes.scene2d.Actor;
import com.badlogic.gdx.scenes.scene2d.Stage;

public class Sunburst extends Actor {

  private final Mesh mesh;
  private final ShaderProgram shader;
  private final Matrix4 transformMatrix;

  public Sunburst(Stage stage) {
    this.x = stage.centerX();
    this.y = stage.centerY();
    String vertexShader = "attribute vec4 " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                                  + "attribute vec4 " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                                  + "uniform mat4 u_transformMatrix;\n" //
                                  + "uniform mat4 u_projectionViewMatrix;\n" //
                                  + "varying vec4 v_color;\n" //
                                  + "\n" //
                                  + "void main()\n" //
                                  + "{\n" //
                                  + "   v_color = " + ShaderProgram.COLOR_ATTRIBUTE + ";\n" //
                                  + "   gl_Position =  u_transformMatrix * u_projectionViewMatrix * " + ShaderProgram.POSITION_ATTRIBUTE + ";\n" //
                                  + "}\n";
    String fragmentShader = "#ifdef GL_ES\n" //
                                    + "#define LOWP lowp\n" //
                                    + "precision mediump float;\n" //
                                    + "#else\n" //
                                    + "#define LOWP \n" //
                                    + "#endif\n" //
                                    + "varying LOWP vec4 v_color;\n" //
                                    + "void main()\n"//
                                    + "{\n" //
                                    + "  gl_FragColor = v_color;\n" //
                                    + "}";
    shader = new ShaderProgram(vertexShader, fragmentShader);

    float radius = stage.width();
    int i = 0;
    float[] vertices = new float[378];
    for (int angle = 0; angle < 360; angle += 20) {
      i = makePoint(radius, i, vertices, angle);
      i = makePoint(radius, i, vertices, angle + 10);
      vertices[i++] = x;
      vertices[i++] = y;
      vertices[i++] = 0f;
      vertices[i++] = 0f;
      vertices[i++] = 0f;
      vertices[i++] = 0.5f;
      vertices[i++] = 0f;
    }

    mesh = new Mesh(true, vertices.length, 0, VertexAttribute.Position(), VertexAttribute.ColorUnpacked());
    mesh.setVertices(vertices);
    transformMatrix = new Matrix4();
  }

  private int makePoint(float radius, int i, float[] vertices, int angle) {
    vertices[i++] = x + radius * MathUtils.cosDeg(angle);
    vertices[i++] = y + radius * MathUtils.sinDeg(angle);
    vertices[i++] = 0f;
    vertices[i++] = 0f;
    vertices[i++] = 0f;
    vertices[i++] = 0.5f;
    vertices[i++] = 0.065f;
    return i;
  }

  @Override
  public void draw(SpriteBatch batch, float parentAlpha) {
    Gdx.gl.glBlendFunc(GL10.GL_SRC_ALPHA, GL10.GL_ONE_MINUS_SRC_ALPHA);
    Gdx.gl.glEnable(GL10.GL_BLEND);
    shader.begin();
    shader.setUniformMatrix("u_transformMatrix", transformMatrix.setToRotation(0, 0, 1, rotation));
    shader.setUniformMatrix("u_projectionViewMatrix", getStage().getCamera().combined);
    mesh.render(shader, GL10.GL_TRIANGLES);
    shader.end();
    Gdx.gl.glDisable(GL10.GL_BLEND);
  }

  @Override
  public Actor hit(float x, float y) {
    return null;
  }
}
