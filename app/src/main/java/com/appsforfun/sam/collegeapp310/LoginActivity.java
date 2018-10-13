package com.appsforfun.sam.collegeapp310;

import android.app.Dialog;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.androidadvance.topsnackbar.TSnackbar;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private Button login, signup, forgotPass;
    private TextInputEditText edtEmail, edtPassword, recoveryEmail;
    private TextInputLayout tilPassword;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private ProgressBar progressBar;
    private String prEmail;
    private RelativeLayout relativeLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        setContentView(R.layout.activity_login);

        checkUserState();

        if (getIntent().getBooleanExtra("EXIT", false))
        {
            finish();
        }

        edtEmail = findViewById(R.id.edt_email);
        edtPassword = findViewById(R.id.edt_password);
        tilPassword = findViewById(R.id.til_password);
        progressBar = findViewById(R.id.login_progress);
        relativeLayout = findViewById(R.id.login_layout);

        login = findViewById(R.id.btn_login);
        //login.setBackgroundDrawable(getDrawable2(R.drawable.dialog_btn_next));
        //login.setClickable(true);
        //login.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) login.getBackground()));
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    loginMethod(email,password);
                }
                else {
                    if (TextUtils.isEmpty(email)){
                        edtEmail.setError("Invalid email");
                    }
                    if (TextUtils.isEmpty(password)){
                        tilPassword.setError("Invalid password");
                    }
                }
            }
        });

        signup = findViewById(R.id.btn_signup);
        //signup.setBackgroundDrawable(getDrawable2(R.drawable.signup_button));
        //signup.setClickable(true);
        //signup.setOnTouchListener(new DrawableHotspotTouch((LollipopDrawable) signup.getBackground()));
        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = edtEmail.getText().toString().trim();
                String password = edtPassword.getText().toString().trim();
                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)){
                    signupMethod(email,password);
                }
                else {
                    if (TextUtils.isEmpty(email)){
                        edtEmail.setError("Invalid email");
                    }
                    if (TextUtils.isEmpty(password)){
                        tilPassword.setError("Invalid password");
                    }
                }
            }
        });

        forgotPass = findViewById(R.id.btn_forgotPassword);
        forgotPass.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                iniPassRecoveryDialog();
            }
        });
    }

    private void checkUserState(){
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null){
                    Intent intent = new Intent(LoginActivity.this, HomeActivity.class);
                    startActivity(intent);
                }
            }
        };
    }

    private void loginMethod(String email, String password){
        progressBar.setVisibility(View.VISIBLE);
        mAuth.signInWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    edtEmail.setText("");
                    edtPassword.setText("");
                }
            }
        });

        
    }

    private void signupMethod(String email, String password){
        progressBar.setVisibility(View.VISIBLE);
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (!task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    edtEmail.setText("");
                    edtPassword.setText("");
                }
            }
        });
    }

    private void iniPassRecoveryDialog(){
        final Dialog dialog = new Dialog(LoginActivity.this);
        dialog.setContentView(R.layout.dialog_forgot_password);
        recoveryEmail = dialog.findViewById(R.id.edt_recovery_mail);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                prEmail = recoveryEmail.getText().toString();
                if (!TextUtils.isEmpty(prEmail)){
                    dialog.dismiss();
                    sendPasswordRecoveryMail(prEmail);
                }
                else {
                    recoveryEmail.setError("enter your email");
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void sendPasswordRecoveryMail(final String email){
        progressBar.setVisibility(View.VISIBLE);
        mAuth.sendPasswordResetEmail(email).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    progressBar.setVisibility(View.GONE);
                    showSnackBar("Password recovery email is sent to email " + email, android.R.color.holo_green_dark);
                }
                else {
                    progressBar.setVisibility(View.GONE);
                    showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                }
            }
        });
    }

    private void showSnackBar(String msg, int color){
        TSnackbar snackbar = TSnackbar.make(relativeLayout, msg, TSnackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(LoginActivity.this, android.R.color.white));
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(ContextCompat.getColor(LoginActivity.this, R.color.colorPrimaryDark));
        TextView textView = snackView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(LoginActivity.this, color));
        snackbar.show();
    }

    /*public Drawable getDrawable2(int id){
        return LollipopDrawablesCompat.getDrawable(getResources(), id, getTheme());
    }*/

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthStateListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        if(mAuthStateListener != null){
            mAuth.removeAuthStateListener(mAuthStateListener);
        }
    }
}
