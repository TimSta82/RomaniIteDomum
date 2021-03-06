package com.example.tim.romaniitedomum.artefact;


import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.files.BackendlessFile;
import com.example.tim.romaniitedomum.ApplicationClass;
import com.example.tim.romaniitedomum.R;
import com.example.tim.romaniitedomum.Util.ArtefactImageBitmap;
import com.example.tim.romaniitedomum.Util.BcAc;
import com.example.tim.romaniitedomum.Util.UserScreen;
import com.example.tim.romaniitedomum.Util.Util;
import com.example.tim.romaniitedomum.map.MapActivity;
import com.google.android.gms.maps.model.LatLng;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.ImageLoadingListener;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;


/**
 * Created by TimStaats 27.04.2019
 */

public class EditArtefactFragment extends Fragment {

    private static final String TAG = "EditArtefactFragment";

    public static final int REQUEST_PERMISSION_CODE_AUDIO = 1000;
    public static final int REQUEST_PERMISSION_CODE_CAMERA = 2000;
    public static final int REQUEST_PERMISSION_CODE_GALLERY = 3000;
    public static final int PICK_IMAGE = 1;
    public static final String ORIGIN_CAMERA = "camera";
    public static final String ORIGIN_GALLERY = "gallery";
    public static final String BACKENDLESS_IMAGE_FILE_PATH = "artefactImages";
    public static final String BACKENDLESS_AUDIO_FILE_PATH = "/artefactAudios";
    public static final int BITMAP_QUALITY = 100;

    private ArtefactActivity artefactActivity;

    private View mProgressViewEditArtefact;
    private View mFormViewEditArtefact;
    private TextView tvLoadEditArtefact;

    private ImageButton btnGallery, btnCamera;
    private ImageButton btnAudioPlay, btnAudioStop, btnAudioRecord, btnAudioDelete;
    private ImageView ivArtefact;
    private EditText etArtefactName, etArtefactDescription, etArtefactAge;
    private Button btnAudio, btnEditArtefact, btnAnnoDomini;
    private Spinner spinnerCategory;
    private CategoryAdapter mCategoryAdapter;
    private ArrayList<Category> mCategoryList;
    private Category mCategory;
    private int categoryPosition;
    private String clickedCategory;
    private Bitmap artefactBitmap;
    private String editedImageFileName = "";
    private String editedAudioFileName = "";
    private String imageFilePath = "";
    private String audioFilePath = "";
    private BcAc annoDomini;

    private Artefact mEditArtefact;
    private Artefact mOriginalArtefact;
    private LinearLayout layoutAudio;

