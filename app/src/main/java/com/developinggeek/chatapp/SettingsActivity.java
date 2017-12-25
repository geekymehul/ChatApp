package com.developinggeek.chatapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Callback;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import de.hdodenhof.circleimageview.CircleImageView;
import id.zelory.compressor.Compressor;


public class SettingsActivity extends AppCompatActivity
{
    private TextView tv_name , tv_status;
    private Button btn_status , btn_image;
    private DatabaseReference mDatabase;
    private FirebaseUser currentUser;
    private ProgressDialog mProgress;
    private CircleImageView userImage;
    private StorageReference mStorage;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        tv_name = (TextView) findViewById(R.id.setting_txt_name);
        tv_status = (TextView) findViewById(R.id.setting_txt_status);
        btn_image = (Button) findViewById(R.id.setting_btn_img);
        btn_status = (Button) findViewById(R.id.setting_btn_status);
        userImage = (CircleImageView) findViewById(R.id.setting_user_img);

        mStorage = FirebaseStorage.getInstance().getReference();

        currentUser = FirebaseAuth.getInstance().getCurrentUser();
        String current_uid = currentUser.getUid();

        mDatabase = FirebaseDatabase.getInstance().getReference().child("Users").child(current_uid);
        mDatabase.keepSynced(true);

        mDatabase.addValueEventListener(new ValueEventListener()
        {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot)
            {
                String name = dataSnapshot.child("name").getValue().toString();
                String status = dataSnapshot.child("status").getValue().toString();
                final String image = dataSnapshot.child("image").getValue().toString();
                String thumb_image = dataSnapshot.child("thumb_image").getValue().toString();

                if(!image.equals("default")) {

                    Picasso.with(SettingsActivity.this).load(image).networkPolicy(NetworkPolicy.OFFLINE)
                            .placeholder(R.drawable.user_default).into(userImage, new Callback() {
                        @Override
                        public void onSuccess() {

                        }

                        @Override
                        public void onError() {
                            Picasso.with(SettingsActivity.this).load(image).placeholder(R.drawable.user_default).into(userImage);
                        }
                    });

                }

                tv_name.setText(name);
                tv_status.setText(status);
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        btn_status.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

                Intent statusIntent = new Intent(SettingsActivity.this , StatusActivity.class);
                String oldStatus = tv_status.getText().toString();
                statusIntent.putExtra("status",oldStatus);
                startActivity(statusIntent);

            }
        });

        btn_image.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

               /* Intent galleryIntent = new Intent();
                galleryIntent.setType("image");
                galleryIntent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(galleryIntent,"CHOOSE IMAGE"),1);
*/
                CropImage.activity()
                        .setGuidelines(CropImageView.Guidelines.ON)
                        .start(SettingsActivity.this);
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if(requestCode == 1 && resultCode == RESULT_OK )
        {
            Uri imageUri = data.getData();

            CropImage.activity(imageUri)
                    .setMinCropWindowSize(500,500)
                    .setAspectRatio(1,1)
                    .start(this);

        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK)
            {
                mProgress = new ProgressDialog(SettingsActivity.this);
                mProgress.setCanceledOnTouchOutside(false);
                mProgress.setTitle("Updating Image");
                mProgress.setMessage("Please wit while we update your image");
                mProgress.show();

                Uri resultUri = result.getUri();

                final File thumbFile = new File(resultUri.getPath());

                String current_user_id = currentUser.getUid();


                Bitmap thumbBitmap = new Compressor(this)
                        .setMaxHeight(200)
                        .setMaxWidth(200)
                        .setQuality(50)
                        .compressToBitmap(thumbFile);

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                thumbBitmap.compress(Bitmap.CompressFormat.JPEG, 50, baos);
                final byte[] thumb_byte = baos.toByteArray();

                StorageReference fileStorage = mStorage.child("profile_images").child(current_user_id + ".jpg");
                final StorageReference thumbFileStorage = mStorage.child("profile_images").child("thumb").child(current_user_id + ".jpg");

                fileStorage.putFile(resultUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                {
                    @Override
                    public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task)
                    {
                        if(task.isSuccessful())
                        {
                            @SuppressWarnings("VisibleForTests")
                            final String download_url = task.getResult().getDownloadUrl().toString();

                            UploadTask uploadTask = thumbFileStorage.putBytes(thumb_byte);
                            uploadTask.addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>()
                            {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> thumb_task)
                                {
                                    @SuppressWarnings("VisibleForTests")
                                    String thumb_downloadUrl = thumb_task.getResult().getDownloadUrl().toString();

                                    if(thumb_task.isSuccessful())
                                    {
                                        Map updateHashMap = new HashMap<String, String>();
                                        updateHashMap.put("image",download_url);
                                        updateHashMap.put("thumb_image",thumb_downloadUrl);

                                        mDatabase.updateChildren(updateHashMap).addOnCompleteListener(new OnCompleteListener<Void>()
                                        {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task)
                                            {
                                                if(task.isSuccessful())
                                                {
                                                    mProgress.dismiss();
                                                }

                                            }
                                        });
                                    }
                                    else
                                    {
                                        Toast.makeText(SettingsActivity.this,
                                                "error in uploading thumbnail", Toast.LENGTH_LONG).show();

                                    }
                                }
                            });

                        }
                        else
                        {
                            Toast.makeText(SettingsActivity.this, "unSuccessful", Toast.LENGTH_LONG).show();
                            mProgress.dismiss();
                        }
                    }
                });
            }
            else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE)
            {
                Exception error = result.getError();
            }
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

}
