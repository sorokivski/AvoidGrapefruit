package com.example.avoidgrapefruit.home;

import android.app.ActivityOptions;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.interactions.InteractionsActivity;
import com.example.avoidgrapefruit.products.ProductActivity;
import com.example.avoidgrapefruit.user.UserProfileActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.shape.CornerFamily;
import com.google.android.material.shape.MaterialShapeDrawable;

public abstract class BaseActivity extends AppCompatActivity {

    protected BottomNavigationView bottomNavView;


    @IdRes
    protected abstract int getBottomNavMenuItemId();

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupBottomNavigation();

        if (bottomNavView != null) {
            bottomNavView.setSelectedItemId(getBottomNavMenuItemId());
        }
    }

    private void setupBottomNavigation() {
        bottomNavView = findViewById(R.id.bottom_nav_view);
        if (bottomNavView == null) return;

        if (bottomNavView.getBackground() instanceof MaterialShapeDrawable) {
            MaterialShapeDrawable bg = (MaterialShapeDrawable) bottomNavView.getBackground();
            float cornerRadius = getResources().getDimension(R.dimen.corner_radius);
            bg.setShapeAppearanceModel(
                    bg.getShapeAppearanceModel().toBuilder()
                            .setAllCorners(CornerFamily.ROUNDED, cornerRadius)
                            .build()
            );
        }

        int selectedColor = ContextCompat.getColor(this, R.color.grapefruit_red);
        int unselectedColor = ContextCompat.getColor(this, R.color.gray);

        int[][] states = new int[][]{
                new int[]{android.R.attr.state_checked},
                new int[]{}
        };
        int[] colors = new int[]{selectedColor, unselectedColor};
        ColorStateList colorStateList = new ColorStateList(states, colors);

        bottomNavView.setItemIconTintList(colorStateList);
        bottomNavView.setItemTextColor(colorStateList);

        bottomNavView.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Class<?> target = null;

            if (itemId == R.id.interactions) target = InteractionsActivity.class;
            else if (itemId == R.id.product) target = ProductActivity.class;
            else if (itemId == R.id.user) target = UserProfileActivity.class;

            if (target != null && !this.getClass().equals(target)) {
                Intent intent = new Intent(this, target);
                startActivity(intent, ActivityOptions.makeCustomAnimation(this, 0, 0).toBundle());
                finish();
            }

            return true;
        });
    }
}