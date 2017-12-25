package com.developinggeek.chatapp;

import android.app.ProgressDialog;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class ProfileActivity extends AppCompatActivity
{

    private ImageView userImage;
    private TextView tv_name , tv_status , tv_info;
    private Button btn_request , btn_decline;
    private DatabaseReference mDatabase , mFriendReqDatabase , mFriendDatabase , mNotificationDatabase;
    private ProgressDialog mProgress;
    private String currentState;
    private FirebaseUser currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        //getting the userId of the user
        final String userId = getIntent().getStringExtra("user_id");

        //typecasting
        tv_name = (TextView)findViewById(R.id.profile_user_name);
        tv_status = (TextView)findViewById(R.id.profile_user_status);
        tv_info = (TextView)findViewById(R.id.profile_user_info);
        userImage = (ImageView)findViewById(R.id.profile_user_img);
        btn_request = (Button)findViewById(R.id.profile_user_btn_request);
        btn_decline = (Button) findViewById(R.id.profile_user_btn_decline);

        //initialising the variable
        currentState = "not_friends";

        btn_decline.setVisibility(View.INVISIBLE);
        btn_decline.setEnabled(false);

        //initialising progressDialog
        mProgress = new ProgressDialog(ProfileActivity.this);
        mProgress.setTitle("Loading user Data");
        mProgress.setMessage("please wait while we load the data");
        mProgress.setCanceledOnTouchOutside(false);
        mProgress.show();

        //initialising the firebase
        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(userId);
        mFriendReqDatabase = FirebaseDatabase.getInstance().getReference().child("Friends_req");
        mFriendDatabase = FirebaseDatabase.getInstance().getReference().child("Friends");
        mNotificationDatabase = FirebaseDatabase.getInstance().getReference().child("Notifications");
        currentUser = FirebaseAuth.getInstance().getCurrentUser();

        //fetching the data
        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                String image = dataSnapshot.child("image").getValue().toString();

                tv_name.setText(name);
                tv_status.setText(status);
                Picasso.with(ProfileActivity.this).load(image).placeholder(R.drawable.user_default).into(userImage);

                //for friends request feature
                mFriendReqDatabase.child(currentUser.getUid()).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot)
                    {
                      if(dataSnapshot.hasChild(userId))
                      {
                         String req_type =  dataSnapshot.child(userId).child("request_type").getValue().toString();

                         if(req_type.equals("received"))
                         {
                           currentState = "req_received";
                           btn_request.setText("Accept Friend Request");

                             btn_decline.setVisibility(View.VISIBLE);
                             btn_decline.setEnabled(true);
                         }

                         else if(req_type.equals("send"))
                         {
                             currentState = "req_send";

                             btn_decline.setVisibility(View.INVISIBLE);
                             btn_decline.setEnabled(false);
                         }
                      }

                      else
                      {
                          mFriendDatabase.child(currentUser.getUid()).addValueEventListener(new ValueEventListener()
                          {
                              @Override
                              public void onDataChange(DataSnapshot dataSnapshot)
                              {
                                 if(dataSnapshot.hasChild(userId))
                                 {
                                     btn_request.setEnabled(true);
                                     btn_request.setText("Unfriend this person");
                                     currentState = "not_friends";

                                     btn_decline.setVisibility(View.INVISIBLE);
                                     btn_decline.setEnabled(false);
                                 }
                              }

                              @Override
                              public void onCancelled(DatabaseError databaseError) {

                              }
                          });

                      }

                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });

                mProgress.dismiss();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        //for sending friend request
        btn_request.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

                //setting it as cancel request button
                btn_request.setEnabled(false);

                if(currentState.equals("not_friends"))
              {
                //saving the friend request in database
                  mFriendReqDatabase.child(currentUser.getUid()).child(userId).child("request_type")
                        .setValue("send").addOnCompleteListener(new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task)
                    {
                      if(task.isSuccessful())
                      {
                        //saving the send friend request in other users database
                          mFriendReqDatabase.child(userId).child(currentUser.getUid()).child("request_type")
                                 .setValue("received").addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid)
                             {

                                 HashMap<String , String> notifyData = new HashMap<String, String>();
                                 notifyData.put("from",currentUser.getUid());
                                 notifyData.put("type","request");

                                 mNotificationDatabase.child(userId).push().setValue(notifyData)
                                         .addOnSuccessListener(new OnSuccessListener<Void>() {
                                     @Override
                                     public void onSuccess(Void aVoid) {

                                         btn_request.setText("Cancel Friend Request");
                                         currentState = "req_send";

                                         btn_decline.setVisibility(View.INVISIBLE);
                                         btn_decline.setEnabled(false);

                                         Toast.makeText(ProfileActivity.this, "Friend Request Send", Toast.LENGTH_SHORT).show();
                                     }
                                 });


                             }
                         });
                      }
                      else
                      {
                          Toast.makeText(ProfileActivity.this, "Failed Sending Friend Request", Toast.LENGTH_LONG).show();
                      }
                    }
                });
              }
                btn_request.setEnabled(true);

              //for canceling the request
              if(currentState.equals("req_send"))
              {
                  mFriendReqDatabase.child(currentUser.getUid()).child(userId)
                                  .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                      @Override
                      public void onSuccess(Void aVoid)
                      {
                          mFriendReqDatabase.child(userId).child(currentUser.getUid())
                                         .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                             @Override
                             public void onSuccess(Void aVoid)
                             {
                                 btn_request.setEnabled(true);
                                 btn_request.setText("Send Friend Request");
                                 currentState = "not_friends";

                                 btn_decline.setVisibility(View.INVISIBLE);
                                 btn_decline.setEnabled(false);

                                 Toast.makeText(ProfileActivity.this, "Friend Request Cancelled", Toast.LENGTH_SHORT).show();
                             }
                         });
                      }
                  });
              }

              if(currentState.equals("req_received"))
              {
                  final String currentDate = DateFormat.getDateTimeInstance().format(new Date());

                  mFriendDatabase.child(currentUser.getUid()).child(userId).child("date")
                                  .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>()
                  {
                      @Override
                      public void onSuccess(Void aVoid)
                      {
                          mFriendDatabase.child(userId).child(currentUser.getUid()).child("date")
                                  .setValue(currentDate).addOnSuccessListener(new OnSuccessListener<Void>() {
                              @Override
                              public void onSuccess(Void aVoid)
                              {
                                  mFriendReqDatabase.child(currentUser.getUid()).child(userId)
                                          .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                      @Override
                                      public void onSuccess(Void aVoid)
                                      {
                                          mFriendReqDatabase.child(userId).child(currentUser.getUid())
                                                  .removeValue().addOnSuccessListener(new OnSuccessListener<Void>() {
                                              @Override
                                              public void onSuccess(Void aVoid)
                                              {
                                                  btn_request.setEnabled(true);
                                                  btn_request.setText("Unfriend this person");
                                                  currentState = "not_friends";

                                                  btn_decline.setVisibility(View.INVISIBLE);
                                                  btn_decline.setEnabled(false);
                                              }
                                          });
                                      }
                                  });
                              }
                          });


                      }
                  });

              }

            }
        });

    }
}
