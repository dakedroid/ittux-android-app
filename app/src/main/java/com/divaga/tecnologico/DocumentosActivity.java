package com.divaga.tecnologico;

import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.divaga.tecnologico.adapter.DocumentoAdapter;
import com.divaga.tecnologico.adapter.PublicacionAdapter;
import com.divaga.tecnologico.model.Documento;
import com.divaga.tecnologico.model.Publicacion;
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

public class DocumentosActivity extends AppCompatActivity implements DocumentoAdapter.OnDocumentoSelectedListener{

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private DocumentoAdapter mAdapter;

    private static final int LIMIT = 50;


    @BindView(R.id.recycler_documentos)
    RecyclerView mPublicacionesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos);

        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("documentos")
                //.orderBy("numComents", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new DocumentoAdapter(mQuery, this) {
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

        //writeOnServer();

    }

    @Override
    public void OnDocumentoSelected(DocumentSnapshot documento) {
        Toast.makeText(this, "Descargando Documento", Toast.LENGTH_SHORT).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

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

    public void writeOnServer() {

        WriteBatch batch = mFirestore.batch();

        DocumentReference restRef = mFirestore.collection("documentos").document();


        Documento documento = new Documento();


        documento.setName("Formato_prueba_2.pdf");

        documento.setType("pdf");

        documento.setCategory("Residencias");


        documento.setPath("none implemented yet");

        documento.setUsername(FirebaseAuth.getInstance().getCurrentUser().getDisplayName());





        // Add restaurant
        batch.set(restRef, documento);

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
                    Log.d("DocumentoActivity", "Write batch succeeded.");
                } else {
                    Log.w("DocumentoActivity", "write batch failed.", task.getException());
                }
            }
        });
    }
}
