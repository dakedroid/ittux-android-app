package com.divaga.tecnologico.permissions;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.divaga.tecnologico.R;
import com.divaga.tecnologico.adapter.AsignarPermisosAdapter;
import com.divaga.tecnologico.fragments.AsignarPermisosDialogFragment;
import com.divaga.tecnologico.fragments.PublishAvisoDialogFragment;
import com.divaga.tecnologico.model.User;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AsignarPermisosActivity extends AppCompatActivity implements AsignarPermisosAdapter.OnAsignarPermisosSelectedListener, AsignarPermisosDialogFragment.AsignarPermisosListener {

    private static final int LIMIT = 50;
    public ProgressDialog mProgressDialog;
    DocumentSnapshot user;
    @BindView(R.id.recycler_asignar_permisos)
    RecyclerView mAsignarPermisosRecycler;
    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private AsignarPermisosDialogFragment mDialog;
    private AsignarPermisosAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_asignar_permisos);

        ButterKnife.bind(this);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDialog = new AsignarPermisosDialogFragment();

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        mAsignarPermisosRecycler.setNestedScrollingEnabled(false);

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("usuarios")
                .orderBy("userName", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new AsignarPermisosAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mAsignarPermisosRecycler.setVisibility(View.GONE);
                    // mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mAsignarPermisosRecycler.setVisibility(View.VISIBLE);
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

        mAsignarPermisosRecycler.setLayoutManager(new LinearLayoutManager(this));
        mAsignarPermisosRecycler.setAdapter(mAdapter);

        mAsignarPermisosRecycler.setNestedScrollingEnabled(false);

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
    public void OnAsignarPermisosSelected(DocumentSnapshot user) {


        this.user = user;

        mDialog.show(getSupportFragmentManager(), PublishAvisoDialogFragment.TAG);

    }

    @Override
    public void onSubirPermiso(int[] permisos) {

        User user = this.user.toObject(User.class);


        String permiso = "";


        if (permisos[0] == 1) {

            permiso += "1";

        }

        if (permisos[1] == 1) {

            permiso += "2";

        }

        if (permisos[2] == 1) {

            permiso += "3";

        }

        if (permisos[3] == 1) {

            permiso += "4";

        }

        if (permisos[4] == 1) {

            permiso += "5";

        }


        Log.i("PRUEBA", this.user.getReference().getPath());

        Map<String, Object> initialData = new HashMap<>();
        initialData.put("permisos", permiso);

        mFirestore.collection("usuarios").document(this.user.getReference().getId()).update(initialData);


    }


    public void onSubirAviso(User aviso) {


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
