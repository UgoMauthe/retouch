package com.mauthe.retouch;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.media.*;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.Toast;

import java.io.File;
import java.io.OutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;


public class IRMainActivity extends Activity {


    public static String IR_FILE_TO_OPEN = "FILE_TO_OPEN";
    private static int IR_IMAGE_RESULTS = 1;
    private ImageButton btnGallery;
    private ImageButton btnSnapShot;

    private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 100;
    private Uri fileUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_irmain);

        btnGallery = (ImageButton) findViewById(R.id.ivPickFromGallery);
        btnSnapShot = (ImageButton) findViewById(R.id.ivTakeShoot);

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Create the Intent for Image Gallery.
                Intent i = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                i.putExtra(Intent.EXTRA_LOCAL_ONLY, true);
                // Start new activity with the LOAD_IMAGE_RESULTS to handle back the results when image is picked from the Image Gallery.
                startActivityForResult(i, IR_IMAGE_RESULTS);
            }
        });


        btnSnapShot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                String path = Environment.getExternalStorageDirectory().toString() + "/" + getResources().getString(R.string.app_name);



                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

                String newFilename = "camerashot-" + sdf.format(new Date()) + "_0.jpg";


                File tmp = new File(path);
                tmp.mkdir();

                File file = new File(path,newFilename);

                fileUri = Uri.fromFile(file);

                intent.putExtra(MediaStore.EXTRA_OUTPUT, fileUri); // set the image file name

                // start the image capture Intent
                startActivityForResult(intent, CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
            }
        });


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        String imagePath = "";
        if (requestCode == IR_IMAGE_RESULTS && resultCode == RESULT_OK && data != null) {
            Uri imageData = data.getData();
            imagePath = BitmapUtils.getRealImagePathFromURI(this,imageData,MediaStore.Images.Media.DATA);
            if (imagePath.length() <=0) {
                imagePath = BitmapUtils.getRealImagePathFromURI(this,imageData,MediaStore.Images.Media.DISPLAY_NAME);
            }
        }

        if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                String[] media = new String[1];
                media[0] = fileUri.toString().replace("file://","");
                MediaScannerConnection.scanFile(this,media,null,null);
                imagePath = media[0];
            } else if (resultCode == RESULT_CANCELED) {
                // User cancelled the image capture
            } else {
                // Image capture failed, advise user
            }
        }
        if (imagePath.length() > 0) {
            Intent intent = new Intent(IRMainActivity.this,IREditActivity.class);
            intent.putExtra(IR_FILE_TO_OPEN,imagePath);
            startActivity(intent);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.irmain, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
