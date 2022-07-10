package com.mayank.harmonyplayer;

import android.content.Context;
import android.content.Intent;
import android.os.CountDownTimer;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;


public class HomeLoaderScreenActivity extends AppCompatActivity {
    ProgressBar progressBarCircle;
    ArrayList<Song> favList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_loader_screen);

        progressBarCircle = findViewById(R.id.progressBarCircle);
        File favFile = new File(getApplicationContext().getFilesDir(), "favourites");

        if(favFile == null){
            // create a new Song Favourite File
            String filename = "favourites";
            favList = new ArrayList<Song>();
            try(FileOutputStream fos = getApplicationContext().openFileOutput(filename, Context.MODE_PRIVATE)){
                fos.write("".getBytes());
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        getSupportActionBar().hide();

        new CountDownTimer(3000, 1000){
            @Override
            public void onFinish() {
                startActivity(new Intent(HomeLoaderScreenActivity.this, ListMusicActivity.class));
                finish();
            }

            @Override
            public void onTick(long millisUntilFinished) {
            }
        }.start();
    }




}