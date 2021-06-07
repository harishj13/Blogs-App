package com.example.blogsapp.Activities;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.example.blogsapp.Fragments.HomeFragment;
import com.example.blogsapp.Model.Comment;
import com.example.blogsapp.Model.CommentAdapter;
import com.example.blogsapp.R;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class PostDetailActivity extends AppCompatActivity {

    ImageView imgPost,imgUserPost,imgCurrentUser;
    TextView txtPostDesc,txtPostDateName,txtPostTitle;
    EditText editTextComment;
    Button btnAddComment;
    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;
    String PostKey;
    FirebaseDatabase firebaseDatabase;
    RecyclerView RvComment;
    CommentAdapter commentAdapter;
    List<Comment> listComment;
    static String COMMENT_KEY = "Comment";
    Button btnShare,btnDeleletePost;
    private DatabaseReference clickPostRef;
    String databaseUserId,currentuserId;
    ImageButton post_detail_like;
    private DatabaseReference mDatabaseLike;
    private boolean mProcessLike = false;
    TextView txtPostLikeCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_detail);

        //statue bar transparent
        Window w = getWindow();
        w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        getSupportActionBar().hide();

        RvComment = findViewById(R.id.rv_comment);

        //Like and Dislike
        post_detail_like = findViewById(R.id.post_detail_like);
        //post_detail_dislike = findViewById(R.id.post_detail_dislike);

        txtPostLikeCount = findViewById(R.id.post_detail_like_count);

        imgPost =  findViewById(R.id.post_detail_img);
        imgUserPost = findViewById(R.id.post_detail_user_img);
        imgCurrentUser = findViewById(R.id.post_detail_currentuser_img);

        txtPostTitle = findViewById(R.id.post_detail_title);
        txtPostDesc = findViewById(R.id.post_detail_desc);
        txtPostDateName = findViewById(R.id.post_detail_date_name);

        editTextComment = findViewById(R.id.post_detail_comment);
        btnAddComment = findViewById(R.id.post_detail_add_comment_btn);

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();
        firebaseDatabase = FirebaseDatabase.getInstance();
        currentuserId = firebaseAuth.getCurrentUser().getUid();

        btnShare = findViewById(R.id.post_detail_share);
        btnDeleletePost = findViewById(R.id.post_detail_delete);

        btnDeleletePost.setVisibility(View.INVISIBLE);

        //getting post id
        PostKey = getIntent().getExtras().getString("postKey");

        clickPostRef = FirebaseDatabase.getInstance().getReference().child("Posts").child(PostKey);

        //like reference
        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mDatabaseLike.keepSynced(true);






         //delete post
        clickPostRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                if (snapshot.exists())
                {
                    databaseUserId = snapshot.child("userId").getValue().toString();

                    if (currentuserId.equals(databaseUserId))
                    {
                        btnDeleletePost.setVisibility(View.VISIBLE);
                    }

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });


        //like imageview
        post_detail_like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                mProcessLike = true;

                   mDatabaseLike.addValueEventListener(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {

                            if (mProcessLike)
                            {
                                if (snapshot.child(PostKey).hasChild(firebaseAuth.getCurrentUser().getUid()))
                                {
                                    mDatabaseLike.child(PostKey).child(firebaseAuth.getCurrentUser().getUid()).removeValue();
                                    mProcessLike = false;
                                   // post_detail_like.setImageResource(R.drawable.ic_dis);
                                } else
                                    {
                                    mDatabaseLike.child(PostKey).child(firebaseAuth.getCurrentUser().getUid()).setValue(firebaseAuth.getCurrentUser().getDisplayName());
                                    mProcessLike = false;
                                    //post_detail_like.setImageResource(R.drawable.ic_like);
                                }
                            }

                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });



                   setLikeBtn(PostKey);
                   setLikeCount(PostKey,txtPostLikeCount);
            }
        });




        //share button
        btnShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                String shareText = "Please Checkout the Post And Download the Application Now";
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_TEXT,shareText);
                startActivity(Intent.createChooser(shareIntent,"Share Using"));

            }
        });


        //delete post
        btnDeleletePost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                
                DeleteCurrentPost();

            }
        });



        //comment button
        btnAddComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btnAddComment.setVisibility(View.INVISIBLE);
                DatabaseReference commentReference = firebaseDatabase.getReference(COMMENT_KEY).child(PostKey).push();

                String comment_content = editTextComment.getText().toString();
                String uid = firebaseUser.getUid();
                String uname = firebaseUser.getDisplayName();
                String uimg = firebaseUser.getPhotoUrl().toString();

                Comment comment = new Comment(comment_content,uid,uimg,uname);

                commentReference.setValue(comment).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {

                        showMessage("Comment Added");
                        editTextComment.setText("");
                        btnAddComment.setVisibility(View.VISIBLE);

                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {

                        showMessage(" Failed to Add Comment :- "+e.getMessage());
                    }
                });
            }
        });

        //getIntent
        String postImage = getIntent().getExtras().getString("postImage");
        Glide.with(this).load(postImage).into(imgPost);

        String postTitle = getIntent().getExtras().getString("title");
        txtPostTitle.setText(postTitle);

        String userPostImage = getIntent().getExtras().getString("userPhoto");
        Glide.with(this).load(userPostImage).into(imgUserPost);

        String postDescription = getIntent().getExtras().getString("description");
        txtPostDesc.setText(postDescription);

        //set comment image
        Glide.with(this).load(firebaseUser.getPhotoUrl()).into(imgCurrentUser);

        //getting post id
        //PostKey = getIntent().getExtras().getString("postKey");

        String Date = timestampToString(getIntent().getExtras().getLong("postDate"));
        txtPostDateName.setText(Date);


        //ini Recyclerview Comment
        iniRvComment();

    }

    private void setLikeCount(String postKey, final TextView txtPostLikeCount) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postKey);
        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                txtPostLikeCount.setText(snapshot.getChildrenCount() + " Likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void setLikeBtn(String postKey) {

        DatabaseReference mDatabaseLike;
        final FirebaseAuth mAuth;

        mDatabaseLike = FirebaseDatabase.getInstance().getReference().child("Likes");
        mAuth = FirebaseAuth.getInstance();

        mDatabaseLike.keepSynced(true);

        mDatabaseLike.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(PostKey).hasChild(mAuth.getCurrentUser().getUid()))
                {
                    post_detail_like.setImageResource(R.drawable.ic_like);
                }
                else
                {
                    post_detail_like.setImageResource(R.drawable.ic_dis);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


    private void DeleteCurrentPost() {

        clickPostRef.removeValue();
        sendToMainActivity();
        Toast.makeText(this, "Post Has Been Deleted", Toast.LENGTH_SHORT).show();
    }

    private void sendToMainActivity() {

        Intent sendToHome = new Intent(PostDetailActivity.this,HomeActivity.class);
        startActivity(sendToHome);
    }


    //retrieving comment
    private void iniRvComment() {

        RvComment.setLayoutManager(new LinearLayoutManager(this));

        DatabaseReference commentRef = firebaseDatabase.getReference(COMMENT_KEY).child(PostKey);
        commentRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {

                listComment = new ArrayList<>();
                for (DataSnapshot snap : snapshot.getChildren())
                {
                    Comment comment = snap.getValue(Comment.class);
                    listComment.add(comment);
                }

                commentAdapter = new CommentAdapter(getApplicationContext(),listComment);
                RvComment.setAdapter(commentAdapter);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void showMessage(String message) {
        Toast.makeText(PostDetailActivity.this, message, Toast.LENGTH_SHORT).show();
    }

    private String timestampToString(long time)
    {
        Calendar calendar = Calendar.getInstance(Locale.ENGLISH);
        calendar.setTimeInMillis(time);
        String date = DateFormat.format("dd-MM-yyyy",calendar).toString();
        return date;
    }
}