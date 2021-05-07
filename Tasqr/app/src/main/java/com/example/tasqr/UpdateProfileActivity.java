package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.tasqr.classes.User;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class UpdateProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UpdateProfileActivity";

    private User user;

    private Bundle bndl = new Bundle();

    private FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private DatabaseReference rootRef = database.getReference();
    private DatabaseReference usersRef = rootRef.child("Users");

    private StorageReference avatarRef;
    private StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Images");;

    private ImageView avatarImageView;
    private EditText nameEditText, surnameEditText, passwordEditText, passwordConfirmEditText;
    private Button buttonSave;
    private Uri avatarUri, uri, cropped_uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        avatarImageView = (ImageView) findViewById(R.id.user_avatar);
        nameEditText = findViewById(R.id.editTextName);
        surnameEditText = findViewById(R.id.editTextSurname);
        passwordEditText = findViewById(R.id.editTextPassword);
        passwordConfirmEditText = findViewById(R.id.editTextPasswordConfirm);
        buttonSave = findViewById(R.id.button_save);

        avatarImageView.setOnClickListener(this);
        buttonSave.setOnClickListener(this);

        bndl = getIntent().getExtras();
        user = bndl.getParcelable("user");
        if (bndl.getString("uri") != null)
            avatarUri = Uri.parse(bndl.getString("uri"));

        nameEditText.setText(user.getName());
        surnameEditText.setText(user.getSurname());

        avatarRef = FirebaseStorage.getInstance().getReference("Images/" + user.getMail());

        if (avatarUri != null)
            Picasso.with(UpdateProfileActivity.this).load(avatarUri).into(avatarImageView);
        else
            avatarImageView.setImageResource(R.drawable.avatar);
    }

    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.user_avatar:
                Filechooser();
                break;
            case R.id.button_save:
                Intent returnIntent = onButtonSave();
                if (returnIntent == null)
                    break;
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
        }
    }

    /* choose a file from gallery */
    private void Filechooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);

        /* fetch result from activity, request code is 200 since CropImage class requests it for further work */
        startActivityForResult(intent, CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE);
    }

    /* uploads a file to database */
    private void Fileuploader() {
        StorageReference Ref = mStorageRef.child(user.getMail());
        if (cropped_uri != null) {
            Ref.putFile(cropped_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
//                    Toast.makeText(UpdateProfileActivity.this, "Image uploaded succesfully", Toast.LENGTH_LONG).show();
                }
            })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
//                            Toast.makeText(UpdateProfileActivity.this, "Image upload failed", Toast.LENGTH_LONG).show();
                        }
                    });
        }
    }

    /* we need to fetch result from activity */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        /* request code for crop image addon is specified by constant */
        if (requestCode == CropImage.PICK_IMAGE_CHOOSER_REQUEST_CODE && resultCode == Activity.RESULT_OK && data != null && data.getData() != null) {
            uri = data.getData();
            if (CropImage.isReadExternalStoragePermissionsRequired(this, uri)) {
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
            else {
                startCrop(uri);
            }
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avatarImageView.setImageURI(result.getUri());
                cropped_uri = result.getUri();
            }
        }
    }

    /* starts cropping activity */
    private void startCrop(Uri imageuri) {
        CropImage.activity(imageuri)
                .setAspectRatio(1,1)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setMultiTouchEnabled(true)
                .start(this);
    }

    private Intent onButtonSave() {
        User newUserData = new User(user);
        String name = nameEditText.getText().toString();
        String surname = surnameEditText.getText().toString();
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();

        Intent returnIntent = new Intent();

        if (!password.equals("") || !passwordConfirm.equals("")) {
            if (!password.equals(passwordConfirm)) {
                Utilities.toastMessage("New password and confirmation password do not match.", UpdateProfileActivity.this);
                return null;
            }
            else {
                newUserData.setPassword(password);
            }
        }

        if (!name.equals(user.getName())) {
            newUserData.setName(name);
            returnIntent.putExtra("new_name", name);
        }

        if (!surname.equals(user.getSurname())) {
            newUserData.setSurname(surname);
            returnIntent.putExtra("new_surname", surname);
        }

        usersRef.child(user.getId()).setValue(newUserData);

        /* upload new avatar */
        Fileuploader();

        if (cropped_uri != null)
            returnIntent.putExtra("new_avatar_uri", cropped_uri.toString());

        return returnIntent;
    }
}