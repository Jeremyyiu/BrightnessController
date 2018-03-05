package com.example.jeremy.brightnessadjuster;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Switch;
import android.widget.Toast;

import com.gc.materialdesign.views.Slider;

public class MainActivity extends AppCompatActivity {

    private static final String SCREEN_BRIGHTNESS_STRING = "Current screen brightness value is ";
    private int initialBrightness = 0;
    private boolean isInitialAutoBrightness = false;
    Slider brightnessSlider = null;
    BrightnessObserver brightnessObserver = null;
    AutoBrightnessObserver autobrightnessObserver = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupInitialBrightness();
        // get and initialize brightness slider
        brightnessSlider = (Slider) findViewById(R.id.brightnessSlider);
        initSlider(brightnessSlider);

        final Uri BRIGHTNESS_URL = Settings.System.getUriFor(android.provider.Settings.System.SCREEN_BRIGHTNESS);
        brightnessObserver = new BrightnessObserver(new Handler());
        getApplicationContext().getContentResolver()
                .registerContentObserver(BRIGHTNESS_URL, true, brightnessObserver);

        final Uri AUTOBRIGHTNESS_URL = Settings.System.getUriFor(Settings.System.SCREEN_BRIGHTNESS_MODE);
        autobrightnessObserver = new AutoBrightnessObserver(new Handler());
        getApplicationContext().getContentResolver()
                .registerContentObserver(AUTOBRIGHTNESS_URL, true, autobrightnessObserver);
    }

    private void initSlider(Slider slider) {
        final int minBrightness = 10;
        final int maxBrightness = 255;
        // set range of slider
        brightnessSlider.setMax(maxBrightness - minBrightness);

        int screenBrightness = getCurrentBrightness();
        slider.setValue(screenBrightness);
        slider.setOnValueChangedListener(new Slider.OnValueChangedListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onValueChanged(int progress) {

                Context context = getApplicationContext();

                //Checks if this app can modify system settings
                boolean canWriteSettings = Settings.System.canWrite(context);

                if (canWriteSettings) {
                    progress = progress + minBrightness;

                    setBrightnessToManual();
                    setCurrentBrightness(progress);

                } else {
                    //if currently cant modify system settings, app will ask for permission
                    Toast.makeText(context, "Please Enable Write Permissions", Toast.LENGTH_SHORT).show();
                    askWritePermissions();
                }
            }
        });
    }

    /**
     * Shows the modify system settings panel to allow the user to add WRITE_SETTINGS permissions for this app.
     */
    private void askWritePermissions() {
        Intent intent = new Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS);
        getApplicationContext().startActivity(intent);
    }

    /**
     * Toggles Autobrightness
     *
     * @param view - update changes to the  screen
     */
    public void autoBrightnessSwitchClick(View view) {
        Switch autoBrightnessSwitch = findViewById(R.id.switch1);
        if (autoBrightnessSwitch.isChecked()) {
            setBrightnessToAuto();
        } else {
            setBrightnessToManual();
        }
        brightnessSlider.setValue(getCurrentBrightness());
    }

    /**
     * Stores initial brightness values
     */
    private void setupInitialBrightness() {
        initialBrightness = getCurrentBrightness();
        isInitialAutoBrightness = checkIfAutoBrightness() == 1;
    }

    private int checkIfAutoBrightness() {
        return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, 0);
    }

    /**
     * Get current system brightness value as an integer
     */
    private int getCurrentBrightness() {
        return Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, 0);
    }

    private void setCurrentBrightness(int screenBrightnessValue) {
        Settings.System.putInt(getApplicationContext().getContentResolver(), Settings.System.SCREEN_BRIGHTNESS, screenBrightnessValue);
    }

    /**
     * Turn automatic brightness mode on - set manual mode off
     */
    private void setBrightnessToAuto() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_AUTOMATIC);
    }

    /**
     * Turn automatic brightness mode off - set manual mode on
     */
    private void setBrightnessToManual() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_BRIGHTNESS_MODE, Settings.System.SCREEN_BRIGHTNESS_MODE_MANUAL);
    }


    public void revertBtn(View view) {
        revertBrightness(initialBrightness, isInitialAutoBrightness);
    }

    /**
     * Checks if the initial brightness was automatic or manual and reverts the brightness to the brightness before changes.
     */
    private void revertBrightness(int initialBrightnessValue, boolean wasAuto) {
        if (wasAuto) {
            setBrightnessToAuto();
        } else {
            setBrightnessToManual();
            setCurrentBrightness(initialBrightnessValue);
            brightnessSlider.setValue(initialBrightnessValue);
        }
    }

    private void autoBrightnessToggle() {
        Switch autoBrightnessSwitch = findViewById(R.id.switch1);
        if (checkIfAutoBrightness() == 1) {
            autoBrightnessSwitch.setChecked(true);
        } else if (checkIfAutoBrightness() == 0) {
            autoBrightnessSwitch.setChecked(false);
        }
    }


    /**
     * BrightnessObserver: Handle the change in brightness in real time and change the Slider value
     */
    private class BrightnessObserver extends ContentObserver {
        public BrightnessObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            autoBrightnessToggle();
            brightnessSlider.setValue(getCurrentBrightness());
        }
    }

    /**
     * AutoBrightnessObserver: Handle the change in brightness in real time and change the Slider Value
     */
    private class AutoBrightnessObserver extends ContentObserver {
        public AutoBrightnessObserver(Handler h) {
            super(h);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return true;
        }

        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);
            autoBrightnessToggle();
            brightnessSlider.setValue(getCurrentBrightness());
        }
    }
}
