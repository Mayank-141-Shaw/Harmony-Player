package com.mayank.harmonyplayer;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;
import java.util.Random;

public class MusicPlayerActivity extends AppCompatActivity implements View.OnClickListener {

    //declaration
    TextView tvTime, tvDuration, tvTitle, tvArtist;
    SeekBar seekBarTime, seekBarVolume;
    Button btnPlay, btnBkd, btnFwd, btnShuffle, btnLoopMusic, btnFav;
    int songIndex;
    Song song = null;

    boolean shouldLoop; // initially set on  autoplay
    boolean shouldShuffle; // initially set on linear sequence mode later randomly picks an index to play a random song
    boolean isFav;

    MediaPlayer musicPlayer;
    ArrayList<Song> songArrayList;

    Random rand = new Random(); // to select random index

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        File favFile = new File(getApplicationContext().getFilesDir(), "favourites");


        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        if(song == null) {
            song = (Song) getIntent().getExtras().get("song");
            Log.i("info", "Old Song");
        }
        songArrayList = (ArrayList<Song>) getIntent().getExtras().get("songArrayList");
        songIndex = (int) getIntent().getExtras().get("songIndex");


        tvTime = findViewById(R.id.tvTime);
        tvDuration = findViewById(R.id.tvDuration);
        seekBarTime = findViewById(R.id.seekBarTime);
        seekBarVolume = findViewById(R.id.seekBarVolume);
        btnPlay = findViewById(R.id.btnPlay);
        btnBkd = findViewById(R.id.btnBkd);
        btnFwd = findViewById(R.id.btnFwd);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnLoopMusic = findViewById(R.id.btnLoopMusic);
        btnFav = findViewById(R.id.favBtn);
        tvTitle = findViewById(R.id.tvTitle);
        tvArtist = findViewById(R.id.tvArtist);

        tvTitle.setText(song.getTitle());
        tvArtist.setText(song.getArtist());

        musicPlayer = new MediaPlayer();

        try{
            song = songArrayList.get(songIndex);
            musicPlayer.setDataSource(song.getPath());
            musicPlayer.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        }

        shouldLoop = false;
        shouldShuffle = false;

        musicPlayer.setLooping(shouldLoop);
        musicPlayer.seekTo(0);
        musicPlayer.setVolume(0.5f, 0.5f);

        String duration = millisecondsToString(musicPlayer.getDuration());
        tvDuration.setText(duration);
        btnPlay.setBackgroundResource(R.drawable.ic_pause_circle);

        seekBarVolume.setProgress(50);

        // listening to the buttons
        btnPlay.setOnClickListener(this);
        btnBkd.setOnClickListener(this);
        btnFwd.setOnClickListener(this);
        btnShuffle.setOnClickListener(this);
        btnLoopMusic.setOnClickListener(this);

        // fav button
        isFav = false;
        btnFav.setOnClickListener(this);

