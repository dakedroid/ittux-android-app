package com.divaga.tecnologico.permissions;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.NotificationCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.divaga.tecnologico.InicioActivity;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.model.Notificacion;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SendNotification extends AppCompatActivity implements View.OnClickListener {

    private FirebaseFirestore mFirestore;

    public ProgressDialog mProgressDialog;

    @BindView(R.id.text_notificacion)
    EditText textoEdtx;

    @BindView(R.id.boton_notificacion)
    Button enviarBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_send_notification);

        ButterKnife.bind(this);


    }

    private void sendNotification(String messageBody) {

        showProgressDialog();

        WriteBatch batch = mFirestore.batch();

        DocumentReference restRef = mFirestore.collection("mensajes").document();


        Notificacion notificacion = new Notificacion();

        notificacion.setTitle("Aviso");
        notificacion.setDescription(messageBody);

        // Add restaurant
        batch.set(restRef, notificacion);


        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {


                    hideProgressDialog();


                    Log.d("AvisosActivity", "Write batch succeeded.");
                } else {
                    Log.w("AvisosActivity", "write batch failed.", task.getException());
                }
            }
        });
    }

    @Override
    public void onClick(View view) {
        String texto = textoEdtx.getText().toString();

        sendNotification(texto);
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
