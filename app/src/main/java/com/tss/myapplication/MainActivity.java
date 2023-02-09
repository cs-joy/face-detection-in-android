package com.tss.myapplication;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.chaquo.python.PyObject;
import com.chaquo.python.Python;
import com.chaquo.python.android.AndroidPlatform;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private Button btnDetect, BSelectImage;
    private ImageView IVPreviewImage;
    private BitmapDrawable drawable;
    private Bitmap bitmap, selectedImageBitmap, detectionBitmap;
    private String objDetectString, imgString = "";
    private int SELECT_PICTURE = 200;
    private Uri selectedImageUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();
        selectImage();
        faceDetection();
    }

    private void faceDetection() {
        if (! Python.isStarted()) {
            Python.start(new AndroidPlatform(this));
        }

        final Python pyInstance = Python.getInstance();

        btnDetect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                drawable  = (BitmapDrawable)IVPreviewImage.getDrawable();
                bitmap = drawable.getBitmap();
                imgString = getStringImage(bitmap);

                PyObject module = pyInstance.getModule("face_detection");

                PyObject obj = module.callAttr("main", imgString);
                objDetectString = obj.toString();
                byte data[] = android.util.Base64.decode(objDetectString, Base64.DEFAULT);
                detectionBitmap = BitmapFactory.decodeByteArray(data, 0, data.length);

                IVPreviewImage.setImageBitmap(detectionBitmap);

            }
        });
    }

    private String getStringImage(Bitmap bitmap) {
        //
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = android.util.Base64.encodeToString(imageBytes, Base64.DEFAULT);

        return encodedImage;
    }

    private void selectImage() {
        BSelectImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent();
                i.setType("image/*");
                i.setAction(Intent.ACTION_GET_CONTENT);

                launchSomeActivity.launch(i);
            }
        });
    }

    ActivityResultLauncher<Intent> launchSomeActivity
            = registerForActivityResult(
            new ActivityResultContracts
                    .StartActivityForResult(),
            result -> {
                if (result.getResultCode()
                        == Activity.RESULT_OK) {
                    Intent data = result.getData();

                    if (data != null
                            && data.getData() != null) {
                        selectedImageUri = data.getData();

                        try {
                            selectedImageBitmap
                                    = MediaStore.Images.Media.getBitmap(
                                    this.getContentResolver(),
                                    selectedImageUri);
                        }
                        catch (IOException e) {
                            e.printStackTrace();
                        }
                        IVPreviewImage.setImageBitmap(
                                selectedImageBitmap);
                    }
                }
            });

    // this function is triggered when user
    // selects the image from the imageChooser
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK) {

            // compare the resultCode with the
            // SELECT_PICTURE constant
            if (requestCode == SELECT_PICTURE) {
                // Get the url of the image from data
                selectedImageUri = data.getData();
                if (null != selectedImageUri) {
                    // update the preview image in the layout
                    IVPreviewImage.setImageURI(selectedImageUri);
                }
            }
        }
    }

    private void initViews() {
        // register the UI widgets with their appropriate IDs
        BSelectImage = findViewById(R.id.BSelectImage);
        btnDetect = findViewById(R.id.btnDetect);
        IVPreviewImage = findViewById(R.id.IVPreviewImage);
    }
}