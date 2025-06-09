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
    private List<ConversationItem> conversations = new ArrayList<>();
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_messages, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupButtons();
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        loadConversations();
    }
    
    private void initializeViews(View view) {
        conversationsRecyclerView = view.findViewById(R.id.conversations_recycler_view);
        emptyStateCard = view.findViewById(R.id.empty_state_card);
        findMatchesButton = view.findViewById(R.id.find_matches_button);
    }
    
    private void setupRecyclerView() {
        conversationsAdapter = new ConversationsAdapter(conversations, this::onConversationSelected);
        conversationsRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        conversationsRecyclerView.setAdapter(conversationsAdapter);
    }
    
    private void setupButtons() {
        findMatchesButton.setOnClickListener(v -> {
            // Navigate to matches fragment
            Navigation.findNavController(v).navigate(R.id.nav_matches);
        });
    }
    
    private void loadConversations() {
        // For now, we'll create mock conversations since the backend might need conversation list endpoint
        createMockConversations();
    }
    
    private void createMockConversations() {
        conversations.clear();
        
        // Add some sample conversations
        conversations.add(new ConversationItem(
            "Alice Johnson",
            "Hey! I saw we're both studying Computer Science. Want to study together?",
            "2 hours ago",
            true
        ));
        
        conversations.add(new ConversationItem(
            "Bob Smith", 
            "Thanks for the help with the algorithms assignment!",
            "1 day ago",
            false
        ));
        
        conversations.add(new ConversationItem(
            "Carol Davis",
            "Are you available for the group project meeting tomorrow?",
            "2 days ago",
            false
        ));
        
        conversationsAdapter.notifyDataSetChanged();
        updateEmptyState();
    }
    
    private void updateEmptyState() {
        if (conversations.isEmpty()) {
            emptyStateCard.setVisibility(View.VISIBLE);
            conversationsRecyclerView.setVisibility(View.GONE);
        } else {
            emptyStateCard.setVisibility(View.GONE);
            conversationsRecyclerView.setVisibility(View.VISIBLE);
        }
    }
    
    private void onConversationSelected(ConversationItem conversation) {
        // For now, show a toast. Later this could navigate to a chat detail fragment
        Toast.makeText(getContext(), "Opening chat with " + conversation.getName(), Toast.LENGTH_SHORT).show();
        
        // TODO: Navigate to conversation detail fragment
        // Bundle args = new Bundle();
        // args.putString("username", conversation.getName());
        // Navigation.findNavController(requireView()).navigate(R.id.nav_conversation_detail, args);
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