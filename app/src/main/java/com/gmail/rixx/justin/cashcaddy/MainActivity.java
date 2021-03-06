package com.gmail.rixx.justin.cashcaddy;

import android.content.Intent;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.auth.AuthUI;
import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gmail.rixx.justin.cashcaddy.model.Category;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.joda.time.LocalDate;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.text.NumberFormat;
import java.util.HashMap;
import java.util.Locale;


public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // constants
    public static final String EXTRA_CATEGORY_ID = "category_id";

    // widgets
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView mNavigationView;
    private RecyclerView mRecycler;
    private FirebaseRecyclerAdapter mAdapter;
    private TextView emailTextView;

    // firebase stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private DatabaseReference mDatabase;

    private String uid;

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
        mRecycler = (RecyclerView) findViewById(R.id.recyclerView);
        emailTextView = (TextView) (mNavigationView.getHeaderView(0).findViewById(R.id.email_text_view));
        mAuth = FirebaseAuth.getInstance();

        setListeners();
    }

    private void setUpRecycler() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new FirebaseRecyclerAdapter<Category, CategoryHolder> (Category.class,
                R.layout.category_recycler_item, CategoryHolder.class, ref.child("categories/" + uid)) {

            @Override
            protected void populateViewHolder(CategoryHolder viewHolder, Category model, int position) {
                viewHolder.setName(model.getName());
                viewHolder.setBalance(model.getBalance());
                viewHolder.setBackground(position);
                viewHolder.setClickListener(model.getKey());
            }

            // so I can get the key too
            @Override
            protected Category parseSnapshot(DataSnapshot snapshot) {
                Category result = super.parseSnapshot(snapshot);
                result.setKey(snapshot.getKey());
                return result;
            }
        };

        mRecycler.setAdapter(mAdapter);
    }

    /**
     * Set listeners on all the stuff
     */
    private void setListeners() {

        fab.setOnClickListener(this);
        mNavigationView.setNavigationItemSelectedListener(this);

        // listen for auth state changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user can't be here if not logged in
                    finish();
                } else {
                    uid = user.getUid();
                    setUpRecycler();
                    emailTextView.setText(user.getEmail());
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    updateBalances();
                }
            }
        };
    }

    private void updateBalances() {
        mDatabase.child(C.PATH_CATEGORIES).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (final DataSnapshot s : dataSnapshot.getChildren()) {
                    Category c = s.getValue(Category.class);

                    boolean shouldUpdate = false;

                    DateTimeFormatter dtf = DateTimeFormat.forPattern("yyyy-MM-dd");

                    LocalDate today = new LocalDate();
                    LocalDate date = dtf.parseLocalDate(c.getLastRefresh());
                    LocalDate lastRefresh = new LocalDate(c.getLastRefresh());

                    switch (c.getRefreshCode()) {
                        case C.REFRESH_CODE_TWO_WEEKS: {

                            date = date.plusWeeks(2);
                            while (today.isAfter(date)) {
                                lastRefresh = lastRefresh.plusWeeks(2);
                                date = date.plusWeeks(2);

                                shouldUpdate = true;
                            }
                            break;
                        }
                        case C.REFRESH_CODE_YEARLY: {

                            date = date.plusYears(1);
                            while (today.isAfter(date)) {
                                lastRefresh = lastRefresh.plusYears(1);
                                date = date.plusYears(1);

                                shouldUpdate = true;
                            }
                            break;
                        }
                        default: {

                            date = date.plusMonths(1);
                            while (today.isAfter(date)) {
                                lastRefresh = lastRefresh.plusMonths(1);
                                date = date.plusMonths(1);

                                shouldUpdate = true;
                            }
                        }
                    }

                    if (shouldUpdate) {
                        HashMap<String, Object> map = new HashMap<>();
                        map.put("lastRefresh", dtf.print(lastRefresh));
                        mDatabase.child(C.PATH_CATEGORIES).child(uid).child(s.getKey())
                                .updateChildren(map).addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                new UpdateBalance(s.getKey(), uid, null, mDatabase);
                            }
                        });
                    }
                }
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    /**
     * Start the add transaction activity
     */
    private void addTransaction() {
        startActivity(new Intent(this, EditTransaction.class));
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

        if (id == R.id.nav_home) {
            return true;
        } else if (id == R.id.nav_manage) {
            // go to the edit categories activity
            startActivity(new Intent(this, EditCategories.class));
        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance(FirebaseApp.getInstance())
                    .signOut(MainActivity.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(MainActivity.this, StartActivity.class));
                            finish();
                        }
                    });
        }

        if (drawer == null) {
            drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        }
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onClick(View v) {

        if (v.getId() == R.id.fab) {
            addTransaction();
        }
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

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (mAdapter != null) {
            mAdapter.cleanup();
        }
    }

    /**
     * A ViewHolder class for use with the category class
     */
    public static class CategoryHolder extends RecyclerView.ViewHolder {
        View mView;

        public CategoryHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setName(String name) {
            TextView field = (TextView) mView.findViewById(R.id.category_name);
            field.setText(name);
        }

        public void setBalance(int balance) {
            TextView field = (TextView) mView.findViewById(R.id.category_balance);

            NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
            String s = n.format(balance / 100.0);

            field.setText(s);

            if (balance < 0) {
                field.setTextColor(mView.getContext().getResources().getColor(R.color.red));
            } else {
                field.setTextColor(mView.getContext().getResources().getColor(android.R.color.holo_green_dark));
            }
        }

        // alternate background colors
        public void setBackground(int index) {
            if (index % 2 == 0) {
                mView.setBackgroundColor(mView.getResources().getColor(R.color.stripeLight));
            } else {
                mView.setBackgroundColor(mView.getResources().getColor(R.color.stripe));
            }
        }

        public void setClickListener(final String category) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent i = new Intent(v.getContext(), ViewTransactions.class);
                    i.putExtra(EXTRA_CATEGORY_ID, category);

                    v.getContext().startActivity(i);
                }
            });
        }
    }
}
