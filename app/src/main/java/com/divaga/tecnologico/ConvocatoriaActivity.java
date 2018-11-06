package com.divaga.tecnologico;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.app.ProgressDialog;
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
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.divaga.tecnologico.adapter.ConvocatoriaAdapter;
import com.divaga.tecnologico.fragments.PublishConvocatoriaDialogFragment;
import com.divaga.tecnologico.model.Convocatoria;
import com.divaga.tecnologico.model.Documento;
import com.divaga.tecnologico.model.Publicacion;
import com.divaga.tecnologico.model.User;
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
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class ConvocatoriaActivity extends AppCompatActivity implements ConvocatoriaAdapter.OnConvocatoriaSelectedListener, View.OnClickListener, PublishConvocatoriaDialogFragment.ConvocatoriaListener{


    private StorageReference storageRef;
    private FirebaseFirestore mFirestore;
    private Query mQuery;

    private  Convocatoria mDocumento;

    public ProgressDialog mProgressDialog;

    private static String userPhotoUrl;
    private static String userName;
    private static String userPerssions;

    private ConvocatoriaAdapter mAdapter;

    private CardView publishDialogLayout;
    private PublishConvocatoriaDialogFragment mPublishDialog;

    private static final int LIMIT = 50;

    @BindView(R.id.recycler_convocatoria)
    RecyclerView mConvocatoriaRecycler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_convocatoria);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        ButterKnife.bind(this);

        FirebaseFirestore.setLoggingEnabled(true);

        // Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get ${LIMIT} restaurants
        mQuery = mFirestore.collection("convocatoria")
                .orderBy("datepublic", Query.Direction.DESCENDING)
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

        publishDialogLayout = findViewById(R.id.publish_dialog_convocatoria);

        mPublishDialog = new PublishConvocatoriaDialogFragment();

        storageRef = FirebaseStorage.getInstance().getReference();

        getUser(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());

    }

    private void getUser(final String Uid) {

        final ArrayList<User> mArrayList = new ArrayList<>();

        Log.i("PRUEBITA",Uid);

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

                    if (userPerssions.contains("2")) {

                        publishDialogLayout.setVisibility(View.VISIBLE);

                    }else {

                        publishDialogLayout.setVisibility(View.GONE);

                    }

                }
            }

        });

    }

    @Override
    public void OnConvocatoriaSelected(DocumentSnapshot documento) {

        mDocumento = documento.toObject(Convocatoria.class);

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

    public void writeOnServer(final Convocatoria convocatoria, final String mUID) {


        storageRef.child("convocatoria").child(mUID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {


                WriteBatch batch = mFirestore.batch();

                DocumentReference restRef = mFirestore.collection("convocatoria").document();

                convocatoria.setPostId(mUID);

                convocatoria.setUsername(userName);

                convocatoria.setPath(uri.toString());

                convocatoria.setUser_photo(userPhotoUrl);

                // Add restaurant
                batch.set(restRef, convocatoria);

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            hideProgressDialog();

                            Log.d("ConvocatoriaActivity", "Write batch succeeded.");

                        } else {

                            Log.w("ConvocatoriaActivity", "write batch failed.", task.getException());

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

    public void download(final Convocatoria documento){

        Toast.makeText(this, "Descargando Documento", Toast.LENGTH_SHORT).show();

        showProgressDialog();

        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference().child("convocatoria");

        final File root = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + File.separator + "TECNOLOGICO_ARCHIVOS" + File.separator +  documento.getDescription() + documento.getType());

        //final File localFile =  ("documentos", documento.getType().replace(".", ""))


        storageRef.child(documento.getPostId()).getFile(root).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {


                hideProgressDialog();

                showFileDialog( root.getPath(), documento.getType());

                Toast.makeText(ConvocatoriaActivity.this, "Descarga finalizada", Toast.LENGTH_SHORT).show();
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {

                //Log.w(TAG, "download:FAILURE", exception);

            }
        });
    }

    public void showFileDialog(final String path, final String type){

        final AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ConvocatoriaActivity.this);

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



    @Override
    public void onClick(View view) {
        Log.i("LISTENERLIKE", "CORRECT");

        int id = view.getId();

        if (id == R.id.activity_convocatoria_publish){
            mPublishDialog.show(getSupportFragmentManager(), PublishConvocatoriaDialogFragment.TAG);
        }
    }

    @Override
    public void onSubirConvocatoria(Convocatoria convocatoria, Uri filePath) {
        Log.i("LISTENERSUBIR", "CORRECT");


        uploadImage(convocatoria, filePath);

    }

    public void uploadImage(final Convocatoria convocatoria, Uri filePath){

        final String mUID = UUID.randomUUID().toString();

        final StorageReference fileRef = storageRef.child("convocatoria").child(mUID);


        if(filePath != null){

            showProgressDialog();

            fileRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            hideProgressDialog();
                            Toast.makeText(ConvocatoriaActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            writeOnServer(convocatoria, mUID);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            Toast.makeText(ConvocatoriaActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                            double progress = (100.0*taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                            // mProgressDialog.setMessage("Uploaded ");
                        }
                    });
        }

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
