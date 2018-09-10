package com.divaga.tecnologico;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.divaga.tecnologico.adapter.DocumentoAdapter;
import com.divaga.tecnologico.fragments.PublishDocumentDialogFragment;
import com.divaga.tecnologico.model.Documento;
import com.divaga.tecnologico.sesion.BaseActivity;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DocumentosActivity extends BaseActivity implements DocumentoAdapter.OnDocumentoSelectedListener, View.OnClickListener, PublishDocumentDialogFragment.DocumentoListener{


    private FirebaseAuth mAuth;

    private FirebaseFirestore mFirestore;

    StorageReference storageRef;

    private Query mQuery;

    private DocumentoAdapter mAdapter;


    private PublishDocumentDialogFragment mPublishDialog;

    private static final int LIMIT = 50;


    @BindView(R.id.recycler_documentos)
    RecyclerView mPublicacionesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos);



        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();


        storageRef = FirebaseStorage.getInstance().getReference();

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

        mPublishDialog = new PublishDocumentDialogFragment();

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
    public void onStop() {
        super.onStop();

        if (mAdapter != null) {
            mAdapter.stopListening();
        }
    }

    public void writeOnServer(final Documento documento) {


        storageRef.child("documentos").child(documento.getName()).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {


                Log.i("PATHGET", uri.getPath());

                WriteBatch batch = mFirestore.batch();

                DocumentReference restRef = mFirestore.collection("documentos").document();

                documento.setPath(uri.getPath());

                // Add restaurant
                batch.set(restRef, documento);

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            hideProgressDialog();

                            Log.d("DocumentoActivity", "Write batch succeeded.");

                        } else {

                            Log.w("DocumentoActivity", "write batch failed.", task.getException());

                        }
                    }
                });
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
                // Handle any errors

                Log.i("PATHGET", "fallo");
            }
        });

    }

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if(id == R.id.activity_documentos_publish){
            Log.i("prueba", "click");
            mPublishDialog.show(getSupportFragmentManager(), PublishDocumentDialogFragment.TAG);

        }
    }

    @Override
    public void onSubirDocumento(final Documento documento, Uri filePath) {



        final StorageReference fileRef = storageRef.child("documentos").child(documento.getName());

        fileRef.putFile(filePath).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {

                    }
                })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }


                        // Request the public download URL
                        return fileRef.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(@NonNull Uri downloadUri) {
                        // Upload succeeded

                        /*
                        // [START_EXCLUDE]
                        broadcastUploadFinished(downloadUri, fileUri);
                        showUploadFinishedNotification(downloadUri, fileUri);

                        taskCompleted(); */
                        // [END_EXCLUDE]

                        writeOnServer(documento);

                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed

                        hideProgressDialog();
                        /*
                        // [START_EXCLUDE]
                        broadcastUploadFinished(null, fileUri);
                        showUploadFinishedNotification(null, fileUri);
                        taskCompleted();
                        */
                        // [END_EXCLUDE]
                    }
                });

        // Show loading spinner
        showProgressDialog();
    }
}
