package com.example.spotify;

import androidx.appcompat.app.AppCompatActivity;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;
import android.view.View;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.net.Socket;
import java.util.ArrayList;
import java.lang.*;


public class MainActivity extends AppCompatActivity implements Serializable {
    private Button button;
    private EditText txt_input;
    private TextView lb1_output;
    static int portOfBroker;
    static ArrayList<String> listofsongs = new ArrayList<String>();
    public static final int LAUNCH_ACTIVITY = 1;
    static String message;
    static Socket requestSocket = null;
    static ObjectOutputStream out = null;
    static ObjectInputStream in = null;
    static final long serialVersionUID = -373782829391231342L;
    Switch simpleSwitch;
    static boolean choice;
    Button sub;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        txt_input = (EditText) findViewById(R.id.txt_input);
        lb1_output = (TextView) findViewById(R.id.textView1);
        simpleSwitch = (Switch) findViewById(R.id.simpleSwitch);// initiate Switch
        button = (Button)findViewById(R.id.button4);
        simpleSwitch.setTextSize(25);
        sub = (Button)findViewById(R.id.button5);

    }

    @SuppressLint("LongLogTag")
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == LAUNCH_ACTIVITY) {
            if(resultCode == ListOfSongs.RESULT_OK){

                message = "" + intent.getStringExtra(ListOfSongs.EXTRA_MESSAGE);
                Intent intent2 = new Intent(MainActivity.this, PlaySong.class);
                startActivity(intent2);


            }
            if (resultCode == ListOfSongs.RESULT_CANCELED) {
                //Write your code if there's no result
            }
        }
    }//onActivityResult


    public void onStart() {
        super.onStart();
        simpleSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (isChecked) {
                    choice = true;
                    System.out.println("Online");
                } else {
                    choice = false;
                    System.out.println("Offline");
                }
            }
        });

        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                txt_input = (EditText) findViewById(R.id.txt_input);
                AsyncTaskRunner runner = new AsyncTaskRunner();
                runner.execute();

            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent2 = new Intent(MainActivity.this, Downloads.class);
                startActivity(intent2);
            }
        });
    }

    public class AsyncTaskRunner extends AsyncTask<String,String,String> implements Serializable {
        static final long serialVersionUID = -373782829391231342L;

        int exist;
        ArtistName artist = new ArtistName(txt_input.getText().toString());
        @SuppressLint("WrongThread")
        protected String doInBackground(String... params) {
            System.out.println("artist:" + artist.getArtistName());
            try {

                requestSocket = new Socket("192.168.1.3", 7655);
                out = new ObjectOutputStream(requestSocket.getOutputStream());
                in = new ObjectInputStream(requestSocket.getInputStream());
                portOfBroker = 7655;
                out.writeUTF("192.168.1.3");
                out.writeInt(7655);
                out.writeUTF(artist.getArtistName());
                out.flush();
                int brokerport = in.readInt();

                if (brokerport != 7655) {
                    portOfBroker = brokerport;
                    requestSocket.close();
                    Socket request = new Socket("192.168.1.3", brokerport+1);
                    out = new ObjectOutputStream(request.getOutputStream());
                    in = new ObjectInputStream(request.getInputStream());
                    out.writeUTF("1192.168.1.3");
                    out.writeInt(brokerport);
                    out.writeUTF(artist.getArtistName()); //successfully sends artistName to BrokerNode
                    out.flush();
                    System.out.println("Connect with other broker");
                }
                int port = in.readInt();
                exist = in.readInt();
                System.out.println("exist: " + exist);
                if(exist!=1){
                    lb1_output.setVisibility(View.VISIBLE);
                    lb1_output.setText("Please insert another artist");
                    artist.setArtistName(txt_input.toString());
                    sub.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            if(txt_input.getText().toString().trim().equalsIgnoreCase("")){
                                txt_input.setError("Type an artist");
                            }
                            txt_input = (EditText) findViewById(R.id.txt_input);

                            AsyncTaskRunner runner = new AsyncTaskRunner();
                            runner.execute();
                        }
                    });

                }
                if (exist == 1) {
                    try {
                        //lb1_output.d
                        lb1_output.setText("");

                        int size = in.readInt();

                        for(int i=0; i<size;i++) {
                            listofsongs.add(in.readUTF());
                            System.out.println(listofsongs.get(i));
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    Intent intent= new Intent(MainActivity.this, ListOfSongs.class);
                    startActivityForResult(intent, LAUNCH_ACTIVITY);

                } else {
                    System.out.println("The artist you searched doesn't exist.");
                    System.out.println("Please try again.");
                }

            }
            catch (IOException e){
                Log.e("MessageSender", "" + e);

            }
            return null;
        }

    }

}







