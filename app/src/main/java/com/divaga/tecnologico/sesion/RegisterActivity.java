/**
 * Copyright 2016 Google Inc. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.divaga.tecnologico.sesion;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.divaga.tecnologico.InicioActivity;
import com.divaga.tecnologico.R;
import com.divaga.tecnologico.customfonts.MyEditText;
import com.divaga.tecnologico.model.User;
import com.divaga.tecnologico.storage.MyUploadService;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import de.hdodenhof.circleimageview.CircleImageView;

/**
 * Demonstrate Firebase Authentication using a Google ID Token.
 */
public class RegisterActivity extends BaseActivity implements View.OnClickListener {

    private static final String TAG = "GoogleActivity";
    private static final int RC_SIGN_IN = 9001;
    private static final int RC_TAKE_PICTURE = 101;

    // [START declare_auth]
    private FirebaseAuth mAuth;

    private FirebaseUser user;
    private User mUser;
    // [END declare_auth]

    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef;

    //private TextView mStatusTextView;
    //private TextView mDetailTextView;

    // private TextView mStatusTextView;
    // private TextView mDetailTextView;
    private MyEditText mEmailField;
    private MyEditText mPasswordField;
    private MyEditText mNameField;
    private CircleImageView circleImageView;

    private Uri mFileUri = null;


    int loginMode = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        // Views
       // mStatusTextView = findViewById(R.id.status);
       // mDetailTextView = findViewById(R.id.detail);
        mEmailField = findViewById(R.id.field_email);
        mPasswordField = findViewById(R.id.field_password);
        mNameField = findViewById(R.id.field_name);


        // Button listeners
        //findViewById(R.id.sign_out_button).setOnClickListener(this);
        //findViewById(R.id.disconnect_button).setOnClickListener(this);

        // Views
        //  mStatusTextView = findViewById(R.id.status);
        //  mDetailTextView = findViewById(R.id.detail);
        // Buttons
        findViewById(R.id.btn_registrar).setOnClickListener(this);
        findViewById(R.id.back).setOnClickListener(this);

        circleImageView = findViewById(R.id.register_profile_pic);
        circleImageView.setOnClickListener(this);
       // findViewById(R.id.email_create_account_button).setOnClickListener(this);
       // findViewById(R.id.sign_out_button).setOnClickListener(this);
       // findViewById(R.id.verify_email_button).setOnClickListener(this);

        // [START config_signin]


        // [START initialize_auth]
        mAuth = FirebaseAuth.getInstance();
        // [END initialize_auth]

