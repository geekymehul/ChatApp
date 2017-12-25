package com.developinggeek.chatapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ServerValue;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatActivity extends AppCompatActivity
{

    private String mUserChat , mUserName;
    private Toolbar mToolbar;
    private DatabaseReference mRootRef;
    private TextView mTitleView , mLastSeenView;
    private CircleImageView mProfileImage;
    private FirebaseAuth mAuth;
    private String mCurrentUid;
    private ImageView mAdd , mSend;
    private EditText mMsg;
    private RecyclerView mMessagesList;
    private final List<Messages> messagesList = new ArrayList<>();
    private LinearLayoutManager mLinearLayout;
    private MessageAdapter mAdapter;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat);

        mRootRef = FirebaseDatabase.getInstance().getReference();

        mAuth = FirebaseAuth.getInstance();

        mCurrentUid = mAuth.getCurrentUser().getUid();

        mUserChat = getIntent().getStringExtra("user_id");
        mUserName = getIntent().getStringExtra("user_name");

        mToolbar = (Toolbar)findViewById(R.id.chat_toolbar);
        setSupportActionBar(mToolbar);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);
        actionBar.setDisplayShowCustomEnabled(true);

        getSupportActionBar().setTitle(mUserName);

        LayoutInflater inflater = (LayoutInflater)this.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View action_bar_view = inflater.inflate(R.layout.chat_custom_bar , null);

        actionBar.setCustomView(action_bar_view);

        mLastSeenView = (TextView)findViewById(R.id.custom_bar_seen);
        mTitleView = (TextView)findViewById(R.id.custom_bar_name);
        mProfileImage = (CircleImageView)findViewById(R.id.custon_bar_img);

        mTitleView.setText(mUserName);

        mAdd = (ImageView)findViewById(R.id.chat_add);
        mMsg = (EditText)findViewById(R.id.chat_txt_msg);
        mSend = (ImageView)findViewById(R.id.chat_send);

        mAdapter = new MessageAdapter(messagesList);

        mMessagesList = (RecyclerView)findViewById(R.id.messages_list);

        mLinearLayout = new LinearLayoutManager(this);

        mMessagesList.setHasFixedSize(true);
        mMessagesList.setLayoutManager(mLinearLayout);
        mMessagesList.setAdapter(mAdapter);

        mRootRef.child("Users").child(mUserChat).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
              String online = dataSnapshot.child("online").getValue().toString();
              String image = dataSnapshot.child("image").getValue().toString();

              if(online.equals("true"))
              {
                  mLastSeenView.setText("Online");
              }
              else
              {
                  GetTime getTime = new GetTime();

                  long lastTime = Long.parseLong(online);

                  String lastSeenTime = getTime.getTimeAgo(lastTime,getApplicationContext());

                  mLastSeenView.setText(lastSeenTime);
              }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        mRootRef.child("Chat").child(mCurrentUid).addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {

                if(!dataSnapshot.hasChild(mUserChat))
                {
                    Map chatAddMap = new HashMap();
                    chatAddMap.put("seen",false);
                    chatAddMap.put("time", ServerValue.TIMESTAMP);

                    Map chatUserMap = new HashMap();
                    chatUserMap.put("Chat/" + mCurrentUid + "/" + mUserChat , chatAddMap);
                    chatUserMap.put("Chat/" + mUserChat  + "/" + mCurrentUid , chatAddMap);

                    mRootRef.updateChildren(chatUserMap, new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                            if(databaseError != null) {

                                Log.d("CHAT_LOG" , databaseError.getMessage().toString());

                            }

                        }
                    });
                }

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
        
        mSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v) {
        
                sendMessage();
                
            }
        });

    }

    private void loadMessages()
    {
        mRootRef.child(mCurrentUid).child(mUserChat).addChildEventListener(new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });
    }

    private void sendMessage()
    {
        String message = mMsg.getText().toString();

        if(!TextUtils.isEmpty(message))
        {

            String current_user_ref = "messages/" + mCurrentUid + "/" + mUserChat;
            String chat_user_ref = "messages/" + mUserChat  + "/" + mCurrentUid;

            DatabaseReference user_message_push = mRootRef.child("messages").child(mCurrentUid)
                                                  .child(mUserChat).push();

            String pushId = user_message_push.getKey();

            Map messageMap = new HashMap();
            messageMap.put("message" , message);
            messageMap.put("seen" , false);
            messageMap.put("type" , "text");
            messageMap.put("time" , ServerValue.TIMESTAMP);

            Map messageUserMap = new HashMap();
            messageUserMap.put(current_user_ref + "/" + pushId , messageMap);
            messageUserMap.put(chat_user_ref + "/" + pushId , messageMap);

            mRootRef.updateChildren(messageUserMap, new DatabaseReference.CompletionListener() {
                @Override
                public void onComplete(DatabaseError databaseError, DatabaseReference databaseReference) {

                }
            });

        }

    }

}
