package com.app.blog.firebase.blogappfirebase_based;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {
    private EditText et_userName, et_email, et_password;
    private String userName, email, password;
    private Button b_register;

    private FirebaseAuth mAuth;
    private DatabaseReference users_db;

    private ProgressDialog mProgress;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        mProgress = new ProgressDialog(this);

        et_email = (EditText) findViewById(R.id.et_reg_email);
        et_password = (EditText) findViewById(R.id.et_reg_password);
        et_userName = (EditText) findViewById(R.id.et_reg_userName);
        b_register = (Button) findViewById(R.id.b_register);

        b_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startRegister();
            }
        });

    }

    private void startRegister() {
        userName = et_userName.getText().toString().trim();
        password = et_password.getText().toString().trim();
        email = et_email.getText().toString().trim();

        if (validate()){
            mProgress.setMessage("registering..");
            mProgress.show();

            users_db = FirebaseDatabase.getInstance().getReference().child("blog").child("users");
            mAuth = FirebaseAuth.getInstance();
            mAuth.createUserWithEmailAndPassword(email,password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){

                        String userId = task.getResult().getUser().getUid();

                        DatabaseReference mdb2 = users_db.child(userId);

                        mdb2.child("name").setValue(userName);
                        mdb2.child("profile_pic").setValue("default");

                        mProgress.dismiss();

                        startActivity(new Intent(RegisterActivity.this,MainActivity.class).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
                    }
                }
            });
        }


    }


    public boolean validate() {
        boolean valid = true;

        //TODO: test if the user already exists in db
        if (userName.isEmpty() || userName.length() < 3) {
            et_userName.setError("username must be at least 3 characters");
            valid = false;
        } else {
            et_userName.setError(null);
        }

        if (email.isEmpty() || email.length() < 3) {
            et_email.setError("username must be at least 3 characters");
            valid = false;
        } else {
            et_email.setError(null);
        }

        if (password.isEmpty() || password.length() < 4 || password.length() > 10) {
            et_password.setError("password must be between 4 and 10 alphanumeric characters");
            valid = false;
        } else {
            et_password.setError(null);
        }

        return valid;
    }

}
