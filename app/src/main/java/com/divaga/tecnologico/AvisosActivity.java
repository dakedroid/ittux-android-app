package com.divaga.tecnologico;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.divaga.tecnologico.adapter.AvisosAdapter;
import com.divaga.tecnologico.fragments.PublishAvisoDialogFragment;
import com.divaga.tecnologico.model.Aviso;
import com.divaga.tecnologico.model.User;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AvisosActivity extends AppCompatActivity implements AvisosAdapter.OnAvisosSelectedListener, PublishAvisoDialogFragment.AvisoListener, View.OnClickListener {


    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private static String userPhotoUrl;
    private static String userName;
    private static String userPerssions;

    public ProgressDialog mProgressDialog;

    private CardView publishDialogLayout;

    private PublishAvisoDialogFragment mPublishDialog;

    private AvisosAdapter mAdapter;

    private static final int LIMIT = 50;


    @BindView(R.id.recycler_avisos)
    RecyclerView mAvisosRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avisos);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        publishDialogLayout = findViewById(R.id.publish_dialog_aviso);

        mPublishDialog = new PublishAvisoDialogFragment();

        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        mAvisosRecycler.setNestedScrollingEnabled(false);

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("aviso")
                .orderBy("datepublic", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new AvisosAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mAvisosRecycler.setVisibility(View.GONE);
                    // mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mAvisosRecycler.setVisibility(View.VISIBLE);
                    //mEmptyView.setVisibility(View.GONE);
                }
            }

            @Override
            protected void onError(FirebaseFirestoreException e) {
                // Show a snackbar on errors
                Snackbar.make(findViewById(android.R.id.content),
                        "Error: check logs for info.", Snackbar.LENGTH_LONG).show();
            }
        };

        mAvisosRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAvisosRecycler.setAdapter(mAdapter);

        mAvisosRecycler.setNestedScrollingEnabled(false);


        getUser();


    }


    @Override
    protected void onStart() {
        super.onStart();

        // Start listening for Firestore updates
        if (mAdapter != null) {
            mAdapter.startListening();
        }


    }

    @Override
    protected void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
    }

    @Override
    public void OnAvisosSelected(DocumentSnapshot avisos) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_RESTAURANT_ID, avisos.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

    }

    public void writeOnServer() {


    }


    @Override
    public void onSubirAviso(Aviso aviso) {

        showProgressDialog();


        WriteBatch batch = mFirestore.batch();

        DocumentReference restRef = mFirestore.collection("aviso").document();

        aviso.setUsername(userName);
        aviso.setUser_photo(userPhotoUrl);

        // Add restaurant
        batch.set(restRef, aviso);


        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {


                    hideProgressDialog();


                    Log.d("AvisosActivity", "Write batch succeeded.");
                } else {
                    Log.w("AvisosActivity", "write batch failed.", task.getException());
                }
            }
        });
    }

    private void getUser() {


        String Uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        final ArrayList<User> mArrayList = new ArrayList<>();

        Log.i("PRUEBITA", Uid);

        Query mQuery = mFirestore.collection("usuarios")
                .whereEqualTo("userId", Uid)
                .limit(1);


        mQuery.get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot documentSnapshot) {
                if (documentSnapshot.isEmpty()) {
                    Log.d("PRUEBITA", "onSuccess: LIST EMPTY");
                    return;
                } else {
                    // Convert the whole Query Snapshot to a list
                    // of objects directly! No need to fetch each
                    // document.

                    List<User> types = documentSnapshot.toObjects(User.class);

                    mArrayList.addAll(types);

                    userPhotoUrl = mArrayList.get(0).getPhoto();

                    userName = mArrayList.get(0).getUserName();

                    userPerssions = mArrayList.get(0).getPermisos();


                    if (userPerssions.contains("1")) {

                        publishDialogLayout.setVisibility(View.VISIBLE);

                    } else {

                        publishDialogLayout.setVisibility(View.GONE);

                    }

                }
            }

        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.activity_aviso_publish) {
            mPublishDialog.show(getSupportFragmentManager(), PublishAvisoDialogFragment.TAG);
        }
    }

    public void showProgressDialog() {
        if (mProgressDialog == null) {
            mProgressDialog = new ProgressDialog(this);
            mProgressDialog.setMessage(getString(R.string.loading));
            mProgressDialog.setIndeterminate(true);
        }

        mProgressDialog.show();
        mProgressDialog.setCanceledOnTouchOutside(false);
    }

    public void hideProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.dismiss();
        }
    }
}
