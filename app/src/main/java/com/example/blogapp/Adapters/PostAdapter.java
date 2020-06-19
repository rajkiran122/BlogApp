package com.example.blogapp.Adapters;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.blogapp.Activities.PostDetailActivity;
import com.example.blogapp.Models.Posts;
import com.example.blogapp.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.myListViewHolder> {
    Context mContext;
    List<Posts> mLists;

    public PostAdapter(Context mContext, List<Posts> mLists) {
        this.mContext = mContext;
        this.mLists = mLists;
    }

    @NonNull
    @Override
    public myListViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {

      View row = LayoutInflater.from(mContext).inflate(R.layout.row_post_item,parent,false);

        return new myListViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull myListViewHolder holder, int position) {

        holder.title.setText(mLists.get(position).getTitle());
        Glide.with(mContext).load(mLists.get(position).getPicture()).into(holder.postImage);
        Glide.with(mContext).load(mLists.get(position).getUserPhoto()).into(holder.userPhoto);

        holder.userPhoto.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.fade_transition_animation));
        holder.rLayout.setAnimation(AnimationUtils.loadAnimation(mContext,R.anim.rellayout_anim));



    }

    @Override
    public int getItemCount() {
        return mLists.size();
    }

    public class myListViewHolder extends RecyclerView.ViewHolder {

        TextView title;
        ImageView postImage, userPhoto;
        RelativeLayout rLayout;

        public myListViewHolder(@NonNull View itemView) {
            super(itemView);

            title = itemView.findViewById(R.id.row_title_name);
            postImage = itemView.findViewById(R.id.row_postimage);
            userPhoto = itemView.findViewById(R.id.row_userPhoto);
            rLayout = itemView.findViewById(R.id.relLay);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    Intent postDetailActivity = new Intent(mContext, PostDetailActivity.class);
                    int pos = getAdapterPosition();

                    postDetailActivity.putExtra("title",mLists.get(pos).getTitle());
                    postDetailActivity.putExtra("postImage",mLists.get(pos).getPicture());
                    postDetailActivity.putExtra("description",mLists.get(pos).getDescription());
                    postDetailActivity.putExtra("postKey",mLists.get(pos).getPostKey());
                    postDetailActivity.putExtra("userPhoto",mLists.get(pos).getUserPhoto());
//                    postDetailActivity.putExtra("userName",mLists.get(pos).getUserName());

                    long timestamp = (long) mLists.get(pos).getTimeStamp();
                    postDetailActivity.putExtra("date",timestamp);
                    mContext.startActivity(postDetailActivity);


                }
            });

        }
    }

}
