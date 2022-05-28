package com.example.instagram;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommentActivity extends AppCompatActivity {
    private TextInputEditText commentEdit;
    private Button mAddCommentBtn;
    private RecyclerView mCommentRecyclerView;
    private FirebaseFirestore firebaseFirestore;
    private String post_id;
    private String currentUserId;
    private FirebaseAuth firebaseAuth;
    private CommentAdapter adapter;
    private List<Comments> mList;
    private List<User> usersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_comment);

        commentEdit = findViewById(R.id.post_commentEditText);
        mAddCommentBtn = findViewById(R.id.add_comment);
        mCommentRecyclerView = findViewById(R.id.comment_recyclerView);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        mList = new ArrayList<>();
        usersList = new ArrayList<>();
        adapter = new CommentAdapter(this,usersList,mList);

        post_id = getIntent().getStringExtra("postid");
        mCommentRecyclerView.setHasFixedSize(true);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mCommentRecyclerView.setAdapter(adapter);


        firebaseFirestore.collection("Posts/" + post_id + "/Comments").addSnapshotListener(this, new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                for(DocumentChange documentChange : value.getDocumentChanges()){
                    if(documentChange.getType() == DocumentChange.Type.ADDED){
                        Comments comments = documentChange.getDocument().toObject(Comments.class).withId(documentChange.getDocument().getId());
                        String userId = documentChange.getDocument().getString("user");

                        firebaseFirestore.collection("Users").document(userId).get()
                                .addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                    @Override
                                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                        if(task.isSuccessful()){
                                            User user = task.getResult().toObject(User.class);
                                            usersList.add(user);
                                            mList.add(comments);
                                            adapter.notifyDataSetChanged();
                                        }
                                        else{
                                            Toast.makeText(CommentActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });
                    }
                    else{
                        adapter.notifyDataSetChanged();
                    }
                }
            }
        });

        mAddCommentBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String comment = commentEdit.getText().toString();
                if(!comment.isEmpty()){
                    Map<String,Object> commentMap = new HashMap<>();
                    commentMap.put("comment",comment);
                    commentMap.put("time", FieldValue.serverTimestamp());
                    commentMap.put("user",currentUserId);
                    firebaseFirestore.collection("Posts/" + post_id + "/Comments").add(commentMap).addOnCompleteListener(new OnCompleteListener<DocumentReference>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentReference> task) {
                            if(task.isSuccessful()){
                                Toast.makeText(CommentActivity.this,"Comment added successfully",Toast.LENGTH_SHORT).show();

                            }
                            else{
                                Toast.makeText(CommentActivity.this,task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(CommentActivity.this,"Please write comment",Toast.LENGTH_SHORT).show();
                }
            }
        });




    }
}