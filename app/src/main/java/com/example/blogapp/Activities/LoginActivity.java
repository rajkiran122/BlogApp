package com.example.blogapp.Activities;

import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    FirebaseAuth mAuth;
    private TextInputLayout email_text_Input, password_text_Input;
    private Button loginBtn;
    private TextView register_text_Btn;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Views();

        email_text_Input.setErrorTextColor(ColorStateList.valueOf(Color.RED));
        password_text_Input.setErrorTextColor(ColorStateList.valueOf(Color.RED));

        mAuth = FirebaseAuth.getInstance();

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                signIn();
            }
        });

        register_text_Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
            }
        });

    }

    private void signIn() {

        String email = email_text_Input.getEditText().getText().toString().trim();
        String password = password_text_Input.getEditText().getText().toString().trim();


        if (email.isEmpty() && password.isEmpty()) {
            Toast.makeText(this, "Please fill up the details..", Toast.LENGTH_SHORT).show();
        } else if (email.isEmpty()) {
            Toast.makeText(this, "Please enter email address..", Toast.LENGTH_SHORT).show();
        } else if (password.isEmpty()) {
            Toast.makeText(this, "Please enter password..", Toast.LENGTH_SHORT).show();
        } else {

            showProgressBar();
            loginBtn.setVisibility(View.GONE);

            mAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                hideProgressBar();
                                loginBtn.setVisibility(View.VISIBLE);
                                startActivity(new Intent(LoginActivity.this, Home.class));
                            } else {

                                Toast.makeText(LoginActivity.this, "Error:" + task.getException(), Toast.LENGTH_SHORT).show();

                                hideProgressBar();
                                loginBtn.setVisibility(View.VISIBLE);

                                if (task.getException() instanceof FirebaseAuthInvalidCredentialsException) {
                                    Toast.makeText(LoginActivity.this, "Password doesnt match!!", Toast.LENGTH_LONG).show();
                                } else if (task.getException() instanceof FirebaseAuthInvalidUserException) {
                                    Toast.makeText(LoginActivity.this, "User doesnt exist!!", Toast.LENGTH_LONG).show();
                                }

                            }
                        }
                    });

        }
    }


    public void Views() {
        email_text_Input = findViewById(R.id.email_Input_Login);
        password_text_Input = findViewById(R.id.password_Input_Login);
        loginBtn = findViewById(R.id.login_Button);
        progressBar = findViewById(R.id.progressBar);
        register_text_Btn = findViewById(R.id.registerActivityOpenBtn);
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null) {
            startActivity(new Intent(LoginActivity.this, Home.class));
            finish();
        }

    }

    private void showProgressBar() {
        progressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgressBar() {
        progressBar.setVisibility(View.GONE);
    }

}
