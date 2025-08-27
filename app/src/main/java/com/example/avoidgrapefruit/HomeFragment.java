package com.example.avoidgrapefruit.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.avoidgrapefruit.R;
import com.example.avoidgrapefruit.auth.GoogleAuth;
import com.example.avoidgrapefruit.auth.LoginActivity;
import com.example.avoidgrapefruit.products.ProductActivity;
import com.google.firebase.auth.FirebaseAuth;

public class HomeFragment extends Fragment {

    private GoogleAuth googleAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Inflate fragment layout
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        googleAuth = new GoogleAuth(requireActivity());

        // Logout button
        Button btnLogout = view.findViewById(R.id.btnLogout);
        btnLogout.setOnClickListener(v -> logoutUser());

        // Go to Products button
        Button btnGoToProducts = view.findViewById(R.id.btnGoToProducts);
        btnGoToProducts.setOnClickListener(v -> {
            Intent intent = new Intent(requireContext(), ProductActivity.class);
            startActivity(intent);
        });

        return view;
    }

    private void logoutUser() {
        // Sign out from Firebase
        FirebaseAuth.getInstance().signOut();

        // Sign out from Google
        googleAuth.signOut();

        Toast.makeText(requireContext(), "Logged out successfully", Toast.LENGTH_SHORT).show();

        // Navigate back to LoginActivity and clear back stack
        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}
