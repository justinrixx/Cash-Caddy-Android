package com.gmail.rixx.justin.cashcaddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
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
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.Locale;

public class EditCategories extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, View.OnClickListener {

    // widgets
    private FloatingActionButton fab;
    private DrawerLayout drawer;
    private NavigationView mNavigationView;
    private RecyclerView mRecycler;
    private FirebaseRecyclerAdapter mAdapter;
    private TextView emailTextView;

    // auth stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;

    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_categories);
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
                viewHolder.setAmount(model.getAmount());
                viewHolder.setBackground(position);
                viewHolder.setClickListener(model);
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
                }
            }
        };
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

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.edit_categories, menu);
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
            startActivity(new Intent(this, MainActivity.class));
            finish();
        } else if (id == R.id.nav_manage) {
            return true;
        } else if (id == R.id.nav_logout) {
            AuthUI.getInstance(FirebaseApp.getInstance())
                    .signOut(EditCategories.this)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        public void onComplete(@NonNull Task<Void> task) {
                            // user is now signed out
                            startActivity(new Intent(EditCategories.this, StartActivity.class));
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
        switch (v.getId()) {
            case R.id.fab: {
                addCategory();
            }
        }
    }

    private void addCategory() {
        startActivity(new Intent(this, EditCategory.class));
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

        public void setAmount(int balance) {
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

        public void setClickListener(final Category c) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final AppCompatDialog dialog = new AppCompatDialog(mView.getContext());

                    dialog.setContentView(R.layout.dialog_category);
                    dialog.setTitle(c.getName());
                    TextView amount = (TextView) dialog.findViewById(R.id.amount_text_view);
                    TextView refresh = (TextView) dialog.findViewById(R.id.refresh_text_view);
                    Button edit = (Button) dialog.findViewById(R.id.edit_btn);

                    NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
                    String s = n.format(c.getAmount() / 100.0);

                    String refreshCode;

                    switch (c.getRefreshCode()) {
                        case C.REFRESH_CODE_TWO_WEEKS: {
                            refreshCode = "every two weeks";
                            break;
                        }
                        case C.REFRESH_CODE_YEARLY: {
                            refreshCode = "yearly";
                            break;
                        }
                        default: {
                            refreshCode = "monthly";
                        }
                    }

                    if (amount != null && refresh != null) {
                        amount.setText("Amount: " + s);
                        refresh.setText("Refreshes " + refreshCode);
                    }

                    if (edit != null) {
                        edit.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                Intent i = new Intent(v.getContext(), EditCategory.class);
                                i.putExtra(EditCategory.EXTRA_CATEGORY, c);

                                v.getContext().startActivity(i);

                                dialog.dismiss();
                            }
                        });
                    }

                    dialog.show();
                }
            });
        }
    }
}
