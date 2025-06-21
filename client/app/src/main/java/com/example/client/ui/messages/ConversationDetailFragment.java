package com.example.client.ui.messages;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.client.R;
import com.example.client.api.ApiClient;
import com.example.client.api.BinomeApiService;
import com.example.client.api.ApiModels;
import com.example.client.api.DebugLogger;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.button.MaterialButton;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class ConversationDetailFragment extends Fragment {
    
    private RecyclerView messagesRecyclerView;
    private EditText messageInput;
    private MaterialButton sendButton;
    private MaterialToolbar toolbar;
    private MessagesAdapter messagesAdapter;
    private BinomeApiService apiService;
    private DebugLogger debugLogger;
    private List<MessageItem> messages = new ArrayList<>();
    private String conversationPartner;
    private String currentUsername = "You"; // This should be loaded from user session
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        debugLogger = DebugLogger.getInstance(requireContext());
        
        // Get the conversation partner from arguments
        if (getArguments() != null) {
            conversationPartner = getArguments().getString("username", "Unknown User");
        }
        
        debugLogger.logScreenView("CONVERSATION_DETAIL");
        debugLogger.logAction("LIFECYCLE", "CONVERSATION", "ConversationDetailFragment onCreateView started for: " + conversationPartner);
        
        return inflater.inflate(R.layout.fragment_conversation_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        debugLogger.logAction("LIFECYCLE", "CONVERSATION", "ConversationDetailFragment onViewCreated started");
        
        initializeViews(view);
        setupToolbar();
        setupRecyclerView();
        setupButtons();
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        debugLogger.logAppEvent("API_INIT", "ApiService instance retrieved in ConversationDetailFragment");
        
        loadMessages();
    }
    
    private void initializeViews(View view) {
        debugLogger.logAction("UI_INIT", "CONVERSATION", "Initializing conversation detail views");
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        toolbar = view.findViewById(R.id.conversation_toolbar);
    }
    
    private void setupToolbar() {
        if (toolbar != null && conversationPartner != null) {
            toolbar.setTitle(conversationPartner);
            toolbar.setNavigationIcon(R.drawable.ic_arrow_back);
            toolbar.setNavigationOnClickListener(v -> {
                debugLogger.logUserAction("CONVERSATION", "NAVIGATION", "User clicked back button");
                requireActivity().onBackPressed();
            });
        }
    }
    
    private void setupRecyclerView() {
        debugLogger.logAction("UI_SETUP", "CONVERSATION", "Setting up RecyclerView for messages");
        messagesAdapter = new MessagesAdapter(messages, currentUsername);
        LinearLayoutManager layoutManager = new LinearLayoutManager(getContext());
        layoutManager.setStackFromEnd(true); // Start from bottom (most recent messages)
        messagesRecyclerView.setLayoutManager(layoutManager);
        messagesRecyclerView.setAdapter(messagesAdapter);
    }
    
    private void setupButtons() {
        debugLogger.logAction("UI_SETUP", "CONVERSATION", "Setting up button click listeners");
        sendButton.setOnClickListener(v -> sendMessage());
    }
    
    private void loadMessages() {
        debugLogger.logApiCall("CONVERSATION", "/api/messages/conversation", "GET");
        
        // For now, create mock messages since the API endpoint might not be fully implemented
        createMockMessages();
        
        // TODO: Implement actual API call when backend is ready
        /*
        Call<List<ApiModels.Message>> call = apiService.getMessagesForConversation(conversationPartner);
        
        if (call != null) {
            call.enqueue(new Callback<List<ApiModels.Message>>() {
                @Override
                public void onResponse(Call<List<ApiModels.Message>> call, Response<List<ApiModels.Message>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        debugLogger.logApiSuccess("CONVERSATION", "/api/messages/conversation", "GET");
                        
                        messages.clear();
                        for (ApiModels.Message dto : response.body()) {
                            messages.add(new MessageItem(
                                dto.senderName,
                                dto.content,
                                dto.timestamp,
                                dto.senderName.equals(currentUsername)
                            ));
                        }
                        messagesAdapter.notifyDataSetChanged();
                        scrollToBottom();
                    } else {
                        debugLogger.logApiError("CONVERSATION", "/api/messages/conversation", "GET", "Response code: " + response.code());
                        createMockMessages();
                    }
                }
                
                @Override
                public void onFailure(Call<List<ApiModels.Message>> call, Throwable t) {
                    debugLogger.logApiError("CONVERSATION", "/api/messages/conversation", "GET", "Network failure: " + t.getMessage());
                    createMockMessages();
                }
            });
        } else {
            createMockMessages();
        }
        */
    }
    
    private void createMockMessages() {
        debugLogger.logAction("CONVERSATION_DATA", "CONVERSATION", "Creating mock messages for conversation with: " + conversationPartner);
        messages.clear();
        
        // Create some realistic conversation based on the conversation partner
        messages.add(new MessageItem(
            conversationPartner,
            "Hey! I saw we're both studying Computer Science. Want to work on a project together?",
            "2 hours ago",
            false
        ));
        
        messages.add(new MessageItem(
            currentUsername,
            "Hi! That sounds great! What kind of project were you thinking about?",
            "2 hours ago",
            true
        ));
        
        messages.add(new MessageItem(
            conversationPartner,
            "I was thinking maybe something with machine learning or web development. I'm particularly interested in AI applications.",
            "1 hour ago",
            false
        ));
        
        messages.add(new MessageItem(
            currentUsername,
            "Perfect! I've been working on some ML projects lately. Do you have experience with Python and TensorFlow?",
            "1 hour ago",
            true
        ));
        
        messages.add(new MessageItem(
            conversationPartner,
            "Yes! I've done a few projects with both. Maybe we could build something that combines computer vision with natural language processing?",
            "45 minutes ago",
            false
        ));
        
        messages.add(new MessageItem(
            currentUsername,
            "That's exactly what I was thinking! When would be a good time to meet up and discuss this further?",
            "30 minutes ago",
            true
        ));
        
        messagesAdapter.notifyDataSetChanged();
        scrollToBottom();
        debugLogger.logAction("CONVERSATION_DATA", "CONVERSATION", "Created " + messages.size() + " mock messages");
    }
    
    private void sendMessage() {
        String messageText = messageInput.getText().toString().trim();
        if (messageText.isEmpty()) {
            return;
        }
        
        debugLogger.logUserAction("CONVERSATION", "SEND_MESSAGE", "User sending message to: " + conversationPartner);
        
        // Add message to local list immediately for better UX
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm a", Locale.getDefault());
        String timestamp = sdf.format(new Date());
        
        MessageItem newMessage = new MessageItem(currentUsername, messageText, timestamp, true);
        messages.add(newMessage);
        messagesAdapter.notifyItemInserted(messages.size() - 1);
        scrollToBottom();
        
        // Clear input
        messageInput.setText("");
        
        // TODO: Send message to API
        debugLogger.logApiCall("CONVERSATION", "/api/messages/send", "POST");
        
        // Mock response after a short delay
        messagesRecyclerView.postDelayed(() -> {
            String response = generateMockResponse(messageText);
            MessageItem responseMessage = new MessageItem(conversationPartner, response, timestamp, false);
            messages.add(responseMessage);
            messagesAdapter.notifyItemInserted(messages.size() - 1);
            scrollToBottom();
            debugLogger.logAction("CONVERSATION_DATA", "CONVERSATION", "Received mock response from: " + conversationPartner);
        }, 1500); // Simulate network delay
    }
    
    private String generateMockResponse(String userMessage) {
        // Simple mock response generator
        if (userMessage.toLowerCase().contains("when") || userMessage.toLowerCase().contains("time")) {
            return "How about tomorrow at 2 PM in the library? Does that work for you?";
        } else if (userMessage.toLowerCase().contains("meet") || userMessage.toLowerCase().contains("discuss")) {
            return "Sounds good! I'm free most afternoons this week.";
        } else if (userMessage.toLowerCase().contains("project") || userMessage.toLowerCase().contains("work")) {
            return "Great! I have some ideas we could explore. Let's discuss them when we meet.";
        } else {
            return "That sounds interesting! Tell me more about that.";
        }
    }
    
    private void scrollToBottom() {
        if (messages.size() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }
    
    // Inner class for message items
    public static class MessageItem {
        private String senderName;
        private String content;
        private String timestamp;
        private boolean isFromCurrentUser;
        
        public MessageItem(String senderName, String content, String timestamp, boolean isFromCurrentUser) {
            this.senderName = senderName;
            this.content = content;
            this.timestamp = timestamp;
            this.isFromCurrentUser = isFromCurrentUser;
        }
        
        // Getters
        public String getSenderName() { return senderName; }
        public String getContent() { return content; }
        public String getTimestamp() { return timestamp; }
        public boolean isFromCurrentUser() { return isFromCurrentUser; }
    }
}