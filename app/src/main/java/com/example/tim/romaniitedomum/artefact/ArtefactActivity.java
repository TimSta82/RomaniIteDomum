package com.example.tim.romaniitedomum.artefact;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.example.tim.romaniitedomum.ApplicationClass;
import com.example.tim.romaniitedomum.MainActivity;
import com.example.tim.romaniitedomum.R;
import com.example.tim.romaniitedomum.Util.ArtefactImageBitmap;
import com.example.tim.romaniitedomum.Util.UserScreen;
import com.example.tim.romaniitedomum.Util.Util;
import com.example.tim.romaniitedomum.impressum.ImpressumActivity;
import com.example.tim.romaniitedomum.map.MapActivity;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * Created by TimStaats 03.03.2019
 */

public class ArtefactActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "ArtefactActivity";
    public static final int PICK_IMAGE = 1;

    public boolean isCamera = false;
    public boolean isGallery = false;
    public boolean isAtListFragment = false;
    public boolean isAtDetailFragment = false;
    public boolean isEditMode = false;
    public boolean isImageChanged = false;
    private String mOrigin = "";

    public UserScreen currentScreen = null;
    private DrawerLayout drawer;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_artefact);

        initDrawerAndToolbar();

        Intent intent = getIntent();
        String content = intent.getStringExtra(getResources().getString(R.string.navigate_to_artefact_activity));
        Bundle args = new Bundle();
        if (content.equals("list")) {
            ArtefactListFragment artefactListFragment = new ArtefactListFragment();
            fragmentSwitcher2(artefactListFragment, true, "ArtefactListFragment");
        } else if (content.equals(Util.MARKER_CLICK)) {
            ArtefactDetailFragment artefactDetailFragment = new ArtefactDetailFragment();
            args.putString(Util.ARTEFACT_OBJECT_ID, intent.getStringExtra(Util.ARTEFACT_OBJECT_ID));
            artefactDetailFragment.setArguments(args);
            fragmentSwitcher2(artefactDetailFragment, false, "ArtefactDetailFragment");
        } else {
            NewArtefactFragment newArtefactFragment = new NewArtefactFragment();
            String content2 = intent.getStringExtra(getResources().getString(R.string.origin));
            if (content2.equals(Util.BUTTON_ADD_ARTEFACT)) { // creating Artefact at device location
                args.putString(getResources().getString(R.string.origin), Util.BUTTON_ADD_ARTEFACT);
                args.putDouble("latitude", ApplicationClass.mDeviceLocation.getLatitude());
                args.putDouble("longitude", ApplicationClass.mDeviceLocation.getLongitude());
            } else if (content2.equals(Util.ON_MAP_LONG_CLICK)) { // creating Artefact at marker location
                args.putString(getResources().getString(R.string.origin), Util.ON_MAP_LONG_CLICK);
                args.putDouble("latitude", ApplicationClass.mArtefactLatLng.latitude);
                args.putDouble("longitude", ApplicationClass.mArtefactLatLng.longitude);
            }
            newArtefactFragment.setArguments(args);
            fragmentSwitcher2(newArtefactFragment, false, "");
        }
    }

    // for navigation_drawer needed
    @Override
    public void onBackPressed() {
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
            Intent intent;
            switch (currentScreen) {
                case ARTEFACT_DETAIL:
                    if (isAtListFragment) {
                        // list -> detail
                        // isAtListFragment = false;
                        isAtDetailFragment = false;
                        fragmentSwitcher2(new ArtefactListFragment(), false, ""); // get back to artefactList
                    } else {
                        // map -> detail
                        intent = new Intent(ArtefactActivity.this, MapActivity.class);
                        intent.putExtra(getResources().getString(R.string.origin),
                                Util.ARTEFACT_DETAIL_FRAGMENT); // move camera to artefactLocation
                        startActivity(intent);
                    }
                    break;
                case ARTEFACT_LIST:
                    if (isAtListFragment) {
                        // map -> list
                        isAtListFragment = false;
                        intent = new Intent(ArtefactActivity.this, MapActivity.class);
                        intent.putExtra(getResources().getString(R.string.origin),
                                Util.ARTEFACT_LIST_FRAGMENT); // moving camera to last screenProjection location
                        startActivity(intent);
                    }
                    break;
                case NEW_ARTEFACT:
                    // mapLongClick -> newArtefact
                    // floatingButtonClick -> new Artefact
                    intent = new Intent(ArtefactActivity.this, MapActivity.class);
                    intent.putExtra(getResources().getString(R.string.origin), Util.ARTEFACT_LIST_FRAGMENT); // ListFragment for moving camera to deviceLocation
                    startActivity(intent);
                    break;
                case EDIT_ARTEFACT:
                    isEditMode = false;
                    isImageChanged = false;
                    if (!isAtListFragment) {
                        // map -> artefactDetail -> edit
                        isAtDetailFragment = false;
                        fragmentSwitcher2(new ArtefactDetailFragment(), false, "");
                    } else {
                        // list -> artefactDetail -> edit
                        isAtListFragment = false;
                        isAtDetailFragment = false;
                        fragmentSwitcher2(new ArtefactListFragment(), false, ""); // get back to artefactList
                    }
            }
        }
    }

    private void initDrawerAndToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawer = findViewById(R.id.drawer_layout_artefact_activity);
        NavigationView navigationView = findViewById(R.id.nav_view);

        View header = navigationView.getHeaderView(0);
        TextView tvNavName = header.findViewById(R.id.text_nav_user_name);
        String name = ApplicationClass.user.getProperty(getResources().getString(R.string.backendless_property_name)).toString();
        tvNavName.setText(name);
        TextView tvNavEmail = header.findViewById(R.id.text_nav_user_email);
        String email = ApplicationClass.user.getEmail();
        tvNavEmail.setText(email);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.setCheckedItem(R.id.nav_artefacts);

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar,
                R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();
    }

    public void fragmentSwitcher2(Fragment fragment, boolean toBackstack, String name) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_artefact_fragment, fragment);
        if (toBackstack) {
            transaction.addToBackStack(name);
        } else {

        }
        transaction.commit();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        ArtefactImageBitmap artefactImageBitmap = ArtefactImageBitmap.getInstance();
        Fragment fragment;
        if (isEditMode) {
            isImageChanged = true;
            fragment = new EditArtefactFragment();
        } else {
            fragment = new NewArtefactFragment();
        }
        Bundle args = new Bundle();

        // source
        // https://stackoverflow.com/questions/40056938/androidhandling-backpress-on-camera-intent
        if (resultCode == Activity.RESULT_OK) {
            if (isGallery == true) {
                Uri uri = data.getData();
                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();
                    artefactImageBitmap.setByteArray(byteArray);
                    mOrigin = "gallery";
                    args.putString(getResources().getString(R.string.origin), mOrigin);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                isGallery = false;
            } else if (isCamera == true) {
                Bitmap bitmap = (Bitmap) data.getExtras().get("data");
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream);
                byte[] byteArray = stream.toByteArray();
                artefactImageBitmap.setByteArray(byteArray);
                mOrigin = "camera";
                args.putString(getResources().getString(R.string.origin), mOrigin);
                isCamera = false;
            }
        } else {
            isCamera = false;
            isGallery = false;
            mOrigin = "";
            args.putString(getResources().getString(R.string.origin), mOrigin);
        }
        fragment.setArguments(args);
        fragmentSwitcher2(fragment, false, "");
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
        Intent intent;
        switch (menuItem.getItemId()) {
            case R.id.nav_artefacts:
                Log.d(TAG, "onNavigationItemSelected: artefacts");
                break;
            case R.id.nav_map:
                Log.d(TAG, "onNavigationItemSelected: map clicked");
                intent = new Intent(ArtefactActivity.this, MapActivity.class);
                switch (currentScreen) {                    
                    case EDIT_ARTEFACT:
                        Log.d(TAG, "onNavigationItemSelected: edit artefact");
                        intent.putExtra(getResources().getString(R.string.navigate_to_artefact_activity), "list");
                        intent.putExtra(getResources().getString(R.string.origin), Util.EDIT_ARTEFACT_FRAGMENT);
                        break;
                    case ARTEFACT_LIST:
                        Log.d(TAG, "onNavigationItemSelected: list");
                        intent.putExtra(getResources().getString(R.string.navigate_to_artefact_activity), "list");
                        intent.putExtra(getResources().getString(R.string.origin), Util.ARTEFACT_LIST_FRAGMENT);
                        break;
                    case ARTEFACT_DETAIL:
                        Log.d(TAG, "onNavigationItemSelected: artefact detail");
                        intent.putExtra(getResources().getString(R.string.navigate_to_artefact_activity), "list");
                        intent.putExtra(getResources().getString(R.string.origin), Util.ARTEFACT_DETAIL_FRAGMENT);
                        break;
                    default:
                        Log.d(TAG, "onNavigationItemSelected: default");
                        break;
                }
                
                startActivity(intent);
                break;
            case R.id.nav_impressum:
                Log.d(TAG, "onNavigationItemSelected: impressum");
                intent = new Intent(ArtefactActivity.this, ImpressumActivity.class);
                intent.putExtra(getResources().getString(R.string.origin), Util.ARTEFACT_ACTIVITY);
                //intent.putExtra("impressum", "impressum");
                startActivity(intent);
                break;
            case R.id.nav_settings:
                Log.d(TAG, "onNavigationItemSelected: settings");
                Toast.makeText(this, "Settings clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_logout:
                Backendless.UserService.logout(new AsyncCallback<Void>() {
                    @Override
                    public void handleResponse(Void response) {
                        Log.d(TAG, "handleResponse: logout");
                        Toast.makeText(ArtefactActivity.this, getResources().getText(R.string.user_logout), Toast.LENGTH_SHORT).show();
                        Intent intent = new Intent(ArtefactActivity.this, MainActivity.class);
                        startActivity(intent);
                        ArtefactActivity.this.finish();
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        Toast.makeText(ArtefactActivity.this, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

                break;
/*
            case R.id.nav_send:
                Log.d(TAG, "onNavigationItemSelected: send");
                Toast.makeText(this, "Send clicked", Toast.LENGTH_SHORT).show();
                break;
            case R.id.nav_share:
                Log.d(TAG, "onNavigationItemSelected: share");
                Toast.makeText(this, "Share clicked", Toast.LENGTH_SHORT).show();
                break;
*/
        }

        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void hideSoftKeyboard() {
        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
    }
}
