package com.example.tim.romaniitedomum.map;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;

import com.example.tim.romaniitedomum.R;

public class MapActivity extends AppCompatActivity {

    private static final String TAG = "MapActivity";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.map_container, new MapFragment());
        transaction.commit();
    }
}