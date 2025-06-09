package com.example.client.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class ConversationsAdapter extends RecyclerView.Adapter<ConversationsAdapter.ConversationViewHolder> {
    
    private List<MessagesFragment.ConversationItem> conversations;
    private MessagesFragment.OnConversationSelectedListener listener;
    
    public ConversationsAdapter(List<MessagesFragment.ConversationItem> conversations, 
                               MessagesFragment.OnConversationSelectedListener listener) {
        this.conversations = conversations;
        this.listener = listener;
    }
    
    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_conversation, parent, false);
        return new ConversationViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        holder.bind(conversations.get(position), listener);
    }
    
    @Override
    public int getItemCount() {
        return conversations.size();
    }
    
    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView nameText;
        private final MaterialTextView lastMessageText;
        private final MaterialTextView timestampText;
        private final View unreadIndicator;
        
        public ConversationViewHolder(@NonNull View itemView) {
            super(itemView);
            nameText = itemView.findViewById(R.id.name_text);
            lastMessageText = itemView.findViewById(R.id.last_message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            unreadIndicator = itemView.findViewById(R.id.unread_indicator);
        }
        
        public void bind(MessagesFragment.ConversationItem conversation, 
                        MessagesFragment.OnConversationSelectedListener listener) {
            nameText.setText(conversation.getName());
            lastMessageText.setText(conversation.getLastMessage());
            timestampText.setText(conversation.getTimestamp());
            
            // Show/hide unread indicator and style text accordingly
            if (conversation.hasUnreadMessages()) {
                unreadIndicator.setVisibility(View.VISIBLE);
                nameText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_700));
                lastMessageText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_600));
                nameText.setTypeface(nameText.getTypeface(), android.graphics.Typeface.BOLD);
            } else {
                unreadIndicator.setVisibility(View.GONE);
                nameText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_900));
                lastMessageText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_500));
                nameText.setTypeface(nameText.getTypeface(), android.graphics.Typeface.NORMAL);
            }
            
            itemView.setOnClickListener(v -> listener.onConversationSelected(conversation));
        }
    }
} 