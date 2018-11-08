package com.divaga.tecnologico;

import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.divaga.tecnologico.adapter.DocumentoAdapter;
import com.divaga.tecnologico.fragments.PublishDocumentDialogFragment;
import com.divaga.tecnologico.model.Documento;
import com.divaga.tecnologico.model.User;
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
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class DocumentosActivity extends BaseActivity implements DocumentoAdapter.OnDocumentoSelectedListener, View.OnClickListener, PublishDocumentDialogFragment.DocumentoListener{

    private FirebaseAuth mAuth;

    private FirebaseFirestore mFirestore;

    StorageReference storageRef;

    private Query mQuery;

    private DocumentoAdapter mAdapter;

    private  Documento mDocumento;

    private static String userPhotoUrl;

    private PublishDocumentDialogFragment mPublishDialog;

    private static final int LIMIT = 50;
    private static String userName;
    private static String userPerssions;
    private CardView publishDialogLayout;





    @BindView(R.id.recycler_documentos)
    RecyclerView mPublicacionesRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_documentos);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        ButterKnife.bind(this);


        FirebaseFirestore.setLoggingEnabled(true);

        mAuth = FirebaseAuth.getInstance();

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();


        storageRef = FirebaseStorage.getInstance().getReference();

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("documentos")
                .orderBy("category", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new DocumentoAdapter(mQuery, this, getApplicationContext()) {
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

        publishDialogLayout = findViewById(R.id.publish_dialog_documento);

        mPublishDialog = new PublishDocumentDialogFragment();

        getUser();

    }

    @Override
    public void OnDocumentoSelected(DocumentSnapshot documento) {


        mDocumento = documento.toObject(Documento.class);

        if (isStoragePermissionGranted()){

            download(mDocumento);

        }


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

    @Override
    public boolean onSupportNavigateUp(){
        finish();
        return true;
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


                    if (userPerssions.contains("4")) {

                        publishDialogLayout.setVisibility(View.VISIBLE);

                    } else {

                        publishDialogLayout.setVisibility(View.GONE);

                    }

                }
            }

        });

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

    public void download(final Documento documento){

        Toast.makeText(this, "Descargando Documento", Toast.LENGTH_SHORT).show();

        showProgressDialog();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("documentos");

        final File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "TECNOLOGICO_ARCHIVOS" + File.separator +  documento.getName() + documento.getType());

        //final File localFile =  ("documentos", documento.getType().replace(".", ""))


        storageRef.child(documento.getName()).getFile(root).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {


                hideProgressDialog();

                showFileDialog( root.getPath(), documento.getType());

                Toast.makeText(DocumentosActivity.this, "Descarga finalizada", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                //Log.w(TAG, "download:FAILURE", exception);

            }
        });
    }

    public void showFileDialog(final String path, final String type){

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(DocumentosActivity.this);

        // set title
        alertDialogBuilder.setTitle("Tu archivo ya se descargo");

        // set dialog message
        alertDialogBuilder
                .setMessage("Seleccione la opcion de 'Abrir archivo'")
                .setCancelable(false)
                .setPositiveButton("Abrir archivo",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, close
                        // current activity

                        dialog.cancel();

                        File file = new File(path);
                        Intent target = new Intent(Intent.ACTION_VIEW);

                        if(type.equals(".pdf")){
                            target.setDataAndType(Uri.fromFile(file),"application/pdf");

                        }else if (type.equals(".jpg") || type.equals(".jpeg") || type.equals(".png")){

                            target.setDataAndType(Uri.fromFile(file),"image/*");

                        }else if (type.equals(".doc") || type.equals(".docx") || type.equals(".odt")){

                            String word =  "application/vnd.openxmlformats-officedocument.wordprocessingml.document";

                            target.setDataAndType(Uri.fromFile(file), word);

                        }else if (type.equals(".pptx") || type.equals(".ppt")){

                            target.setDataAndType(Uri.fromFile(file), "application/vnd.openxmlformats-officedocument.presentationml.presentation");

                        }else if (type.equals(".xls") || type.equals(".xlsx")){

                            target.setDataAndType(Uri.fromFile(file), "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");

                        }else if (type.equals(".zip")){

                            target.setDataAndType(Uri.fromFile(file), "application/zip");

                        }

                        target.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);

                        Intent intent = Intent.createChooser(target, "Open File");
                        try {
                            startActivity(intent);
                        } catch (ActivityNotFoundException e) {
                            // Instruct the user to install a PDF reader here, or something
                        }

                    }
                })
                .setNegativeButton("Cancelar",new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        // if this button is clicked, just close
                        // the dialog box and do nothing
                        dialog.cancel();
                    }
                });

        // create alert dialog
        AlertDialog alertDialog = alertDialogBuilder.create();

        // show it
        alertDialog.show();
    }

    public  boolean isStoragePermissionGranted() {
        if (Build.VERSION.SDK_INT >= 23) {
            if (checkSelfPermission(android.Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                Log.v("This Activity","Permission is granted");
                return true;
            } else {

                Log.v("This Activity","Permission is revoked");
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
                return false;
            }
        }
        else { //permission is automatically granted on sdk<23 upon installation
            Log.v("This Activity","Permission is granted");
            return true;
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
            Log.v("This Activity","Permission: "+permissions[0]+ "was "+grantResults[0]);
            //resume tasks needing this permission
            download(mDocumento);

        }
    }

}
