package com.gmail.rixx.justin.cashcaddy;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.gmail.rixx.justin.cashcaddy.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.Locale;

public class ViewTransactions extends AppCompatActivity {

    private String categoryID;

    private RecyclerView mRecycler;
    private FirebaseRecyclerAdapter mAdapter;

    // auth stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String uid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_view_transactions);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        categoryID = getIntent().getStringExtra(MainActivity.EXTRA_CATEGORY_ID);
        if (categoryID == null || categoryID.equals("")) {
            finish();
        }

        mRecycler = (RecyclerView) findViewById(R.id.recyclerView);
        mAuth = FirebaseAuth.getInstance();

        setListeners();
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
     * Set listeners on all the stuff
     */
    private void setListeners() {

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
                }
            }
        };
    }

    private void setUpRecycler() {

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference();

        mRecycler.setHasFixedSize(true);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));

        mAdapter = new FirebaseRecyclerAdapter<Transaction, TransactionHolder> (Transaction.class,
                R.layout.transaction_recycler_item, TransactionHolder.class,
                ref.child("transactions/" + uid).orderByChild("category").equalTo(categoryID)) {

            @Override
            protected void populateViewHolder(TransactionHolder viewHolder, Transaction model, int position) {
                viewHolder.setDate(model.getDate());
                viewHolder.setAmount(model.getAmount());
                viewHolder.setBackground(position);
                viewHolder.setClickListener(model.getComment());
            }
        };

        mRecycler.setAdapter(mAdapter);
    }

    /**
     * A ViewHolder class for use with the category class
     */
    public static class TransactionHolder extends RecyclerView.ViewHolder {
        View mView;

        public TransactionHolder(View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setDate(String name) {
            TextView field = (TextView) mView.findViewById(R.id.transaction_date);
            field.setText(name);
        }

        public void setAmount(int amount) {
            TextView field = (TextView) mView.findViewById(R.id.transaction_amount);

            NumberFormat n = NumberFormat.getCurrencyInstance(Locale.US);
            String s = n.format(amount / 100.0);

            field.setText(s);
        }

        // alternate background colors
        public void setBackground(int index) {
            if (index % 2 == 0) {
                mView.setBackgroundColor(mView.getResources().getColor(R.color.stripeLight));
            } else {
                mView.setBackgroundColor(mView.getResources().getColor(R.color.stripe));
            }
        }

        public void setClickListener(final String description) {
            mView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Toast.makeText(v.getContext(),
                            (description.equals("") ? "No description" : description),
                            Toast.LENGTH_SHORT).show();
                }
            });
        }
    }
}