        seekBarVolume.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                float volume = progress / 100f;
                musicPlayer.setVolume(volume, volume);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        seekBarTime.setMax(musicPlayer.getDuration());
        seekBarTime.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if(fromUser){
                    musicPlayer.seekTo(progress);
                    seekBar.setProgress(progress);
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        musicPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                // reset the entire activity window details with the new song data and values

                tvTitle.setText(song.getTitle());
                tvArtist.setText(song.getArtist());

                // reset the player position to start from the 0th millisecond
                mp.seekTo(0);
                mp.setLooping(shouldLoop);

                double current = mp.getCurrentPosition();
                String elapsedTime = millisecondsToString((int) current);
                tvTime.setText(elapsedTime);
                seekBarTime.setProgress(0);

                String duration = millisecondsToString(mp.getDuration());
                tvDuration.setText(duration);
                btnPlay.setBackgroundResource(R.drawable.ic_pause_circle);
                seekBarTime.setMax(mp.getDuration());

                // set on prepared listener to start as soon as the player is prepared
                mp.start();
                Toast.makeText(MusicPlayerActivity.this, "Now Playing :: "+song.getTitle(), Toast.LENGTH_SHORT).show();
            }
        });

        // basically used as autoplay
        musicPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                /*mp.stop();
                mp.reset();
                // increment the song index
                songIndex++;
                if( songIndex == songArrayList.size() ){
                    songIndex = 0;
                }
                try{
                    song = songArrayList.get(songIndex);
                    Log.i("info", "New Song");
                    mp.setDataSource(song.getPath());
                    mp.prepare();
                } catch (IOException e) {
                    e.printStackTrace();
                } catch (NullPointerException e){ }
                */
                // check if shuffle is on or off
                if(shouldShuffle){
                    changeOnShuffle(mp);  // shuffle is on, shuffle to a new random song
                }else{
                    onForwardCall(mp);   // shuffle is off, set on next play song
                }
            }
        });

        // changing the current status of the seekbar and continously updating as it moves forward
        // using runOnUiThread() constantly updating the seekbar and tvTime timer
        new Thread(new Runnable() {
            @Override
            public void run() {
                while(musicPlayer != null) {
                    if(musicPlayer.isPlaying()){
                        try{
                            final double current = musicPlayer.getCurrentPosition();
                            final String elapsedTime = millisecondsToString((int)current);

                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    tvTime.setText(elapsedTime);
                                    seekBarTime.setProgress((int) current);
                                }
                            });

                            Thread.sleep(1000);
                        }catch(InterruptedException e){ }
                    }

                }
            }
        }).start();
        
    }// end of main


    public String millisecondsToString(int time){
        String elapsedTime = "";
        int minutes = time / 1000 / 60;
        int seconds = time / 1000 % 60;
        elapsedTime = minutes + ":";
        if(seconds < 10){
            elapsedTime += "0";
        }
        elapsedTime += seconds;
        return elapsedTime;
    }


    @Override
    public void onClick(View v) {
        switch(v.getId()){
            case R.id.favBtn:
                if(isFav){
                    btnFav.setBackgroundResource(R.drawable.ic_outline_favorite_border_24);
                    isFav = false;
                }else{
                    isFav = true;
                    btnFav.setBackgroundResource(R.drawable.ic_baseline_favorite_fill);
                }
                break;

            case R.id.btnPlay:
                if(musicPlayer.isPlaying()){
                    //is playing
                    musicPlayer.pause();
                    btnPlay.setBackgroundResource(R.drawable.ic_baseline_play_circle);
                }else{
                    // on pause
                    musicPlayer.start();
                    btnPlay.setBackgroundResource(R.drawable.ic_pause_circle);
                }
                break;

            case R.id.btnFwd:
                if(musicPlayer != null){
                    // is playing so stop the song immediately and switch to the next song in the list
                    onForwardCall(musicPlayer);
                }
                break;

            case R.id.btnBkd:
                if(musicPlayer != null){
                    // is playing so stop the song immediately and switch the previous song in the list
                    onBackwardCall(musicPlayer);
                }
                break;

            case R.id.btnLoopMusic:
                if(shouldLoop){
                    shouldLoop = false;
                    musicPlayer.setLooping(shouldLoop);
                    Toast.makeText(MusicPlayerActivity.this, "Stopped Looping", Toast.LENGTH_SHORT).show();
                }else{
                    shouldLoop = true;
                    musicPlayer.setLooping(shouldLoop);
                    Toast.makeText(MusicPlayerActivity.this, "Started Looping", Toast.LENGTH_SHORT).show();
                }
                break;

            case R.id.btnShuffle:
                if(!shouldShuffle){
                    // if music is not shuffling we choose a random index value within the size range of the sonArrayList
                    // and we pass the index to the songIndex to be played on completing the current ongoing song
                    shouldShuffle = true;
                    Toast.makeText(MusicPlayerActivity.this, "Shuffle :: ON", Toast.LENGTH_SHORT).show();
                }else{
                    shouldShuffle = false;
                    Toast.makeText(MusicPlayerActivity.this, "Shuffle :: OFF", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    private void onBackwardCall(MediaPlayer mp) {
        mp.stop();
        mp.reset();
        // decrement the song
        songIndex--;
        if(songIndex == -1){
            songIndex = songArrayList.size()-1;
        }
        try{
            song = songArrayList.get(songIndex);
            Log.i("info", "previous Song");
            mp.setDataSource(song.getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){ }
    }


    private void onForwardCall(MediaPlayer mp) {
        mp.stop();
        mp.reset();
        // increment the song index
        songIndex++;
        if( songIndex == songArrayList.size() ){
            songIndex = 0;
        }
        try{
            song = songArrayList.get(songIndex);
            Log.i("info", "New Song");
            mp.setDataSource(song.getPath());
            mp.prepare();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (NullPointerException e){ }
    }

    private void changeOnShuffle(MediaPlayer mp){
        mp.stop();
        mp.reset();
        // pick a random index value
        songIndex = rand.nextInt(songArrayList.size());
        try{
            song = songArrayList.get(songIndex);
            Log.i("info", "Shuffled New Song");
            mp.setDataSource(song.getPath());
            mp.prepare();
        }catch(IOException e){
            e.printStackTrace();
        } catch (NullPointerException e){   }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if(item.getItemId() == android.R.id.home){
            finish();
            if(musicPlayer.isPlaying()){
                musicPlayer.stop();
            }
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onPause() {
        if(musicPlayer.isPlaying()){
            musicPlayer.pause();
        }
        super.onPause();
    }

    @Override
    protected void onPostResume() {
        if(!musicPlayer.isPlaying()){
            musicPlayer.start();
        }
        super.onPostResume();
    }

    @Override
    protected void onDestroy() {
        finish();
        if(musicPlayer.isPlaying()){
            musicPlayer.stop();
            musicPlayer.release();
        }
        super.onDestroy();
    }


}
