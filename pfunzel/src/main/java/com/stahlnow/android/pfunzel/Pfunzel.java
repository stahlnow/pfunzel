package com.stahlnow.android.pfunzel;

import android.app.Activity;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;

import java.io.IOException;

public class Pfunzel extends Activity {

    private static final String TAG = "Pfunzel";

    private Camera camera;
    private boolean isOn;
    private boolean switchIsOn;

    private PfunzelBackground pb;
    private VerticalSeekBar hueBar, saturationBar;
    private ImageButton switchButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pfunzel);

        isOn = false;
        switchIsOn = false;


        switchButton = (ImageButton)findViewById(R.id.pfunzel_switch);

        pb = (PfunzelBackground)findViewById(R.id.background);

        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,LinearLayout.LayoutParams.WRAP_CONTENT);
        params.weight = 1;

        LinearLayout newOne = new LinearLayout(this);
        newOne.setLayoutParams(params);
        newOne.setOrientation(LinearLayout.HORIZONTAL);

        LinearLayout.LayoutParams hueParams = new LinearLayout.LayoutParams(128, 600);
        hueParams.setMargins(47, 500, 0, 20);
        hueBar =  new VerticalSeekBar(this);
        hueBar.setLayoutParams(hueParams);
        hueBar.setMax(255);
        hueBar.setProgressDrawable(getResources().getDrawable(android.R.color.transparent));
        hueBar.setThumb(getResources().getDrawable(R.drawable.kultpfunzel_button_red_01a));
        hueBar.setThumbOffset(0);

        LinearLayout.LayoutParams saturationParams = new LinearLayout.LayoutParams(128, 600);
        saturationParams.setMargins(360, 500, 0, 20);
        saturationBar = new VerticalSeekBar(this);
        saturationBar.setLayoutParams(saturationParams);
        saturationBar.setMax(255);
        saturationBar.setProgressDrawable(getResources().getDrawable(android.R.color.transparent));
        saturationBar.setThumb(getResources().getDrawable(R.drawable.kultpfunzel_button_green_01a));
        saturationBar.setThumbOffset(0);

        hueBar.setOnSeekBarChangeListener(colorBarChangeListener);
        saturationBar.setOnSeekBarChangeListener(colorBarChangeListener);

        newOne.addView(hueBar);
        newOne.addView(saturationBar);

        addContentView(newOne, params);


        switchButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                switchIsOn = !switchIsOn;
                switchButton.setBackgroundResource( switchIsOn ? R.drawable.pfunzel_switch_on : R.drawable.pfunzel_switch_off);
            }

        });
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        //Event handling logic

        try {
            Thread.sleep(16);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return true;
    }

    OnSeekBarChangeListener colorBarChangeListener = new OnSeekBarChangeListener() {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {


            pb.getThread().setColor(Color.argb(255, hueBar.getProgress(), saturationBar.getProgress(), 0));

            // Continue with IntBuffers as before...


            /*
            if (switchIsOn)
                iv.setColorFilter(ColorFilterGenerator.adjustHue(hueBar.getProgress()-180, 1.0f));
            else
                //iv.setColorFilter(Color.argb(255, hueBar.getProgress(), saturationBar.getProgress(), 0), PorterDuff.Mode.ADD);
                //iv.setColorFilter(new ColorMatrixColorFilter(colorMatrix));

                iv.setColorFilter(new PorterDuffColorFilter(Color.RED, PorterDuff.Mode.ADD));
            //iv.setColorFilter(Color.rgb(255,255,255), PorterDuff.Mode.SRC);
            */
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            // TODO Auto-generated method stub

        }
    };


    public void toggle(View v) {
        if (isOn)
            turnLedOff();
        else
            turnLedOn();
    }


    @Override
    public void onPause() {
        super.onPause();
        if (isOn)
            turnLedOff();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!isOn)
            turnLedOn();
    }


    private void turnLedOn() {
        // Does the device have a camera?
        PackageManager pm = getPackageManager();
        if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Log.e(TAG, "Device has no camera!");
        } else {
            // In order to work, the camera needs a surface to turn on.
            // Here I pass it a dummy Surface Texture to make it happy.
            camera = Camera.open();
            try {
                camera.setPreviewTexture(new SurfaceTexture(0));
            } catch (IOException e) {
                e.printStackTrace();
            }
            camera.startPreview();
            Parameters p = camera.getParameters();
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
        }

        isOn = true;
    }

    private void turnLedOff() {
        if (camera != null) {
            // Stopping the camera is enough to turn off the LED
            camera.stopPreview();
            camera.release();
            camera = null;
        }

        isOn = false;
    }
}