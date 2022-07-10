package com.mayank.harmonyplayer;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Objects;

public class ListMusicActivity extends AppCompatActivity{

    private static final int REQUEST_PERMISSION = 99;

    SongsAdapter songsAdapter;
    ArrayList<Song> songArrayList;
    ListView lvSongs;
    private int songPosition;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_music);

        Intent incomingIntent = getIntent();

        lvSongs = findViewById(R.id.lvSongs);
        songArrayList = new ArrayList<>();

        songsAdapter = new SongsAdapter(this, songArrayList);
        lvSongs.setAdapter(songsAdapter);

        if(ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_PERMISSION);
            return;
        }else{
            // you have permissions to read from external storage
            try {
                getSongs(this);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        lvSongs.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                startActivity(sendSongIntent(position));
            }
        });
        // change it later

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == REQUEST_PERMISSION){
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                try {
                    getSongs(this);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void getSongs(Context context) throws IOException {
        // read songs from phone
        //ContentResolver contentResolver = getContentResolver();
        Uri songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
        String[] projection = { MediaStore.Audio.AudioColumns.DATA, MediaStore.Audio.AudioColumns.TITLE, MediaStore.Audio.AudioColumns.ARTIST, };

        // provide songuri and projection only to read all file in the device
        // change selectionArgs as new String[]{"%yourFoldername%"} to find only in specific folders
        Cursor songCursor = context.getContentResolver().query(songUri, projection, null, null, null);
        Cursor albumCursor = null;

        if(songCursor != null && songCursor.moveToFirst()){

            int indexTitle = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE);
            int indexArtist = songCursor.getColumnIndex(MediaStore.Audio.Media.ARTIST);
            int indexData = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA);

            do{
                String title = songCursor.getString(indexTitle);
                String artist = songCursor.getString(indexArtist);
                String data = songCursor.getString(indexData);
                songArrayList.add(new Song(title, artist, data));
            }while(songCursor.moveToNext());
        }
        
        songCursor.close();

        // arrange the songs in order
        // bubble sort
        ArrayList<Song> tempList = songArrayList;
        int n = songArrayList.size();
        for(int i = 0; i< n-1; i++){
            for(int j = 0; j< n-i-1; j++){
                if(tempList.get(j).getTitle().compareTo( tempList.get(j+1).getTitle() ) > 0) {
                    Song temp = tempList.get(j);
                    tempList.set(j, tempList.get(j+1));
                    tempList.set(j+1, temp);
                }
            }
        }
        songArrayList = tempList;

        songsAdapter.notifyDataSetChanged();
    }

    public Uri getUrlForResource(int resourceId){
        return Uri.parse("android.resource://" + Objects.requireNonNull(R.class.getPackage()).getName() + "/" + resourceId);
    }

    private Intent sendSongIntent(int position){
        songPosition = position;
        Song song = songArrayList.get(position);
        Intent openMusicPlayer = new Intent(ListMusicActivity.this, MusicPlayerActivity.class);
        openMusicPlayer.putExtra("song", song);
        openMusicPlayer.putExtra("songArrayList", songArrayList);
        openMusicPlayer.putExtra("songIndex", position);
        return openMusicPlayer;
    }

}