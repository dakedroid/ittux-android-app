package com.divaga.tecnologico;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.divaga.tecnologico.adapter.PublicacionAdapter;
import com.divaga.tecnologico.fragments.PublishPublicacionDialogFragment;
import com.divaga.tecnologico.model.Publicacion;
import com.divaga.tecnologico.model.User;
import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.share.ShareApi;
import com.facebook.share.Sharer;
import com.facebook.share.model.SharePhoto;
import com.facebook.share.model.SharePhotoContent;
import com.facebook.share.widget.ShareDialog;
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
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InicioActivity extends FragmentActivity implements PublicacionAdapter.OnPublicacionSelectedListener, View.OnClickListener, PublishPublicacionDialogFragment.PublicacionListener{


    private static final int LIMIT = 50;

    private static String userPhotoUrl;
    private static String userName;
    private static String userPerssions;

    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private DocumentReference mPublicacionRef;
    private StorageReference storageRef;

    public ProgressDialog mProgressDialog;

    private String post_id;

    private PublicacionAdapter mAdapter;
    private PublishPublicacionDialogFragment mPublishDialog;

    private LinearLayout photo;
    private CardView publishDialogLayout;

    private static final String PERMISSION = "publish_actions";
    private final String PENDING_ACTION_BUNDLE_KEY = "com.divaga.tecnologico:PendingAction";
    private PendingAction pendingAction = PendingAction.NONE;
    private boolean canPresentShareDialogWithPhotos;
    private ShareDialog shareDialog;
    private CallbackManager callbackManager;

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
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(LIMIT);

        // RecyclerView
        mAdapter = new PublicacionAdapter(mQuery, this, getApplicationContext()) {
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

        storageRef = FirebaseStorage.getInstance().getReference();

        // facebook stuff

        callbackManager = CallbackManager.Factory.create();


        shareDialog = new ShareDialog(this);
        shareDialog.registerCallback(
                callbackManager,
                shareCallback);

        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }

        // Can we present the share dialog for photos?
        canPresentShareDialogWithPhotos = ShareDialog.canShow(
                SharePhotoContent.class);


        mPublishDialog = new PublishPublicacionDialogFragment();


        getUser(Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid());


        publishDialogLayout = findViewById(R.id.publish_dialog_posts);


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
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
    }

    @Override
    public void OnPublicacionSelected(DocumentSnapshot publicacion) {

        Intent intent = new Intent(this, DetailActivity.class);
        intent.putExtra(DetailActivity.KEY_RESTAURANT_ID, publicacion.getId());

        startActivity(intent);
        overridePendingTransition(R.anim.slide_in_from_right, R.anim.slide_out_to_left);

    }

    @Override
    public void OnShareSelected(String title, LinearLayout photo) {

        this.photo = photo;

        this.photo.setBackgroundColor(getResources().getColor(R.color.colorWhite));

        onClickPostPhoto();
    }

    @Override
    public void OnLikeSelected(DocumentSnapshot publicacion) {


        post_id = publicacion.getId();

        Toast.makeText(this, "Id consegido", Toast.LENGTH_SHORT).show();

        mPublicacionRef = mFirestore.collection("publicaciones").document(post_id);

        addLike(mPublicacionRef)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Detailactivity", "Comentario agregado");

                        Toast.makeText(InicioActivity.this, "agregado", Toast.LENGTH_SHORT).show();

                        // Hide keyboard and scroll to top
                        //hideKeyboard();
                       // mComentariosRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DetailActivity", "Error con el Comentadio", e);

                        // Show failure message and hide keyboard
                       // hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Fallo en cargar el comentario",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }



    // Facebook Stuff

    private void onClickPostPhoto() {
        performPublish(PendingAction.POST_PHOTO, canPresentShareDialogWithPhotos);
    }

    private void performPublish(PendingAction action, boolean allowNoToken) {
        if (AccessToken.isCurrentAccessTokenActive() || allowNoToken) {
            pendingAction = action;
            handlePendingAction();
        }
    }

    private void handlePendingAction() {
        PendingAction previouslyPendingAction = pendingAction;
        // These actions may re-set pendingAction if they are still pending, but we assume they
        // will succeed.
        pendingAction = PendingAction.NONE;

        switch (previouslyPendingAction) {
            case NONE:
                break;
            case POST_PHOTO:
                postPhoto();
                break;
        }
    }

    public Bitmap getBitmap(LinearLayout layout){
        layout.setDrawingCacheEnabled(true);
        layout.buildDrawingCache();
        Bitmap bmp = Bitmap.createBitmap(layout.getDrawingCache());
        layout.setDrawingCacheEnabled(false);
        return bmp;
    }

    private void postPhoto() {

        Bitmap image = getBitmap(photo);
        //Bitmap image = BitmapFactory.decodeResource(getResources(), R.drawable.pizza_monster);
        SharePhoto sharePhoto = new SharePhoto.Builder().setBitmap(image).build();
        ArrayList<SharePhoto> photos = new ArrayList<>();
        photos.add(sharePhoto);

        SharePhotoContent sharePhotoContent = new SharePhotoContent.Builder().setPhotos(photos).build();
        if (canPresentShareDialogWithPhotos) {
            shareDialog.show(sharePhotoContent);
        } else if (hasPublishPermission()) {
            ShareApi.share(sharePhotoContent, shareCallback);
        } else {
            pendingAction = PendingAction.POST_PHOTO;
            // We need to get new permissions, then complete the action when we get called back.
            LoginManager.getInstance().logInWithPublishPermissions(this, Arrays.asList(PERMISSION));
        }
    }

    private boolean hasPublishPermission() {
        return AccessToken.isCurrentAccessTokenActive()
                && AccessToken.getCurrentAccessToken().getPermissions().contains("publish_actions");
    }

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    private Task<Void> addLike(final DocumentReference publicacionRf) {
        // Create reference for new comentario, for use inside the transaction

        // In a transaction, add the new comentarioand update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {

                Publicacion publicacion = transaction.get(publicacionRf).toObject(Publicacion.class);

                // Compute new number of comentarios

                int newNumLikes= publicacion.getNumLikes() + 1;

                //Toast.makeText(InicioActivity.this, "task", Toast.LENGTH_SHORT).show();

                // Set new restaurant info
                publicacion.setNumLikes(newNumLikes);

                // Commit to Firestore
                transaction.set(publicacionRf, publicacion);

                return null;
            }
        });
    }

    private FacebookCallback<Sharer.Result> shareCallback = new FacebookCallback<Sharer.Result>() {
        @Override
        public void onCancel() {
            Log.d("HelloFacebook", "Canceled");
        }

        @Override
        public void onError(FacebookException error) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
            String title = getString(R.string.error);
            String alertMessage = error.getMessage();
            showResult(title, alertMessage);
        }

        @Override
        public void onSuccess(Sharer.Result result) {
            Log.d("HelloFacebook", "Success!");
            if (result.getPostId() != null) {
                String title = getString(R.string.success);
                String id = result.getPostId();
                String alertMessage = "Post id:" + id;
                showResult(title, alertMessage);
            }
        }

        private void showResult(String title, String alertMessage) {
            new AlertDialog.Builder(InicioActivity.this)
                    .setTitle(title)
                    .setMessage(alertMessage)
                    .setPositiveButton(R.string.ok, null)
                    .show();
        }
    };


    // Listeners Stuff

    @Override
    public void onClick(View view) {
        int id = view.getId();

        if (id == R.id.activity_inicio_publish){
            mPublishDialog.show(getSupportFragmentManager(), PublishPublicacionDialogFragment.TAG);
        }
    }

    @Override
    public void onSubirPublicacion(final Publicacion publicacion, Uri filePath) {

        uploadImage(publicacion, filePath);

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


                    if (userPerssions.contains("1")) {

                        publishDialogLayout.setVisibility(View.VISIBLE);

                    }else {

                        publishDialogLayout.setVisibility(View.GONE);

                    }

                }
            }

        });

    }


    public void uploadImage(final Publicacion publicacion, Uri filePath){

        final String mUID = UUID.randomUUID().toString();

        final StorageReference fileRef = storageRef.child("publicaciones").child(mUID);


        if(filePath != null){

            showProgressDialog();

            fileRef.putFile(filePath)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {

                            hideProgressDialog();
                            Toast.makeText(InicioActivity.this, "Uploaded", Toast.LENGTH_SHORT).show();

                            writeOnServer(publicacion, mUID);
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            hideProgressDialog();
                            Toast.makeText(InicioActivity.this, "Failed "+e.getMessage(), Toast.LENGTH_SHORT).show();
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

    public void writeOnServer(final Publicacion publicacion, final String mUID) {


        storageRef.child("publicaciones").child(mUID).getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(final Uri uri) {


                WriteBatch batch = mFirestore.batch();

                DocumentReference restRef = mFirestore.collection("publicaciones").document();

                publicacion.setUsername(userName);

                publicacion.setPhoto(uri.toString());

                publicacion.setUser_photo(userPhotoUrl);

                // Add restaurant
                batch.set(restRef, publicacion);

                batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {

                            hideProgressDialog();

                            Log.d("InicioActivity", "Write batch succeeded.");

                        } else {

                            Log.w("InicioActivity", "write batch failed.", task.getException());

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
