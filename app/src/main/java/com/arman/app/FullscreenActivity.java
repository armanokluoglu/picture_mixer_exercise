package com.arman.app;

import android.annotation.SuppressLint;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.widget.CardView;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Random;
import java.util.UUID;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FullscreenActivity extends AppCompatActivity {
    /**
     * Whether or not the system UI should be auto-hidden after
     * {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static final boolean AUTO_HIDE = true;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after
     * user interaction before hiding the system UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    /**
     * Some older devices needs a small delay between UI widget updates
     * and a change of the status and navigation bar.
     */
    private static final int UI_ANIMATION_DELAY = 300;
    private final Handler mHideHandler = new Handler();
    private View mContentView;
    private final Runnable mHidePart2Runnable = new Runnable() {
        @SuppressLint("InlinedApi")
        @Override
        public void run() {
            // Delayed removal of status and navigation bar

            // Note that some of these constants are new as of API 16 (Jelly Bean)
            // and API 19 (KitKat). It is safe to use them, as they are inlined
            // at compile-time and do nothing on earlier devices.
            mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION);
        }
    };
    private View mControlsView;
    private final Runnable mShowPart2Runnable = new Runnable() {
        @Override
        public void run() {
            // Delayed display of UI elements
            ActionBar actionBar = getSupportActionBar();
            if (actionBar != null) {
                actionBar.show();
            }
            mControlsView.setVisibility(View.VISIBLE);
        }
    };
    private boolean mVisible;
    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    /**
     * Touch listener to use for in-layout UI controls to delay hiding the
     * system UI. This is to prevent the jarring behavior of controls going away
     * while interacting with activity UI.
     */
    private final View.OnTouchListener mDelayHideTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            if (AUTO_HIDE) {
                delayedHide(AUTO_HIDE_DELAY_MILLIS);
            }
            return false;
        }
    };
    private ImageView image;
    private Button randomize;
    private Button done;
    private Button setWall;
    private Uri imageUri;
    private Button close;
    private boolean changed;
    private Bitmap bm;
    private TextView estTime;
    private int[] pixelsOfBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fullscreen);

        mVisible = true;
        mControlsView = findViewById(R.id.fullscreen_content_controls);
        mContentView = findViewById(R.id.fullscreen_content);


        // Set up the user interaction to manually show or hide the system UI.
        mContentView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                toggle();
            }
        });

        // Upon interacting with UI controls, delay any scheduled hide()
        // operations to prevent the jarring behavior of controls going away
        // while interacting with the UI.
        image = findViewById(R.id.image);
        done = findViewById(R.id.done);
        randomize = findViewById(R.id.randomize);
        setWall = findViewById(R.id.setWall);
        close = findViewById(R.id.close);
        estTime = findViewById(R.id.estimatedTime);
        changed = false;
        bm = null;

        Intent intent = getIntent();
        imageUri = intent.getParcelableExtra("imageUri");
        image.setImageURI(imageUri);

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
                setResult(RESULT_CANCELED, intent);
                finish();
            }
        });

        done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(changed) {
                    imageUri = getImageUri(bm);
                }
                Intent intent = new Intent(FullscreenActivity.this, MainActivity.class);
                intent.putExtra("imageUri2", imageUri);
                setResult(RESULT_OK, intent);
                finish();
            }
        });

        randomize.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                changed = true;
                randomizeImage();
                image.setImageBitmap(bm);
            }
        });

        setWall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                WallpaperManager myWallpaperManager = WallpaperManager.getInstance(getApplicationContext());
                Bitmap bm = ((BitmapDrawable)image.getDrawable()).getBitmap();
                try {
                    myWallpaperManager.setBitmap(bm);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(100);
    }

    private void randomizeImage() {
        long startTime = System.currentTimeMillis();
        bm = ((BitmapDrawable)image.getDrawable()).getBitmap();
        Random random = new Random();
        bm = bm.copy(bm.getConfig() , true);
        int height = bm.getHeight();
        int width = bm.getWidth();
        pixelsOfBitmap = new int[width * height];
        bm.getPixels(pixelsOfBitmap, 0, width,0,0, width, height);
        int randomizedPixelNumber;
        if(height * width > 10000) {
            randomizedPixelNumber = 100;
        } else {
            throw new IllegalArgumentException("Image is too small");
        }
        while(randomizedPixelNumber != 0) {
            int randColorRed = random.nextInt(255 + 1 + 40) - 255;
            int randColorBlue = random.nextInt(255 + 1 + 40) - 255;
            int randColorGreen = random.nextInt(255 + 1 + 40) - 255;
            int x = Math.abs((int) ((Math.random() * width) - 1));
            int y = Math.abs((int) ((Math.random() * height) - 1));
            changeNearbyPixels(x, y, randColorRed, randColorGreen, randColorBlue, width, height, true, true, true, true);
            randomizedPixelNumber--;
        }
        bm.setPixels(pixelsOfBitmap, 0, width, 0, 0, width, height);
        long estimatedTime = System.currentTimeMillis() - startTime;
        estTime.setText("Operation was completed in: " + estimatedTime + " milliseconds");
        estTime.setVisibility(View.VISIBLE);
    }

    private void changeNearbyPixels(int x, int y, int randColorRed, int randColorGreen, int randColorBlue, int width, int height, boolean left, boolean right, boolean up, boolean down) {
        int main = getPixel(x, y, width);
        setPixel(x, y, Color.argb(255, Math.abs(Color.red(main) + randColorRed) % 255, Math.abs(Color.green(main) + randColorGreen) % 255, Math.abs(Color.blue(main) + randColorBlue) % 255), width);
        if(x < width - 1 && right) {
            int pix1 = getPixel(x + 1, y, width);
            if(alikePixel(main, pix1)) {
                changeNearbyPixels(x + 1, y, randColorRed, randColorGreen, randColorBlue, width, height, false, true, false, false);
            }
        }
        if(x > 0 && left) {
            int pix2 = getPixel(x - 1, y, width);
            if(alikePixel(main, pix2)) {
                changeNearbyPixels(x - 1, y, randColorRed, randColorGreen, randColorBlue, width, height, true, false, false, false);
            }
        }
        if(y > 0 && down) {
            int pix7 = getPixel(x, y - 1, width);
            if(alikePixel(main, pix7)) {
                changeNearbyPixels(x, y - 1, randColorRed, randColorGreen, randColorBlue, width, height, true, true, false, true);
            }
        }
        if(y < height - 1 && up) {
            int pix8 = getPixel(x, y + 1, width);
            if(alikePixel(main, pix8)) {
                changeNearbyPixels(x, y + 1, randColorRed, randColorGreen, randColorBlue, width, height, true, true, true, false);
            }
        }
    }

    private int getPixel(int x, int y, int width) {return pixelsOfBitmap[x + y * width];}

    private void setPixel(int x, int y, int color, int width) {pixelsOfBitmap[x + y * width] = color;}

    private boolean alikePixel(int pix1, int pix2) {
        if(Math.abs(Color.red(pix1) - Color.red(pix2)) < 20 && Math.abs(Color.blue(pix1) - Color.blue(pix2)) < 20 && Math.abs(Color.green(pix1) - Color.green(pix2)) < 20) {
            return true;
        } else {
            return false;
        }
    }

    public Uri getImageUri(Bitmap src) {
        String name = UUID.randomUUID().toString()+".jpg";
        File direct = new File(Environment.getExternalStorageDirectory() + "/AppTestFolder");

        if (!direct.exists()) {
            File directory = new File("/sdcard/AppTestFolder/");
            directory.mkdirs();
        }

        File file = new File(new File("/sdcard/AppTestFolder/"), name);
        if (file.exists()) {
            file.delete();
        }
        try {
            FileOutputStream out = new FileOutputStream(file);
            src.compress(Bitmap.CompressFormat.JPEG, 100, out);
            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return Uri.parse(file.getAbsolutePath());
    }

    private void toggle() {
        if (mVisible) {
            hide();
        } else {
            show();
        }
    }

    private void hide() {
        // Hide UI first
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }
        mControlsView.setVisibility(View.GONE);
        mVisible = false;

        // Schedule a runnable to remove the status and navigation bar after a delay
        mHideHandler.removeCallbacks(mShowPart2Runnable);
        mHideHandler.postDelayed(mHidePart2Runnable, UI_ANIMATION_DELAY);
    }

    @SuppressLint("InlinedApi")
    private void show() {
        // Show the system bar
        mContentView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION);
        mVisible = true;

        // Schedule a runnable to display UI elements after a delay
        mHideHandler.removeCallbacks(mHidePart2Runnable);
        mHideHandler.postDelayed(mShowPart2Runnable, UI_ANIMATION_DELAY);
    }

    /**
     * Schedules a call to hide() in delay milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
