package com.deadsleep.app.ui;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.databinding.ActivityRoutineAdvisorBinding;
import com.deadsleep.app.manager.RoutineAdvisor;
import com.deadsleep.app.model.RoutineSuggestion;
import com.deadsleep.app.ui.adapter.RoutineAdapter;

import java.util.List;

/**
 * Screen displaying rule-based personalized sleep schedule recommendations.
 */
public class RoutineAdvisorActivity extends AppCompatActivity implements RoutineAdapter.OnApplySuggestionListener {

    private ActivityRoutineAdvisorBinding binding;
    private RoutineAdapter adapter;
    private RoutineAdvisor advisor;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRoutineAdvisorBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        advisor = RoutineAdvisor.getInstance(this);
        binding.btnBackAdvisor.setOnClickListener(v -> finish());

        adapter = new RoutineAdapter(this);
        binding.rvAdvisor.setLayoutManager(new LinearLayoutManager(this));
        binding.rvAdvisor.setAdapter(adapter);

        loadSuggestions();
    }

    private void loadSuggestions() {
        AppDatabase.getExecutor().execute(() -> {
            List<RoutineSuggestion> list = advisor.generateRecommendations();
            runOnUiThread(() -> adapter.setSuggestions(list));
        });
    }

    @Override
    public void onApply(RoutineSuggestion suggestion) {
        advisor.applySuggestion(suggestion);
        Toast.makeText(this, "✓ Applied: " + suggestion.getTitle(), Toast.LENGTH_SHORT).show();
    }
}
