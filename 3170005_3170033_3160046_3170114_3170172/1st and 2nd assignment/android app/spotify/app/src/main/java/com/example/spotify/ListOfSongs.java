package com.example.spotify;

import androidx.appcompat.app.AppCompatActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import java.io.Serializable;
import java.util.ArrayList;



public class ListOfSongs extends AppCompatActivity implements Serializable {
    ListView songlist;
    static String itemValue;
    Intent intent;
    public static final String EXTRA_MESSAGE = "com.example.spotify.MESSAGE";
    public static final int RESULT_OK = 1;
    public static  int RESULT_CANCELLED = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list_of_songs);
        intent = new Intent(ListOfSongs.this, MainActivity.class);

    }

    public void onStart() {
        super.onStart();
        MainActivity c = new MainActivity();
        ArrayList<String> songs = new ArrayList<>();
        songlist = (ListView) findViewById(R.id.listview);

        for (int i = 0; i < c.listofsongs.size(); i++) {
            songs.add(c.listofsongs.get(i));
        }
        if (c.listofsongs == null) System.out.println("NULL");

        ArrayAdapter arrayAdapter = new ArrayAdapter(ListOfSongs.this, android.R.layout.simple_list_item_1, songs);
        songlist.setAdapter(arrayAdapter);
        songlist.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                itemValue = (String) songlist.getItemAtPosition(position);
                int a = c.listofsongs.size()-1;
                while(c.listofsongs!=null && a>=0){

                    c.listofsongs.remove(a);
                    a--;

                }
                intent.putExtra(EXTRA_MESSAGE, itemValue);
                setResult(ListOfSongs.RESULT_OK, intent);
                finish();

            }

        });
    }

}
