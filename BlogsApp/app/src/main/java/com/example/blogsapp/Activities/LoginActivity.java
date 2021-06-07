package com.example.blogsapp.Activities;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.blogsapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText userMail,userPassword;
    private Button btnLogin;
    private ProgressBar loginProgress;
    private FirebaseAuth mAuth;
    private Intent HomeActivity;
    private ImageView loginPhoto;
    private TextView recoverPassword;
    private AlertDialog.Builder builder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        userMail = (EditText) findViewById(R.id.login_mail);
        userPassword = (EditText) findViewById(R.id.login_password);
        btnLogin = (Button) findViewById(R.id.loginBtn);
        loginProgress = (ProgressBar) findViewById(R.id.login_progress);
        recoverPassword = (TextView) findViewById(R.id.recoverPassword);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        mAuth = FirebaseAuth.getInstance();

        HomeActivity = new Intent(this, com.example.blogsapp.Activities.Home.class);

        loginPhoto = (ImageView) findViewById(R.id.login_photo);
        loginPhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                Intent register = new Intent(getApplicationContext(),RegisterActivity.class);
                startActivity(register);
            }
        });

        recoverPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                showRecoverPasswordDialog();
            }
        });

        loginProgress.setVisibility(View.INVISIBLE);

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                loginProgress.setVisibility(View.VISIBLE);
                btnLogin.setVisibility(View.INVISIBLE);

                final String mail = userMail.getText().toString();
                final String password = userPassword.getText().toString();

                if (mail.isEmpty() || password.isEmpty()) {
                    showMessage("Please Verify All Field");
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }
                else
                {
                    signIn(mail,password);
                }


            }
        });


    }


    //password recovery
    private void showRecoverPasswordDialog() {

        //set dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Recover Password");
        builder.setMessage("Enter Email To Received The Reset Link");
        //set layout
        LinearLayout linearLayout =  new LinearLayout(this);
        //set views in dialog
        final EditText emailEt = new EditText(this);
        emailEt.setHint("Enter Email");
        emailEt.setInputType(InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        emailEt.setMinEms(16);

        linearLayout.addView(emailEt);
        linearLayout.setPadding(10,10,10,10);

        builder.setView(linearLayout);

        //buttons recover
        builder.setPositiveButton("Recover", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                String recoverPass = emailEt.getText().toString().trim();
                if (TextUtils.isEmpty(recoverPass))
                {
                    showMessage("please provide email");
                    return;
                }
                beginRecovery(recoverPass);

            }
        }).setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {

                dialogInterface.dismiss();
            }
        });

        builder.create().show();
    }

    //password recovery
    private void beginRecovery(String recoverPass) {
        mAuth.sendPasswordResetEmail(recoverPass).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {

                if (task.isSuccessful())
                {
                    showMessage("Email Sent");
                }
                else
                {
                    showMessage("Failed");
                }

            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {

                showMessage(e.getMessage());
            }
        });
    }

    private void signIn(String mail, String password) {

        mAuth.signInWithEmailAndPassword(mail,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {

                if (task.isSuccessful()) {


                    if (mAuth.getCurrentUser().isEmailVerified())
                    {
//                        loginProgress.setVisibility(View.INVISIBLE);
//                        btnLogin.setVisibility(View.VISIBLE);
                        btnLogin.setVisibility(View.VISIBLE);
                        loginProgress.setVisibility(View.INVISIBLE);
                        updateUI();
                    }
                    else
                    {
                        showMessage("Please Verify Your Email id");
                        btnLogin.setVisibility(View.VISIBLE);
                        loginProgress.setVisibility(View.GONE);
                    }

                }
                else {
                    showMessage(task.getException().getMessage());
                    btnLogin.setVisibility(View.VISIBLE);
                    loginProgress.setVisibility(View.INVISIBLE);
                }

            }
        });
    }

    private void updateUI() {
        //Intent i = new Intent(LoginActivity.this,HomeActivity.class);
        startActivity(HomeActivity);
        finish();
    }

    private void showMessage(String text) {
        Toast.makeText(LoginActivity.this, text, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onStart() {
        super.onStart();

        FirebaseUser user = mAuth.getCurrentUser();

        if (user != null && user.isEmailVerified())
        {
            updateUI();
        }
    }
}