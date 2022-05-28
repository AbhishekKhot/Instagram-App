package com.example.instagram;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import de.hdodenhof.circleimageview.CircleImageView;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    private List<Post> postList;
    private List<User> userList;
    private Activity context;
    private FirebaseFirestore firebaseFirestore;
    private FirebaseAuth firebaseAuth;

    public PostAdapter(List<Post> postList, List<User> userList, Activity context) {
        this.postList = postList;
        this.userList = userList;
        this.context = context;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.post,parent,false);
        firebaseFirestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, @SuppressLint("RecyclerView") int position) {
        Post post = postList.get(position);
        holder.setPostPic(post.getImage());
        holder.setPostCaption(post.getCaption());


        long milliseconds = post.getTime().getTime();
        String date  = DateFormat.format("MM/dd/yyyy" , new Date(milliseconds)).toString();
        holder.setPostDate(date);

        String userName= userList.get(position).getName();
        String userImage = userList.get(position).getImage();
        holder.setProfilePic(userImage);
        holder.setPostUserName(userName);

        String postId = post.PostId;
        String currentUserId = Objects.requireNonNull(firebaseAuth.getCurrentUser()).getUid();

        holder.likePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUserId).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if(!task.getResult().exists()){
                            Map<String,Object> likeMap = new HashMap<>();
                            likeMap.put("timestamp", FieldValue.serverTimestamp());
                            firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUserId).set(likeMap);
                        }
                        else{
                            firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUserId).delete();
                        }
                    }
                });
            }
        });

        firebaseFirestore.collection("Posts/" + postId + "/Likes").document(currentUserId).addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error == null){
                    if(value.exists()){
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.ic_outline_favorite_border_24));
                    }
                    else{
                        holder.likePic.setImageDrawable(context.getDrawable(R.drawable.ic_outline_favorite_border_24));
                    }
                }
            }
        });

        firebaseFirestore.collection("Posts/" + postId + "/Likes").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if(error==null){
                    if(!value.isEmpty()){
                        int count = value.size();
                        holder.setPostLikes(count);
                    }
                    else{
                        holder.setPostLikes(0);
                    }
                }
            }
        });

        holder.commentPic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
               //move to comment fragment
                Intent intent = new Intent(context,CommentsFragment.class);
                intent.putExtra("postid",postId);
                context.startActivity(intent);
            }
        });

        if(currentUserId.equals(post.getUser())){
            holder.deleteButton.setVisibility(View.VISIBLE);
            holder.deleteButton.setClickable(true);
            holder.deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alert = new AlertDialog.Builder(context);
                    alert.setTitle("Delete")
                            .setMessage("Are you Sure?")
                            .setNegativeButton("No",null)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    firebaseFirestore.collection("Posts/" + postId + "/Comments").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                                                        firebaseFirestore.collection("Posts/" + postId + "/Comments").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });
                                    firebaseFirestore.collection("Posts/" + postId + "/Likes").get()
                                            .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                                                    for(QueryDocumentSnapshot snapshot : task.getResult()){
                                                        firebaseFirestore.collection("Posts/" + postId + "Likes/").document(snapshot.getId()).delete();
                                                    }
                                                }
                                            });
                                    firebaseFirestore.collection("Posts").document(postId).delete();
                                    postList.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                    alert.show();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    public class PostViewHolder extends RecyclerView.ViewHolder{
        ImageView postPic,commentPic,likePic;
        CircleImageView profilePic;
        TextView postUserName,postDate,postCaption,postLike;
        ImageButton deleteButton;
        View view;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            view = itemView;
            likePic = view.findViewById(R.id.like_btn);
            commentPic = view.findViewById(R.id.comments_post);
            deleteButton = view.findViewById(R.id.delete_btn);
        }

        public void setPostLikes(int count){
            postLike = view.findViewById(R.id.like_count_tv);
            postLike.setText(count + " Likes");
        }

        public void setPostPic(String postUrl){
            postPic = view.findViewById(R.id.user_post);
            Glide.with(context).load(postUrl).into(postPic);
        }

        public void setProfilePic(String profilePicUrl){
            profilePic = view.findViewById(R.id.profile_pic);
            Glide.with(context).load(profilePicUrl).into(profilePic);
        }

        public void setPostUserName(String userName){
            postUserName = view.findViewById(R.id.username_tv);
            postUserName.setText(userName);
        }

        public void setPostDate(String date){
            postDate = view.findViewById(R.id.date_tv);
            postDate.setText(date);
        }

        public void setPostCaption(String caption){
            postCaption = view.findViewById(R.id.caption_tv);
            postCaption.setText(caption);
        }
    }
}
