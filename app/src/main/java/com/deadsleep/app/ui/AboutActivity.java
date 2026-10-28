package com.deadsleep.app.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.databinding.ActivityAboutBinding;

/**
 * About screen presenting app credits, version, and the 100% offline privacy guarantee.
 */
public class AboutActivity extends AppCompatActivity {

    private ActivityAboutBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnBackAbout.setOnClickListener(v -> finish());
    }
}
