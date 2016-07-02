package com.gmail.rixx.justin.cashcaddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // constants
    private static final int RC_SIGN_IN = 100;

    // widgets
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView mNavigationView;
    private Button signInButton;
    private Button signUpButton;
    private RecyclerView mRecycler;

    // auth stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        fab = (FloatingActionButton) findViewById(R.id.fab);

        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.nav_view);
        signInButton = (Button) findViewById(R.id.btn_signIn);
        signUpButton = (Button) findViewById(R.id.btn_signUp);
        mRecycler = (RecyclerView) findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();

        setListeners();
    }

    /**
     * Set listeners on all the stuff
     */
    private void setListeners() {

        // click listeners for the buttons
        signInButton.setOnClickListener(this);
        signUpButton.setOnClickListener(this);
        fab.setOnClickListener(this);

        // listen for auth state changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    userLoggedIn(true);
                } else {
                    userLoggedIn(false);
                }
            }
        };
    }

    /**
     * Toggle all the stuff that should be visible when the auth state changes
     * @param loggedIn Whether or not the user is logged in
     */
    private void userLoggedIn(boolean loggedIn) {

        if (loggedIn) {
            // hide the sign up and sign in buttons
            signInButton.setVisibility(View.GONE);
            signUpButton.setVisibility(View.GONE);

            // put listeners on the fab and navigation view
            mNavigationView.setNavigationItemSelectedListener(this);

            // show the fab and the recycler
            mRecycler.setVisibility(View.VISIBLE);
            fab.setVisibility(View.VISIBLE);

            // set up the recycler
        } else {
            // hide the recycler and fab
            fab.setVisibility(View.GONE);
            mRecycler.setVisibility(View.GONE);

            // remove the listener
            mNavigationView.setNavigationItemSelectedListener(null);

            // show the sign in and sign up buttons
            signInButton.setVisibility(View.VISIBLE);
            signUpButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start the add transaction activity
     */
    private void addTransaction() {
        Snackbar.make(fab, "No functionality for this yet", Snackbar.LENGTH_SHORT).show();
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

    @Override
    public void onBackPressed() {
        if (drawer == null) {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        }
        if (drawer != null && drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_camera) {
            // Handle the camera action
        } else if (id == R.id.nav_gallery) {

        } else if (id == R.id.nav_slideshow) {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        if (drawer == null) {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {

        switch (v.getId()) {
            case R.id.fab:
                addTransaction();
                break;
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
        Toast.makeText(this, "Sign up", Toast.LENGTH_SHORT).show();
    }

    /**
     * Kicks off FirebaseUI to sign the user in
     */
    private void signIn() {

        startActivityForResult(
                AuthUI.getInstance().createSignInIntentBuilder().build(),
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
}
