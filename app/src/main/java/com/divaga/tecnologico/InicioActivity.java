package com.divaga.tecnologico;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;

import com.divaga.tecnologico.adapter.PublicacionAdapter;
import com.divaga.tecnologico.model.Publicacion;
import com.divaga.tecnologico.sesion.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InicioActivity extends AppCompatActivity implements PublicacionAdapter.OnPublicacionSelectedListener{


    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private PublicacionAdapter mAdapter;

    private static final int LIMIT = 50;


    @BindView(R.id.recycler_publicaciones)
    RecyclerView mPublicacionesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inicio);

        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();



        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("publicaciones")
                //.orderBy("numComents", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new PublicacionAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mPublicacionesRecycler.setVisibility(View.GONE);
                    // mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mPublicacionesRecycler.setVisibility(View.VISIBLE);
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

        mPublicacionesRecycler.setLayoutManager(new LinearLayoutManager(this));
        mPublicacionesRecycler.setAdapter(mAdapter);

        mPublicacionesRecycler.setNestedScrollingEnabled(false);




        // writeOnServer();
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
    public void OnPublicacionSelected(DocumentSnapshot publicacion) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_RESTAURANT_ID, publicacion.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

    }

    public void writeOnServer() {

        WriteBatch batch = mFirestore.batch();

        DocumentReference restRef = mFirestore.collection("publicaciones").document();


        Publicacion publicacion = new Publicacion();

        publicacion.setUsername("Depto. de comunicacion y difusion");
        publicacion.setUser_photo("https://www.ittux.edu.mx/sites/default/files/styles/medium/public/tec-transp.png?itok=v_2qwjVj");

        publicacion.setDescription("Bienvenidos al tecnologico de tuxtepec.");
        publicacion.setPhoto("https://www.nvinoticias.com/sites/default/files/styles/node/public/notas/2017/04/07/image_67_copia.jpg?itok=g7jJ57PP");

        //publicacion.setTimecreated(1203123);
        publicacion.setNumLikes(0);
        publicacion.setNumComments(0);

        // Add restaurant
        batch.set(restRef, publicacion);

        /*
        // Add  to subcollection
        for (Comentario comentarios : comentarioss) {
            batch.set(restRef.collection("comentarios").document(), comentario);
        }
        */

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {
                    Log.d("InicioActivity", "Write batch succeeded.");
                } else {
                    Log.w("InicioActivity", "write batch failed.", task.getException());
                }
            }
        });
    }


}
