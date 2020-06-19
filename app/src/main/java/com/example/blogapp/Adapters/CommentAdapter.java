package com.example.blogapp.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogapp.Models.Comment;
import com.example.blogapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.myViewHolder>
{

    Context mContext;
    List<Comment> mLists;

    public CommentAdapter(Context mContext, List<Comment> mLists) {
        this.mContext = mContext;
        this.mLists = mLists;
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

        View row = LayoutInflater.from(mContext).inflate(R.layout.row_comment,parent,false);
        return new myViewHolder(row);

    }

    @Override
    public void onBindViewHolder(@NonNull myViewHolder holder, int position) {

        Glide.with(mContext).load(mLists.get(position).getuImg()).into(holder.cmt_userPhoto);
        holder.cmt_userName.setText(mLists.get(position).getuName());
        holder.cmt_content.setText(mLists.get(position).getContent());

        holder.cmt_date.setText(timeStamptoString((Long)mLists.get(position).getTimeStamp()));

    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public class myViewHolder extends RecyclerView.ViewHolder {

        TextView cmt_userName,cmt_content,cmt_date;
        CircleImageView cmt_userPhoto;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            cmt_userName = itemView.findViewById(R.id.comment_username);
            cmt_content = itemView.findViewById(R.id.comment_content);
            cmt_date = itemView.findViewById(R.id.comment_date);
            cmt_userPhoto = itemView.findViewById(R.id.comment_userPhoto);

        }
    }

    private String timeStamptoString(long time) {

        SimpleDateFormat sdf = new SimpleDateFormat("hh:mm aa"+"\t"+"dd/MM/yyyy");
        String date = sdf.format(new Date());

        return date;

    }

}
