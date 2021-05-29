package com.example.tasqr;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private static final String TAG = "RecyclerViewAdapter";

    private ArrayList<String> skills = new ArrayList<>();
    private OnSkillListener onSkillListener;
    private Context context;
    private boolean[] selected;
    private ArrayList<View>itemViewList = new ArrayList<>();
    private boolean isEditable;

    public RecyclerViewAdapter(ArrayList<String> skills, Context context, OnSkillListener onSkillListener, boolean[] selected, boolean isEditable) {
        Log.e(TAG, "onCreate: " + skills.size());
        this.skills = skills;
        this.context = context;
        this.onSkillListener = onSkillListener;
        this.selected = selected;
        this.isEditable = isEditable;
    }

    @Override
    public @NotNull ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.skill_item, parent, false);
        ViewHolder holder = new ViewHolder(view, onSkillListener, isEditable);

        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.skill.setText(skills.get(position));

        if (selected[position]) {
            ImageView tag = holder.itemView.findViewById(R.id.skillImage);
            tag.setImageResource(R.drawable.tagmarked);
        }
    }

    @Override
    public int getItemCount() {
        return skills.size();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        TextView skill;
        ConstraintLayout parentLayout;
        OnSkillListener onSkillListener;
        boolean isEditable;

        public ViewHolder(View itemView, OnSkillListener onSkillListener, boolean isEditable) {
            super(itemView);
            skill = itemView.findViewById(R.id.skillName);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            this.onSkillListener = onSkillListener;
            this.isEditable = isEditable;

            if (isEditable) {
                itemView.setOnClickListener(this);
            }
        }

        @Override
        public void onClick(View v) {
            if (isEditable) {
                int pos = getAdapterPosition();
                ImageView tag = v.findViewById(R.id.skillImage);
                if (selected[pos]) {
                    tag.setImageResource(R.drawable.tag);
                } else {
                    tag.setImageResource(R.drawable.tagmarked);
                }
                selected[pos] = !selected[pos];
                onSkillListener.onSkillClick(pos);
            }
        }
    }

    public interface OnSkillListener {
        void onSkillClick(int position);
    }
}
