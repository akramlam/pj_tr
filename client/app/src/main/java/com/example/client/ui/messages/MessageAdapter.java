package com.example.client.ui.messages;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.example.client.api.ApiModels;
import com.google.android.material.textview.MaterialTextView;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    
    private final List<ApiModels.Message> messages;
    
    public MessageAdapter(List<ApiModels.Message> messages) {
        this.messages = messages;
    }
    
    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }
    
    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        ApiModels.Message message = messages.get(position);
        holder.bind(message);
    }
    
    @Override
    public int getItemCount() {
        return messages.size();
    }
    
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        private final MaterialTextView senderText;
        private final MaterialTextView contentText;
        private final MaterialTextView timestampText;
        
        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            senderText = itemView.findViewById(R.id.message_sender);
            contentText = itemView.findViewById(R.id.message_content);
            timestampText = itemView.findViewById(R.id.message_timestamp);
        }
        
        public void bind(ApiModels.Message message) {
            senderText.setText(message.getSender());
            contentText.setText(message.getContent());
            timestampText.setText(formatTimestamp(message.getTimestamp()));
        }
        
        private String formatTimestamp(String timestamp) {
            // TODO: Implement proper timestamp formatting
            if (timestamp == null) return "";
            try {
                return timestamp.substring(11, 16); // Show just HH:MM
            } catch (Exception e) {
                return "";
            }
        }
    }
}