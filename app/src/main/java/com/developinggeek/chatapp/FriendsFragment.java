package com.developinggeek.chatapp;


import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;


public class FriendsFragment extends Fragment
{

    RecyclerView mFriendsList;

    DatabaseReference mFriendDatabase;
    DatabaseReference mUsersDatabase;

    FirebaseAuth mAuth;

    String mCurrent_user_id;

    View mMainView;


    public FriendsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        mMainView= inflater.inflate(R.layout.fragment_friends, container, false);

        mFriendsList=(RecyclerView) mMainView.findViewById(R.id.friends_list);
        mAuth=FirebaseAuth.getInstance();

        mCurrent_user_id=mAuth.getCurrentUser().getUid();

        mFriendDatabase= FirebaseDatabase.getInstance().getReference().child("Friends").child(mCurrent_user_id);
        mFriendDatabase.keepSynced(true);
        mUsersDatabase=FirebaseDatabase.getInstance().getReference().child("Users");

        mUsersDatabase.keepSynced(true);
        mFriendsList.setHasFixedSize(true);
        mFriendsList.setLayoutManager(new LinearLayoutManager(getContext()));

        return  mMainView;
    }

    @Override
    public void onStart() {
        super.onStart();

        FirebaseRecyclerAdapter<Friends,FriendsViewHolder> friendsRecyclerViewAdapter=new FirebaseRecyclerAdapter<Friends, FriendsViewHolder>(
                Friends.class,
                R.layout.user_single_layout,
                FriendsViewHolder.class,
                mFriendDatabase
        ) {
            @Override
            protected void populateViewHolder(final FriendsViewHolder friendsViewHolder, Friends friends, int position) {

                friendsViewHolder.setDate(friends.getDate());

                final String list_user_id=getRef(position).getKey();

                mUsersDatabase.child(list_user_id).addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        final String usernme=dataSnapshot.child("name").getValue().toString();
                        String userThumb=dataSnapshot.child("thumb_image").getValue().toString();

                        if(dataSnapshot.hasChild("online"))
                        {
                            String userOnline =  dataSnapshot.child("online").getValue().toString();
                            friendsViewHolder.setUserOnline(userOnline);
                        }


                        friendsViewHolder.setNmae(usernme);
                        friendsViewHolder.setUserImage(userThumb,getContext());

                        friendsViewHolder.mView.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v)
                            {

                                CharSequence options[] = new CharSequence[]{"open profile" , "send message"};

                                AlertDialog.Builder dialog = new AlertDialog.Builder(getContext());
                                dialog.setTitle("select options");
                                dialog.setItems(options, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialogInterface, int i)
                                    {

                                        if(i==0)
                                        {
                                            Intent profileIntent = new Intent(getContext(),ProfileActivity.class);
                                            profileIntent.putExtra("user_id" , list_user_id);
                                            startActivity(profileIntent);
                                        }

                                        if(i==1)
                                        {
                                            Intent chatIntent = new Intent(getContext(),ChatActivity.class);
                                            chatIntent.putExtra("user_id" , list_user_id);
                                            chatIntent.putExtra("user_name" , usernme);
                                            startActivity(chatIntent);
                                        }

                                    }
                                });

                                dialog.show();
                            }
                        });


                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {

                    }
                });
            }

        };
        mFriendsList.setAdapter(friendsRecyclerViewAdapter);
    }

    public static class FriendsViewHolder extends RecyclerView.ViewHolder {

        View mView;
        public FriendsViewHolder(View itemView) {
            super(itemView);

            mView=itemView;
        }

        public void setDate(String date)
        {
            TextView userNameView=(TextView) mView.findViewById(R.id.user_status);
            userNameView.setText(date);
        }

        public void setNmae(String name)
        {
            TextView userNameView=(TextView) mView.findViewById(R.id.user_name);
            userNameView.setText(name);
        }

        public void setUserImage(String thumb_image, Context context)
        {
            CircleImageView userImageView=(CircleImageView) mView.findViewById(R.id.user_image_view);
            Picasso.with(context).load(thumb_image).placeholder(R.drawable.user_default).into(userImageView);

        }

        public void setUserOnline(String userOnline)
        {
            ImageView onlineImg = (ImageView)mView.findViewById(R.id.user_online_icon);

            if(userOnline.equals("true"))
            {
                onlineImg.setVisibility(View.VISIBLE);
            }
            else
            {
               onlineImg.setVisibility(View.INVISIBLE);
            }

        }

    }

}