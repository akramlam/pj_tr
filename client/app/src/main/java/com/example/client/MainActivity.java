package com.example.client;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.AppCompatActivity;

import com.example.client.databinding.ActivityMainBinding;
import com.example.client.api.ApiClient;
import com.example.client.api.DebugLogger;

public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private ApiClient apiClient;
    private DebugLogger debugLogger;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Initialize debug logger first
        debugLogger = DebugLogger.getInstance(this);
        debugLogger.setUserId(getIntent().getStringExtra("username"));
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onCreate started");
        
        android.util.Log.d("MainActivity", "onCreate started");

        com.example.client.databinding.ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Initialize API client
        apiClient = ApiClient.getInstance(this);
        debugLogger.logAppEvent("API_INIT", "ApiClient initialized");
        android.util.Log.d("MainActivity", "ApiClient initialized");

        setSupportActionBar(binding.appBarMain.toolbar);
        binding.appBarMain.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                debugLogger.logButtonClick("MAIN", "FAB_CREATE_PROFILE");
                Snackbar.make(view, "Create new profile or find matches!", Snackbar.LENGTH_LONG)
                        .setAction("Profile", null)
                        .setAnchorView(R.id.fab).show();
            }
        });
        
        DrawerLayout drawer = binding.drawerLayout;
        NavigationView navigationView = binding.navView;
        
        android.util.Log.d("MainActivity", "Setting up navigation");
        debugLogger.logAppEvent("NAVIGATION_SETUP", "Setting up navigation drawer and menu");
        
        // Update navigation header with user info
        updateNavigationHeader(navigationView);
        
        // Updated menu IDs for new navigation items
        mAppBarConfiguration = new AppBarConfiguration.Builder(
                R.id.nav_dashboard, R.id.nav_profile, R.id.nav_matches, R.id.nav_messages)
                .setOpenableLayout(drawer)
                .build();
        
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
        NavigationUI.setupWithNavController(navigationView, navController);
        
        // Handle logout menu item separately
        navigationView.setNavigationItemSelectedListener(item -> {
            String itemTitle = item.getTitle().toString();
            debugLogger.logUserAction("MAIN", "NAVIGATION_MENU_CLICK", itemTitle);
            
            if (item.getItemId() == R.id.nav_logout) {
                handleLogout();
                return true;
            } else {
                // Let NavigationUI handle other items
                boolean handled = NavigationUI.onNavDestinationSelected(item, navController);
                if (handled) {
                    drawer.closeDrawers();
                }
                return handled;
            }
        });
        
        android.util.Log.d("MainActivity", "onCreate completed successfully");
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onCreate completed successfully");
    }

    @Override
    protected void onStart() {
        super.onStart();
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onStart");
    }

    @Override
    protected void onResume() {
        super.onResume();
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onResume");
        debugLogger.logScreenView("MAIN");
    }

    @Override
    protected void onPause() {
        super.onPause();
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onPause");
    }

    @Override
    protected void onStop() {
        super.onStop();
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onStop");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        debugLogger.logAppEvent("LIFECYCLE", "MainActivity onDestroy");
    }

    private void updateNavigationHeader(NavigationView navigationView) {
        debugLogger.logAction("UI_UPDATE", "MAIN", "Updating navigation header");
        
        View headerView = navigationView.getHeaderView(0);
        TextView navUsername = headerView.findViewById(R.id.textView);
        TextView navEmail = headerView.findViewById(R.id.textView2);
        
        String username = apiClient.getUsername();
        if (username != null) {
            navUsername.setText(username);
            navEmail.setText(username + "@example.com");
            
            // Add offline mode indicator
            if (apiClient.isMockMode()) {
                navUsername.setText(username + " (Offline)");
                navEmail.setText("Running in offline mode");
                debugLogger.logAction("UI_UPDATE", "MAIN", "Set navigation header for offline mode: " + username);
            } else {
                debugLogger.logAction("UI_UPDATE", "MAIN", "Set navigation header for online mode: " + username);
            }
        } else {
            navUsername.setText("Guest User");
            navEmail.setText("Not logged in");
            debugLogger.logAction("UI_UPDATE", "MAIN", "Set navigation header for guest user");
        }
    }

    private void handleLogout() {
        debugLogger.logUserAction("MAIN", "LOGOUT", "User initiated logout");
        
        // Clear stored token and user data
        apiClient.logout();
        
        // Show confirmation message
        Toast.makeText(this, "Logged out successfully", Toast.LENGTH_SHORT).show();
        debugLogger.logAppEvent("AUTH", "User logged out successfully");
        
        // Navigate back to login screen
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
        debugLogger.logAppEvent("NAVIGATION", "Navigated to LoginActivity after logout");
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}