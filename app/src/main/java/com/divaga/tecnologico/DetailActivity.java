package com.divaga.tecnologico;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.adapter.ComentarioAdapter;
import com.divaga.tecnologico.model.Comentario;
import com.divaga.tecnologico.model.Publicacion;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.Transaction;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;

import javax.annotation.Nullable;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class DetailActivity extends AppCompatActivity implements EventListener<DocumentSnapshot>, ComentarioDialogFragment.ComentarioListener {

    public static final String KEY_RESTAURANT_ID = "key_restaurant_id";

    @BindView(R.id.publicacion_image)
    ImageView mImageView;

    @BindView(R.id.publicacion_username)
    TextView mUsernameView;

    @BindView(R.id.publicacion_description)
    TextView mDescriptionView;

    @BindView(R.id.publicacion_num_comments)
    TextView mNumComentariosView;

    @BindView(R.id.publicacion_fecha)
    TextView mFechaView;

    @BindView(R.id.publicacion_hora)
    TextView mHoraView;

    @BindView(R.id.view_empty_comentarios)
    ViewGroup mEmptyView;

    @BindView(R.id.recycler_comentarios)
    RecyclerView mComentariosRecycler;

    private FirebaseFirestore mFirestore;
    private DocumentReference mPublicacionRef;
    private ListenerRegistration mPublicacionRegistration;

    private ComentarioAdapter mComentarioAdapter;

    private ComentarioDialogFragment mComentarioDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comentarios);

        ButterKnife.bind(this);

        String restaurantId = getIntent().getExtras().getString(KEY_RESTAURANT_ID);
        if (restaurantId == null) {
            throw new IllegalArgumentException("Must pass extra " + KEY_RESTAURANT_ID);
        }

        // Initialize Firestore
        mFirestore = FirebaseFirestore.getInstance();

        // Get reference to the restaurant
        mPublicacionRef = mFirestore.collection("publicaciones").document(restaurantId);

        // Get comentarios
        Query comentariosQuery = mPublicacionRef
                .collection("comentarios")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .limit(50);

        // RecyclerView
        mComentarioAdapter = new ComentarioAdapter(comentariosQuery) {
            @Override
            protected void onDataChanged() {
                if (getItemCount() == 0) {
                    mComentariosRecycler.setVisibility(View.GONE);
                    mEmptyView.setVisibility(View.VISIBLE);
                } else {
                    mComentariosRecycler.setVisibility(View.VISIBLE);
                    mEmptyView.setVisibility(View.GONE);
                }
            }
        };
        mComentariosRecycler.setLayoutManager(new LinearLayoutManager(this));
        mComentariosRecycler.setAdapter(mComentarioAdapter);

        mComentarioDialog = new ComentarioDialogFragment();
    }

    @Override
    protected void onStart() {
        super.onStart();

        mComentarioAdapter.startListening();
        mPublicacionRegistration = mPublicacionRef.addSnapshotListener(this);
    }

    @Override
    protected void onStop() {
        super.onStop();

        mComentarioAdapter.stopListening();

        if (mPublicacionRegistration != null) {
            mPublicacionRegistration.remove();
            mPublicacionRegistration = null;
        }
    }

    @Override
    public void onEvent(@Nullable DocumentSnapshot snapshot, @Nullable FirebaseFirestoreException e) {
        if (e != null) {
            Log.w("DetailActivity", "restaurant:onEvent", e);
            return;
        }

        onComentarioLoaded(snapshot.toObject(Publicacion.class));
    }


    private static final SimpleDateFormat FORMAT  = new SimpleDateFormat("MM/dd/yyyy", Locale.US);

    private static final SimpleDateFormat HOUR_FORMAT  = new SimpleDateFormat("h:mm:ss", Locale.US);



    private void onComentarioLoaded(Publicacion publicacion) {


        HOUR_FORMAT.setTimeZone(TimeZone.getTimeZone("GMT"));

        mUsernameView.setText(publicacion.getUsername());
        mDescriptionView.setText(publicacion.getDescription());

        mNumComentariosView.setText(String.valueOf(publicacion.getNumComments()));

        if (publicacion.getTimestamp() != null) {
            mFechaView.setText(FORMAT.format(publicacion.getTimestamp()));
            mHoraView.setText(HOUR_FORMAT.format(publicacion.getTimestamp()));
        }

        Glide.with(mImageView.getContext())
                .load(publicacion.getPhoto())
                .into(mImageView);
    }

    @OnClick(R.id.publicacion_button_back)
    public void onBackArrowClicked(View view) {
        onBackPressed();
    }

    @OnClick(R.id.fab_show_comentario_dialog)
    public void onAddComentarioClicked(View view) {
        mComentarioDialog.show(getSupportFragmentManager(), ComentarioDialogFragment.TAG);
    }

    private Task<Void> addComentario(final DocumentReference restaurantRef, final Comentario comentario) {
        // Create reference for new comentario, for use inside the transaction
        final DocumentReference comentarioRef = restaurantRef.collection("comentarios").document();

        // In a transaction, add the new comentarioand update the aggregate totals
        return mFirestore.runTransaction(new Transaction.Function<Void>() {
            @Override
            public Void apply(Transaction transaction) throws FirebaseFirestoreException {
                Publicacion publicacion = transaction.get(restaurantRef).toObject(Publicacion.class);

                // Compute new number of comentarios
                int newNumComents= publicacion.getNumComments() + 1;


                // Set new restaurant info
                publicacion.setNumComments(newNumComents);


                // Commit to Firestore
                transaction.set(restaurantRef, publicacion);
                transaction.set(comentarioRef, comentario);

                return null;
            }
        });
    }

    private void hideKeyboard() {
        View view = getCurrentFocus();
        if (view != null) {
            ((InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE))
                    .hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    @Override
    public void onComentar(Comentario comentario) {

        addComentario(mPublicacionRef, comentario)
                .addOnSuccessListener(this, new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("Detailactivity", "Comentario agregado");

                        // Hide keyboard and scroll to top
                        hideKeyboard();
                        mComentariosRecycler.smoothScrollToPosition(0);
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DetailActivity", "Error con el Comentadio", e);

                        // Show failure message and hide keyboard
                        hideKeyboard();
                        Snackbar.make(findViewById(android.R.id.content), "Fallo en cargar el comentario",
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }
}
