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
import com.example.client.api.ApiClient;
import com.example.client.api.BinomeApiService;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textview.MaterialTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ConversationDetailFragment extends Fragment {
    
    private RecyclerView messagesRecyclerView;
    private MessagesAdapter messagesAdapter;
    private TextInputEditText messageInput;
    private MaterialButton sendButton;
    private MaterialTextView conversationTitle;
    private MaterialButton backButton;
    private BinomeApiService apiService;
    private DebugLogger debugLogger;
    private List<MessageItem> messages = new ArrayList<>();
    private String conversationPartnerName;
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        debugLogger = DebugLogger.getInstance(requireContext());
        debugLogger.logScreenView("CONVERSATION_DETAIL");
        debugLogger.logAction("LIFECYCLE", "CONVERSATION_DETAIL", "ConversationDetailFragment onCreateView started");
        return inflater.inflate(R.layout.fragment_conversation_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        debugLogger.logAction("LIFECYCLE", "CONVERSATION_DETAIL", "ConversationDetailFragment onViewCreated started");
        
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
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        debugLogger.logAppEvent("API_INIT", "ApiService instance retrieved in ConversationDetailFragment");
        loadMessages();
    }
    
    private void initializeViews(View view) {
        debugLogger.logAction("UI_INIT", "CONVERSATION_DETAIL", "Initializing conversation detail fragment views");
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        conversationTitle = view.findViewById(R.id.conversation_title);
        backButton = view.findViewById(R.id.back_button);
    }
    
    private void setupRecyclerView() {
        debugLogger.logAction("UI_SETUP", "CONVERSATION_DETAIL", "Setting up RecyclerView for messages");
        messagesAdapter = new MessagesAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messagesAdapter);
    }
    
    private void setupTitle() {
        conversationTitle.setText(conversationPartnerName);
    }
    
    private void setupButtons() {
        debugLogger.logAction("UI_SETUP", "CONVERSATION_DETAIL", "Setting up button click listeners");
        
        backButton.setOnClickListener(v -> {
            debugLogger.logButtonClick("CONVERSATION_DETAIL", "BACK");
            debugLogger.logUserAction("CONVERSATION_DETAIL", "NAVIGATION", "User clicked back button");
            Navigation.findNavController(v).navigateUp();
        });
        
        sendButton.setOnClickListener(v -> {
            debugLogger.logButtonClick("CONVERSATION_DETAIL", "SEND_MESSAGE");
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty()) {
                sendMessage(messageText);
            }
        });
    }
    
    private void loadMessages() {
        debugLogger.logApiCall("CONVERSATION_DETAIL", "/api/messages/" + conversationPartnerName, "GET");
        
        // For now, create mock messages since the API might not be fully implemented
        createMockMessages();
    }
    
    private void createMockMessages() {
        debugLogger.logAction("MESSAGES_DATA", "CONVERSATION_DETAIL", "Creating mock messages");
        messages.clear();
        
        // Add some sample messages
        messages.add(new MessageItem(
            "Hey! I saw we're both studying Computer Science. Want to work on a project together?",
            false, // received message
            "10:30 AM"
        ));
        
        messages.add(new MessageItem(
            "That sounds great! I'm really interested in machine learning projects. What did you have in mind?",
            true, // sent message
            "10:35 AM"
        ));
        
        messages.add(new MessageItem(
            "I was thinking we could build a recommendation system for students to find study groups.",
            false,
            "10:40 AM"
        ));
        
        messages.add(new MessageItem(
            "Perfect! I have some experience with Python and TensorFlow. When can we start?",
            true,
            "10:45 AM"
        ));
        
        messagesAdapter.notifyDataSetChanged();
        scrollToBottom();
        debugLogger.logAction("MESSAGES_DATA", "CONVERSATION_DETAIL", "Created " + messages.size() + " mock messages");
    }
    
    private void sendMessage(String messageText) {
        debugLogger.logUserAction("CONVERSATION_DETAIL", "SEND_MESSAGE", "User sending message: " + messageText);
        
        // Add message to the list immediately (optimistic UI)
        messages.add(new MessageItem(messageText, true, "Now"));
        messagesAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Clear the input
        messageInput.setText("");
        
        // TODO: Send to API
        // For now, just show a confirmation
        Toast.makeText(getContext(), "Message sent!", Toast.LENGTH_SHORT).show();
        
        debugLogger.logAction("MESSAGE_SENT", "CONVERSATION_DETAIL", "Message added to conversation");
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