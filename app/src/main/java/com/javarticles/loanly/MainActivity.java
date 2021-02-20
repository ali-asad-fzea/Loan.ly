package com.javarticles.loanly;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.ProgressDialog;
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
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URI;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.google.firebase.storage.UploadTask;
import com.javarticles.loanly.FormData;

public class MainActivity extends AppCompatActivity implements
        FetchAddressTask.OnTaskCompleted{
    ConstraintLayout parentView;
    EditText name_edittext;
    EditText email_edittext;
    EditText phone_edittext;
    Button button_selectphoto1;
    Button button_selectphoto2;
    Button button_selectphoto3;
    Button button_selectphoto4;
    TextView textView_image1data;
    TextView textView_image2data;
    TextView textView_image3data;
    TextView textView_image4data;
    Button button_submit;
    String[] sampleimagesinfo;
    String filename;
    String name_value;
    String email_value;
    String phone_value;
    boolean uploadsuccess;

    int imagenumber = -1;
    String mCurrentPhotoPath;
    String[] compressedphotopath;
    ProgressBar progressBar;

    ////data class////
    FormData formData;
    /////////////////

    //////location/////
    Location mLastLocation;
    String address;
    FusedLocationProviderClient mFusedLocationClient;
    //////////////////

    /////////firebase////////////
    // Creating StorageReference and DatabaseReference object.
    StorageReference storageReference;
    DatabaseReference databaseReference;
    ////////////////////////////


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        parentView=findViewById(R.id.parentLayout);
        name_edittext = findViewById(R.id.editTextTextPersonName);
        email_edittext = findViewById(R.id.editTextTextEmailAddress);
        phone_edittext= findViewById(R.id.editTextPhone);
        button_selectphoto1 = findViewById(R.id.buttonimage1);
        button_selectphoto2 = findViewById(R.id.buttonimage2);
        button_selectphoto3 = findViewById(R.id.buttonimage3);
        button_selectphoto4 = findViewById(R.id.buttonimage4);
        textView_image1data = findViewById(R.id.textViewimage1);
        textView_image2data = findViewById(R.id.textViewimage2);
        textView_image3data = findViewById(R.id.textViewimage3);
        textView_image4data = findViewById(R.id.textViewimage4);
        progressBar=findViewById(R.id.progressBar);
        button_submit = findViewById(R.id.submit);

        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        // Assign FirebaseStorage instance to storageReference.
        storageReference = FirebaseStorage.getInstance().getReference("Images");

        // Assign FirebaseDatabase instance with root database name.
        databaseReference = FirebaseDatabase.getInstance().getReference("Images");

        name_value=email_value=phone_value="";
        sampleimagesinfo=new String[4];
        compressedphotopath=new String[]{null,null,null,null};

        formData=new FormData();

        button_submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit_function();
            }
        });

        button_selectphoto1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 0;
                capture_function();
            }
        });

        button_selectphoto2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 1;
                capture_function();
            }
        });

        button_selectphoto3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 2;
                capture_function();
            }
        });

        button_selectphoto4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imagenumber = 3;
                capture_function();
            }
        });
    }

    void submit_function(){
        ////////upload image with text to firebase////
        //https://androidjson.com/upload-image-to-firebase-storage/

        name_value=name_edittext.getText().toString().trim();
        email_value=email_edittext.getText().toString().trim();
        phone_value=phone_edittext.getText().toString().trim();

        if(name_value.matches("") || email_value.matches("") || phone_value.matches("")
        || compressedphotopath[0]==null || compressedphotopath[1]==null || compressedphotopath[2]==null || compressedphotopath[3]==null){
            Toast.makeText(MainActivity.this,"Form Incomplete!!!",Toast.LENGTH_SHORT).show();
        }
        else{
            formData.setPersonName(name_value);
            formData.setPersonEmail(email_value);
            formData.setPersonPhone(phone_value);

            progressBar.setVisibility(View.VISIBLE);
            uploadsuccess=false;
            File pathfile;
            for(int i=0;i<4;i++){
                final int finalI = i;

                pathfile=new File(MainActivity.this.compressedphotopath[finalI]);
                final StorageReference storageReference2 = storageReference.child(sampleimagesinfo[finalI]);
                storageReference2.putFile(Uri.fromFile(pathfile))
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                Toast.makeText(getApplicationContext(), "Image "+String.valueOf(finalI) +" Uploaded Successfully ", Toast.LENGTH_LONG).show();
                                switch (finalI){
                                    case 0:{
                                        storageReference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri downloadUrl = uri;
                                                formData.setImage1Url(downloadUrl.toString());
                                            }
                                        });
                                        break;
                                    }
                                    case 1:{
                                        storageReference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri downloadUrl = uri;
                                                formData.setImage2Url(downloadUrl.toString());
                                            }
                                        });
                                        break;
                                    }
                                    case 2:{
                                        storageReference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri downloadUrl = uri;
                                                formData.setImage3Url(downloadUrl.toString());
                                            }
                                        });
                                        break;
                                    }
                                    case 3:{
                                        storageReference2.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                Uri downloadUrl = uri;
                                                formData.setImage4Url(downloadUrl.toString());
                                            }
                                        });
                                        uploadsuccess=true;
                                        saveToDatabase_function(uploadsuccess);
                                        progressBar.setVisibility(View.GONE);
                                        break;
                                    }
                                }

                            }
                        })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception exception) {
                                Toast.makeText(MainActivity.this,"Error uploading "+String.valueOf(finalI+1),Toast.LENGTH_SHORT).show();
                                if(finalI==3){
                                    progressBar.setVisibility(View.GONE);
                                }
                            }
                        });
            }

        }
    }
    void saveToDatabase_function(boolean uploadfilesuccess){
        if(uploadfilesuccess){
            String ImageUploadId = databaseReference.push().getKey();
            databaseReference.child(ImageUploadId).setValue(formData);
            Snackbar.make(parentView,"Successfully Uploaded",Snackbar.LENGTH_SHORT).show();
        }
        else{
            Snackbar.make(parentView,"Error Occured",Snackbar.LENGTH_SHORT).show();
        }
    }

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
            MainActivity.this.compressedphotopath[imagenumber]=imagecompress.getAbsolutePath();

            ///////saving compressed image///////
            try (FileOutputStream out = new FileOutputStream(imagecompress)) {
                bmpWithBorder.compress(Bitmap.CompressFormat.JPEG, 50, out);

            } catch (IOException e) {
                e.printStackTrace();
            }
            /////////////////////////////////

            /////fetching file info////
            File tempCompressedFile=new File(MainActivity.this.compressedphotopath[imagenumber]);
            sampleimagesinfo[imagenumber] = tempCompressedFile.getName() ;
            filename=tempCompressedFile.getName() + "\n" + String.valueOf(tempCompressedFile.length() / 1000) + " KB";
            //////////////////////////

            switch (imagenumber) {
                case 0: {
                    textView_image1data.setText(filename);
                    break;
                }

                case 1: {
                    textView_image2data.setText(filename);
                    break;
                }

                case 2: {
                    textView_image3data.setText(filename);
                    break;
                }
                case 3: {
                    textView_image4data.setText(filename);
                    break;
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
