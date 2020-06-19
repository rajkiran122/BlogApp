package com.example.blogapp.Activities;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogapp.Adapters.CommentAdapter;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostDetailActivity extends AppCompatActivity {

    String PostKey;
    private TextView postDetail_title, postDetail_desc, postDetail_dateAdded;
    private CircleImageView currentUserPhoto, postedUserPhoto;
    private ImageView postImage;
    private EditText writeComment;
    private Button addCommentBtn;
    private FirebaseAuth mAuth;

    private FirebaseUser firebaseUser;

    private FirebaseDatabase mDatabase;

    private RecyclerView rvComment;
    private CommentAdapter commentAdapter;

    private List<Comment> commentList;

    final static String COMMENT_KEY = "Comments";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        views();

        rvComment = findViewById(R.id.rv_comment);



        //lets set the status bar into transparent

//        Window window = getWindow();
//        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//
//
//        this.getSupportActionBar().hide();

        mAuth = FirebaseAuth.getInstance();
        firebaseUser = mAuth.getCurrentUser();
        mDatabase = FirebaseDatabase.getInstance();


        addCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                addCommentBtn.setVisibility(View.INVISIBLE);

                DatabaseReference commentRef = mDatabase.getReference("Comments").child(PostKey).push();

                String comment_content = writeComment.getText().toString();
                String uid = firebaseUser.getUid();
                String uname = firebaseUser.getDisplayName();
                String uimg = firebaseUser.getPhotoUrl().toString();

                Comment comment = new Comment(comment_content, uid, uimg, uname);

                commentRef.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Toast.makeText(PostDetailActivity.this, "Comment added...", Toast.LENGTH_SHORT).show();
                        addCommentBtn.setVisibility(View.VISIBLE);
                        writeComment.setText("");
                    }
                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Toast.makeText(PostDetailActivity.this, "Error:" + e.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });

            }
        });


        String postImageUri = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImageUri).into(postImage);

        String title = getIntent().getExtras().getString("title");
        postDetail_title.setText(title);

        String currentUserPhotoUri = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(currentUserPhotoUri).into(postedUserPhoto);

        String detail = getIntent().getExtras().getString("description");
        postDetail_desc.setText(detail);

        Glide.with(this).load(firebaseUser.getPhotoUrl()).into(currentUserPhoto);

        //it helps in retrieving the comments
        PostKey = getIntent().getExtras().getString("postKey");

        String date = timeStamptoString(getIntent().getExtras().getShort("date"));
        postDetail_dateAdded.setText(date);

        iniRvComment();


    }

    private void iniRvComment() {

        rvComment.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference commentRef = mDatabase.getReference("Comments").child(PostKey);

        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                commentList = new ArrayList<>();

                for (DataSnapshot snap : dataSnapshot.getChildren())
                {
                    Comment comment =  snap.getValue(Comment.class);

                    commentList.add(comment);
                }

                commentAdapter = new CommentAdapter(getApplicationContext(),commentList);
                rvComment.setAdapter(commentAdapter);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }


    private String timeStamptoString(long time) {

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");
        String date = sdf.format(new Date());

        return date;

    }

    public void views() {

        postDetail_title = findViewById(R.id.post_detail_title);
        postDetail_desc = findViewById(R.id.post_detail_description);
        postDetail_dateAdded = findViewById(R.id.date_added_detail);
        currentUserPhoto = findViewById(R.id.post_detail_currentUserphoto);
        postedUserPhoto = findViewById(R.id.post_detail_postUserPhoto);
        postImage = findViewById(R.id.post_detail_img);
        writeComment = findViewById(R.id.post_detail_comment);
        addCommentBtn = findViewById(R.id.post_detail_addcomment);

    }

}