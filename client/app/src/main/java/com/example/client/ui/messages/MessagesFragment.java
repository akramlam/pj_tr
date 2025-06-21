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
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagesFragment extends Fragment {
    
    private RecyclerView conversationsRecyclerView;
    private ConversationsAdapter conversationsAdapter;
    private MaterialCardView emptyStateCard;
    private MaterialButton findMatchesButton;
    private BinomeApiService apiService;
    private DebugLogger debugLogger;
    private List<ConversationItem> conversations = new ArrayList<>();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        debugLogger = DebugLogger.getInstance(requireContext());
        debugLogger.logScreenView("MESSAGES");
        debugLogger.logAction("LIFECYCLE", "MESSAGES", "MessagesFragment onCreateView started");
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        debugLogger.logAction("LIFECYCLE", "MESSAGES", "MessagesFragment onViewCreated started");
        
        initializeViews(view);
        setupRecyclerView();
        setupButtons();
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        debugLogger.logAppEvent("API_INIT", "ApiService instance retrieved in MessagesFragment");
        loadConversations();
    }

    @Override
    public void onResume() {
        super.onResume();
        debugLogger.logScreenView("MESSAGES");
        // Refresh conversations when returning to messages
        loadConversations();
    }
    
    private void initializeViews(View view) {
        debugLogger.logAction("UI_INIT", "MESSAGES", "Initializing messages fragment views");
        conversationsRecyclerView = view.findViewById(R.id.conversations_recycler_view);
        emptyStateCard = view.findViewById(R.id.empty_state_card);
        findMatchesButton = view.findViewById(R.id.find_matches_button);
    }
    
    private void setupRecyclerView() {
        debugLogger.logAction("UI_SETUP", "MESSAGES", "Setting up RecyclerView for conversations");
        conversationsAdapter = new ConversationsAdapter(conversations, this::onConversationSelected);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationsRecyclerView.setAdapter(conversationsAdapter);
    }
    
    private void setupButtons() {
        debugLogger.logAction("UI_SETUP", "MESSAGES", "Setting up button click listeners");
        findMatchesButton.setOnClickListener(v -> {
            debugLogger.logButtonClick("MESSAGES", "FIND_MATCHES");
            debugLogger.logUserAction("MESSAGES", "NAVIGATION", "User clicked find matches button");
            Navigation.findNavController(v).navigate(R.id.nav_matches);
        });
    }
    
    private void loadConversations() {
        debugLogger.logApiCall("MESSAGES", "/api/messages/conversations", "GET");
        
        // First try to load from API
        Call<List<ApiModels.Conversation>> call = apiService.getConversations();
        
        if (call != null) {
            call.enqueue(new Callback<List<ApiModels.Conversation>>() {
                @Override
                public void onResponse(Call<List<ApiModels.Conversation>> call, Response<List<ApiModels.Conversation>> response) {
                    if (response.isSuccessful() && response.body() != null) {
                        debugLogger.logApiSuccess("MESSAGES", "/api/messages/conversations", "GET");
                        debugLogger.logAction("MESSAGES_DATA", "MESSAGES", "Loaded " + response.body().size() + " conversations from API");
                        
                        conversations.clear();
                        for (ApiModels.Conversation dto : response.body()) {
                            conversations.add(new ConversationItem(
                                dto.displayName,
                                dto.lastMessage,
                                dto.timestamp,
                                dto.hasUnreadMessages
                            ));
                        }
                        conversationsAdapter.notifyDataSetChanged();
                        updateEmptyState();
                    } else {
                        debugLogger.logApiError("MESSAGES", "/api/messages/conversations", "GET", "Response code: " + response.code());
                        // Fall back to mock conversations
                        createMockConversations();
                    }
                }
                
                @Override
                public void onFailure(Call<List<ApiModels.Conversation>> call, Throwable t) {
                    debugLogger.logApiError("MESSAGES", "/api/messages/conversations", "GET", "Network failure: " + t.getMessage());
                    
                    // Check if this is a network connectivity issue
                    if (isNetworkError(t)) {
                        debugLogger.logAppEvent("FALLBACK", "Enabling mock mode for conversations loading");
                        ApiClient.getInstance(getContext()).enableMockMode(true);
                        Toast.makeText(getContext(), "Backend unavailable. Using offline mode.", Toast.LENGTH_LONG).show();
                        loadConversations(); // Retry with mock service
                    } else {
                        // Fall back to mock conversations
                        createMockConversations();
                    }
                }
            });
        } else {
            // API service doesn't have the method, use mock data
            debugLogger.logAction("MESSAGES_DATA", "MESSAGES", "API method not available, using mock conversations");
            createMockConversations();
        }
    }
    
    private void createMockConversations() {
        debugLogger.logAction("MESSAGES_DATA", "MESSAGES", "Creating mock conversations");
        conversations.clear();
        
        // Add some sample conversations
        conversations.add(new ConversationItem(
            "Alice Johnson",
            "Hey! I saw we're both studying Computer Science. Want to work on a project together?",
            "2 hours ago",
            true
        ));
        
        conversations.add(new ConversationItem(
            "Bob Smith", 
            "Thanks for the help with the algorithms assignment! Really appreciate it.",
            "1 day ago",
            false
        ));
        
        conversations.add(new ConversationItem(
            "Carol Davis",
            "Are you available for the group project meeting tomorrow at 3 PM?",
            "2 days ago",
            false
        ));
        
        conversations.add(new ConversationItem(
            "David Wilson",
            "Great match! I'm also interested in machine learning projects. Let's chat!",
            "5 hours ago",
            true
        ));
        
        conversationsAdapter.notifyDataSetChanged();
        updateEmptyState();
        debugLogger.logAction("MESSAGES_DATA", "MESSAGES", "Created " + conversations.size() + " mock conversations");
    }
    
    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            debugLogger.logAction("UI_STATE", "MESSAGES", "Showing empty state - no conversations");
            emptyStateCard.setVisibility(View.VISIBLE);
            conversationsRecyclerView.setVisibility(View.GONE);
        } else {
            debugLogger.logAction("UI_STATE", "MESSAGES", "Showing conversations list with " + conversations.size() + " items");
            emptyStateCard.setVisibility(View.GONE);
            conversationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void onConversationSelected(ConversationItem conversation) {
        debugLogger.logUserAction("MESSAGES", "CONVERSATION_SELECTED", "User selected conversation with: " + conversation.getName());
        
        // Navigate to conversation detail fragment
        Bundle args = new Bundle();
        args.putString("username", conversation.getName());
        Navigation.findNavController(requireView()).navigate(R.id.nav_conversation_detail, args);
    }
    
    private boolean isNetworkError(Throwable t) {
        return t instanceof java.net.ConnectException || 
               t instanceof java.net.SocketTimeoutException ||
               t instanceof java.io.IOException;
    }
    
    // Inner class for conversation items
    public static class ConversationItem {
        private String name;
        private String lastMessage;
        private String timestamp;
        private boolean hasUnreadMessages;
        
        public ConversationItem(String name, String lastMessage, String timestamp, boolean hasUnreadMessages) {
            this.name = name;
            this.lastMessage = lastMessage;
            this.timestamp = timestamp;
            this.hasUnreadMessages = hasUnreadMessages;
        }
        
        // Getters
        public String getName() { return name; }
        public String getLastMessage() { return lastMessage; }
        public String getTimestamp() { return timestamp; }
        public boolean hasUnreadMessages() { return hasUnreadMessages; }
    }
    
    public interface OnConversationSelectedListener {
        void onConversationSelected(ConversationItem conversation);
    }
} 