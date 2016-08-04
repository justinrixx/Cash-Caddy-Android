package com.gmail.rixx.justin.cashcaddy;

import android.os.AsyncTask;
import android.util.Log;

import com.gmail.rixx.justin.cashcaddy.model.Category;
import com.gmail.rixx.justin.cashcaddy.model.Transaction;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

/**
 * Updates the balance of a category
 */
public class UpdateBalance extends AsyncTask<Void, Void, Void> {

    private DatabaseReference mDatabase;
    private UpdateBalanceDelegate mDelegate;
    private String category;
    private String uid;

    public UpdateBalance(String category, String uid, UpdateBalanceDelegate mDelegate, DatabaseReference mDatabase) {
        this.mDatabase = mDatabase;
        this.mDelegate = mDelegate;
        this.category = category;
        this.uid = uid;
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mDelegate != null) {
            mDelegate.onUpdateFinished();
        }
    }

    @Override
    protected Void doInBackground(Void... params) {

        // get the category
        mDatabase.child(C.PATH_CATEGORIES).child(uid).child(category).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                final Category c = dataSnapshot.getValue(Category.class);

                // get all the transactions after the last refresh
                mDatabase.
                        child(C.PATH_TRANSACTIONS)
                        .child(uid)
                        .child(category)
                        .orderByChild("date")
                        .startAt(c.getLastRefresh())
                        .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {

                        // add up the total transaction amounts since last refresh
                        int delta = 0;

                        for (DataSnapshot snap : dataSnapshot.getChildren()) {
                            Transaction t = snap.getValue(Transaction.class);

                            delta += t.getAmount();
                        }

                        HashMap<String, Object> updates = new HashMap<>();
                        Map<String, Object> map = c.toMap();
                        map.put("balance", c.getAmount() - delta);
                        updates.put(dataSnapshot.getKey(), map);

                        // update the balance
                        mDatabase
                                .child(C.PATH_CATEGORIES)
                                .child(uid)
                                .updateChildren(updates);
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        Log.e("UpdateBalance", databaseError.getMessage());
                    }
                });
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                Log.e("UpdateBalance", databaseError.getMessage());
            }
        });

        return null;
    }
}
