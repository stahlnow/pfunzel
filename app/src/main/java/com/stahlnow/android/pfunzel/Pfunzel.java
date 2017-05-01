package com.stahlnow.android.pfunzel;

import android.annotation.TargetApi;
import android.app.Activity;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.SurfaceTexture;


import android.hardware.Camera;

import android.nfc.Tag;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;


import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.stahlnow.pfunzel.R;

import java.io.IOException;


public class Pfunzel extends Activity {
	
	private static final String TAG = "Pfunzel";

	private boolean isOn;
    private boolean blueIsOn;

    private VerticalSeekBar pfunzel_slider_left;
    private VerticalSeekBar pfunzel_slider_right;
    private ToggleButton pfunzel_button;
    private ToggleButton pfunzel_switch;
    private ImageView mask;
    private ImageView lense;

    // support for api < 23
    private Camera mCamera;

    // support for api > 23
    private Camera2Object mCamera2Object;
    private float mBrightness;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pfunzel);
		isOn = false;


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            mCamera2Object = new Camera2Object(this);
        }

        // get reference to all ui elements
        pfunzel_slider_left = (VerticalSeekBar) findViewById(R.id.pfunzel_slider_left);
        pfunzel_slider_left.setProgress((int) (Math.random() * 100));
        pfunzel_slider_right = (VerticalSeekBar) findViewById(R.id.pfunzel_slider_right);
        pfunzel_slider_right.setProgress((int) (Math.random() * 100));


        pfunzel_button = (ToggleButton) findViewById(R.id.pfunzel_button);

        pfunzel_switch = (ToggleButton) findViewById(R.id.pfunzel_switch);


        lense = (ImageView) findViewById(R.id.pfunzel_lense);

        // set listeners

        pfunzel_slider_left.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // f(x) = log(1+x)/log(1+max)
                // with minimum != 0 better use:
                // f(x) = log(x/min) / log(max/min)
                float bLog = (float)(Math.log(mBrightness/1.0) / Math.log(255.0/1.0));
                float alpha = clamp(bLog, 0.6f, 0.9f);

                if (!blueIsOn)
                    lense.setAlpha(Math.min(alpha, Math.max((float)(progress/100.0), (float)(pfunzel_slider_right.getProgress()/100.0) )));
                else
                    lense.setAlpha(alpha);
                lense.setColorFilter(Color.argb(
                        255,
                        (int)((progress/100.0)*255),
                        (int)((pfunzel_slider_right.getProgress()/100.0)*255),
                        blueIsOn ? ((int)(Math.max(alpha, Math.max((float)(progress/100.0), (float)(pfunzel_slider_right.getProgress()/100.0) ))*255)) : 0),
                        PorterDuff.Mode.MULTIPLY);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        pfunzel_slider_right.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                float bLog = (float)(Math.log(mBrightness/1.0) / Math.log(255.0/1.0));
                float alpha = clamp(bLog, 0.6f, 0.9f);

                if (!blueIsOn)
                    lense.setAlpha(Math.min(alpha, Math.max((float)(progress/100.0), (float)(pfunzel_slider_left.getProgress()/100.0) )));
                else
                    lense.setAlpha(alpha);
                lense.setColorFilter(Color.argb(
                        255,
                        (int)((pfunzel_slider_left.getProgress()/100.0)*255),
                        (int)((progress/100.0)*255),
                        blueIsOn ? ((int)(Math.max(alpha, Math.max((float)(progress/100.0), (float)(pfunzel_slider_left.getProgress()/100.0) ))*255)) : 0),
                        PorterDuff.Mode.MULTIPLY);
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {}
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {}
        });

        // update sliders
        updateSliders();

        // listen on switch
        pfunzel_switch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public synchronized void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                mask = (ImageView) findViewById(R.id.pfunzel_mask);
                if (isChecked) {
                    pfunzel_slider_left.setEnabled(false);
                    pfunzel_slider_right.setEnabled(false);
                    pfunzel_button.setVisibility(View.GONE);
                    mask.setVisibility(View.VISIBLE);
                } else {
                    pfunzel_slider_left.setEnabled(true);
                    pfunzel_slider_right.setEnabled(true);
                    pfunzel_button.setVisibility(View.VISIBLE);
                    mask.setVisibility(View.INVISIBLE);
                }

            }
        });

        // check button
        pfunzel_button.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public synchronized void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    blueIsOn = true;
                } else {
                    blueIsOn = false;
                }

                // update sliders by slightly changing the faders
                updateSliders();
            }
        });
	}

    private static float clamp(float val, float min, float max) {
        return Math.max(min, Math.min(max, val));
    }

	private void updateSliders() {
        int direction = 1;
        if (pfunzel_slider_left.getProgress() == pfunzel_slider_left.getMax())
            direction = -1;
        pfunzel_slider_left.incrementProgressBy(direction);
        pfunzel_slider_left.incrementProgressBy(-direction);

        if (pfunzel_slider_right.getProgress() == pfunzel_slider_right.getMax())
            direction = -1;
        pfunzel_slider_right.incrementProgressBy(direction);
        pfunzel_slider_right.incrementProgressBy(-direction);

    }
	
	public void toggle(View v) {
	    if (isOn)
	    	ledoff();
	    else
	    	ledon();
	}
	

    @Override
    public void onPause() {
    	super.onPause();

        if (isOn)
    		ledoff();
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putBoolean(getResources().getString(R.string.buttonState), pfunzel_button.isChecked());
        editor.putBoolean(getResources().getString(R.string.switchState), pfunzel_switch.isChecked());
        editor.commit();

    }
    
    @Override
    public void onResume() {
    	super.onResume();

        try {
            mBrightness = (float)(Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS));
        } catch (Settings.SettingNotFoundException e) {
            mBrightness = 255.0f;
            Log.w(TAG, e.toString());
        }

    	if (!isOn)
    		ledon();

        updateSliders(); // screen brightness could have changed..

        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        if (pfunzel_button != null)
            pfunzel_button.setChecked(sharedPref.getBoolean(getResources().getString(R.string.buttonState), false));
        if (pfunzel_switch != null)
            pfunzel_switch.setChecked(sharedPref.getBoolean(getResources().getString(R.string.switchState), false));

    }


    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
	private void ledon() {


        if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.M) {

            // Does the device have a camera?
            PackageManager pm = getPackageManager();
            if (!pm.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
                Log.e(TAG, "Device has no camera!");
            } else {
                // In order to work, the camera needs a surface to turn on.
                // Here I pass it a dummy Surface Texture to make it happy.
                mCamera = Camera.open();
                try {
                    mCamera.setPreviewTexture(new SurfaceTexture(0));
                } catch (IOException e) {
                    Log.e(TAG, e.toString());
                }
                mCamera.startPreview();
                Camera.Parameters p = mCamera.getParameters();
                p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
                mCamera.setParameters(p);
            }


        } else if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {
            if (mCamera2Object != null)
                mCamera2Object.ledon();
        }

		isOn = true;
	}

	private void ledoff() {

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {

            if (mCamera != null) {
                // Stopping the camera is enough to turn off the LED
                mCamera.stopPreview();
                mCamera.release();
                mCamera = null;
            }


        } else if (Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.M) {

            if (mCamera2Object != null)
                mCamera2Object.ledoff();

        }

		isOn = false;
	}
}

