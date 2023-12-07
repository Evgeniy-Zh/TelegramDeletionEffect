package org.telegram.ui.Components.DeletionEffect;


import android.opengl.GLES20;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.FloatBuffer;

public class PointList {
    private final int BYTES_PER_POINT = 8;
    private final int BYTES_PER_COLOR = 16;

    private FloatBuffer pointData;
    private int bufferHandle;

    private int numPoints;
    final int buffers[] = new int[1];

    private int width = 200, height = 500;


    public PointList(int numPoints) {
        this.numPoints = numPoints;
    }


    public void setInput(float[][] points, float[][] colors) {
        numPoints = points.length;

        // Create and populate buffer.
        pointData = ByteBuffer.allocateDirect(numPoints * BYTES_PER_POINT + numPoints * BYTES_PER_COLOR)
                .order(ByteOrder.nativeOrder()).asFloatBuffer();
        pointData.position(0);


        for (int i = 0; i < numPoints; i++) {
            float[] p = points[i];
            float posX = 2.0f * (p[0] + 0.5f) / width - 1.0f;
            float posY = -(2.0f * (p[1] + 0.5f) / height - 1.0f);

            pointData.put(posX);
            pointData.put(posY);

            float[] c = colors[i];
            pointData.put(c[0]);
            pointData.put(c[1]);
            pointData.put(c[2]);
            pointData.put(c[3]);
        }


        // Send buffer to GPU.
        GLES20.glGenBuffers(1, buffers, 0);
        bufferHandle = buffers[0];
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, bufferHandle);
        pointData.position(0);

        GLES20.glBufferData(GLES20.GL_ARRAY_BUFFER, numPoints * BYTES_PER_POINT + numPoints * BYTES_PER_COLOR, pointData, GLES20.GL_STATIC_DRAW);
        GLES20.glBindBuffer(GLES20.GL_ARRAY_BUFFER, 0);

    }

    public void setWindowSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public int getPositionBufferHandle() {
        return bufferHandle;
    }

    public int getSize() {
        return numPoints;
    }


}