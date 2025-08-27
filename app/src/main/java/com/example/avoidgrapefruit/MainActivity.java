package com.example.avoidgrapefruit;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.avoidgrapefruit.home.HomeFragment;
import com.example.avoidgrapefruit.products.ProductActivity;
import com.example.avoidgrapefruit.user.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Load homepage first
        replaceFragment(new HomeFragment());

        BottomNavigationView bottomNavigation = findViewById(R.id.bottomNavigation);
        bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId(); // Get the ID once
            if (itemId == R.id.home) {
                replaceFragment(new HomeFragment());  // homepage
            } else if (itemId == R.id.product) {
                // Open ProductActivity
                Intent productIntent = new Intent(MainActivity.this, ProductActivity.class);
                startActivity(productIntent);
            } else if (itemId == R.id.user) {
                replaceFragment(new UserFragment()); // user settings
            }
            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}