        storageRef = storage.getReference();
    }

    // [START on_start_check_user]
    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = mAuth.getCurrentUser();

        updateUIEmail(currentUser);
    }
    // [END on_start_check_user]

    // [START onactivityresult]

    // [END onactivityresult]

    // [START auth_with_google]
    // [END auth_with_google]

    // [START signin]

    // [END signin]

    private void signOut() {
        // Firebase sign out
        mAuth.signOut();

    }


    @Override
    public void onClick(View v) {
        int i = v.getId();



        /*
        else if (i == R.id.sign_out_button) {
            //signOut();
        } else if (i == R.id.disconnect_button) {
            //revokeAccess();
        }
        */

        if (i == R.id.btn_registrar) {
            createAccount(mEmailField.getText().toString(), mPasswordField.getText().toString(), mNameField.getText().toString(), "0");
        }else if (i == R.id.back){
            startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
        }else if (i == R.id.register_profile_pic){
            launchCamera();
        }


    }

    // email
    private void createAccount(String email, String password, final String name, final String permisos) {
        Log.d(TAG, "createAccount:" + email);
        if (!validateForm()) {
            return;
        }

        showProgressDialog();

        // [START create_user_with_email]
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");



                            user = mAuth.getCurrentUser();

                            mUser = new User(user, name, permisos);

                            if (mFileUri != null) {
                                uploadFromUri(mFileUri);
                            }


                        } else {
                            // If sign in fails, display a message to the user.
                            Log.w(TAG, "createUserWithEmail:failure", task.getException());
                            Toast.makeText(RegisterActivity.this, "Authentication failed.",
                                    Toast.LENGTH_SHORT).show();
                            updateUIEmail(null);
                        }

                        // [START_EXCLUDE]
                        hideProgressDialog();
                        // [END_EXCLUDE]
                    }
                });
        // [END create_user_with_email]
    }

    private void writeUser(){

        FirebaseFirestore mFirestore = FirebaseFirestore.getInstance();

        WriteBatch batch = mFirestore.batch();

        DocumentReference restRef = mFirestore.collection("usuarios").document();

        batch.set(restRef, mUser);

        batch.commit().addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()) {

                    updateUIEmail(user);

                } else {
                    Log.w("InicioActivity", "write batch failed.", task.getException());
                }
            }
        });
    }

    private boolean validateForm() {
        boolean valid = true;

        String email = mEmailField.getText().toString();
        if (TextUtils.isEmpty(email)) {
            mEmailField.setError("Requerido.");
            valid = false;
        } else {
            mEmailField.setError(null);
        }

        String password = mPasswordField.getText().toString();
        if (TextUtils.isEmpty(password)) {
            mPasswordField.setError("Requerido.");
            valid = false;
        } else {
            mPasswordField.setError(null);
        }

        String name = mNameField.getText().toString();
        if (TextUtils.isEmpty(name)) {
            mNameField.setError("Requerido.");
            valid = false;
        } else {
            mNameField.setError(null);
        }

        return valid;
    }

    private void updateUIEmail(FirebaseUser user) {

        if (user != null) {
            //mStatusTextView.setText(getString(R.string.emailpassword_status_fmt, user.getEmail(), user.isEmailVerified()));
            //mDetailTextView.setText(getString(R.string.firebase_status_fmt, user.getUid()));
            Log.i("LOGIN EMAIL", user.getEmail());
            Log.i("LOGIN EMAIL", user.getUid());

            startActivity(new Intent(RegisterActivity.this, InicioActivity.class));


           // findViewById(R.id.email_password_buttons).setVisibility(View.GONE);
           // findViewById(R.id.email_password_fields).setVisibility(View.GONE);
           // findViewById(R.id.signed_in_buttons).setVisibility(View.VISIBLE);

//            findViewById(R.id.verify_email_button).setEnabled(!user.isEmailVerified());
        } else {
            //mStatusTextView.setText(R.string.signed_out);
            //mDetailTextView.setText(null);

          //  findViewById(R.id.email_password_buttons).setVisibility(View.VISIBLE);
          //  findViewById(R.id.email_password_fields).setVisibility(View.VISIBLE);
          //  findViewById(R.id.signed_in_buttons).setVisibility(View.GONE);
        }
    }


    // image methods

    private void launchCamera() {
        Log.d(TAG, "launchCamera");

        // Pick an image from storage
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.setType("image/*");
        startActivityForResult(intent, RC_TAKE_PICTURE);



    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.d(TAG, "onActivityResult:" + requestCode + ":" + resultCode + ":" + data);
        if (requestCode == RC_TAKE_PICTURE) {
            if (resultCode == RESULT_OK) {
                mFileUri = data.getData();

                if (mFileUri != null) {
                    //uploadFromUri(mFileUri);

                    // Load image
                    Glide.with(getApplicationContext()).load(mFileUri).into(circleImageView);

                    Log.w(TAG, "File URI is ready");
                } else {
                    Log.w(TAG, "File URI is null");
                }
            } else {
                Toast.makeText(this, "Taking picture failed.", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void uploadFromUri(Uri fileUri) {
        Log.d(TAG, "uploadFromUri:src:" + fileUri.toString());

        // Save the File URI
        mFileUri = fileUri;

        // Clear the last download, if any
        //updateUI(mAuth.getCurrentUser());
        //mDownloadUrl = null;

        // Start MyUploadService to upload the file, so that the file is uploaded
        // even if this Activity is killed or put in the background
        // [START get_child_ref]
        // Get a reference to store file at photos/<FILENAME>.jpg
        final StorageReference photoRef = storageRef.child("photos").child(fileUri.getLastPathSegment());
        // [END get_child_ref]

        // Upload file to Firebase Storage
        Log.d(TAG, "uploadFromUri:dst:" + photoRef.getPath());
        photoRef.putFile(fileUri).
                addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {


                        /*
                        showProgressNotification(getString(R.string.progress_uploading),
                                taskSnapshot.getBytesTransferred(),
                                taskSnapshot.getTotalByteCount());

                                */
                    }
                })
                .continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                    @Override
                    public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                        // Forward any exceptions
                        if (!task.isSuccessful()) {
                            throw task.getException();
                        }

                        Log.d(TAG, "uploadFromUri: upload success");

                        // Request the public download URL
                        return photoRef.getDownloadUrl();
                    }
                })
                .addOnSuccessListener(new OnSuccessListener<Uri>() {
                    @Override
                    public void onSuccess(@NonNull Uri downloadUri) {
                        // Upload succeeded
                        Log.d(TAG, "uploadFromUri: getDownloadUri success");

                        writeUser();


                        /*
                        // [START_EXCLUDE]
                        broadcastUploadFinished(downloadUri, fileUri);
                        showUploadFinishedNotification(downloadUri, fileUri);

                        taskCompleted(); */
                        // [END_EXCLUDE]
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception exception) {
                        // Upload failed
                        Log.w(TAG, "uploadFromUri:onFailure", exception);

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
