package com.example.client.ui.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ConversationDetailFragment extends Fragment {
    
    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private MaterialTextView conversationTitle;
    private MaterialButton backButton;
    private List<MessageItem> messages = new ArrayList<>();
    private String conversationPartnerName;
    private FloatingActionButton fab;
    private MessageStorage messageStorage;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        // Initialize message storage
        messageStorage = MessageStorage.getInstance();
        
        // Hide the floating action button since it's not needed in this view
        fab = getActivity().findViewById(R.id.fab);
        if (fab != null) {
            fab.hide();
        }
        
        // Get the conversation partner name from arguments
        if (getArguments() != null) {
            conversationPartnerName = getArguments().getString("username", "Unknown");
        } else {
            conversationPartnerName = "Unknown";
        }
        
        initializeViews(view);
        setupRecyclerView();
        setupButtons();
        setupTitle();
        loadMessages();
    }
    
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        // Show the floating action button again when leaving this view
        if (fab != null) {
            fab.show();
        }
    }
    
    private void initializeViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        conversationTitle = view.findViewById(R.id.conversation_title);
        backButton = view.findViewById(R.id.back_button);
    }
    
    private void setupRecyclerView() {
        messagesAdapter = new MessagesAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messagesAdapter);
    }
    
    private void setupTitle() {
        conversationTitle.setText(conversationPartnerName);
    }
    
    private void setupButtons() {
        backButton.setOnClickListener(v -> {
            Navigation.findNavController(v).navigateUp();
        });
        
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
    }
    
    private void loadMessages() {
        // Load messages from persistent storage
        messages.clear();
        List<MessageItem> storedMessages = messageStorage.getMessages(conversationPartnerName);
        messages.addAll(storedMessages);
        messagesAdapter.notifyDataSetChanged();
        scrollToBottom();
    }
    
    private void sendMessage(String messageText) {
        // Generate current timestamp
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        String currentTime = sdf.format(new Date());
        
        // Create new message
        MessageItem newMessage = new MessageItem(messageText, true, currentTime);
        
        // Add to local list
        messages.add(newMessage);
        
        // Add to persistent storage
        messageStorage.addMessage(conversationPartnerName, newMessage);
        
        // Update UI
        messagesAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Clear the input
        messageInput.setText("");
        
        // Show confirmation
        Toast.makeText(getContext(), "Message sent!", Toast.LENGTH_SHORT).show();
    }
    
    private void scrollToBottom() {
        if (messages.size() > 0) {
            messagesRecyclerView.scrollToPosition(messages.size() - 1);
        }
    }
    
    // Inner class for message items
    public static class MessageItem {
        private String content;
        private boolean isSent; // true if sent by current user, false if received
        private String timestamp;
        
        public MessageItem(String content, boolean isSent, String timestamp) {
            this.content = content;
            this.isSent = isSent;
            this.timestamp = timestamp;
        }
        
        // Getters
        public String getContent() { return content; }
        public boolean isSent() { return isSent; }
        public String getTimestamp() { return timestamp; }
    }
}