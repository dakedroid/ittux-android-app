package com.divaga.tecnologico;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.adapter.ConvocatoriaAdapter;
import com.divaga.tecnologico.adapter.DocumentoAdapter;
import com.divaga.tecnologico.model.Convocatoria;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConvocatoriaActivity extends AppCompatActivity implements ConvocatoriaAdapter.OnConvocatoriaSelectedListener{

    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private ConvocatoriaAdapter mAdapter;

    private static final int LIMIT = 50;


    @BindView(R.id.recycler_convocatoria)
    RecyclerView mConvocatoriaRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convocatoria);

        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("convocatoria")
                //.orderBy("numComents", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new ConvocatoriaAdapter(mQuery, this) {
            @Override
            protected void onDataChanged() {
                // Show/hide content if the query returns empty.
                if (getItemCount() == 0) {
                    mConvocatoriaRecycler.setVisibility(View.GONE);
                    // mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mConvocatoriaRecycler.setVisibility(View.VISIBLE);
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

        mConvocatoriaRecycler.setLayoutManager(new LinearLayoutManager(this));
        mConvocatoriaRecycler.setAdapter(mAdapter);

        writeOnServer();

    }

    @Override
    public void OnConvocatoriaSelected(DocumentSnapshot documento) {
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

        DocumentReference restRef = mFirestore.collection("convocatoria").document();

        Convocatoria convocatoria = new Convocatoria();


        convocatoria.setUsername("Depto. Sistema y Computacion");
        convocatoria.setUser_photo("https://www.ittux.edu.mx/sites/default/files/styles/medium/public/tec-transp.png?itok=v_2qwjVj");

        convocatoria.setDescription("Se les comunica a los estudiantes 2018");

        convocatoria.setType(".jpg");
        convocatoria.getDatelimit();

        convocatoria.setPath("none implemented yet");






        // Add restaurant
        batch.set(restRef, convocatoria);

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
                    Log.d("ConvocatoriaActivity", "Write batch succeeded.");
                } else {
                    Log.w("ConvocatoriaActivity", "write batch failed.", task.getException());
                }
            }
        });
    }


}
