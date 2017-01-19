package com.app.blog.firebase.blogappfirebase_based;


import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;

class AddPostActivity extends AppCompatActivity {
    private static final int GALLERY_REQUEST = 1;
    EditText et_postDescription;
    EditText et_postTiltle;
    Uri selectedImgUri;
    ImageButton ib_selectImg;
    private ProgressDialog mProgress;
    private StorageReference mStorageRef;
    private DatabaseReference mDatabase;
    private DatabaseReference users_db;
    private FirebaseUser mUser;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_post);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();
        users_db = mDatabase.child("users");
        mUser = mAuth.getCurrentUser();
        mStorageRef = FirebaseStorage.getInstance().getReference();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        ib_selectImg = (ImageButton) findViewById(R.id.ib_select_post_img);
        et_postDescription = (EditText) findViewById(R.id.et_post_description);
        et_postTiltle = (EditText) findViewById(R.id.et_post_title);
        Button b_add_post = (Button) findViewById(R.id.b_add_post);
        mProgress = new ProgressDialog(this);
        ib_selectImg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });

        b_add_post.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startPosting();
            }
        });
    }

    private void startPosting() {

        final String postTitle = et_postTiltle.getText().toString().trim();
        final String postDescription = et_postDescription.getText().toString().trim();
        if (!TextUtils.isEmpty(postTitle) && !TextUtils.isEmpty(postDescription) && selectedImgUri != null) {
            mProgress.setMessage("posting to App Blog..");
            mProgress.show();
            final String year, month, day;
            year = getCurrentYear();
            month = getCurrentMonth();
            day = getCurrentDay();
            final String postTime = getTimeNow();
            final String fileName = selectedImgUri.getLastPathSegment() + postTime;
            StorageReference filePath = mStorageRef.child("blog")
                    .child("post_images")
                    .child(year)
                    .child(month)
                    .child(day)
                    .child(fileName);
            filePath.putFile(selectedImgUri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    final Uri downloadUrl = taskSnapshot.getDownloadUrl();
                    mProgress.dismiss();

                    users_db.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(DataSnapshot dataSnapshot) {
                            DatabaseReference db = mDatabase.child("blog")
                                    .child("posts")
                                    .push();
                            db.child("post_img").setValue(downloadUrl);
                            db.child("post_title").setValue(postTitle);
                            db.child("post_description").setValue(postDescription);
                            db.child("userId").setValue(mUser.getUid());
                            db.child("post_date").setValue(getCurrentYear()+"/"+getCurrentMonth()+"/"+getCurrentDay());
                            db.child("post_time").setValue(getFTimeNow());
                            db.child("userName").setValue(dataSnapshot.child("name")).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        startActivity(new Intent(AddPostActivity.this, MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                                    } else {
                                        Toast.makeText(AddPostActivity.this, "Failed adding post!", Toast.LENGTH_SHORT).show();
                                    }
                                }

                            });
                        }

                        @Override
                        public void onCancelled(DatabaseError databaseError) {

                        }
                    });

                }
            });
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            selectedImgUri = data.getData();
            ib_selectImg.setImageURI(selectedImgUri);

        }
    }

    public String getCurrentYear() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("YYYY", java.util.Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    public String getCurrentMonth() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("MMM", java.util.Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    public String getCurrentDay() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("dd", java.util.Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    public String getTimeNow() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("hh-mm-ss", java.util.Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

    public String getFTimeNow() {
        Calendar calendar = Calendar.getInstance();
        SimpleDateFormat mdformat = new SimpleDateFormat("hh:mm:ss", java.util.Locale.getDefault());
        return mdformat.format(calendar.getTime());
    }

}
