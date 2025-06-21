package com.example.client.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class MessagesAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    
    private static final int TYPE_SENT = 1;
    private static final int TYPE_RECEIVED = 2;
    
    private List<ConversationDetailFragment.MessageItem> messages;
    
    public MessagesAdapter(List<ConversationDetailFragment.MessageItem> messages) {
        this.messages = messages;
    }
    
    @Override
    public int getItemViewType(int position) {
        return messages.get(position).isSent() ? TYPE_SENT : TYPE_RECEIVED;
    }
    
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_SENT) {
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
        
        public SentMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }
        
        public void bind(ConversationDetailFragment.MessageItem message) {
            messageText.setText(message.getContent());
            timestampText.setText(message.getTimestamp());
        }
    }
    
    static class ReceivedMessageViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView messageText;
        private final MaterialTextView timestampText;
        
        public ReceivedMessageViewHolder(@NonNull View itemView) {
            super(itemView);
            messageText = itemView.findViewById(R.id.message_text);
            timestampText = itemView.findViewById(R.id.timestamp_text);
        }
        
        public void bind(ConversationDetailFragment.MessageItem message) {
            messageText.setText(message.getContent());
            timestampText.setText(message.getTimestamp());
        }
    }
}