package com.javarticles.loanly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted{

    EditText name;
    EditText email;
    EditText phone;
    Button selectphoto1;
    Button selectphoto2;
    Button selectphoto3;
    Button selectphoto4;
    TextView image1data;
    TextView image2data;
    TextView image3data;
    TextView image4data;
    Button submit;
    Bitmap[] sampleimages;
    String[] sampleimagesinfo;

    int imagenumber = -1;
    String mCurrentPhotoPath;
    String compressedphotopath;

    //////location/////
    Location mLastLocation;
    String address;
    FusedLocationProviderClient mFusedLocationClient;
    //////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name = findViewById(R.id.editTextTextPersonName);
        email = findViewById(R.id.editTextTextEmailAddress);
        phone = findViewById(R.id.editTextPhone);
        selectphoto1 = findViewById(R.id.buttonimage1);
        selectphoto2 = findViewById(R.id.buttonimage2);
        selectphoto3 = findViewById(R.id.buttonimage3);
        selectphoto4 = findViewById(R.id.buttonimage4);
        image1data = findViewById(R.id.textViewimage1);
        image2data = findViewById(R.id.textViewimage2);
        image3data = findViewById(R.id.textViewimage3);
        image4data = findViewById(R.id.textViewimage4);
        submit = findViewById(R.id.submit);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        sampleimages = new Bitmap[]{null, null, null, null};
        sampleimagesinfo = new String[]{null, null, null, null};

        /*submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit_function();
            }
        });*/

        selectphoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 0;
                capture_function();
            }
        });

        selectphoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 1;
                capture_function();
            }
        });

        selectphoto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 2;
                capture_function();
            }
        });

        selectphoto4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 3;
                capture_function();
            }
        });
    }

    /*void submit_function(){

    }*/
    void capture_function() {
        final int REQUEST_IMAGE_CAPTURE = 1;

        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                ex.printStackTrace();

            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(MainActivity.this,
                        "com.javarticles.loanly.fileprovider",
                        photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
            }
        }


    }


    //creating a temporary file for image
    private File createImageFile() throws IOException {
        // Create an image file name
        SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_");
        String timeStamp = sdf.format(new Date());
        String imageFileName = timeStamp;


        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpeg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        MainActivity.this.mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
                getLocation_function();

        } else {
            Toast.makeText(MainActivity.this, "Something Went Wrong !!!", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("MissingPermission")
    void getLocation_function() {

        mFusedLocationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            mLastLocation=location;
                            new FetchAddressTask(MainActivity.this,
                                    MainActivity.this).execute(location);
                            Log.d("Location", String.valueOf(mLastLocation.getLatitude()) + " " + String.valueOf(mLastLocation.getLongitude()));

                        }
                    }
                });
            }

    @Override
    public void onTaskCompleted(String result) {
        // Update the UI
        address=result+"\nLatitude = "+String.valueOf(mLastLocation.getLatitude())+", Longitude = "+String.valueOf(mLastLocation.getLongitude());

        ////////////calling watermarkFunction//////
        watermarkAndCompress_function();
        Log.d("Final Address",result);
    }

    void watermarkAndCompress_function(){
        //Uri uri = data.getData();
        File file = new File(MainActivity.this.mCurrentPhotoPath);
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), Uri.fromFile(file));

            Bitmap bmpWithBorder = Bitmap.createBitmap(bitmap.getWidth() , bitmap.getHeight() + 300 * 2, bitmap.getConfig());
            Canvas canvas = new Canvas(bmpWithBorder);
            canvas.drawColor(Color.WHITE);
            canvas.drawBitmap(bitmap, 0,0, null);

            // new antialiased Paint
            TextPaint paint=new TextPaint(Paint.ANTI_ALIAS_FLAG);
            // text color - #3D3D3D
            paint.setColor(Color.BLACK);
            // text size in pixels
            paint.setTextSize(125);
            // text shadow
            paint.setShadowLayer(1f, 0f, 1f, Color.WHITE);

            // set text width to canvas width
            int textWidth = canvas.getWidth() - 50;
            ///////////////////text//////////
            String gText=address;
            // init StaticLayout for text
            StaticLayout textLayout = new StaticLayout(
                    gText, paint, textWidth, Layout.Alignment.ALIGN_CENTER, 1.0f, 0.0f, false);


            // get position of text's top left corner
            float x = (bmpWithBorder.getWidth() - textWidth)/2;
            float y = bitmap.getHeight()+50;

            // draw text to the Canvas
            canvas.save();
            canvas.translate(x, y);
            textLayout.draw(canvas);
            canvas.restore();
            //////////////////////////

            //////saving compressed bitmap////
            SimpleDateFormat sdf = new SimpleDateFormat("yyMMdd_HHmmss_");
            String timeStamp = sdf.format(new Date());
            String imageFileName = "Compressed_" + timeStamp;

            File dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
            File imagecompress = File.createTempFile(
                    imageFileName,
                    ".jpeg",
                    dir
            );

            //getting path of compressed image///////
            MainActivity.this.compressedphotopath=imagecompress.getAbsolutePath();

            ///////saving compressed image///////
            try (FileOutputStream out = new FileOutputStream(imagecompress)) {
                bmpWithBorder.compress(Bitmap.CompressFormat.JPEG, 50, out);

            } catch (IOException e) {
                e.printStackTrace();
            }
            /////////////////////////////////

            /////fetching file info////
            File tempCompressedFile=new File(MainActivity.this.compressedphotopath);
            sampleimagesinfo[imagenumber] = tempCompressedFile.getName() + "\n" + String.valueOf(tempCompressedFile.length() / 1000) + " KB";
            //////////////////////////

            switch (imagenumber) {
                case 0: {
                    image1data.setText(sampleimagesinfo[imagenumber]);
                    break;
                }

                case 1: {
                    image2data.setText(sampleimagesinfo[imagenumber]);
                    break;
                }

                case 2: {
                    image3data.setText(sampleimagesinfo[imagenumber]);
                    break;
                }
                case 3: {
                    image4data.setText(sampleimagesinfo[imagenumber]);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
