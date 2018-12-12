package com.example.lenovo.emotion;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.microsoft.projectoxford.emotion.EmotionServiceClient;
import com.microsoft.projectoxford.emotion.EmotionServiceRestClient;
import com.microsoft.projectoxford.emotion.contract.FaceRectangle;
import com.microsoft.projectoxford.emotion.contract.RecognizeResult;
import com.microsoft.projectoxford.emotion.contract.Scores;
import com.microsoft.projectoxford.emotion.rest.EmotionServiceException;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    ImageView imageView;
    Button btnTakepic,btnProcess;
    int TAKE_PICTURE_CODE=100;
    Bitmap mBitmap;
    EmotionServiceClient restClient = new EmotionServiceRestClient("enter ur subscription key");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initViews();


    }

    private void initViews() {
        btnProcess = (Button) findViewById(R.id.btnProcess);
        btnTakepic = (Button) findViewById(R.id.btnTakepic);
        imageView = (ImageView) findViewById(R.id.imageview);

        btnTakepic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takepicfromgallery();

            }
        });

        btnProcess.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                processimage();
            }
         });
    }

    private void processimage() {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        mBitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);
        ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        AsyncTask<InputStream, String, List<RecognizeResult>> processAsync = new AsyncTask<InputStream, String, List<RecognizeResult>>() {

            ProgressDialog mDialog = new ProgressDialog(MainActivity.this);

            @Override
            protected void onPreExecute() {
                mDialog.show();

            }

            @Override
            protected void onProgressUpdate(String... values) {
                mDialog.setMessage(values[0]);
            }


            @Override
            protected List<RecognizeResult> doInBackground(InputStream... paramas) {
                publishProgress("Please wait...");
                List<RecognizeResult> result = null;
                try {
                    result = restClient.recognizeImage(paramas[0]);
                } catch (EmotionServiceException e) {
                    e.printStackTrace();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return result;
            }

            @Override
            protected void onPostExecute(List<RecognizeResult> recognizeResults) {
                mDialog.dismiss();
                for (RecognizeResult res : recognizeResults) {
                    String status = getEmotion(res);
                    imageView.setImageBitmap(ImageHelper.drawRectOnBitmap(mBitmap, res.faceRectangle, status));
                }
            }
        };
        processAsync.execute(inputStream) ;
    }

    private String getEmotion(RecognizeResult res) {
        List<Double> list =new ArrayList<>();
        Scores scores=res.scores;
        list.add(scores.anger);
        list.add(scores.happiness);
        list.add(scores.contempt);

        list.add(scores.disgust);
        list.add(scores.fear);
        list.add(scores.neutral);
        list.add(scores.sadness);
        list.add(scores.surprise);


        Collections.sort(list);
        double maxNum= list.get(list.size() -1);


        if(maxNum==scores.anger)
            return  "anger";
        else if(maxNum==scores.happiness)
            return "happy";

        else if(maxNum==scores.contempt)
            return "contempt";

        else if(maxNum==scores.disgust)
            return "disgust";

        else if(maxNum==scores.fear)
            return "fear";

        else if(maxNum==scores.neutral)
            return "neutal";

        else if(maxNum==scores.sadness)
            return "sadness";

        else if(maxNum==scores.surprise)
            return "surprise";
        else
            return "can't detect anything";

    }

            @Override
             protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == TAKE_PICTURE_CODE) {
            Uri selectedImageUri=data.getData();
            InputStream in =null;
            try {
                in=getContentResolver().openInputStream(selectedImageUri);
            } catch (FileNotFoundException e) {
                e.printStackTrace();

            }
            mBitmap=BitmapFactory.decodeStream(in);
            imageView.setImageBitmap(mBitmap);
        }
    }


    private void takepicfromgallery()
    {
        Intent intent =new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent,TAKE_PICTURE_CODE);
    }

}