package com.gmail.rixx.justin.cashcaddy;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.TextView;
import android.widget.Toast;

import com.gmail.rixx.justin.cashcaddy.model.Category;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class EditCategory extends AppCompatActivity {

    public static final String EXTRA_CATEGORY = "extra_category";

    private EditText nameEdittext;
    private EditText amountEdittext;
    private TextView dateTextview;
    private Button deleteButton;
    private RadioButton monthRadio;
    private RadioButton twoWeekRadio;
    private RadioButton yearRadio;

    private boolean edit = false;
    private Category mCategory;

    // auth stuff
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private String uid;

    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_category);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        nameEdittext   = (EditText) findViewById(R.id.name_edittext);
        amountEdittext = (EditText) findViewById(R.id.amount_edittext);
        dateTextview   = (TextView) findViewById(R.id.date_textview);
        deleteButton   = (Button) findViewById(R.id.btn_delete);
        monthRadio     = (RadioButton) findViewById(R.id.month_radiobutton);
        twoWeekRadio   = (RadioButton) findViewById(R.id.two_week_radiobutton);
        yearRadio      = (RadioButton) findViewById(R.id.year_radiobutton);

        mAuth = FirebaseAuth.getInstance();

        if (getIntent().hasExtra(EXTRA_CATEGORY)) {
            edit = true;
            showDelete();
            mCategory = getIntent().getParcelableExtra(EXTRA_CATEGORY);
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
                    setClickListeners();
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
        deleteButton.setVisibility(View.VISIBLE);
    }

    private void setClickListeners() {

        // save
        Button saveBtn = (Button) findViewById(R.id.btn_save);
        if (saveBtn != null) {
            saveBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    String name = nameEdittext.getText().toString();
                    String amountString = amountEdittext.getText().toString();
                    String dateString = dateTextview.getText().toString();

                    // not sure what the choice was
                    String refreshCode;

                    // check the radio buttons
                    if (monthRadio.isChecked()) {
                        refreshCode = C.REFRESH_CODE_MONTHLY;
                    } else if (twoWeekRadio.isChecked()) {
                        refreshCode = C.REFRESH_CODE_TWO_WEEKS;
                    } else {
                        refreshCode = C.REFRESH_CODE_YEARLY;
                    }

                    TextInputLayout nameLayout = (TextInputLayout) findViewById(R.id.input_layout_name);
                    TextInputLayout amountLayout = (TextInputLayout) findViewById(R.id.input_layout_dollar);

                    // do some error checking
                    if (name.equals("")) {
                        nameLayout.setErrorEnabled(true);
                        nameLayout.setError("Please enter a name for the category");

                        return;
                    } else {
                        nameLayout.setErrorEnabled(false);
                    }
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

                    Category c = new Category(amount, amount, dateString, name, refreshCode);
                    Map<String, Object> map = c.toMap();

                    // overwriting an existing category or creating a new one?
                    if (edit) {
                        key = mCategory.getKey();
                        map.remove("balance");
                    } else {
                        key = mDatabase.child(C.PATH_CATEGORIES).child(uid).push().getKey();
                    }

                    Map<String, Object> updates = new HashMap<>();
                    updates.put("/" + key, map);
                    mDatabase.child(C.PATH_CATEGORIES).child(uid).updateChildren(updates);

                    finish();
                }
            });
        }

        // delete
        if (edit) {
            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mCategory.getKey() != null && !mCategory.getKey().equals("")) {
                        // the category
                        mDatabase.child(C.PATH_TRANSACTIONS).child(uid).child(mCategory.getKey()).removeValue();

                        // the transactions
                        mDatabase.child(C.PATH_CATEGORIES).child(uid).child(mCategory.getKey()).removeValue();

                        finish();
                    } else {
                        Toast.makeText(EditCategory.this, "Error deleting category", Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }

        Calendar c = Calendar.getInstance();
        SimpleDateFormat df = new SimpleDateFormat("MM-dd-yyyy", Locale.US);

        // set the textview to today
        if (!edit) {
            String formattedDate = df.format(c.getTime());
            dateTextview.setText(formattedDate);
        } else {
            // or whenever the category object says to
            dateTextview.setText(mCategory.getLastRefresh());

            // set the category name
            nameEdittext.setText(mCategory.getName());

            // set the amount
            amountEdittext.setText(String.format(java.util.Locale.US,"%.2f", mCategory.getAmount() / 100.0));

            // set the radio button
            switch (mCategory.getRefreshCode()) {
                case C.REFRESH_CODE_TWO_WEEKS: {
                    twoWeekRadio.setChecked(true);
                    break;
                }
                case C.REFRESH_CODE_YEARLY: {
                    yearRadio.setChecked(true);
                    break;
                }
                default: {
                    monthRadio.setChecked(true);
                    break;
                }
            }
        }

        // set an onclick listener
        dateTextview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DialogFragment picker = new DatePickerFragment();
                picker.show(getSupportFragmentManager(), "Date Picker");
            }
        });
    }
}
