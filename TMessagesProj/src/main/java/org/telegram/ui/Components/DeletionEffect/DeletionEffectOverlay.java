package org.telegram.ui.Components.DeletionEffect;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.opengl.GLSurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import org.telegram.messenger.AndroidUtilities;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class DeletionEffectOverlay {

    private FrameLayout overlayView;

    private GLSurfaceView glSurfaceView;

    private Context context;

    private Bitmap parentViewBitmap = null;

    private List<View> targetViews;

    private ViewGroup parent;
    private int relativeTop;
    private int relativeLeft;
    private int relativeRight;
    private int relativeBottom;

    private ThreadPoolExecutor executor;

    private int[] viewPosition = new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE};


    public DeletionEffectOverlay(FrameLayout overlayView) {
        this.overlayView = overlayView;
        this.context = overlayView.getContext();
        targetViews = new ArrayList<>();
        executor = new ThreadPoolExecutor(1, 2, 10, TimeUnit.MINUTES, new LinkedBlockingQueue<>());
    }

    public void prepare(ViewGroup parent) {
        clear();
        this.parent = parent;
        boolean createNewBitmap = parentViewBitmap == null ||
                parentViewBitmap.getWidth() != parent.getWidth() ||
                parentViewBitmap.getHeight() != parent.getHeight();

        if (createNewBitmap)
            parentViewBitmap = Bitmap.createBitmap(parent.getWidth(), parent.getHeight(), Bitmap.Config.ARGB_8888);
    }


    public void addTargetView(View viewToRemove) {
        targetViews.add(viewToRemove);

        Rect offsetViewBounds = new Rect();
        viewToRemove.getDrawingRect(offsetViewBounds);
        parent.offsetDescendantRectToMyCoords(viewToRemove, offsetViewBounds);

        int relativeTop = Math.max(offsetViewBounds.top, 0);
        int relativeLeft = Math.max(offsetViewBounds.left, 0);
        int relativeRight = Math.min(offsetViewBounds.right, parent.getWidth());
        int relativeBottom = Math.min(offsetViewBounds.bottom, parent.getHeight());

        this.relativeTop = Math.min(this.relativeTop, relativeTop);
        this.relativeLeft = Math.min(this.relativeLeft, relativeLeft);
        this.relativeRight = Math.max(this.relativeRight, relativeRight);
        this.relativeBottom = Math.max(this.relativeBottom, relativeBottom);

        int[] viewPosition = new int[2];
        viewToRemove.getLocationInWindow(viewPosition);

        this.viewPosition[0] = Math.min(this.viewPosition[0], viewPosition[0]);
        this.viewPosition[1] = Math.min(this.viewPosition[1], viewPosition[1]);


    }

    public void start(Runnable afterFirstFrameRendered) {
        //todo: if bitmap and renderer initialization executed in background thread it leads to glitches in recyclerView (some messages play resize anim)

        if (glSurfaceView != null) overlayView.removeView(glSurfaceView);

        if (parent == null) throw new IllegalStateException("prepare was not called!");

        glSurfaceView = new GLSurfaceView(context);

        glSurfaceView.setEGLContextClientVersion(2);
        glSurfaceView.setZOrderOnTop(true);

        glSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
        glSurfaceView.getHolder().setFormat(PixelFormat.RGBA_8888);

        ParticleRenderer renderer = new ParticleRenderer(context, overlayView.getWidth(), overlayView.getHeight());

        renderer.afterFirstFrameRendered = afterFirstFrameRendered;

        int[] overlayPosition = new int[2];
        overlayView.getLocationInWindow(overlayPosition);

        glSurfaceView.setRenderer(renderer);

        if (parentViewBitmap != null) {
            parentViewBitmap.eraseColor(Color.TRANSPARENT);
        }

        AndroidUtilities.snapshotView(parent, parentViewBitmap);

        Bitmap bitmap = Bitmap.createBitmap(
                parentViewBitmap,
                relativeLeft,
                relativeTop,
                relativeRight - relativeLeft,
                relativeBottom - relativeTop
        );

        int[] size = new int[]{
                bitmap.getWidth(),
                bitmap.getHeight()
        };

        this.viewPosition[0] = Math.max(this.viewPosition[0], overlayPosition[0]);
        this.viewPosition[1] = Math.max(this.viewPosition[1], overlayPosition[1]);

        renderer.start(bitmap, viewPosition, size, overlayPosition);

        overlayView.addView(glSurfaceView);

        parent = null;

    }

    private void clear() {
        targetViews.clear();
        relativeTop = Integer.MAX_VALUE;
        relativeLeft = Integer.MAX_VALUE;
        relativeRight = 0;
        relativeBottom = 0;

        viewPosition[0] = Integer.MAX_VALUE;
        viewPosition[1] = Integer.MAX_VALUE;
    }


}
