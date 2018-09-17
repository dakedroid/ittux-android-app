package com.divaga.tecnologico;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.divaga.tecnologico.adapter.PublicacionAdapter;
import com.divaga.tecnologico.model.Publicacion;
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
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;
import com.google.firebase.firestore.WriteBatch;

import java.util.ArrayList;
import java.util.Arrays;

import butterknife.BindView;
import butterknife.ButterKnife;

public class InicioActivity extends FragmentActivity implements PublicacionAdapter.OnPublicacionSelectedListener{


    private FirebaseFirestore mFirestore;
    private Query mQuery;
    private DocumentReference mPublicacionRef;

    String post_id;

    private PublicacionAdapter mAdapter;

    private static final int LIMIT = 50;

    private LinearLayout photo;

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
                //.orderBy("numComents", Query.Direction.DESCENDING)
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

    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
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

}
