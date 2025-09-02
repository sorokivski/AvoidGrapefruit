package com.example.avoidgrapefruit;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.example.avoidgrapefruit.HomeFragment;
import com.example.avoidgrapefruit.ProductFragment;
import com.example.avoidgrapefruit.user.UserFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager;
    private BottomNavigationView bottomNavigation;
    private List<Fragment> fragmentList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        viewPager = findViewById(R.id.viewPager);
        bottomNavigation = findViewById(R.id.bottomNavigation);

        // Prepare fragments
        fragmentList = new ArrayList<>();
        fragmentList.add(new HomeFragment());    // index 0
        fragmentList.add(new ProductFragment()); // index 1
        fragmentList.add(new UserFragment());    // index 2


        // Set up adapter
        ViewPagerAdapter adapter = new ViewPagerAdapter(this, fragmentList);
        viewPager.setAdapter(adapter);

        // BottomNavigationView → ViewPager2
        bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.home) {
                viewPager.setCurrentItem(0, true);
                return true;
            } else if (id == R.id.product) {
                viewPager.setCurrentItem(1, true);
                return true;
            } else if (id == R.id.user) {
                viewPager.setCurrentItem(2, true);
                return true;
            }
            return false;
        });


        // ViewPager2 → BottomNavigationView
        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        bottomNavigation.setSelectedItemId(R.id.home);
                        break;
                    case 1:
                        bottomNavigation.setSelectedItemId(R.id.product);
                        break;
                    case 2:
                        bottomNavigation.setSelectedItemId(R.id.user);
                        break;
                }
            }

        });
    }
}
