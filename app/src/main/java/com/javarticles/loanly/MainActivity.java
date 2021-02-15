package com.javarticles.loanly;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        name=findViewById(R.id.editTextTextPersonName);
        email=findViewById(R.id.editTextTextEmailAddress);
        phone=findViewById(R.id.editTextPhone);
        selectphoto1=findViewById(R.id.buttonimage1);
        selectphoto2=findViewById(R.id.buttonimage2);
        selectphoto3=findViewById(R.id.buttonimage3);
        selectphoto4=findViewById(R.id.buttonimage4);
        image1data=findViewById(R.id.textViewimage1);
        image2data=findViewById(R.id.textViewimage2);
        image3data=findViewById(R.id.textViewimage3);
        image4data=findViewById(R.id.textViewimage4);
        submit=findViewById(R.id.submit);


    }
}
