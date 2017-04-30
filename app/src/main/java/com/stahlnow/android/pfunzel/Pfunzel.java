package com.stahlnow.android.pfunzel;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCharacteristics;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import android.hardware.camera2.CameraManager;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.ToggleButton;

import com.stahlnow.pfunzel.R;


public class Pfunzel extends Activity {
	
	private static final String TAG = "Pfunzel";

	private boolean isOn;
    private boolean blueIsOn;

    private CameraManager manager;
    private VerticalSeekBar pfunzel_slider_left;
    private VerticalSeekBar pfunzel_slider_right;
    private ToggleButton pfunzel_button;
    private ToggleButton pfunzel_switch;
    private ImageView mask;
    private ImageView lense;


    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_pfunzel);
		isOn = false;
        manager = (CameraManager) getSystemService(Context.CAMERA_SERVICE);

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
                if (!blueIsOn)
                    lense.setAlpha(Math.min(0.9f, Math.max((float)(progress/100.0), (float)(pfunzel_slider_right.getProgress()/100.0) )));
                else
                    lense.setAlpha(0.9f);
                lense.setColorFilter(Color.argb(
                        255,
                        (int)((progress/100.0)*255),
                        (int)((pfunzel_slider_right.getProgress()/100.0)*255),
                        blueIsOn ? ((int)(Math.max(0.9f, Math.max((float)(progress/100.0), (float)(pfunzel_slider_right.getProgress()/100.0) ))*255)) : 0),
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
                if (!blueIsOn)
                    lense.setAlpha(Math.min(0.9f, Math.max((float)(progress/100.0), (float)(pfunzel_slider_left.getProgress()/100.0) )));
                else
                    lense.setAlpha(0.9f);
                lense.setColorFilter(Color.argb(
                        255,
                        (int)((pfunzel_slider_left.getProgress()/100.0)*255),
                        (int)((progress/100.0)*255),
                        blueIsOn ? ((int)(Math.max(0.9f, Math.max((float)(progress/100.0), (float)(pfunzel_slider_left.getProgress()/100.0) ))*255)) : 0),
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
    	if (!isOn)
    		ledon();
        SharedPreferences sharedPref = getPreferences(MODE_PRIVATE);
        if (pfunzel_button != null)
            pfunzel_button.setChecked(sharedPref.getBoolean(getResources().getString(R.string.buttonState), false));
        if (pfunzel_switch != null)
            pfunzel_switch.setChecked(sharedPref.getBoolean(getResources().getString(R.string.switchState), false));
    }

	
	private void ledon() {

        try {
            String[] cams = manager.getCameraIdList();

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cams[0]);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                manager.setTorchMode(cams[0], true);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "Can't access camera.");
        }


		isOn = true;
	}

	private void ledoff() {

        try {
            String[] cams = manager.getCameraIdList();

            CameraCharacteristics characteristics = manager.getCameraCharacteristics(cams[0]);
            if (characteristics.get(CameraCharacteristics.FLASH_INFO_AVAILABLE)) {
                manager.setTorchMode(cams[0], false);
            }

        } catch (CameraAccessException e) {
            Log.e(TAG, "Can't access camera.");
        }

		isOn = false;
	}
}

