package com.stahlnow.android.pfunzel;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
import android.graphics.PorterDuffXfermode;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class PfunzelBackground extends SurfaceView implements SurfaceHolder.Callback {


    @SuppressWarnings("unused")
    private static final String TAG = "WaveForm";

    private Context mContext;
    private LenseThread mLenseThread;

    public PfunzelBackground(Context context, AttributeSet attrs) {
        super(context, attrs);

        // register our interest in hearing about changes to our surface
        SurfaceHolder holder = getHolder();
        holder.addCallback(this);

        // create thread only; it's started in surfaceCreated()
        mLenseThread = new LenseThread(holder, context);

        setFocusable(true); // make sure we get input events
    }

    public LenseThread getThread() {
        return mLenseThread;
    }

    /* Callback invoked when the surface dimensions change. */
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        mLenseThread.setSurfaceSize(width, height);
    }

    /*
     * Callback invoked when the Surface has been created and is ready to be
     * used.
     */
    public void surfaceCreated(SurfaceHolder holder) {
        // start the thread here so that we don't busy-wait in run()
        // waiting for the surface to be created
        mLenseThread.setRunning(true);
        mLenseThread.start();
    }

    /*
     * Callback invoked when the Surface has been destroyed and must no longer
     * be touched. WARNING: after this method returns, the Surface/Canvas must
     * never be touched again!
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
        // we have to tell thread to shut down & wait for it to finish, or else
        // it might touch the Surface after we return and explode
        boolean retry = true;
        mLenseThread.setRunning(false);
        while (retry) {
            try {
                mLenseThread.join();
                retry = false;
            } catch (InterruptedException e) {
            }
        }
    }




    public class LenseThread extends Thread {

        @SuppressWarnings("unused")
        private String TAG = "LenseThread";

        /**
         * Current height of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasHeight = 1;

        /**
         * Current width of the surface/canvas.
         *
         * @see #setSurfaceSize
         */
        private int mCanvasWidth = 1;

        /** Indicate whether the surface has been created & is ready to draw */
        private boolean mRun = false;

        /** Handle to the surface manager object we interact with */
        private SurfaceHolder mSurfaceHolder;

        /** Handle shader */
        private Paint p;
        private Bitmap pfunzel, lense;
        private int color = 0;


        public LenseThread(SurfaceHolder holder, Context context) {
            mSurfaceHolder = holder;
            mContext = context;


        }


        public void setColor(int c) {

            this.color = c;
            p.setColorFilter(new PorterDuffColorFilter(this.color, PorterDuff.Mode.MULTIPLY));

        }

        /**
         * Used to signal the thread whether it should be running or not.
         * Passing true allows the thread to run; passing false will shut it
         * down if it's already running. Calling start() after this was most
         * recently called with false will result in an immediate shutdown.
         *
         * @param run true to run, false to shut down
         */
        public void setRunning(boolean run) {
            mRun = run;
        }


        @Override
        public void run() {

            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inPreferredConfig = Bitmap.Config.ARGB_8888;

            Bitmap pfunzel_raw = BitmapFactory.decodeResource(getResources(), R.drawable.pfunzel, options);
            float scalePfunzel = (float)pfunzel_raw.getHeight()/(float)getHeight();
            int pfunzelWidth = Math.round(pfunzel_raw.getWidth()/scalePfunzel);
            int pfunzelHeight = Math.round(pfunzel_raw.getHeight()/scalePfunzel);
            pfunzel = Bitmap.createScaledBitmap(pfunzel_raw, pfunzelWidth, pfunzelHeight, true);

            Bitmap lense_raw = BitmapFactory.decodeResource(getResources(), R.drawable.pfunzel_lense, options);
            float scaleLense = (float)lense_raw.getWidth()/(float)getWidth();
            int lenseWidth = Math.round(lense_raw.getWidth()/scaleLense);
            int lenseHeight = Math.round(lense_raw.getHeight()/scaleLense);
            lense = Bitmap.createScaledBitmap(lense_raw, lenseWidth, lenseHeight, true);

            p = new Paint();
            p.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.ADD));
            p.setColorFilter(new PorterDuffColorFilter(this.color, PorterDuff.Mode.MULTIPLY));
            p.setShader(new BitmapShader(lense, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP));

            while (mRun) {
                Canvas c = null;
                try {
                    c = mSurfaceHolder.lockCanvas(null);
                    synchronized (mSurfaceHolder) {
                        // draw
                        draw(c);
                    }
                } finally {
                    // do this in a finally so that if an exception is thrown
                    // during the above, we don't leave the Surface in an
                    // inconsistent state
                    if (c != null) {
                        mSurfaceHolder.unlockCanvasAndPost(c);
                    }
                }
            }
        }



        private void draw(Canvas canvas) {
            canvas.drawColor(Color.BLACK);
            canvas.drawBitmap(pfunzel, 0, 0, null);
            canvas.drawRect(0, 0, pfunzel.getWidth(), pfunzel.getHeight(), p);
        }

        /* Callback invoked when the surface dimensions change. */
        public void setSurfaceSize(int width, int height) {
            // synchronized to make sure these all change atomically
            synchronized (mSurfaceHolder) {
                mCanvasWidth = width;
                mCanvasHeight = height;

                // don't forget to resize the background image
                // mBackgroundImage = mBackgroundImage.createScaledBitmap(mBackgroundImage, width, height, true);
            }
        }
    }



}


