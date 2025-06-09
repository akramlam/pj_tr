package com.example.client.ui.matches;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.example.client.api.ApiModels;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import java.util.List;

public class MatchesAdapter extends RecyclerView.Adapter<MatchesAdapter.MatchViewHolder> {
    
    private List<ApiModels.PotentialMatch> matches;
    private MatchesFragment.OnMatchActionListener listener;
    
    public MatchesAdapter(List<ApiModels.PotentialMatch> matches, MatchesFragment.OnMatchActionListener listener) {
        this.matches = matches;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_potential_match, parent, false);
        return new MatchViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        holder.bind(matches.get(position), listener);
    }
    
    @Override
    public int getItemCount() {
        return matches.size();
    }
    
    static class MatchViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView usernameText;
        private final MaterialTextView formationText;
        private final MaterialTextView compatibilityText;
        private final ChipGroup skillsChipGroup;
        private final MaterialButton likeButton;
        private final MaterialButton passButton;
        
        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            usernameText = itemView.findViewById(R.id.username_text);
            formationText = itemView.findViewById(R.id.formation_text);
            compatibilityText = itemView.findViewById(R.id.compatibility_text);
            skillsChipGroup = itemView.findViewById(R.id.skills_chip_group);
            likeButton = itemView.findViewById(R.id.like_button);
            passButton = itemView.findViewById(R.id.pass_button);
        }
        
        public void bind(ApiModels.PotentialMatch match, MatchesFragment.OnMatchActionListener listener) {
            usernameText.setText(match.username);
            formationText.setText(match.formation != null ? match.formation : "Formation not specified");
            compatibilityText.setText(String.format("%d%%", match.compatibilityScore));
            
            skillsChipGroup.removeAllViews();
            if (match.commonSkills != null) {
                for (String skill : match.commonSkills) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(skill);
                    chip.setChipBackgroundColorResource(R.color.purple_100);
                    chip.setTextColor(itemView.getContext().getColor(R.color.purple_700));
                    skillsChipGroup.addView(chip);
                }
            }
            
            likeButton.setOnClickListener(v -> listener.onMatchAction(match, true));
            passButton.setOnClickListener(v -> listener.onMatchAction(match, false));
        }
    }
} 