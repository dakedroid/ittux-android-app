package com.divaga.tecnologico;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.divaga.tecnologico.Utils.GlideApp;
import com.divaga.tecnologico.model.User;
import com.divaga.tecnologico.sesion.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;


public class DashboardActivity extends AppCompatActivity implements View.OnClickListener  {


    private static String userPerssions;

    GoogleSignInClient mGoogleSignInClient;

    FirebaseAuth firebaseAuth;
    FirebaseAuth.AuthStateListener mAuthListener;

    TextView txvName;

    TextView txvEmail;

    CardView LayoutConfiguracionCardView;

    CircleImageView photoUser;

    FirebaseFirestore mFirestore;

    String userEmail = "";
    String userName = "";
    String userPhotoUrl = "";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        firebaseAuth = FirebaseAuth.getInstance();

        LayoutConfiguracionCardView = findViewById(R.id.dashboar_btn_6);

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                if (user == null) {
                    Intent intent = new Intent(DashboardActivity.this, LoginActivity.class);
                    startActivity(intent);
                    finish();
                }
            }


        };


        txvEmail = findViewById(R.id.dashboar_email);
        txvName = findViewById(R.id.dashboar_user);
        photoUser = findViewById(R.id.dashboar_photo);


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);

        mFirestore = FirebaseFirestore.getInstance();

        getUserData();

    }

    @Override
    protected void onStart() {
        super.onStart();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        firebaseAuth.removeAuthStateListener(mAuthListener);
    }

    @Override
    protected void onResume() {
        super.onResume();
        firebaseAuth.addAuthStateListener(mAuthListener);
    }

    private void updateUI() {


        txvEmail.setText(userEmail);
        txvName.setText(userName);


        GlideApp.with(getApplicationContext())
                .load(userPhotoUrl)
                .into(photoUser);


    }


    private void getUserData() {


        final ArrayList<User> mArrayList = new ArrayList<>();

        String Uid = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();


        if (!Uid.equals("")) {
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

                        userEmail = mArrayList.get(0).getEmail();

                        userPerssions = mArrayList.get(0).getPermisos();

                        Toast.makeText(DashboardActivity.this, userPerssions, Toast.LENGTH_SHORT).show();


                        if (userPerssions.contains("6")) {

                            LayoutConfiguracionCardView.setVisibility(View.VISIBLE);


                        }else {

                            LayoutConfiguracionCardView.setVisibility(View.GONE);

                        }

                        updateUI();

                    }
                }
            });
        }

    }


    @Override
    public void onClick(View view) {

        int i = view.getId();

        if (i == R.id.dashboar_btn_1) {
            startActivity(new Intent(DashboardActivity.this, AvisosActivity.class));


        }
        if (i == R.id.dashboar_btn_2) {
            startActivity(new Intent(DashboardActivity.this, InicioActivity.class));


        }
        if (i == R.id.dashboar_btn_3) {

            startActivity(new Intent(DashboardActivity.this, ConvocatoriaActivity.class));
        }
        if (i == R.id.dashboar_btn_4) {
            startActivity(new Intent(DashboardActivity.this, DocumentosActivity.class));
        }
        if (i == R.id.dashboar_btn_5) {
            signOut();
        }

        if (i == R.id.dashboar_btn_6) {
            startActivity(new Intent(DashboardActivity.this, PermisosActivity.class));
        }


    }



    private void signOut() {
        // Firebase sign out
        firebaseAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

        startActivity(new Intent(DashboardActivity.this, LoginActivity.class));
    }
}