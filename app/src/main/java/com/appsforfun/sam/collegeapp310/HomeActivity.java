package com.appsforfun.sam.collegeapp310;

import android.app.Dialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.androidadvance.topsnackbar.TSnackbar;
import com.appsforfun.sam.collegeapp310.adapters.ViewPagerAdapter;
import com.appsforfun.sam.collegeapp310.fragments.Friday;
import com.appsforfun.sam.collegeapp310.fragments.Monday;
import com.appsforfun.sam.collegeapp310.fragments.Thursday;
import com.appsforfun.sam.collegeapp310.fragments.Tuesday;
import com.appsforfun.sam.collegeapp310.fragments.Wednesday;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.FirebaseDatabase;

public class HomeActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthStateListener;
    private Toolbar mToolbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private FirebaseDatabase database;
    private FirebaseUser user;
    private RelativeLayout relativeLayout, relativeLayout1;
    private TextView loadingMsg;

    private TextInputEditText reEnterPassword, edtNewPassword, edtReNewPassword, edtOldPassword;
    private TextInputLayout til_Pass1, til_Pass2, til_Pass3;
    private String rePass, newPass, reNewPass, oldPass;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        checkUserState();
        mToolbar = findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar();
        viewPager = findViewById(R.id.viewPager);
        setupViewPager(viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(viewPager);
        database = FirebaseDatabase.getInstance();
        user = mAuth.getCurrentUser();
        relativeLayout = findViewById(R.id.rl_delete_view);
        relativeLayout1 = findViewById(R.id.relativeLayout1);
        loadingMsg = findViewById(R.id.label);
    }

    private void setupViewPager(ViewPager viewPager){
        ViewPagerAdapter adapter = new ViewPagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Monday(), "MON");
        adapter.addFragment(new Tuesday(), "TUE");
        adapter.addFragment(new Wednesday(), "WED");
        adapter.addFragment(new Thursday(), "THU");
        adapter.addFragment(new Friday(), "FRI");
        viewPager.setAdapter(adapter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.home_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.signOut){
            mAuth.signOut();
            return true;
        }
        else if (item.getItemId() == R.id.deleteAccount){
            iniDeleteAccDialog();
            return true;
        }
        else if (item.getItemId() == R.id.resetPassword){
            iniResetPassDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkUserState(){
        mAuth = FirebaseAuth.getInstance();
        mAuthStateListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user == null){
                    finish();
                }
            }
        };
    }

    private void iniDeleteAccDialog(){

        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.dialog_reauth_password);
        reEnterPassword = dialog.findViewById(R.id.edt_reEnter_password);
        til_Pass1 = dialog.findViewById(R.id.til_password);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rePass = reEnterPassword.getText().toString();
                if (!TextUtils.isEmpty(rePass)){
                    deleteAccount(rePass);
                    dialog.dismiss();
                }
                else {
                    til_Pass1.setError("please enter your password");
                }
            }
        });
        ImageButton btnPrevious = dialog.findViewById(R.id.btn_previous);
        btnPrevious.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                rePass = "";
                dialog.dismiss();
            }
        });
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();

    }

    private void deleteAccount(String password){
        loadingMsg.setText("Deleting...");
        relativeLayout.setVisibility(View.VISIBLE);
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), password);
        mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    user.delete().addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (!task.isSuccessful()){
                                relativeLayout.setVisibility(View.GONE);
                                showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                            }
                            else {
                                database.getReference(user.getUid()).removeValue().addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        relativeLayout.setVisibility(View.GONE);
                                    }
                                });
                            }
                        }
                    });
                }
                else {
                    relativeLayout.setVisibility(View.GONE);
                    showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                }
            }
        });

    }

    private void iniResetPassDialog(){
        final Dialog dialog = new Dialog(HomeActivity.this);
        dialog.setContentView(R.layout.dialog_update_password);
        edtOldPassword = dialog.findViewById(R.id.edt_old_password);
        edtNewPassword = dialog.findViewById(R.id.edt_new_password);
        edtReNewPassword = dialog.findViewById(R.id.edt_renew_password);
        til_Pass1 = dialog.findViewById(R.id.til_password1);
        til_Pass2 = dialog.findViewById(R.id.til_password2);
        til_Pass3 = dialog.findViewById(R.id.til_password3);
        ImageButton btnNext = dialog.findViewById(R.id.btn_next);
        btnNext.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                oldPass = edtOldPassword.getText().toString();
                newPass = edtNewPassword.getText().toString();
                reNewPass = edtReNewPassword.getText().toString();
                if (!TextUtils.isEmpty(newPass) && !TextUtils.isEmpty(reNewPass) && !TextUtils.isEmpty(oldPass)){
                    if (newPass.equals(reNewPass)){
                        dialog.dismiss();
                        resetPassword(reNewPass, oldPass);
                    }
                    else {
                        Toast.makeText(HomeActivity.this, "password incorrect", Toast.LENGTH_SHORT).show();
                    }
                }
                else {
                   if (TextUtils.isEmpty(newPass)){
                       til_Pass2.setError(" ");
                   }
                   if (TextUtils.isEmpty(oldPass)){
                       til_Pass1.setError(" ");
                   }
                   if (TextUtils.isEmpty(reNewPass)){
                       til_Pass3.setError(" ");
                   }
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

    private void resetPassword(final String passwordNew, String passwordOld){
        loadingMsg.setText("Updating...");
        relativeLayout.setVisibility(View.VISIBLE);

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), passwordOld);
        mAuth.getCurrentUser().reauthenticate(credential).addOnCompleteListener(new OnCompleteListener<Void>() {
            @Override
            public void onComplete(@NonNull Task<Void> task) {
                if (task.isSuccessful()){
                    user.updatePassword(passwordNew).addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if (task.isSuccessful()){
                                mAuth.signOut();
                                relativeLayout.setVisibility(View.GONE);
                            }
                            else {
                                showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                                relativeLayout.setVisibility(View.GONE);
                            }
                        }
                    });
                }
                else {
                    relativeLayout.setVisibility(View.GONE);
                    showSnackBar(task.getException().getMessage(), android.R.color.holo_red_dark);
                }
            }
        });
    }

    private void showSnackBar(String msg, int color){
        TSnackbar snackbar = TSnackbar.make(relativeLayout1, msg, TSnackbar.LENGTH_LONG);
        snackbar.setActionTextColor(ContextCompat.getColor(HomeActivity.this, android.R.color.white));
        View snackView = snackbar.getView();
        snackView.setBackgroundColor(ContextCompat.getColor(HomeActivity.this, R.color.colorPrimaryDark));
        TextView textView = snackView.findViewById(com.androidadvance.topsnackbar.R.id.snackbar_text);
        textView.setTextColor(ContextCompat.getColor(HomeActivity.this, color));
        snackbar.show();
    }

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(HomeActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        intent.putExtra("EXIT", true);
        startActivity(intent);
    }

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

// TODO: 12-10-2018 logo
// TODO: 12-10-2018 arrange in hour order