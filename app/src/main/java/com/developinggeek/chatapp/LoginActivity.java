package com.developinggeek.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.iid.FirebaseInstanceId;

public class LoginActivity extends AppCompatActivity
{

    private Toolbar mToolbar;
    private TextInputLayout edt_pass;
    private TextInputLayout edt_email;
    private Button btn_login;
    private ProgressDialog loginProgress;
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mToolbar = (Toolbar)findViewById(R.id.login_page_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Login");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loginProgress = new ProgressDialog(this);

        mAuth = FirebaseAuth.getInstance();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        edt_email = (TextInputLayout) findViewById(R.id.login_edt_email);
        edt_pass = (TextInputLayout) findViewById(R.id.login_edt_password);
        btn_login = (Button) findViewById(R.id.login_btn_login);

        btn_login.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                String email = edt_email.getEditText().getText().toString();
                String password = edt_pass.getEditText().getText().toString();

                if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password))
                {
                    login_user(email , password);
                }

            }
        });

    }

    private void login_user(String email, String password)
    {
      mAuth.signInWithEmailAndPassword(email , password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
          @Override
          public void onComplete(@NonNull Task<AuthResult> task)
          {
             if(task.isSuccessful())
             {
                 String currentUid = mAuth.getCurrentUser().getUid();
                 String tokenId = FirebaseInstanceId.getInstance().getToken();

                 mDatabase.child(currentUid).child("device_token").setValue(tokenId).addOnSuccessListener(new OnSuccessListener<Void>() {
                     @Override
                     public void onSuccess(Void aVoid) {

                     }
                 });

                 loginProgress.dismiss();
                 Intent mainIntent = new Intent(LoginActivity.this , MainActivity.class);
                 mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                 startActivity(mainIntent);
             }
              else
             {
                 loginProgress.hide();
                 Toast.makeText(LoginActivity.this, "could not sign in", Toast.LENGTH_SHORT).show();
             }
          }
      });

    }

}
