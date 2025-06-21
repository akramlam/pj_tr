package com.example.client.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int VIEW_TYPE_SENT = 1;
    private static final int VIEW_TYPE_RECEIVED = 2;
    
    private List<ConversationDetailFragment.MessageItem> messages;
    private String currentUsername;
    
    public MessagesAdapter(List<ConversationDetailFragment.MessageItem> messages, String currentUsername) {
        this.messages = messages;
        this.currentUsername = currentUsername;
    }
    
    @Override
    public int getItemViewType(int position) {
        ConversationDetailFragment.MessageItem message = messages.get(position);
        return message.isFromCurrentUser() ? VIEW_TYPE_SENT : VIEW_TYPE_RECEIVED;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SENT) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_sent, parent, false);
            return new SentMessageViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_message_received, parent, false);
            return new ReceivedMessageViewHolder(view);
        }
    }
    
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ConversationDetailFragment.MessageItem message = messages.get(position);
        
        if (holder instanceof SentMessageViewHolder) {
            ((SentMessageViewHolder) holder).bind(message);
        } else if (holder instanceof ReceivedMessageViewHolder) {
            ((ReceivedMessageViewHolder) holder).bind(message);
        }
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class SentMessageViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView messageText;
        private final MaterialTextView timestampText;
        private final MaterialCardView messageCard;
        
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            messageCard = itemView.findViewById(R.id.message_card);
        }
        
        public void bind(ConversationDetailFragment.MessageItem message) {
            messageText.setText(message.getContent());
            timestampText.setText(message.getTimestamp());
            
            // Style for sent messages (blue/purple theme)
            messageCard.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_500));
            messageText.setTextColor(ContextCompat.getColor(itemView.getContext(), android.R.color.white));
            timestampText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_200));
        }
    }
    
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView senderText;
        private final MaterialTextView messageText;
        private final MaterialTextView timestampText;
        private final MaterialCardView messageCard;
        
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.sender_text);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
            messageCard = itemView.findViewById(R.id.message_card);
        }
        
        public void bind(ConversationDetailFragment.MessageItem message) {
            senderText.setText(message.getSenderName());
            messageText.setText(message.getContent());
            timestampText.setText(message.getTimestamp());
            
            // Style for received messages (light gray theme)
            messageCard.setCardBackgroundColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_50));
            messageText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_900));
            senderText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_700));
            timestampText.setTextColor(ContextCompat.getColor(itemView.getContext(), R.color.purple_400));
        }
    }
}