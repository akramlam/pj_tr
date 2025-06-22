package com.example.client.ui.matches;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class StudyPartnersAdapter extends RecyclerView.Adapter<StudyPartnersAdapter.StudyPartnerViewHolder> {
    
    private List<MatchesFragment.PotentialMatch> studyPartners;
    
    public StudyPartnersAdapter(List<MatchesFragment.PotentialMatch> studyPartners) {
        this.studyPartners = studyPartners != null ? studyPartners : new ArrayList<>();
    }
    
    @NonNull
    @Override
    public StudyPartnerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_potential_match, parent, false);
        return new StudyPartnerViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull StudyPartnerViewHolder holder, int position) {
        MatchesFragment.PotentialMatch partner = studyPartners.get(position);
        holder.bind(partner);
    }
    
    @Override
    public int getItemCount() {
        return studyPartners != null ? studyPartners.size() : 0;
    }
    
    public void updateData(List<MatchesFragment.PotentialMatch> newStudyPartners) {
        if (this.studyPartners == null) {
            this.studyPartners = new ArrayList<>();
        }
        this.studyPartners.clear();
        if (newStudyPartners != null) {
            this.studyPartners.addAll(newStudyPartners);
        }
        notifyDataSetChanged();
    }
    
    class StudyPartnerViewHolder extends RecyclerView.ViewHolder {
        private TextView partnerName;
        private TextView partnerFormation;
        private TextView compatibilityScore;
        private ChipGroup skillsChipGroup;
        private MaterialCardView cardView;
        
        public StudyPartnerViewHolder(@NonNull View itemView) {
            super(itemView);
            partnerName = itemView.findViewById(R.id.username_text);
            partnerFormation = itemView.findViewById(R.id.formation_text);
            compatibilityScore = itemView.findViewById(R.id.compatibility_text);
            skillsChipGroup = itemView.findViewById(R.id.skills_chip_group);
            cardView = (MaterialCardView) itemView;
        }
        
        public void bind(MatchesFragment.PotentialMatch partner) {
            partnerName.setText(partner.getUsername());
            partnerFormation.setText(partner.getFormation());
            compatibilityScore.setText(partner.getCompatibilityScore() + "% Match");
            
            // Clear existing chips
            skillsChipGroup.removeAllViews();
            
            // Add skill chips
            if (partner.getCommonSkills() != null) {
                for (String skill : partner.getCommonSkills()) {
                    Chip chip = new Chip(itemView.getContext());
                    chip.setText(skill);
                    chip.setChipBackgroundColorResource(R.color.primary_blue_light);
                    chip.setTextColor(itemView.getContext().getColor(R.color.text_on_primary));
                    chip.setClickable(false);
                    skillsChipGroup.addView(chip);
                }
            }
        }
    }
} 