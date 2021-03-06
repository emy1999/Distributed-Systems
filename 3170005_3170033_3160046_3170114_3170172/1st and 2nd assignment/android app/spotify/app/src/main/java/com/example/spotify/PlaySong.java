package com.example.spotify;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.TextView;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import android.media.MediaPlayer.OnCompletionListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

public class PlaySong extends AppCompatActivity implements Serializable {

    ObjectInputStream in;
    ObjectOutputStream out;
    File fis;
    static File temp;
    static ArrayList<Value> pieces;
    int chunks = 0;
    static String str ;
    SeekBar seekBar;
    boolean wasPlaying = false;
    FloatingActionButton fab;
    Button back;
    MediaPlayer player = new MediaPlayer();
    static int num_clicks =0;
    static int stop_pos = 0;
    static int offline=0;
    static ArrayList<String> downloads = new ArrayList<>();
    static  ArrayList<File> downloadedSongs = new ArrayList<>();
    boolean pressed = false;

    MainActivity m = new MainActivity();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_song);
        back = (Button)findViewById(R.id.button2);
        fab = (FloatingActionButton)findViewById(R.id.button);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AsyncTaskRunner1 runner = new AsyncTaskRunner1();
                runner.execute();
                num_clicks++;
                stop_pos = player.getCurrentPosition();
                player.pause();
                fab.setImageDrawable(ContextCompat.getDrawable(PlaySong.this, android.R.drawable.ic_media_play));
                if(num_clicks%2!=0){
                    player.seekTo(stop_pos);
                    player.start();
                    seekBar.setMax(player.getDuration());
                    seekBar.setProgress(stop_pos);
                    fab.setImageDrawable(ContextCompat.getDrawable(PlaySong.this, android.R.drawable.ic_media_pause));
                }

            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                pressed = true;
                for(int i=0; i<m.listofsongs.size();i++){
                    m.listofsongs.remove(i);
                }

                System.out.println("Back pressed releasing the player");

                if(player!=null && m.choice==false) {
                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.stop();
                    player.reset();
                    player.release();
                    player = null;
                }
                if(player!=null && m.choice==true) {

                    try {
                        Thread.sleep(2500);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    player.reset();
                    player.release();
                    player = null;
                    if(temp!=null){
                        temp.delete();
                    }
                }

                try {
                    m.requestSocket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                back.setVisibility(View.GONE);
                m.choice=false;
                Intent intent = new Intent(PlaySong.this, MainActivity.class);
                startActivity(intent);

            }
        });



        final TextView seekBarHint = findViewById(R.id.textView1);
        seekBar = (SeekBar)findViewById(R.id.seekbar);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

                seekBarHint.setVisibility(View.VISIBLE);
            }

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromTouch) {
                seekBarHint.setVisibility(View.VISIBLE);
                int x = (int) Math.ceil(progress / 1000f);

                if (x ==0 && player != null && !player.isPlaying()) {
                    clearMediaPlayer();
                    fab.setImageDrawable(ContextCompat.getDrawable(PlaySong.this, android.R.drawable.ic_media_play));
                    PlaySong.this.seekBar.setProgress(0);
                }

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {


                if (player != null && player.isPlaying()) {
                    player.seekTo(seekBar.getProgress());
                }
            }
        });


    }

    protected void onDestroy() {
        super.onDestroy();
        clearMediaPlayer();
    }

    private void clearMediaPlayer() {
        player.stop();
        player.reset();
        player = null;
    }



    public class AsyncTaskRunner1 extends AsyncTask<String,String,String> implements Serializable {
        static final long serialVersionUID = -373782829391231342L;
        MainActivity k = new MainActivity();
        boolean on_off =  k.choice;


        @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
        @SuppressLint("WrongThread")
        protected String doInBackground(String... params) {

            publishProgress("Sleeping...");

            try {

                Socket request = k.requestSocket;
                out = k.out;
                in = k.in;
                out.writeUTF(k.message);
                out.flush();
                pieces = new ArrayList<>();

                if(on_off==false ) {
                    System.out.println("Offline");
                    try {
                        str = in.readUTF();
                        chunks = in.readInt();

                        if (str == null) str = "songReceived";
                        fis = new File(getFilesDir() + "/" + str.concat(".mp3"));
                        FileOutputStream fileOutputStream = null;

                        fileOutputStream = new FileOutputStream(fis);

                        for (int i = 1; i <= chunks; i++) {

                            Value value = new Value((MusicFile) in.readObject());
                            System.err.println(i);
                            System.out.println("Song broker send me:  " + value.getMusicfile().getTrackName());

                            fileOutputStream.write(value.getMusicfile().getMusicFileExtract());
                            fileOutputStream.flush();

                            pieces.add(value);
                            out.writeUTF("ok");
                            out.flush();

                        }
                        fileOutputStream.close();
                        downloads.add(str);
                        downloadedSongs.add(fis);

                        try {
                            if (player != null && player.isPlaying()) {
                                clearMediaPlayer();
                                seekBar.setProgress(0);
                                wasPlaying = true;
                            }
                            if (!wasPlaying) {

                                if (player == null) {
                                    player = new MediaPlayer();
                                }

                                fab.setImageDrawable(ContextCompat.getDrawable(PlaySong.this, android.R.drawable.ic_media_pause));
                                player.setDataSource(fis.getPath());
                                player.prepare();
                                player.setVolume(0.5f, 0.5f);
                                player.setLooping(false);
                                seekBar.setMax(player.getDuration());
                                if (pressed == false) {
                                    player.start();
                                    int currentPosition = player.getCurrentPosition();
                                    int total = player.getDuration();

                                    while (player != null && player.isPlaying() && currentPosition < total && !back.isPressed()) {
                                        try {

                                            currentPosition = player.getCurrentPosition();
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                        seekBar.setProgress(currentPosition);

                                    }

                                }
                                player.setOnCompletionListener(new OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        System.err.println("Releasing the player");
                                        mp.reset();
                                        offline++;


                                    }
                                });
                            }

                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }
                else{
                    try {
                        str = in.readUTF();
                        chunks = in.readInt();
                        System.out.println("number of chunks:   " + chunks);
                        if (str == null) str = "songReceived";

                        FileOutputStream fileOutputStream = null;
                        Value value = new Value((MusicFile) in.readObject());

                        System.out.println("Song broker send me:  " + value.getMusicfile().getTrackName());
                        temp = new File(getFilesDir() + "/" + str.concat(".mp3"));
                        fileOutputStream = new FileOutputStream(temp);
                        fileOutputStream.write(value.getMusicfile().getMusicFileExtract());
                        fileOutputStream.flush();

                        if (player != null && player.isPlaying()) {
                            clearMediaPlayer();
                            seekBar.setProgress(0);
                            wasPlaying = true;
                            fab.setImageDrawable(ContextCompat.getDrawable(PlaySong.this, android.R.drawable.ic_media_play));
                        }

                        if (!wasPlaying) {

                            if (player == null) {
                                player = new MediaPlayer();
                            }
                            fab.setImageDrawable(ContextCompat.getDrawable(PlaySong.this, android.R.drawable.ic_media_pause));
                            player.setDataSource(temp.getPath());
                            player.prepare();
                            player.setVolume(0.5f, 0.5f);
                            player.setLooping(false);
                            seekBar.setMax(player.getDuration());
                            player.start();
                            int j = 2;
                            out.writeUTF("ok");
                            out.flush();
                            int current = 0;
                            while (player.isPlaying()) {
                                if (j <= chunks) {
                                    for (int i = 2; i <= chunks; i++) {
                                        try {
                                            Value value1 = new Value((MusicFile) in.readObject());
                                            System.err.println(i);
                                            System.out.println("Song broker send me:  " + value.getMusicfile().getTrackName());
                                            fileOutputStream.write(value1.getMusicfile().getMusicFileExtract());
                                            fileOutputStream.flush();
                                            j++;
                                            out.writeUTF("ok");
                                            out.flush();
                                            current = player.getCurrentPosition();
                                            seekBar.setProgress(current);
                                            if(pressed==true){
                                                player.stop();

                                            }

                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }

                                    }

                                }
                                stop_pos = player.getCurrentPosition();
                                seekBar.setProgress(stop_pos);
                            }
                            if (pressed==false){
                                player.reset();
                                player.setDataSource(temp.getPath());
                                player.prepare();
                                player.setVolume(0.5f, 0.5f);
                                player.setLooping(false);
                                seekBar.setMax(player.getDuration());
                                player.seekTo(stop_pos);
                                player.start();
                                int currentPosition = player.getCurrentPosition();
                                int total = player.getDuration();
                                while (player != null && player.isPlaying() && currentPosition < total) {
                                    try {

                                        currentPosition = player.getCurrentPosition();
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }

                                    seekBar.setProgress(currentPosition);
                                }
                                player.setOnCompletionListener(new OnCompletionListener() {
                                    @Override
                                    public void onCompletion(MediaPlayer mp) {
                                        System.err.println("Releasing the player");

                                        if (mp != null) {
                                            mp.stop();
                                            mp.reset();
                                            mp.release();
                                            mp = null;
                                        }

                                        if (temp != null) {
                                            temp.delete();
                                        }


                                    }
                                });

                            }

                        }

                    } catch (IOException | ClassNotFoundException e) {
                        e.printStackTrace();
                    }

                }

            } catch (IOException  e) {
                e.printStackTrace();
            }

            return null;
        }

    }

}





