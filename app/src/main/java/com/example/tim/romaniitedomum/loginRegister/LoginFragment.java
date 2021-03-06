package com.example.tim.romaniitedomum.loginRegister;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;
import com.backendless.async.callback.AsyncCallback;
import com.backendless.exceptions.BackendlessFault;
import com.backendless.persistence.DataQueryBuilder;
import com.backendless.persistence.local.UserIdStorageFactory;
import com.example.tim.romaniitedomum.ApplicationClass;
import com.example.tim.romaniitedomum.MainActivity;
import com.example.tim.romaniitedomum.Util.BcAc;
import com.example.tim.romaniitedomum.Util.Util;
import com.example.tim.romaniitedomum.artefact.Artefact;
import com.example.tim.romaniitedomum.map.MapActivity;
import com.example.tim.romaniitedomum.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by TimStaats 21.02.2019
 */

public class LoginFragment extends Fragment {

    private static final String TAG = "LoginFragment";

    private MainActivity mainActivity;

    private View mProgressView;
    private View mLoginFormView;
    private TextView tvLoad;

    // source
    // https://www.youtube.com/watch?v=veOZTvAdzJ8
    // TextInputLayout is replacing EditText
    private TextInputLayout textInputLoginEmail, textInputLoginPassword;

    private Button btnLogin;
    private Button btnRegister;
    private TextView tvResetPassword;

    private String email = "";
    private String password = "";


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        Log.d(TAG, "onCreateView: called");
        initLogin(view);

        showProgress(true);
        tvLoad.setText(getResources().getText(R.string.login_check_credentials));

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: clicked");

                if (!validateEmail() | !validatePassword()) {
                    Toast.makeText(getContext(), getResources().getText(R.string.toast_empty_fields), Toast.LENGTH_SHORT).show();
                } else {
                    Log.d(TAG, "onClick: login: clicked");

                    showProgress(true);
                    tvLoad.setText(getResources().getText(R.string.login_user));
                    Backendless.UserService.login(email, password, new AsyncCallback<BackendlessUser>() {
                        @Override
                        public void handleResponse(BackendlessUser response) {
                            ApplicationClass.user = response;
                            Toast.makeText(getContext(), getResources().getText(R.string.toast_login_successful), Toast.LENGTH_SHORT).show();
                            // if user is successful logged in
                            retrieveArtefactsFromBackendless();
                            //navigateToMapActivity();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    }, true);

                }
            }
        });

        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                register();
            }
        });

        tvResetPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: Reset Password: clicked");

                if (!validateEmail()){
                    Toast.makeText(getContext(), getResources().getText(R.string.toast_forgot_password), Toast.LENGTH_SHORT).show();
                } else {
                    showProgress(true);
                    tvLoad.setText(getResources().getText(R.string.reset_sending_information));

                    Backendless.UserService.restorePassword(email, new AsyncCallback<Void>() {
                        @Override
                        public void handleResponse(Void response) {
                            Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_reset_password_instructions), Toast.LENGTH_SHORT).show();
                            showProgress(false);

                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    });
                }
            }
        });

        // User stays logged in
        Backendless.UserService.isValidLogin(new AsyncCallback<Boolean>() {
            @Override
            public void handleResponse(Boolean response) {

                if (response){
                    String userObjectId = UserIdStorageFactory.instance().getStorage().get();

                    tvLoad.setText(getResources().getText(R.string.login_user));

                    Backendless.Data.of(BackendlessUser.class).findById(userObjectId, new AsyncCallback<BackendlessUser>() {

                        @Override
                        public void handleResponse(BackendlessUser response) {
                            ApplicationClass.user = response;
                            // if user is successful logged in
                            retrieveArtefactsFromBackendless();
                        }

                        @Override
                        public void handleFault(BackendlessFault fault) {
                            Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                            showProgress(false);
                        }
                    });
                } else {
                    showProgress(false);
                }
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                showProgress(false);
            }
        });

        return view;
    }

    // login
    private void retrieveArtefactsFromBackendless() {
        tvLoad.setText(getResources().getText(R.string.retrieve_artefacts_from_backendless));
        // retrieve more than 10 artefact objects
        // source
        // https://backendless.com/docs/android/data_data_paging.html
        DataQueryBuilder queryBuilder = DataQueryBuilder.create();
        queryBuilder.setPageSize(25).setOffset(0);
        Backendless.Data.of(Artefact.class).find(queryBuilder, new AsyncCallback<List<Artefact>>() {
            @Override
            public void handleResponse(List<Artefact> response) {
                ApplicationClass.mArtefactList = response;
                navigateToMapActivity();
            }

            @Override
            public void handleFault(BackendlessFault fault) {
                Toast.makeText(getContext(), getResources().getText(R.string.toast_backendless_error) + fault.getMessage(), Toast.LENGTH_SHORT).show();
                // avoid starting with an empty artefact list
                avoidEmptyArtefactList();
                navigateToMapActivity();
            }
        });
    }

    private void avoidEmptyArtefactList() {
        List<Artefact> list = new ArrayList<>();
        Artefact artefact = new Artefact();
        artefact.setArtefactName("Civitas");
        artefact.setCategoryName(Util.CATEGORY_SPIELSTAETTE);
        artefact.setAnnoDomini(BcAc.BEFORE_CHRIST.toString());
        artefact.setArtefactImageUrl("");
        artefact.setArtefactAudioUrl("");
        artefact.setArtefactDescription("greatest app ever");
        artefact.setAuthorName("Marcus Tullio Cicero");
        artefact.setArtefactAge("63");
        artefact.setLatitude(53.50000);
        artefact.setLongitude(10.000);
        list.add(artefact);
        ApplicationClass.mArtefactList = list;
    }

    private void navigateToMapActivity(){
        Intent intent = new Intent(getActivity(), MapActivity.class);
        intent.putExtra(getResources().getString(R.string.origin), TAG);
        startActivity(intent);
        getActivity().finish();
    }

    private void register(){
        mainActivity.fragmentSwitch(new RegisterFragment(), true, "RegisterFragment");
    }

    private boolean validateEmail(){
        email = "";
        email = textInputLoginEmail.getEditText().getText().toString().trim();

        if (email.isEmpty()){
            textInputLoginEmail.setError(getResources().getString(R.string.login_empty_field_error));
            return false;
        } else {
            textInputLoginEmail.setError(null);
            //textInputLoginEmail.setErrorEnabled(false);
            return true;
        }
    }

    private boolean validatePassword(){
        password = textInputLoginPassword.getEditText().getText().toString().trim();

        if (password.isEmpty()){
            textInputLoginPassword.setError(getResources().getString(R.string.login_empty_field_error));
            return false;
        } else {
            textInputLoginPassword.setError(null);
            return true;
        }
    }

    private void initLogin(View view) {
        mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.login_progress);
        tvLoad = view.findViewById(R.id.tvLoad);

        textInputLoginEmail = view.findViewById(R.id.textinput_login_email);
        textInputLoginPassword = view.findViewById(R.id.textinput_login_password);

        btnLogin = view.findViewById(R.id.button_login_confirm);
        btnRegister = view.findViewById(R.id.button_login_register);
        tvResetPassword = view.findViewById(R.id.text_login_reset_password);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mainActivity = (MainActivity) getActivity();
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

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });

            tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoad.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            tvLoad.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

}
