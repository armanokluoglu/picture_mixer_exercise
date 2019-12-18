package com.arman.app;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.Toolbar;
import android.util.DisplayMetrics;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.support.constraint.ConstraintLayout.LayoutParams;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{
    private CardView fabPopUpCard;
    private CardView clickedPopUpCard;
    private CardView clickedCard;
    private Button fabCardImageUploadButton;
    private Button fabCardOkButton;
    private Button fabCardCloseButton;
    private Button clickedCardCloseButton;
    private Button clickedCardEditButton;
    private Button clickedCardDeleteButton;
    private FloatingActionButton fab;
    private ImageView fabCardImage;
    private ImageView clickedCardImage;
    private Uri fabCardImageUri;
    private Uri clickedCardImageURI;
    private ConstraintLayout layout;
    private static final int PICK_IMAGE = 100;
    private static final int PHOTO_REQUEST = 2;
    private static final int PHOTO_EDIT_REQUEST = 3;
    private static final int WRITE_REQUEST_CODE = 134;
    private LayoutParams params;
    private CardList cards;
    private List<Uri> uris;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        layout = findViewById(R.id.layout);
        fabPopUpCard = findViewById(R.id.card);
        clickedPopUpCard = findViewById(R.id.card2);
        clickedCard = null;
        clickedCardImageURI = null;
        clickedCardCloseButton = findViewById(R.id.close2);
        clickedCardEditButton = findViewById(R.id.edit);
        clickedCardImage = findViewById(R.id.image2);
        clickedCardDeleteButton = findViewById(R.id.delete);
        fabCardImageUploadButton = findViewById(R.id.uploadButton);
        fabCardImage = findViewById(R.id.image);
        fabCardCloseButton = findViewById(R.id.close);
        fabCardOkButton = findViewById(R.id.ok);
        fab = findViewById(R.id.fab);
        cards = new CardList();
        uris = new ArrayList<>();
        reqPerm();
        getUris();
        initializeCards();

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabPopUpCard.setVisibility(View.VISIBLE);
            }
        });

        fabCardImageUploadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openGallery();
            }
        });

        fabCardOkButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                intent.putExtra("imageUri", fabCardImageUri);
                startActivityForResult(intent, PHOTO_REQUEST);
            }
        });

        fabCardCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                fabPopUpCard.setVisibility(View.INVISIBLE);
                fabCardImage.setImageDrawable(getResources().getDrawable(R.drawable.frame));
            }
        });

        clickedCardEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, FullscreenActivity.class);
                intent.putExtra("imageUri", clickedCardImageURI);
                startActivityForResult(intent, PHOTO_EDIT_REQUEST);
            }
        });

        clickedCardCloseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                clickedPopUpCard.setVisibility(View.INVISIBLE);
                clickedCardImage.setImageDrawable(getResources().getDrawable(R.drawable.frame));
            }
        });

        clickedCardDeleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                File img = new File(clickedCardImageURI.getPath());
                img.delete();
                MediaScannerConnection.scanFile(getApplicationContext(), new String[]{clickedCardImageURI.toString()}, null, null);
                layout.removeView(clickedCard);
                clickedPopUpCard.setVisibility(View.INVISIBLE);
                finish();
                startActivity(getIntent());
            }
        });
    }

    private void initializeCards() {
        for(Uri uri: uris) {
            ImageView iv = new ImageView(getApplicationContext());
            iv.setImageURI(uri);
            createCard(iv);
        }
    }

    private void createCard(ImageView newImage) {
        CardView card = new CardView(getApplicationContext());
        card.setId(View.generateViewId());
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        int widthOfScreen = metrics.widthPixels;
        int widthOfCard = (widthOfScreen / 2) - 75;
        params = new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT);
        params.height = widthOfCard;
        params.width = widthOfCard;
        card.setLayoutParams(params);
        card.setRadius(15);
        card.setCardBackgroundColor(Color.parseColor("#FF008577"));
        card.setCardElevation(5);
        newImage.setLayoutParams(params);
        card.addView(newImage);
        layout.addView(card);
        card.setOnClickListener(this);
        setConstraints(card);
        cards.addCard(card);
    }

    private Uri getCardUri(CardView card) {
        int counter = 0;
        for(CardView c: cards.getCards()) {
            if(c.equals(card)) {
                return uris.get(counter);
            }
            counter++;
        }
        return null;
    }

    private void setConstraints(CardView card) {
        ConstraintSet set = new ConstraintSet();
        set.constrainWidth(card.getId(), ConstraintSet.WRAP_CONTENT);
        set.constrainHeight(card.getId(), ConstraintSet.WRAP_CONTENT);
        if(cards.size() % 2 == 0) {
            set.connect(card.getId(), ConstraintSet.LEFT, ConstraintSet.PARENT_ID, ConstraintSet.LEFT, 50);
        } else {
            set.connect(card.getId(), ConstraintSet.RIGHT, ConstraintSet.PARENT_ID, ConstraintSet.RIGHT, 50);
        }
        if(cards.size() == 0 || cards.size() == 1) {
            set.connect(card.getId(), ConstraintSet.TOP, ConstraintSet.PARENT_ID, ConstraintSet.TOP, 50);
        } else {
            if(cards.size() % 2 == 0) {
                set.connect(card.getId(), ConstraintSet.TOP, cards.get(cards.size() - 1).getId(), ConstraintSet.BOTTOM, 50);
            } else {
                set.connect(card.getId(), ConstraintSet.TOP, cards.get(cards.size() - 2).getId(), ConstraintSet.BOTTOM, 50);
            }
        }
        set.applyTo(layout);
    }

    private void getUris() {
        File[] listFile;
        File file = new File("/sdcard/AppTestFolder");

        if (file.isDirectory()) {
            listFile = file.listFiles();

            for (int i = 0; i < listFile.length; i++) {
                String path = listFile[i].getAbsolutePath();
                Uri uri = Uri.parse(path);
                uris.add(uri);
            }
        }
    }

    private void openGallery() {
        Intent gallery = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
        startActivityForResult(gallery, PICK_IMAGE);
    }

    @Override
    public void onClick(View v) {
        clickedPopUpCard.setVisibility(View.VISIBLE);
        clickedCard = (CardView) v;
        clickedCardImageURI = getCardUri(clickedCard);
        clickedCardImage.setImageURI(clickedCardImageURI);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case WRITE_REQUEST_CODE:
                if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                    //Granted.
                }
                else{
                    //Denied.
                }
                break;
        }
    }

    @TargetApi(23)
    private void reqPerm() {
        String[] permissions = {Manifest.permission.WRITE_EXTERNAL_STORAGE};
        requestPermissions(permissions, WRITE_REQUEST_CODE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK && requestCode == PICK_IMAGE) {
            pickImageFromGallery(data);
        }
        if(resultCode == RESULT_OK && requestCode == PHOTO_REQUEST) {
            getImageFromFullscreen(data);
        }
        if(resultCode == RESULT_CANCELED && requestCode == PHOTO_REQUEST) {
            returnFromFullscreen();
        }
        if(resultCode == RESULT_OK && requestCode == PHOTO_EDIT_REQUEST) {
            getEditedImageFromFullscreen(data);
        }
        if(resultCode == RESULT_CANCELED && requestCode == PHOTO_EDIT_REQUEST) {
            returnFromEditFullscreen();
        }
    }

    //ON ACTIVITY RESULT METHODS
    private void pickImageFromGallery(Intent data) {
        fabCardImageUri = data.getData();
        fabCardImage.setImageURI(fabCardImageUri);
        fabCardOkButton.setVisibility(View.VISIBLE);
    }

    private void getImageFromFullscreen(Intent data) {
        Uri newImageUri = data.getParcelableExtra("imageUri2");
        ImageView newImage = new ImageView(getApplicationContext());
        newImage.setImageURI(newImageUri);
        uris.add(newImageUri);
        createCard(newImage);
        fabPopUpCard.setVisibility(View.INVISIBLE);
        fabCardImage.setImageDrawable(getResources().getDrawable(R.drawable.frame));
        fabCardOkButton.setVisibility(View.INVISIBLE);
    }

    private void returnFromFullscreen() {
        fabPopUpCard.setVisibility(View.INVISIBLE);
        fabCardImage.setImageDrawable(getResources().getDrawable(R.drawable.frame));
        fabCardOkButton.setVisibility(View.INVISIBLE);
    }

    private void getEditedImageFromFullscreen(Intent data) {
        Uri newImageUri = data.getParcelableExtra("imageUri2");
        ImageView newImage = new ImageView(getApplicationContext());
        newImage.setImageURI(newImageUri);
        File img = new File(clickedCardImageURI.getPath());
        img.delete();
        MediaScannerConnection.scanFile(getApplicationContext(), new String[]{clickedCardImageURI.toString()}, null, null);
        clickedCardImageURI = newImageUri;
        uris.add(clickedCardImageURI);
        ((ImageView) clickedCard.getChildAt(0)).setImageURI(clickedCardImageURI);
        clickedCard.setOnClickListener(this);
        clickedPopUpCard.setVisibility(View.INVISIBLE);
        clickedCardImage.setImageDrawable(getResources().getDrawable(R.drawable.frame));
    }

    private void returnFromEditFullscreen() {
        clickedPopUpCard.setVisibility(View.INVISIBLE);
        clickedCardImage.setImageDrawable(getResources().getDrawable(R.drawable.frame));
    }
}
