package com.example.instagram;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import java.util.List;
import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentViewHolder> {
    private Activity context;
    private List<User> userList;
    private List<Comments> commentsList;

    public CommentAdapter(Activity context, List<User> userList, List<Comments> commentsList) {
        this.context = context;
        this.userList = userList;
        this.commentsList = commentsList;
    }

    @NonNull
    @Override
    public CommentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.comment,parent,false);
        return new CommentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentViewHolder holder, int position) {
        Comments comments = commentsList.get(position);
        holder.setmComment(comments.getComment());

        User user = userList.get(position);
        holder.setmUserName(user.getName());
        holder.setCircleImageView(user.getImage());
    }

    @Override
    public int getItemCount() {
        return commentsList.size();
    }

    public class CommentViewHolder extends RecyclerView.ViewHolder{
        TextView mComment,mUserName;
        CircleImageView circleImageView;
        View mView;

        public CommentViewHolder(@NonNull View itemView) {
            super(itemView);
            mView = itemView;
        }

        public void setmComment(String comment) {
            mComment = mView.findViewById(R.id.comment_tv);
            mComment.setText(comment);
        }

        public void setmUserName(String userName){
            mUserName = mView.findViewById(R.id.comment_user);
            mUserName.setText(userName);
        }

        public void setCircleImageView(String profilePic){
            circleImageView = mView.findViewById(R.id.comment_Profile_pic);
            Glide.with(context).load(profilePic).into(circleImageView);
        }
    }
}