    private Bundle mArgs;
    private ImageLoader mLoader;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_edit_artefact, container, false);
        Log.d(TAG, "onCreateView: is called");

        initEditArtefact(view);

        for (int i = 0; i < mCategoryList.size(); i++) {
            if (mCategoryList.get(i).getCategoryName().equals(mEditArtefact.getCategoryName())) {
                categoryPosition = i;
            }
        }
        spinnerCategory.setSelection(categoryPosition);
        spinnerCategory.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                mCategory = (Category) parent.getItemAtPosition(position);
                clickedCategory = mCategory.getCategoryName();
                Log.d(TAG, "onItemSelected: category: " + clickedCategory);
                //Toast.makeText(getContext(), "category: " + clickedCategory, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        btnAnnoDomini.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (annoDomini == BcAc.BEFORE_CHRIST) {
                    annoDomini = BcAc.AFTER_CHRIST;
                    btnAnnoDomini.setText("A.C.");
                } else {
                    annoDomini = BcAc.BEFORE_CHRIST;
                    btnAnnoDomini.setText("B.C.");
                }
                Log.d(TAG, "onClick: anno domini: " + annoDomini);
            }
        });

        btnCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: btnCamera");
                takePhotoWithCamera();
            }
        });

        btnGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d(TAG, "onClick: btnGallery");
                pickImageFromGallery();
            }
        });

        btnEditArtefact.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int age = Integer.parseInt(etArtefactAge.getText().toString());
                if (age > 9999) {
                    etArtefactAge.setText(mOriginalArtefact.getArtefactAge());
                    Toast.makeText(artefactActivity, "Unexpected value", Toast.LENGTH_SHORT).show();
                } else {
                    mEditArtefact.setArtefactName(etArtefactName.getText().toString());
                    mEditArtefact.setArtefactDescription(etArtefactDescription.getText().toString());
                    mEditArtefact.setArtefactAge(String.valueOf(age));
                    mEditArtefact.setCategoryName(mCategory.getCategoryName());
                    mEditArtefact.setCategoryMarkerImage(mCategory.getCategoryMarkerImage());
                    mEditArtefact.setAnnoDomini(annoDomini.toString());

                    Date date = new Date();
                    Timestamp timestamp = new Timestamp(date.getTime());
                    editedImageFileName = "artefactImage_" + mEditArtefact.getArtefactName() + "_" + timestamp + ".png";
                    editedAudioFileName = "artefactAudio_" + mEditArtefact.getArtefactName() + "_" + timestamp + ".3gp";
                    mEditArtefact.setArtefactImageFileName(editedImageFileName);
                    mEditArtefact.setArtefactAudioFileName(editedAudioFileName);
                    // replace ApplicationClass.mArtefact
                    // delete old imageFile
                    // save new imageFile
                    // delete old audioFile
                    // save new audioFile
                    // update artefact backendless
                    ApplicationClass.mArtefactList.remove(mOriginalArtefact);
                    if (artefactActivity.isImageChanged) {
                        deleteImageFileFromBackendless(imageFilePath);
                    } else {
                        updateImageFileNameInBackendless();
                    }
                }
            }
        });

        return view;
    }

    private void pickImageFromGallery() {
        artefactActivity.isEditMode = true;
        artefactActivity.isGallery = true;
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE);
    }

    private void takePhotoWithCamera() {
        artefactActivity.isEditMode = true;
        artefactActivity.isCamera = true;
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        startActivityForResult(intent, REQUEST_PERMISSION_CODE_CAMERA);
    }

    private void deleteImageFileFromBackendless(String imageFilePath) {
        tvLoadEditArtefact.setText(getResources().getString(R.string.artefact_detail_delete_image));
        showProgress(true);
        Log.d(TAG, "deleteImageFileFromBackendless: is called");
        Backendless.Files.remove(imageFilePath, new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                Log.d(TAG, "handleResponse: Image successfully deleted!");

                if (mEditArtefact.getArtefactAudioUrl() != null) {
                    // TODO: audio
                    //deleteAudioFileFromBackendless(audioFilePath);
                } else {
                    uploadImageToBackendless(artefactBitmap, editedImageFileName);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(artefactActivity, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e(TAG, "handleFault: Error: " + fault.getMessage());
            }
        });
    }

    private void deleteAudioFileFromBackendless(String audioFilePath) {
        Log.d(TAG, "deleteAudioFileFromBackendless: is called");
        tvLoadEditArtefact.setText(getResources().getString(R.string.artefact_detail_delete_audio));
        showProgress(true);
        Backendless.Files.remove(audioFilePath, new AsyncCallback<Integer>() {
            @Override
            public void handleResponse(Integer response) {
                updateArtefactInBackendless();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(artefactActivity, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateImageFileNameInBackendless() {
        showProgress(true);
        tvLoadEditArtefact.setText("update filename");
        Backendless.Files.renameFile(imageFilePath, mEditArtefact.getArtefactImageFileName(), new AsyncCallback<String>() {
            @Override
            public void handleResponse(String response) {
                showProgress(false);
                mEditArtefact.setArtefactImageUrl(response);
                updateArtefactInBackendless();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                showProgress(false);
                Toast.makeText(artefactActivity, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateArtefactInBackendless() {

        showProgress(true);
        tvLoadEditArtefact.setText("Updating artefact... please wait...");
        Backendless.Persistence.save(mEditArtefact, new AsyncCallback<Artefact>() {
            @Override
            public void handleResponse(Artefact response) {

                artefactActivity.isEditMode = false;
                artefactActivity.isImageChanged = false;
                ApplicationClass.mArtefact = response;
                ApplicationClass.mArtefactList.add(response);
                Intent intent = new Intent(artefactActivity, MapActivity.class);
                intent.putExtra(Util.ORIGIN, Util.EDIT_ARTEFACT_FRAGMENT);
                startActivity(intent);
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(artefactActivity, "Error: " + fault.getMessage(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });

    }

    private void uploadImageToBackendless(Bitmap artefactBitmap, String fileName) {
        showProgress(true);
        tvLoadEditArtefact.setText(getResources().getText(R.string.new_artefact_save_image));

        Backendless.Files.Android.upload(artefactBitmap, Bitmap.CompressFormat.PNG, BITMAP_QUALITY,
                fileName, BACKENDLESS_IMAGE_FILE_PATH, new AsyncCallback<BackendlessFile>() {
                    @Override
                    public void handleResponse(BackendlessFile response) {
                        mEditArtefact.setArtefactImageUrl(response.getFileURL());
                        Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_image_upload), Toast.LENGTH_SHORT).show();
                        updateArtefactInBackendless();
                        //showProgress(false);

//                        if (!audioExists) {
//                            saveGeoPointToBackendless(mLat, mLng);
//                        } else {
//                            uploadAudioToBackendless(mAudioFile);
//                        }
                    }

                    @Override
                    public void handleFault(BackendlessFault fault) {
                        mEditArtefact.setArtefactAudioUrl("");
                        showProgress(false);
                        Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private ArrayList<Category> populateCategoryList() {
        ArrayList<Category> list = new ArrayList<>();

        list.add(new Category(Util.CATEGORY_BASILIKA, R.drawable.ic_map_basilica));
        list.add(new Category(Util.CATEGORY_BOGEN, R.drawable.ic_map_bogen));
        list.add(new Category(Util.CATEGORY_CHRISTENTUM, R.drawable.ic_map_christentum));
        list.add(new Category(Util.CATEGORY_GRABSTAETTE, R.drawable.ic_map_grabstaette));
        list.add(new Category(Util.CATEGORY_GRUENDUNGSMYTHOS, R.drawable.ic_map_grundungsmythos));
        list.add(new Category(Util.CATEGORY_INFRASTRUKTUR, R.drawable.ic_map_infrastruktur));
        list.add(new Category(Util.CATEGORY_KULTSTAETTE, R.drawable.ic_map_kultstaette));
        list.add(new Category(Util.CATEGORY_PLATZANLAGE, R.drawable.ic_map_platzanlage));
        list.add(new Category(Util.CATEGORY_POLITISCHE_INSTITUTION, R.drawable.ic_map_politische_institution));
        list.add(new Category(Util.CATEGORY_SPIELSTAETTE, R.drawable.ic_map_spielstaette));
        list.add(new Category(Util.CATEGORY_THERME, R.drawable.ic_map_therme));
        list.add(new Category(Util.CATEGORY_WOHNKOMPLEX, R.drawable.ic_map_wohnkomplex));

        return list;
    }

    private void provideOriginalFilePath() {
        // old file paths for accessing Backendless.Files
        audioFilePath = Util.BACKENDLESS_AUDIO_FILE_PATH + mOriginalArtefact.getArtefactAudioFileName();
        imageFilePath = Util.BACKENDLESS_IMAGE_FILE_PATH + mOriginalArtefact.getArtefactImageFileName();
    }

    private void initEditArtefact(View view) {
        tvLoadEditArtefact = view.findViewById(R.id.tvLoad_edit_artefact);
        mProgressViewEditArtefact = view.findViewById(R.id.progress_edit_artefact);
        mFormViewEditArtefact = view.findViewById(R.id.form_edit_artefact);
        btnGallery = view.findViewById(R.id.button_edit_artefact_image_from_gallery);
        btnCamera = view.findViewById(R.id.button_edit_artefact_image_from_camera);
        layoutAudio = view.findViewById(R.id.layout_edit_audio_player);
        btnAudio = view.findViewById(R.id.button_edit_artefact_audio);
        btnAudioPlay = layoutAudio.findViewById(R.id.button_edit_artefact_audio_play);
        btnAudioStop = layoutAudio.findViewById(R.id.button_edit_artefact_audio_stop);
        btnAudioRecord = layoutAudio.findViewById(R.id.button_edit_artefact_audio_record);
        btnAudioDelete = layoutAudio.findViewById(R.id.button_edit_artefact_audio_delete);
        ivArtefact = view.findViewById(R.id.image_edit_artefact);
        spinnerCategory = view.findViewById(R.id.spinner_edit_artefact_category);
        etArtefactName = view.findViewById(R.id.edit_edit_artefact_name);
        etArtefactDescription = view.findViewById(R.id.edit_edit_artefact_description);
        etArtefactAge = view.findViewById(R.id.edit_edit_artefact_age);
        btnEditArtefact = view.findViewById(R.id.button_edit_artefact_save);
        btnAnnoDomini = view.findViewById(R.id.button_edit_artefact_anno_domini);
        btnAnnoDomini.setText("B.C.");
        annoDomini = BcAc.BEFORE_CHRIST;

        provideOriginalFilePath();
        mCategoryList = populateCategoryList();

        mCategoryAdapter = new CategoryAdapter(artefactActivity, mCategoryList);
        spinnerCategory.setAdapter(mCategoryAdapter);

        etArtefactName.setText(mOriginalArtefact.getArtefactName());
        etArtefactDescription.setText(mOriginalArtefact.getArtefactDescription());
        etArtefactAge.setText(mOriginalArtefact.getArtefactAge());

        if (mArgs != null) {
            String origin = mArgs.getString(getResources().getString(R.string.origin));
            ArtefactImageBitmap artefactImageBitmap = ArtefactImageBitmap.getInstance();

            switch (origin) {
                case ORIGIN_CAMERA:
                    Log.d(TAG, "initEditArtefact: origin: camera");
                    artefactBitmap = BitmapFactory.decodeByteArray(artefactImageBitmap.getByteArray(), 0, artefactImageBitmap.getByteArray().length);
                    ivArtefact.setImageBitmap(artefactBitmap);
                    break;
                case ORIGIN_GALLERY:
                    Log.d(TAG, "initEditArtefact: origin: gallery");
                    artefactBitmap = BitmapFactory.decodeByteArray(artefactImageBitmap.getByteArray(), 0, artefactImageBitmap.getByteArray().length);
                    ivArtefact.setImageBitmap(artefactBitmap);
                    break;
                default:
                    Log.d(TAG, "initEditArtefact: default");
                    mLoader.displayImage(mOriginalArtefact.getArtefactImageUrl(), ivArtefact, new ImageLoadingListener() {
                        @Override
                        public void onLoadingStarted(String imageUri, View view) {
                            showProgress(true);
                            tvLoadEditArtefact.setText("Loading artefact... please wait...");
                        }

                        @Override
                        public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                            showProgress(false);
                        }

                        @Override
                        public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                            showProgress(false);
                        }

                        @Override
                        public void onLoadingCancelled(String imageUri, View view) {
                            showProgress(false);
                        }
                    });
                    break;
            }
        }

    }


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        artefactActivity = (ArtefactActivity) getActivity();
        artefactActivity.setTitle("Edit Artefact");
        artefactActivity.currentScreen = UserScreen.EDIT_ARTEFACT;
        mArgs = getArguments();
        mOriginalArtefact = ApplicationClass.mArtefact;
        mEditArtefact = mOriginalArtefact;
        ApplicationClass.mArtefactLatLng = new LatLng(ApplicationClass.mArtefact.getLatitude(), ApplicationClass.mArtefact.getLongitude());
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mFormViewEditArtefact.setVisibility(show ? View.GONE : View.VISIBLE);
            mFormViewEditArtefact.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mFormViewEditArtefact.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressViewEditArtefact.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressViewEditArtefact.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressViewEditArtefact.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            tvLoadEditArtefact.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoadEditArtefact.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvLoadEditArtefact.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressViewEditArtefact.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoadEditArtefact.setVisibility(show ? View.VISIBLE : View.GONE);
            mFormViewEditArtefact.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }
}
