package com.example.blogapp.Activities;

import android.Manifest;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.bumptech.glide.Glide;
import com.example.blogapp.Fragments.HomeFragment;
import com.example.blogapp.Fragments.ProfileFragment;
import com.example.blogapp.Fragments.SettingFragment;
import com.example.blogapp.Models.Posts;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

public class Home extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {


    private static final int REQUEST_CODE = 1;
    private static final int INTENT_CODE = 2;
    FirebaseAuth mAuth;
    FirebaseUser currentUser;

    DrawerLayout drawerLayout;
    Dialog popupAddDialog;


    Uri imageUri;

    private ImageView popupUserImage, popupPostImage, popupAdd;
    private TextView title_pop, desc_pop;
    private ProgressBar popupProgressBar;

    private AppBarConfiguration mAppBarConfiguration;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home2);


        mAuth = FirebaseAuth.getInstance();

        currentUser = mAuth.getCurrentUser();


        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        popUpDialogAdd();


        setUpPopUpImageClick();


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                popupAddDialog.show();

            }
        });
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);


        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_home, R.id.nav_gallery, R.id.nav_slideshow)
                .setDrawerLayout(drawerLayout)
                .build();
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);


        navigationView.setNavigationItemSelectedListener(this);

        getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment,new HomeFragment()).commit();

        updateNavHeader();


    }

    private void setUpPopUpImageClick() {

        popupPostImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                //it is when the post image is clicked
                //so we have to go to the gallery
                //we require permissions here

                if (Build.VERSION.SDK_INT >= 22) {
                    checkRequestForPermission();
                } else {
                    openGallery();
                }


            }
        });

    }

    private void checkRequestForPermission() {

        if (ContextCompat.checkSelfPermission(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(Home.this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                Toast.makeText(this, "Allow for the furthur functioning...", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(Home.this, new String[]
                        {Manifest.permission.READ_EXTERNAL_STORAGE}, REQUEST_CODE);
            }
        } else {
            //when we have permissions to access the gallery
            openGallery();
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == RESULT_OK && requestCode == INTENT_CODE && data != null) {
            imageUri = data.getData();

            popupPostImage.setImageURI(imageUri);
        }

    }

    private void openGallery() {

        Intent galleryIntent = new Intent(Intent.ACTION_GET_CONTENT);
        galleryIntent.setType("images/*");
        startActivityForResult(galleryIntent, INTENT_CODE);

    }

    private void popUpDialogAdd() {

        popupAddDialog = new Dialog(this);

        popupAddDialog.setContentView(R.layout.popup_add_post);

        popupAddDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        popupAddDialog.getWindow().setLayout(Toolbar.LayoutParams.MATCH_PARENT, Toolbar.LayoutParams.WRAP_CONTENT);
        popupAddDialog.getWindow().getAttributes().gravity = Gravity.TOP;


        popupUserImage = popupAddDialog.findViewById(R.id.popup_userImage);
        popupPostImage = popupAddDialog.findViewById(R.id.imageView_popup);
        popupAdd = popupAddDialog.findViewById(R.id.popup_inner_add);
        title_pop = popupAddDialog.findViewById(R.id.popup_title);
        desc_pop = popupAddDialog.findViewById(R.id.popup_description);
        popupProgressBar = popupAddDialog.findViewById(R.id.popup_progressBar);

        if (currentUser!=null) {
            Glide.with(this).load(currentUser.getPhotoUrl()).into(popupUserImage);

            popupAdd.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    popupAdd.setVisibility(View.INVISIBLE);
                    popupProgressBar.setVisibility(View.VISIBLE);


                    if (!title_pop.getText().toString().isEmpty() && !desc_pop.getText().toString().isEmpty()) {
                        //now we have to add object to the database

                        StorageReference mRef = FirebaseStorage.getInstance().getReference().child("blog_images");
                        try {
                            final StorageReference imageFilePath = mRef.child(imageUri.getLastPathSegment());
                            imageFilePath.putFile(imageUri).addOnCompleteListener(new OnCompleteListener<UploadTask.TaskSnapshot>() {
                                @Override
                                public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {
                                    if (task.isSuccessful()) {
                                        imageFilePath.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                            @Override
                                            public void onSuccess(Uri uri) {
                                                String imgDownloadLink = uri.toString();

                                                //Create post object

                                                Posts post = new Posts(title_pop.getText().toString(),
                                                        desc_pop.getText().toString(),
                                                        imgDownloadLink,
                                                        currentUser.getUid(),
                                                        currentUser.getPhotoUrl().toString());

                                                addPost(post);

                                            }
                                        }).addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Toast.makeText(Home.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();

                                                popupAdd.setVisibility(View.VISIBLE);
                                                popupProgressBar.setVisibility(View.GONE);

                                            }
                                        });
                                    }
                                }
                            });

                        } catch (Exception e) {
                            popupAdd.setVisibility(View.VISIBLE);
                            popupProgressBar.setVisibility(View.INVISIBLE);
                            Toast.makeText(Home.this, "Please upload photo...", Toast.LENGTH_SHORT).show();

                        }

                    } else {
                        Toast.makeText(Home.this, "Please enter title and description of your content.Dont forget to upload at least one photo too...", Toast.LENGTH_SHORT).show();

                        popupAdd.setVisibility(View.VISIBLE);
                        popupProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        }
    }

    private void addPost(Posts post) {

        FirebaseDatabase mDatabase = FirebaseDatabase.getInstance();
        DatabaseReference myRef = mDatabase.getReference("Posts").push();

        String key = myRef.getKey();
        post.setPostKey(key);

        //Add post data to firebase database

        myRef.setValue(post).addOnSuccessListener(new OnSuccessListener<Void>() {
            @Override
            public void onSuccess(Void aVoid) {
                Toast.makeText(Home.this, "Post Added Successfully...", Toast.LENGTH_SHORT).show();
                popupProgressBar.setVisibility(View.GONE);
                popupAdd.setVisibility(View.VISIBLE);
                popupAddDialog.dismiss();
                desc_pop.setText("");
                title_pop.setText("");
            }
        });
    }



    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }

    public void updateNavHeader() {
        if (currentUser != null) {
            NavigationView navigationView = findViewById(R.id.nav_view);
            View headerView = navigationView.getHeaderView(0);

            TextView user_name = headerView.findViewById(R.id.nav_username);
            TextView user_email = headerView.findViewById(R.id.nav_useremail);
            ImageView user_photo = headerView.findViewById(R.id.nav_userphoto);

            user_email.setText(currentUser.getEmail());
            user_name.setText(currentUser.getDisplayName());

            //loading picture with glide library

            Glide.with(this).load(currentUser.getPhotoUrl()).into(user_photo);

        }

    }

    @Override
    protected void onStart() {
        super.onStart();

        if (currentUser == null) {
            startActivity(new Intent(Home.this, LoginActivity.class));
            finish();
        }

    }



    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {

        switch (item.getItemId()) {
            case R.id.nav_home:
                getSupportActionBar().setTitle("Home");
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new HomeFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_profile:
                getSupportActionBar().setTitle("Profile");
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new ProfileFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_setting:
                getSupportActionBar().setTitle("Settings");
                getSupportFragmentManager().beginTransaction().replace(R.id.nav_host_fragment, new SettingFragment()).commit();
                drawerLayout.closeDrawer(GravityCompat.START);
                break;

            case R.id.nav_logout:
                mAuth.signOut();
                startActivity(new Intent(Home.this, LoginActivity.class));
                finish();
                break;
        }


        return true;
    }


}