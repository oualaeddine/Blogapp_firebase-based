package com.app.blog.firebase.blogappfirebase_based;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

import java.io.File;

public class ProfileSetupActivity extends AppCompatActivity {


    private static final String TAG = "ProfileSetupActivity";
    private EditText et_ps_firstName, et_ps_lastName;
    private String firstName, lastName;
    private ImageButton ib_ps_profilePic;
    private DatabaseReference users_db;
    private StorageReference mStorageRef;
    private ProgressDialog mProgress;
    private static final int GALLERY_REQUEST = 1;
    private Uri profileImgUri;
    private String userId;
    private Uri downloadUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile_setup);

        users_db = FirebaseDatabase.getInstance().getReference().child("blog").child("users");
        users_db.keepSynced(true);

        FirebaseAuth mAuth = FirebaseAuth.getInstance();

        mStorageRef = FirebaseStorage.getInstance().getReference();

        userId = mAuth.getCurrentUser().getUid();

        mProgress = new ProgressDialog(this);

        et_ps_firstName = (EditText) findViewById(R.id.et_ps_firstName);
        et_ps_lastName = (EditText) findViewById(R.id.et_ps_lastName);
        Button b_setup = (Button) findViewById(R.id.b_setup);
        ib_ps_profilePic = (ImageButton) findViewById(R.id.ib_ps_profilePic);


        ib_ps_profilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
                galleryIntent.setType("image/*");
                startActivityForResult(galleryIntent, GALLERY_REQUEST);
            }
        });
        b_setup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startProfileSetup();
            }
        });
    }

    private void startProfileSetup() {
        firstName = et_ps_firstName.getText().toString().trim();
        lastName = et_ps_lastName.getText().toString().trim();

        if (validate()) {
            mProgress.setMessage("updating profile..");
            mProgress.show();

            Uri file = Uri.fromFile(new File(profileImgUri.getPath()));

            StorageReference profilePicRef = mStorageRef.child("blog").child("profile_pics").child(userId);
            profilePicRef.putFile(file)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            // Get a URL to the uploaded content
                            downloadUrl = taskSnapshot.getDownloadUrl();
                            users_db.child(userId).child("first_name").setValue(firstName);
                            users_db.child(userId).child("lasst_name").setValue(lastName);
                            users_db.child(userId).child("profile_pic").setValue(downloadUrl.toString());
                            mProgress.dismiss();
                            Toast.makeText(ProfileSetupActivity.this, "update succes!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(ProfileSetupActivity.this,ProfileSetupActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));

                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception exception) {
                            // Handle unsuccessful uploads
                            // ...
                        }
                    });


        }
    }

    public boolean validate() {
        boolean valid = true;

        //TODO: test if the user already exists in db
        if (firstName.isEmpty() || firstName.length() < 3) {
            et_ps_firstName.setError("First Name must be at least 3 characters");
            valid = false;
        } else {
            et_ps_firstName.setError(null);
        }
        if (profileImgUri == null) {
            valid = false;
        }


        if (lastName.isEmpty() || lastName.length() < 4) {
            et_ps_lastName.setError("Last Name must be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            et_ps_lastName.setError(null);
        }
        if (!valid)
            Toast.makeText(ProfileSetupActivity.this, "Login Failed", Toast.LENGTH_SHORT).show();


        return valid;
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GALLERY_REQUEST && resultCode == RESULT_OK) {
            Uri imageUri = data.getData();
            CropImage.activity(imageUri)
                    .setGuidelines(CropImageView.Guidelines.ON)
                    .setAspectRatio(1, 1)
                    .start(this);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                Uri croppedImg = result.getUri();
                ib_ps_profilePic.setImageURI(croppedImg);
                profileImgUri = croppedImg;

            } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                Exception error = result.getError();
                Log.e(TAG, "cropImgResult : ");
                error.printStackTrace();
            }
        }
    }
}
