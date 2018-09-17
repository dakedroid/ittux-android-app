package com.divaga.tecnologico;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.divaga.tecnologico.sesion.LoginActivity;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;

public class DasboardActivity extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private GoogleSignInClient mGoogleSignInClient;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        mAuth = FirebaseAuth.getInstance();


        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }


    @Override
    public void onClick(View view) {

        int i = view.getId();
        if (i == R.id.dashboar_btn_1) {
           startActivity(new Intent(DasboardActivity.this, InicioActivity.class));
        }
        if (i == R.id.dashboar_btn_2) {
            startActivity(new Intent(DasboardActivity.this, ConvocatoriaActivity.class));

        }
        if (i == R.id.dashboar_btn_3) {
            startActivity(new Intent(DasboardActivity.this, AvisosActivity.class));

        }
        if (i == R.id.dashboar_btn_4) {
            signOut();
        }
        if (i == R.id.dashboar_btn_5) {
            signOut();
        }
        if (i == R.id.dashboar_btn_6) {
            startActivity(new Intent(DasboardActivity.this, DocumentosActivity.class));
        }
        if (i == R.id.dashboar_btn_7) {
            signOut();
        }
        if (i == R.id.dashboar_btn_8) {
            signOut();
        }
        if (i == R.id.dashboar_btn_9) {
            signOut();
        }
    }

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

        // Google sign out
        mGoogleSignInClient.signOut().addOnCompleteListener(this,
                new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {

                    }
                });

        startActivity(new Intent(DasboardActivity.this, LoginActivity.class));
    }
}
