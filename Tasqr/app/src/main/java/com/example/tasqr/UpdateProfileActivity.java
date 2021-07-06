/*
 * UPDATE PROFILE ACTIVITY
 * Contains     IMAGE profile picture
 *              Button change profile picture, save
 *              EditText forms to change password, name or surname
 */

package com.example.tasqr;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
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

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.grpc.Compressor;

public class UpdateProfileActivity extends AppCompatActivity implements View.OnClickListener {

    private User user;

    ArrayList<String> skills = new ArrayList<>();
    boolean areSkillsChanged;

    private final FirebaseDatabase database = FirebaseDatabase.getInstance("https://tasqr-android-default-rtdb.europe-west1.firebasedatabase.app/");
    private final DatabaseReference rootRef = database.getReference();
    private final DatabaseReference usersRef = rootRef.child("Users");

    private final StorageReference mStorageRef = FirebaseStorage.getInstance().getReference("Images");;

    private ImageView avatarImageView;
    private EditText nameEditText, surnameEditText, passwordEditText, passwordConfirmEditText;
    private Uri avatarUri;
    private Uri cropped_uri;

    private final int LAUNCH_SKILLS_ACTIVITY = 1;

    /* MAIN ON CREATE METHOD */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);

        setBehavior();

        Bundle bndl = getIntent().getExtras();
        user = bndl.getParcelable("user");

        DatabaseReference userRef = database.getReference("Users/" + user.getId());
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.getValue(User.class);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Utilities.toastMessage("error", UpdateProfileActivity.this);
            }
        });

        areSkillsChanged = false;

        if (bndl.getString("uri") != null)
            avatarUri = Uri.parse(bndl.getString("uri"));

        nameEditText.setText(user.getName());
        surnameEditText.setText(user.getSurname());

        if (avatarUri != null)
            Picasso.with(UpdateProfileActivity.this).load(avatarUri).into(avatarImageView);
        else
            avatarImageView.setImageResource(R.drawable.avatar);
    }

    private void setBehavior(){
        ImageView addAvatarImageView = findViewById(R.id.imageViewAddAvatar);
        avatarImageView = findViewById(R.id.user_avatar);
        nameEditText = findViewById(R.id.editTextName);
        surnameEditText = findViewById(R.id.editTextSurname);
        passwordEditText = findViewById(R.id.editTextPassword);
        passwordConfirmEditText = findViewById(R.id.editTextPasswordConfirm);
        Button buttonSave = findViewById(R.id.button_save);
        Button buttonSkills = findViewById(R.id.skill_button);

        addAvatarImageView.setOnClickListener(this);
        avatarImageView.setOnClickListener(this);
        buttonSave.setOnClickListener(this);
        buttonSkills.setOnClickListener(this);
    }

    /* BEHAVIOR LIST */
    @Override
    public void onClick(View v) {
        switch(v.getId()) {
            case R.id.user_avatar:
                Filechooser();
                break;
            case R.id.imageViewAddAvatar:
                Filechooser();
                break;
            case R.id.button_save:
                Intent returnIntent = onButtonSave();
                if (returnIntent == null)
                    break;
                setResult(Activity.RESULT_OK, returnIntent);
                finish();
                break;
            case R.id.skill_button:
                Intent updateSkillIntent = new Intent(this, SkillsActivity.class);
                Bundle skillBundle = new Bundle();
                skillBundle.putParcelable("user", user);
                updateSkillIntent.putExtras(skillBundle);
                startActivityForResult(updateSkillIntent, LAUNCH_SKILLS_ACTIVITY);
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
        cropped_uri = compressFile(cropped_uri);

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
            Uri uri = data.getData();
            if (CropImage.isReadExternalStoragePermissionsRequired(this, uri))
                requestPermissions(new String[] {Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            else
                startCrop(uri);
        }

        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            CropImage.ActivityResult result = CropImage.getActivityResult(data);
            if (resultCode == RESULT_OK) {
                avatarImageView.setImageURI(result.getUri());
                cropped_uri = result.getUri();
            }
        }
        else if (requestCode == LAUNCH_SKILLS_ACTIVITY && resultCode == Activity.RESULT_OK) {
            skills = data.getStringArrayListExtra("skills");
            areSkillsChanged = true;
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

    /* SAVE CHANGES */
    private Intent onButtonSave() {
        User newUserData = new User(user);
        Intent returnIntent = new Intent();

        Pattern p = Pattern.compile("[A-Za-zążźłóęćńśĄŻŹŁÓĘĆŃŚ]{1,40}");
        /* validate */
        if(!passwordValidation(newUserData) ||
                !nameValidation(p, newUserData, returnIntent) ||
                !surnameValidation(p, newUserData, returnIntent))
            return null;


        if (areSkillsChanged) {
            returnIntent.putExtra("new_skills", skills);
            newUserData.setSkills(skills);
        }

        usersRef.child(user.getId()).setValue(newUserData);

        /* upload new avatar */
        Fileuploader();

        if (cropped_uri != null)
            returnIntent.putExtra("new_avatar_uri", cropped_uri.toString());

        return returnIntent;
    }

    private boolean passwordValidation(User newUserData){
        String password = passwordEditText.getText().toString();
        String passwordConfirm = passwordConfirmEditText.getText().toString();

        if (!password.equals("") || !passwordConfirm.equals("")) {
            if (password.length() > 40) {
                Utilities.toastMessage("Password must be at most 40 characters long.", UpdateProfileActivity.this);
                return false;
            }

            if (!password.equals(passwordConfirm)) {
                Utilities.toastMessage("New password and confirmation password do not match.", UpdateProfileActivity.this);
                return false;
            }

            String hashedPassword = new String();

            byte[] salt = new byte[20];
            SecureRandom random = new SecureRandom();
            random.nextBytes(salt);

            try {
                hashedPassword = Utilities.generateHash(password, salt);
            } catch (NoSuchAlgorithmException e) {
            }

            newUserData.setPassword(hashedPassword);
            newUserData.setSalt(Utilities.bytesToHex(salt));

        }
        return true;
    }

    private boolean nameValidation(Pattern p, User newUserData, Intent returnIntent){
        String name = nameEditText.getText().toString();

        if (!name.equals(user.getName())) {
            if (name.isEmpty()) {
                Utilities.toastMessage("Name cannot be empty.", UpdateProfileActivity.this);
                return false;
            }

            if (name.length() > 40) {
                Utilities.toastMessage("Name must be at most 40 letters long.", UpdateProfileActivity.this);
                return false;
            }
            Matcher m = p.matcher(name);
            if (!m.matches()) {
                Utilities.toastMessage("Name can only contain characters between A-Z and a-z.", UpdateProfileActivity.this);
                return false;
            }

            newUserData.setName(name);
            returnIntent.putExtra("new_name", name);
        }
        return true;
    }

    private boolean surnameValidation(Pattern p, User newUserData, Intent returnIntent){
        String surname = surnameEditText.getText().toString();

        if (!surname.equals(user.getSurname())) {
            if (surname.isEmpty()) {
                Utilities.toastMessage("Surname cannot be empty.", UpdateProfileActivity.this);
                return false;
            }
            if (surname.length() > 40) {
                Utilities.toastMessage("Surname must be at most 40 letters long.", UpdateProfileActivity.this);
                return false;
            }
            Matcher m = p.matcher(surname);
            if (!m.matches()) {
                Utilities.toastMessage("Surname can only contain characters between A-Z and a-z.", UpdateProfileActivity.this);
                return false;
            }

            newUserData.setSurname(surname);
            returnIntent.putExtra("new_surname", surname);
        }
        return true;
    }


    private Uri compressFile(Uri uri) {
        Bitmap bitmap, newBitmap;
        try {
            bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), uri);

            newBitmap = Bitmap.createScaledBitmap(bitmap, 300, 300, true);

            File tempFile = File.createTempFile(user.getMail(), ".jpg", null);

            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            newBitmap.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
            byte[] bitmapData = bytes.toByteArray();

            FileOutputStream fos = new FileOutputStream(tempFile);
            fos.write(bitmapData);
            fos.flush();
            fos.close();

            return Uri.fromFile(tempFile);
        }
        catch (Exception e) {
            return uri;
        }
    }
}