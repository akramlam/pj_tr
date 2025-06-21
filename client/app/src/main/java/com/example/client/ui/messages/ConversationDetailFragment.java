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
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textview.MaterialTextView;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import java.util.ArrayList;
import java.util.List;

public class ConversationDetailFragment extends Fragment {
    
    private String otherUsername;
    private RecyclerView messagesRecyclerView;
    private MessageAdapter messageAdapter;
    private EditText messageInput;
    private MaterialButton sendButton;
    private MaterialTextView titleText;
    private BinomeApiService apiService;
    private List<ApiModels.Message> messages = new ArrayList<>();
    
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            otherUsername = getArguments().getString("username");
        }
    }
    
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_conversation_detail, container, false);
    }
    
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        
        initializeViews(view);
        setupRecyclerView();
        setupSendButton();
        
        apiService = ApiClient.getInstance(getContext()).getApiService();
        
        if (otherUsername != null) {
            titleText.setText("Chat with " + otherUsername);
            loadConversation();
        }
    }
    
    private void initializeViews(View view) {
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view);
        messageInput = view.findViewById(R.id.message_input);
        sendButton = view.findViewById(R.id.send_button);
        titleText = view.findViewById(R.id.conversation_title);
    }
    
    private void setupRecyclerView() {
        messageAdapter = new MessageAdapter(messages);
        messagesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        messagesRecyclerView.setAdapter(messageAdapter);
    }
    
    private void setupSendButton() {
        sendButton.setOnClickListener(v -> {
            String messageText = messageInput.getText().toString().trim();
            if (!messageText.isEmpty() && otherUsername != null) {
                sendMessage(messageText);
            }
        });
    }
    
    private void loadConversation() {
        apiService.getConversation(otherUsername).enqueue(new Callback<List<ApiModels.Message>>() {
            @Override
            public void onResponse(Call<List<ApiModels.Message>> call, Response<List<ApiModels.Message>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.clear();
                    messages.addAll(response.body());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            messageAdapter.notifyDataSetChanged();
                            scrollToBottom();
                        });
                    }
                } else {
                    showError("Failed to load conversation");
                }
            }

            @Override
            public void onFailure(Call<List<ApiModels.Message>> call, Throwable t) {
                showError("Failed to load conversation: " + t.getMessage());
            }
        });
    }
    
    private void sendMessage(String content) {
        ApiModels.SendMessageRequest request = new ApiModels.SendMessageRequest(otherUsername, content);
        
        apiService.sendMessage(request).enqueue(new Callback<ApiModels.Message>() {
            @Override
            public void onResponse(Call<ApiModels.Message> call, Response<ApiModels.Message> response) {
                if (response.isSuccessful() && response.body() != null) {
                    messages.add(response.body());
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            messageAdapter.notifyItemInserted(messages.size() - 1);
                            scrollToBottom();
                            messageInput.setText("");
                        });
                    }
                } else {
                    showError("Failed to send message");
                }
            }

            @Override
            public void onFailure(Call<ApiModels.Message> call, Throwable t) {
                showError("Failed to send message: " + t.getMessage());
            }
        });
    }
    
    private void scrollToBottom() {
        if (messages.size() > 0) {
            messagesRecyclerView.smoothScrollToPosition(messages.size() - 1);
        }
    }
    
    private void showError(String message) {
        if (getContext() != null) {
            Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
        }
    }
}