package com.example.jeremy.brightnessadjuster;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.RequiresApi;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

    private static final String SCREEN_BRIGHTNESS_STRING = "Current screen brightness value is ";
    private int initialBrightness = 0;
    private boolean isInitialAutoBrightness = false;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        setupInitialBrightness();

        final TextView screenBrightnessValueTextView = findViewById(R.id.change_screen_brightness_value_text_view);

        SeekBar seekBar = (SeekBar) findViewById(R.id.change_screen_brightness_seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @RequiresApi(api = Build.VERSION_CODES.M)
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {

                Context context = getApplicationContext();

                boolean canWriteSettings = Settings.System.canWrite(context);

                if (canWriteSettings) {
                    //Needs to convert because the max screen brightness is 255 and max seekbar value is 100
                    int screenBrightnessValue = i * 255 / 100;

                    screenBrightnessValueTextView.setText(SCREEN_BRIGHTNESS_STRING + screenBrightnessValue);

                    setBrightnessToManual();
                    setCurrentBrightness(screenBrightnessValue);
                } else {
                    askWritePermissions();
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

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
     * @param view
     */
    public void autoBrightnessSwitchClick(View view) {
        Switch autoBrightnessSwitch = findViewById(R.id.switch1);
        if (autoBrightnessSwitch.isChecked()) {
            setBrightnessToAuto();
        } else {
            setBrightnessToManual();
        }
    }

    /**
     * Stores initial brightness values
     */
    private void setupInitialBrightness() {
        initialBrightness = getCurrentBrightness();
        if (checkIfAutoBrightness() == 1) {
            isInitialAutoBrightness = true;
        } else {
            isInitialAutoBrightness = false;
        }
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
        }
    }
}
