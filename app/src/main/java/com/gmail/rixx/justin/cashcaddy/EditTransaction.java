package com.gmail.rixx.justin.cashcaddy;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.rixx.justin.cashcaddy.model.Category;
import com.gmail.rixx.justin.cashcaddy.model.Transaction;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class EditTransaction extends AppCompatActivity {

    public static final String EXTRA_TRANSACTION = "extra_tranasction";

    private Spinner mSpinner;
    private EditText amountEditText;
    private TextView dateTextView;
    private EditText comments;
    private Button saveBtn;
    private Button deleteBtn;

    private boolean edit = false;
    private Transaction mTransaction;
    Map<String, String> spinnerMap = new HashMap<>();

    // auth stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String uid;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_transaction);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mSpinner       = (Spinner) findViewById(R.id.spinner);
        amountEditText = (EditText) findViewById(R.id.amount_edittext);
        dateTextView   = (TextView) findViewById(R.id.date_textview);
        comments       = (EditText) findViewById(R.id.comments_edittext);
        saveBtn        = (Button) findViewById(R.id.btn_save);
        deleteBtn      = (Button) findViewById(R.id.btn_delete);

        mAuth = FirebaseAuth.getInstance();

        if (getIntent().hasExtra(EXTRA_TRANSACTION)) {
            edit = true;
            showDelete();
            mTransaction = getIntent().getParcelableExtra(EXTRA_TRANSACTION);
        }

        setAuthListener();
    }

    private void setAuthListener() {
        // listen for auth state changes
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null) {
                    // user can't be here if not logged in
                    finish();
                } else {
                    mDatabase = FirebaseDatabase.getInstance().getReference();
                    uid = user.getUid();
                    setup();
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

    private void showDelete() {
        deleteBtn.setVisibility(View.VISIBLE);
    }

    private void setup() {

        mDatabase.child(C.PATH_CATEGORIES).child(uid).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                List<String> spinnerArray = new ArrayList<>();
                for (DataSnapshot snap : dataSnapshot.getChildren()) {
                    Category c = snap.getValue(Category.class);
                    spinnerMap.put(c.getName(), snap.getKey());
                    spinnerArray.add(c.getName());
                }

                ArrayAdapter<String> adapter = new ArrayAdapter<>(EditTransaction.this,
                        android.R.layout.simple_spinner_item, spinnerArray);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

                mSpinner.setAdapter(adapter);

                setClickListeners();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                EditTransaction.this.finish();
            }
        });
    }

    private void setClickListeners() {

        saveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String categoryName = mSpinner.getSelectedItem().toString();
                String categoryId = spinnerMap.get(categoryName);
                String amountString = amountEditText.getText().toString();
                String dateString = dateTextView.getText().toString();
                String comment = comments.getText().toString();

                TextInputLayout amountLayout = (TextInputLayout) findViewById(R.id.input_layout_dollar);

                if (amountString.equals("")) {
                    amountLayout.setErrorEnabled(true);
                    amountLayout.setError("Please enter the allocated amount");

                    return;
                } else {
                    amountLayout.setErrorEnabled(false);
                }

                double d = Double.parseDouble(amountString) * 100;
                int amount = (int) d;

                String key;

                Transaction t = new Transaction(amount, categoryId, comment, dateString);
                Map<String, Object> map = t.toMap();

                if (edit) {
                    mDatabase
                            .child(C.PATH_TRANSACTIONS)
                            .child(uid)
                            .child(mTransaction.getCategory())
                            .child(mTransaction.getKey())
                            .removeValue();
                }

                key = mDatabase.child(C.PATH_TRANSACTIONS).child(uid).child(categoryId).push().getKey();

                Map<String, Object> updates = new HashMap<>();
                updates.put("/" + key, map);
                mDatabase.child(C.PATH_TRANSACTIONS).child(uid).child(categoryId).updateChildren(updates);

                // TODO update balances

                finish();
            }
        });

        if (edit) {
            deleteBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mTransaction.getKey() != null && !mTransaction.getKey().equals("")) {
                        mDatabase
                                .child(C.PATH_TRANSACTIONS)
                                .child(uid)
                                .child(mTransaction.getCategory())
                                .child(mTransaction.getKey())
                                .removeValue();

                        finish();
                    } else {
                        Toast.makeText(EditTransaction.this, "Error deleting transaction", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        // set the textview to today
        if (!edit) {
            String formattedDate = df.format(c.getTime());
            dateTextView.setText(formattedDate);
        } else {
            // or whenever the transaction object says to
            dateTextView.setText(mTransaction.getDate());

            // set the comment
            comments.setText(mTransaction.getComment());

            // set the amount
            amountEditText.setText(String.format(java.util.Locale.US,"%.2f", mTransaction.getAmount() / 100.0));

        }

        // set an onclick listener
        dateTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment picker = new DatePickerFragment();
                picker.show(getSupportFragmentManager(), "Date Picker");
            }
        });
    }
}
