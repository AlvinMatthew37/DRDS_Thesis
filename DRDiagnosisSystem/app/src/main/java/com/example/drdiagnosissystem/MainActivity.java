package com.example.drdiagnosissystem;

import android.Manifest;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    static {
        System.loadLibrary("opencv_java4");
    }

    public Bitmap Bit, argbBitmap;
    private static final String TAG = "MainActivity";
    private Button history_button, gallery_button;
    private LinearLayout scan_button, camera, gallery;
    private Uri imageUri;

    Mat mat, gauss, weighted_mat;


    @SuppressLint({"WrongViewCast", "MissingInflatedId"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        scan_button = findViewById(R.id.scan_button);
        history_button = findViewById(R.id.History);

        history_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, History.class);
                startActivity(intent);
            }
        });

        scan_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Context con = getApplicationContext();
                openDialog();

            }
        });
    }

    public void openDialog(){
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.scan_dialog);

        camera = dialog.findViewById(R.id.camera);
        gallery = dialog.findViewById(R.id.gallery);
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CAMERA)!= PackageManager.PERMISSION_GRANTED)
                {
                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CAMERA}, 101);
                }
                else
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent, 101);
                }
            }
        });

        gallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setData(MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(intent, 3);
            }
        });


        dialog.show();

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (data != null){
            if (requestCode == 3 && resultCode == RESULT_OK){
                imageUri = data.getData();

                try {
                    Bit = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
            if (requestCode == 101){
                Bit = (Bitmap) data.getExtras().get("data");
                int sourceWidth = Bit.getWidth();
                Bit= centerCropBitmap(Bit, sourceWidth, sourceWidth);
            }

            Bit = Bitmap.createScaledBitmap(Bit, 224, 224, true);
            argbBitmap = Bit.copy(Bitmap.Config.ARGB_8888, true);

            Bit = preprocess(argbBitmap);

            ByteArrayOutputStream bStream= new ByteArrayOutputStream();
            Bit.compress(Bitmap.CompressFormat.PNG, 100, bStream);
            byte[] byteArray = bStream.toByteArray();

            ByteArrayOutputStream bStreamOri = new ByteArrayOutputStream();
            argbBitmap.compress(Bitmap.CompressFormat.PNG, 100, bStreamOri);
            byte[] byteArrayOri = bStreamOri.toByteArray();

            Intent intent = new Intent(MainActivity.this, ScanResultActivity.class);
            intent.putExtra("bitmap", byteArray);
            intent.putExtra("bitmapOri", byteArrayOri);
            startActivity(intent);

        }

    }
    Bitmap preprocess(Bitmap argbBitmap){
        mat = new Mat();
        Utils.bitmapToMat(argbBitmap, mat);
        Log.d("MainActivity", "asd"+mat.type());
        Imgproc.cvtColor(mat, mat, Imgproc.COLOR_BGR2RGB);

        gauss = new Mat();
        Imgproc.GaussianBlur(mat, gauss, new Size(0,0), 10);

        weighted_mat = new Mat();
        Core.addWeighted(mat, 4, gauss, -4, 128, weighted_mat);

        Utils.matToBitmap(weighted_mat, Bit);
        return Bit;
    }
    private Bitmap centerCropBitmap(Bitmap source, int targetWidth, int targetHeight) {
        int sourceWidth = source.getWidth();
        int sourceHeight = source.getHeight();

        float xScale = (float) targetWidth / sourceWidth;
        float yScale = (float) targetHeight / sourceHeight;
        float scale = Math.max(xScale, yScale);

        float scaledWidth = scale * sourceWidth;
        float scaledHeight = scale * sourceHeight;

        float left = (targetWidth - scaledWidth) / 2;
        float top = (targetHeight - scaledHeight) / 2;
        float right = left + scaledWidth;
        float bottom = top + scaledHeight;

        Matrix matrix = new Matrix();
        matrix.setScale(scale, scale);
        matrix.postTranslate(left, top);

        Bitmap croppedBitmap = Bitmap.createBitmap(targetWidth, targetHeight, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(croppedBitmap);
        Paint paint = new Paint(Paint.FILTER_BITMAP_FLAG);
        canvas.drawBitmap(source, matrix, paint);

        return croppedBitmap;
    }
}