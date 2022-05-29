package com.example.instagram.ui;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.instagram.R;
import com.example.instagram.model.User;
import com.example.instagram.adapter.CommentAdapter;
import com.example.instagram.model.Comments;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
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

public class CommentsFragment extends Fragment {
    private EditText commentEdit;
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
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_comments, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        commentEdit = view.findViewById(R.id.comment_edittext);
        mAddCommentBtn = view.findViewById(R.id.add_comment);
        mCommentRecyclerView = view.findViewById(R.id.comment_recyclerView);

        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        currentUserId = firebaseAuth.getCurrentUser().getUid();

        mList = new ArrayList<>();
        usersList = new ArrayList<>();
        adapter = new CommentAdapter(requireActivity(),usersList,mList);

        post_id = requireActivity().getIntent().getStringExtra("postid");
        mCommentRecyclerView.setHasFixedSize(true);
        mCommentRecyclerView.setLayoutManager(new LinearLayoutManager(requireActivity()));
        mCommentRecyclerView.setAdapter(adapter);

        firebaseFirestore.collection("Posts/" + post_id + "/Comments").addSnapshotListener(requireActivity(), new EventListener<QuerySnapshot>() {
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
                                            Toast.makeText(requireActivity(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
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
                                Toast.makeText(requireContext(),"Comment added successfully",Toast.LENGTH_SHORT).show();
                            }
                            else{
                                Toast.makeText(requireContext(),task.getException().getMessage(),Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }
                else{
                    Toast.makeText(requireContext(),"Please write comment",Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}