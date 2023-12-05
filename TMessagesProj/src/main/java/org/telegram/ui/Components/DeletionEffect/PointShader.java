package org.telegram.ui.Components.DeletionEffect;


import android.opengl.GLES20;

import org.telegram.messenger.R;
import org.telegram.ui.Components.RLottieDrawable;

public class PointShader {

   private final int programHandle;
   private final int uSizeLocation;
   private final int uColorLocation;
   private final int aPositionLocation;
   private final int deltaTimeHandle;
   private final int aColor;
   private final int aVelocity;

   private final int aStartX;

   public PointShader() {
      final int vertexShaderHandle = GLES20.glCreateShader(GLES20.GL_VERTEX_SHADER);
      GLES20.glShaderSource(vertexShaderHandle, RLottieDrawable.readRes(null, R.raw.deletion_effect_vertex));
      GLES20.glCompileShader(vertexShaderHandle);
      final int fragmentShaderHandle = GLES20.glCreateShader(GLES20.GL_FRAGMENT_SHADER);
      GLES20.glShaderSource(fragmentShaderHandle, RLottieDrawable.readRes(null, R.raw.deletion_effect_fragment));
      GLES20.glCompileShader(fragmentShaderHandle);
      programHandle = GLES20.glCreateProgram();

      GLES20.glAttachShader(programHandle, vertexShaderHandle);
      GLES20.glAttachShader(programHandle, fragmentShaderHandle);
      GLES20.glLinkProgram(programHandle);
      aPositionLocation = GLES20.glGetAttribLocation(programHandle, "a_Position");
      uSizeLocation = GLES20.glGetUniformLocation(programHandle,"u_Size");
      uColorLocation = GLES20.glGetUniformLocation(programHandle, "u_Color");

      deltaTimeHandle = GLES20.glGetUniformLocation(programHandle, "deltaTime");
      aColor = GLES20.glGetAttribLocation(programHandle, "aColor");
      aVelocity = GLES20.glGetAttribLocation(programHandle, "aVelocity");
      aStartX = GLES20.glGetUniformLocation(programHandle, "startX");

   }

   public boolean enable() {
      if (programHandle == 0)
         return false;
      GLES20.glUseProgram(programHandle);
      GLES20.glActiveTexture(GLES20.GL_TEXTURE0);
      return true;
   }

   public int getPointsDataHandle() {
      return aPositionLocation;
   }

   public int getColorHandle(){
      return aColor;
   }

   public int getDeltaTimeHandle(){
      return deltaTimeHandle;
   }

   public int getStartXHandle(){
      return aStartX;
   }

   public void setSize(float size) {
      GLES20.glUniform1f(uSizeLocation, size);
   }

   public void setColor(float red, float green, float blue, float alpha) {
      GLES20.glUniform4f(uColorLocation, red, green, blue, alpha);
   }

   public int getaVelocity() {
      return aVelocity;
   }
}