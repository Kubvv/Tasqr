/* SKILLS ADAPTER STYLING AND BEHAVIOUR */
package com.example.tasqr.Styling;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.example.tasqr.R;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

    private ArrayList<String> skills = new ArrayList<>();
    private final OnSkillListener onSkillListener;
    private final boolean[] selected;
    private final ArrayList<View>itemViewList = new ArrayList<>();
    private final boolean isEditable;

    public RecyclerViewAdapter(ArrayList<String> skills, Context context, OnSkillListener onSkillListener, boolean[] selected, boolean isEditable) {
        this.skills = skills;
        this.onSkillListener = onSkillListener;
        this.selected = selected;
        this.isEditable = isEditable;
    }

    @Override
    public @NotNull ViewHolder onCreateViewHolder(@NonNull @org.jetbrains.annotations.NotNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.skill_item, parent, false);
        return new ViewHolder(view, onSkillListener, isEditable);
    }

    @Override
    public void onBindViewHolder(@NonNull @org.jetbrains.annotations.NotNull RecyclerViewAdapter.ViewHolder holder, int position) {
        holder.skill.setText(skills.get(position));
        holder.skillActive.setText(skills.get(position));
        if(selected[position] && isEditable) {
            holder.skill.setVisibility(View.GONE);
            holder.skillActive.setVisibility(View.VISIBLE);
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
        TextView skillActive;
        ConstraintLayout parentLayout;
        OnSkillListener onSkillListener;
        boolean isEditable;

        public ViewHolder(View itemView, OnSkillListener onSkillListener, boolean isEditable) {
            super(itemView);
            skill = itemView.findViewById(R.id.skill_text);
            skillActive = itemView.findViewById(R.id.skill_text_active);
            parentLayout = itemView.findViewById(R.id.parent_layout);
            this.onSkillListener = onSkillListener;
            this.isEditable = isEditable;

            if (isEditable)
                itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            if (isEditable) {
                int pos = getAdapterPosition();
                TextView tag = v.findViewById(R.id.skill_text);
                TextView tagActive = v.findViewById(R.id.skill_text_active);

                if (selected[pos]) {
                    tag.setVisibility(View.VISIBLE);
                    tagActive.setVisibility(View.GONE);
                } else {
                    tag.setVisibility(View.GONE);
                    tagActive.setVisibility(View.VISIBLE);
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
