package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;
import com.theartofdev.edmodo.cropper.CropImage;
import com.theartofdev.edmodo.cropper.CropImageView;

public class ChangeAvatarActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "ChangeAvatarActivity";

    private ImageView imageViewChosenAvatar;
    private String logged_mail;
    private Button buttonUploadImage, buttonChooseImage;

    private Bundle bndl = new Bundle();

    private StorageReference mStorageRef, fileRef;

    private Uri cropped_uri, uri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_avatar);

        bndl = getIntent().getExtras();
        logged_mail = bndl.getString("logged_mail");

        /* reference to images */
        mStorageRef = FirebaseStorage.getInstance().getReference("Images");
        /* reference to user's avatar */
        fileRef = FirebaseStorage.getInstance().getReference("Images/" + logged_mail);

        imageViewChosenAvatar = (ImageView) findViewById(R.id.imageView_chosen_image);
        buttonUploadImage = (Button) findViewById(R.id.button_upload_image);
        buttonChooseImage = (Button) findViewById(R.id.button_choose_image);

        buttonUploadImage.setOnClickListener(this);
        buttonChooseImage.setOnClickListener(this);

        /* fetch user's avatar from database and set it */
        fileRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
            @Override
            public void onSuccess(Uri uri) {
                Picasso.with(ChangeAvatarActivity.this).load(uri).into(imageViewChosenAvatar);
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception exception) {
//                Toast.makeText(ChangeAvatarActivity.this, "Failed to fetch users avatar from database", Toast.LENGTH_LONG).show();
                imageViewChosenAvatar.setImageResource(R.drawable.avatar);
            }
        });
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button_choose_image:
                Filechooser();
                break;
            case R.id.button_upload_image:
                Fileuploader();
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
        StorageReference Ref = mStorageRef.child(logged_mail);
        
        Ref.putFile(cropped_uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                Toast.makeText(ChangeAvatarActivity.this, "Image uploaded succesfully", Toast.LENGTH_LONG).show();
            }
        })
        .addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Toast.makeText(ChangeAvatarActivity.this, "Image upload failed", Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
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
                imageViewChosenAvatar.setImageURI(result.getUri());
                cropped_uri = result.getUri();
                
                Toast.makeText(this, "SUCCESS!", Toast.LENGTH_SHORT).show();
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
}