package com.gmail.rixx.justin.cashcaddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class StartActivity extends AppCompatActivity implements View.OnClickListener {

    // constants
    private static final int RC_SIGN_IN = 100;

    // widgets
    private Button signInButton;
    private Button signUpButton;

    // auth stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        signInButton = (Button) findViewById(R.id.btn_signIn);
        signUpButton = (Button) findViewById(R.id.btn_signUp);

        mAuth = FirebaseAuth.getInstance();

        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // go to home if the user is logged in already
                    startActivity(new Intent(StartActivity.this, MainActivity.class));
                    finish();
                }
            }
        };
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.btn_signIn:
                signIn();
                break;
            case R.id.btn_signUp:
                signUp();
                break;
        }
    }

    /**
     * Starts an activity to get the user signed up
     */
    private void signUp() {
        startActivity(new Intent(this, SignUpActivity.class));
    }

    /**
     * Kicks off FirebaseUI to sign the user in
     */
    private void signIn() {

        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder()
                        .setTheme(R.style.AppTheme)
                        .build(),
                RC_SIGN_IN);
    }

    @Override
    public void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            if (resultCode == RESULT_OK) {
                // user is signed in
                Toast.makeText(this, "Signed in!", Toast.LENGTH_LONG).show();
            }
        }
    }
}
