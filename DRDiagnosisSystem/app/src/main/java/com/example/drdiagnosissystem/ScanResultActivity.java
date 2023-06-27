package com.example.drdiagnosissystem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.drdiagnosissystem.ml.Model;
import com.example.drdiagnosissystem.ml.ModelEffb1;
import com.example.drdiagnosissystem.ml.ModelEffnetb03class;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.tensorflow.lite.DataType;
import org.tensorflow.lite.support.image.TensorImage;
import org.tensorflow.lite.support.tensorbuffer.TensorBuffer;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Base64;
import java.util.Date;

public class ScanResultActivity extends AppCompatActivity {
    private static final String TAG = "TAG";
    TextView result, desc;
    private Button backbutton;
    private AppCompatImageView imgres;
    DatabaseReference databaseResult;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {


        Bitmap bitmap, bitmapOri;

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_scan_result);
        imgres = findViewById(R.id.testing);
        result = findViewById(R.id.result);
        desc = findViewById(R.id.desc);
        backbutton = findViewById(R.id.backbutton);
        databaseResult = FirebaseDatabase.getInstance().getReference();

        byte[] byteArray = getIntent().getByteArrayExtra("bitmap");
        bitmap = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        byte[] byteArrayOri = getIntent().getByteArrayExtra("bitmapOri");
        bitmapOri = BitmapFactory.decodeByteArray(byteArrayOri, 0, byteArrayOri.length);

        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });


        Log.d(TAG, "onCreate: "+bitmap);
        imgres.setImageBitmap(bitmapOri);

        try {

            int x = classify(bitmap);

            result.setText(Texts.class3labels[x]+"");
            desc.setText(Texts.desc[x]+"");

            String id = databaseResult.push().getKey();

            String base64Image = Base64.getEncoder().encodeToString(byteArrayOri);

            Date currdate = new Date();
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");
            String formattedDate = dateFormat.format(currdate);

            Result res = new Result(base64Image, Texts.class3labels[x], formattedDate);
            insertData(res, id);


        } catch (IOException e) {
            // TODO Handle the exception
        }
    }

    int classify(Bitmap bitmap) throws IOException {
        ModelEffb1 model = ModelEffb1.newInstance(ScanResultActivity.this);
        ModelEffnetb03class model3class = ModelEffnetb03class.newInstance(ScanResultActivity.this);

        // Creates inputs for reference.
        TensorBuffer inputFeature0 = TensorBuffer.createFixedSize(new int[]{1, 224, 224, 3}, DataType.FLOAT32);

        TensorImage tensorImage = new TensorImage(DataType.FLOAT32);
        tensorImage.load(bitmap);

        ByteBuffer byteBuffer = tensorImage.getBuffer();

        inputFeature0.loadBuffer(byteBuffer);


        ModelEffnetb03class.Outputs outputs = model3class.process(inputFeature0);
        TensorBuffer outputFeature0 = outputs.getOutputFeature0AsTensorBuffer();

        int x = getMax(outputFeature0.getFloatArray());
        Log.d("MainActivity", "onClick: " +  outputFeature0.getFloatArray() + "asd" + x);
        model.close();
        return x;
    }

    void insertData(Result res, String id){
        databaseResult.child("results").child(id).setValue(res).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if(task.isSuccessful()){
                    Toast.makeText(ScanResultActivity.this, "Data saved in history", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    int getMax(float[] arr){
        int max=1;
        for(int i=0; i< arr.length;i++){
            if (arr[i]>arr[max]) max=i;
        }
        return max;
    }
}