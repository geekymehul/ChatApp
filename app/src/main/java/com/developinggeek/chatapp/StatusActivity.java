package com.developinggeek.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class StatusActivity extends AppCompatActivity
{
    private TextInputLayout edt_status;
    private Button btn_save;
    private Toolbar mToolbar;
    private FirebaseUser current_user;
    private DatabaseReference mDatabase;
    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_status);

        mToolbar = (Toolbar) findViewById(R.id.status_toolbar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Status");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        edt_status = (TextInputLayout) findViewById(R.id.status_edt_status);
        btn_save = (Button) findViewById(R.id.status_btn_change);

        String status_value = getIntent().getStringExtra("status");
        edt_status.getEditText().setText(status_value);

        current_user = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = current_user.getUid();
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);

        btn_save.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
              mProgress = new ProgressDialog(StatusActivity.this);
              mProgress.setTitle("Saving the Status");
              mProgress.setMessage("please wait while we save our status");
              mProgress.show();

              String newStatus = edt_status.getEditText().getText().toString();
              mDatabase.child("status").setValue(newStatus).addOnCompleteListener(new OnCompleteListener<Void>()
              {
                  @Override
                  public void onComplete(@NonNull Task<Void> task)
                  {
                     if (task.isSuccessful())
                     {
                       mProgress.dismiss();
                     }
                     else
                     {
                         Toast.makeText(StatusActivity.this, "there was some error in saving the status",
                                        Toast.LENGTH_SHORT).show();
                     }
                  }
              });
            }

        });

    }
}
