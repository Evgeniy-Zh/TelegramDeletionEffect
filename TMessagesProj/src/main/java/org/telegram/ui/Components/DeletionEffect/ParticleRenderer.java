package org.telegram.ui.Components.DeletionEffect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;

import org.telegram.messenger.AndroidUtilities;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;


public class ParticleRenderer implements GLSurfaceView.Renderer {

    private final Context context;
    private PointList pointList;
    private PointShader pointShader;
    int width, height;
    private float[][] points;
    private float[][] colors;

    private float firstSolidPixelX = Float.MAX_VALUE;

    public Runnable afterFirstFrameRendered = () -> {
    };

    private long frames = 0;

    public ParticleRenderer(Context context, int width, int height) {
        this.context = context;
        this.width = width;
        this.height = height;
    }

    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        GLES20.glEnable(GLES20.GL_BLEND);
        GLES20.glBlendFunc(GLES20.GL_SRC_ALPHA, GLES20.GL_ONE_MINUS_SRC_ALPHA);
        GLES20.glClearColor(0.0f, 0.0f, 0.0f, 0.0f);
        pointList = new PointList(12000);
        pointList.setWindowSize(width, height);

        pointList.setInput(
                points,
                colors
        );
        pointShader = new PointShader();
        pointShader.enable();
        pointShader.setColor(1.0f, 0.0f, 0.0f, 1.0f);
        pointShader.setSize(3f);
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {
        GLES20.glViewport(0, 0, width, height);
        pointList.setWindowSize(width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT);
        drawPoints();
        if (frames == 2) {
            AndroidUtilities.runOnUIThread(() -> {
                afterFirstFrameRendered.run();
            });
        }
    }

    public void start(Bitmap bitmap, int[] position, int[] size, int[] overlayPosition) {
        int length = size[0] * size[1];

        int step = 2;

        points = new float[length / step + step][2];
        colors = new float[length / step + step][4];

        int bmX = 0;
        int bmY = 0;

        int color = 0;

        int outputDataIndex = 0;
        for (int i = 0; i < length; i += step) {
            float[] curPoint = points[outputDataIndex];
            float[] curColor = colors[outputDataIndex];

            curPoint[0] = position[0] - overlayPosition[0] + bmX;
            curPoint[1] = position[1] - overlayPosition[1] + bmY;

            try {
                color = bitmap.getPixel(bmX, bmY);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
            }

            curColor[0] = Color.red(color) / 255f;
            curColor[1] = Color.green(color) / 255f;
            curColor[2] = Color.blue(color) / 255f;
            curColor[3] = Color.alpha(color) / 255f;

            if (curColor[3] > 0f) {
                firstSolidPixelX = Math.min(firstSolidPixelX, curPoint[0]);
            }

            bmX += step;
            if (bmX >= size[0]) {
                bmX = 0;
                bmY += step;
            }

            outputDataIndex++;

            if (bmY >= size[1]) break;

        }

    }

    private void drawPoints() {
        frames++;

        final int coordinatesPerPoint = 2;
        final int numPoints = pointList.getSize();

        pointShader.enable();
        GLES20.glUniform1f(pointShader.getDeltaTimeHandle(), frames / 100f);
        GLES20.glUniform1f(pointShader.getStartXHandle(), firstSolidPixelX);

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, pointList.getPositionBufferHandle());

        int stride = 24;

        GLES20.glVertexAttribPointer(pointShader.getPointsDataHandle(), coordinatesPerPoint, GLES20.GL_FLOAT, false, stride, 0);
        GLES20.glEnableVertexAttribArray(pointShader.getPointsDataHandle());

        int colorOffset = 8;
        GLES20.glVertexAttribPointer(pointShader.getColorHandle(), 4, GLES20.GL_FLOAT, false, stride, colorOffset);
        GLES20.glEnableVertexAttribArray(pointShader.getColorHandle());


        GLES20.glDrawArrays(GLES20.GL_POINTS, 0, numPoints);

        GLES20.glDisableVertexAttribArray(pointShader.getPointsDataHandle());

        GLES20.glDisableVertexAttribArray(pointShader.getColorHandle());

        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }
}