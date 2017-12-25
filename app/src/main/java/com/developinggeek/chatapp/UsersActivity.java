package com.developinggeek.chatapp;

import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class UsersActivity extends AppCompatActivity {

    private RecyclerView usersList;
    private Toolbar mToolbar;
    private DatabaseReference mDatabase;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_users);

        mToolbar = (Toolbar) findViewById(R.id.users_toolbar);
        usersList = (RecyclerView) findViewById(R.id.users_list);

        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("All Users");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users");

        usersList.setHasFixedSize(true);
        usersList.setLayoutManager(new LinearLayoutManager(this));

    }

    @Override
    protected void onStart()
    {
        super.onStart();

        FirebaseRecyclerAdapter<Users , UsersViewHolder> firebaseRecyclerAdapter = new FirebaseRecyclerAdapter<Users, UsersViewHolder>
                (Users.class , R.layout.user_single_layout , UsersViewHolder.class , mDatabase ) {
            @Override
            protected void populateViewHolder(UsersViewHolder userViewHolder, Users users, int position)
            {
                userViewHolder.setName(users.getName());
                userViewHolder.setStatus(users.getStatus());
                userViewHolder.setThmbImage(users.getThumb_image() , getApplicationContext());

                final String user_id = getRef(position).getKey();

                userViewHolder.mView.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        Intent profileIntent = new Intent(UsersActivity.this,ProfileActivity.class);
                        profileIntent.putExtra("user_id" , user_id);
                        startActivity(profileIntent);
                    }
                });
            }
        };

        usersList.setAdapter(firebaseRecyclerAdapter);
    }

    public static class UsersViewHolder extends RecyclerView.ViewHolder
    {
        View mView;

        public UsersViewHolder(View itemView)
        {
            super(itemView);
            mView = itemView;
        }

        public void setName(String status)
        {
            TextView tvUserName = (TextView) itemView.findViewById(R.id.user_name);
            tvUserName.setText(status);
        }

        public void setStatus(String name)
        {
            TextView tvUserStatus = (TextView) itemView.findViewById(R.id.user_status);
            tvUserStatus.setText(name);
        }

        public void setThmbImage(String thumbImage , Context c)
        {
            CircleImageView userThumbImage = (CircleImageView) itemView.findViewById(R.id.user_image_view);
            Picasso.with(c).load(thumbImage).placeholder(R.drawable.user_default).into(userThumbImage);
        }

    }

}
