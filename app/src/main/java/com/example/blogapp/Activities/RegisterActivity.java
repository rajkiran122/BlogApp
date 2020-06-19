package com.example.blogapp.Activities;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class RegisterActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 1;
    private static final int INTENT_CODE = 2;
    private static final int RATIONALE_CODE = 3;
    private TextInputLayout reg_email, reg_password_create, reg_password_confirm, reg_username;
    private Button registerBtn;
    private ProgressBar progressBar;
    private ImageView imageView;


    private Uri imageUri;

    private FirebaseAuth mAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        reg_email = findViewById(R.id.reg_email);
        reg_password_create = findViewById(R.id.reg_password_create);
        reg_password_confirm = findViewById(R.id.reg_password_confirm);
        registerBtn = findViewById(R.id.registerBtn);
        progressBar = findViewById(R.id.progressBar);
        imageView = findViewById(R.id.nav_userphoto);
        reg_username = findViewById(R.id.reg_username);

        reg_email.setErrorTextColor(ColorStateList.valueOf(Color.RED));
        reg_password_create.setErrorTextColor(ColorStateList.valueOf(Color.RED));
        reg_password_confirm.setErrorTextColor(ColorStateList.valueOf(Color.RED));

        mAuth = FirebaseAuth.getInstance();

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                uploadPhoto();
            }
        });

    }

    private void uploadPhoto() {

        if (Build.VERSION.SDK_INT >= 22) {
            checkRequestForPermission();
        } else {
            openGallery();
        }

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("images/*");
        startActivityForResult(galleryIntent, INTENT_CODE);

    }

    private void checkRequestForPermission() {

        if (ContextCompat.checkSelfPermission(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
            openGallery();
        } else {
            requestStoragePermissions();
        }

    }

    private void requestStoragePermissions() {

        if (ActivityCompat.shouldShowRequestPermissionRationale(RegisterActivity.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {

            new AlertDialog.Builder(this)
                    .setTitle("Permission needed")
                    .setMessage("You have to allow to access images")
                    .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            ActivityCompat.requestPermissions(RegisterActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
                        }
                    })
                    .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                        }
                    })
                    .create().show();

        }else {
            ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},REQUEST_CODE);
        }
    }

    private void createNewAccount() {

        final String username = reg_username.getEditText().getText().toString().trim();
        String email = reg_email.getEditText().getText().toString().trim();
        String password1 = reg_password_create.getEditText().getText().toString().trim();
        String password = reg_password_confirm.getEditText().getText().toString().trim();

        if (!username.isEmpty() && !email.isEmpty() && !password.isEmpty() && !password1.isEmpty() && imageView.getDrawable() != null) {

            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (password.equals(password1)) {

                    if (password.length() >= 6) {
                        progressBar.setVisibility(View.VISIBLE);
                        registerBtn.setVisibility(View.GONE);

                        mAuth.createUserWithEmailAndPassword(email, password)
                                .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            progressBar.setVisibility(View.GONE);
                                            registerBtn.setVisibility(View.VISIBLE);

                                            Toast.makeText(RegisterActivity.this, "Registering!!Hold on for some seconds...", Toast.LENGTH_SHORT).show();

                                            updateInfo(username, imageUri, mAuth.getCurrentUser());

                                        } else {
                                            if (task.getException() instanceof FirebaseAuthUserCollisionException) {
                                                progressBar.setVisibility(View.GONE);
                                                registerBtn.setVisibility(View.VISIBLE);

                                                Toast.makeText(RegisterActivity.this, "Error:" + task.getException(), Toast.LENGTH_SHORT).show();

                                                Toast.makeText(RegisterActivity.this, "Email already in use...", Toast.LENGTH_SHORT).show();
                                            }
                                        }
                                    }
                                });
                    } else {
                        Toast.makeText(this, "Please enter password having more than six characters", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(this, "Password doesnt match", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Invalid email!!", Toast.LENGTH_SHORT).show();
            }
        } else {

            Toast.makeText(this, "Please enter all the details", Toast.LENGTH_SHORT).show();

        }

    }

    private void updateInfo(final String username, Uri imageUri, final FirebaseUser currentUser) {


        StorageReference mRef = FirebaseStorage.getInstance().getReference().child("users_photos");
        final StorageReference imageFilePath = mRef.child(imageUri.getLastPathSegment());
        imageFilePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {

                if (task.isSuccessful()) {
                    imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                        @Override
                        public void onSuccess(Uri uri) {

                            //uri contains user photo

                            UserProfileChangeRequest profileUpdate = new UserProfileChangeRequest.Builder()
                                    .setDisplayName(username)
                                    .setPhotoUri(uri)
                                    .build();


                            currentUser.updateProfile(profileUpdate)
                                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                                        @Override
                                        public void onComplete(@NonNull Task<Void> task) {

                                            if (task.isSuccessful()) {
                                                Toast.makeText(RegisterActivity.this, "Successfully registered...", Toast.LENGTH_SHORT).show();
                                                updateUI();
                                            } else {
                                                Toast.makeText(RegisterActivity.this, "Error:" + task.getException(), Toast.LENGTH_SHORT).show();
                                            }

                                        }
                                    });
                        }
                    });
                } else {
                    Toast.makeText(RegisterActivity.this, "Error:" + task.getException(), Toast.LENGTH_SHORT).show();
                }

            }
        });


    }

    private void updateUI() {

        Intent homeIntent = new Intent(RegisterActivity.this, Home.class);
        startActivity(homeIntent);
        finish();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == INTENT_CODE) {
            if (data != null) {
                imageUri = data.getData();

                imageView.setImageURI(imageUri);
            } else {
                Toast.makeText(this, "Upload the profile picture ", Toast.LENGTH_SHORT).show();
            }
        }

    }
}