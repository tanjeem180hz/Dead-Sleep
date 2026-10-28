package com.deadsleep.app.ui;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.databinding.ActivityGoodMorningBinding;
import com.deadsleep.app.manager.StatsManager;
import com.deadsleep.app.model.SleepSession;
import com.deadsleep.app.util.TimeUtils;

import java.util.List;

/**
 * Summary screen celebrating successful morning wake-up and cognitive challenge completion.
 */
public class GoodMorningActivity extends AppCompatActivity {

    private ActivityGoodMorningBinding binding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityGoodMorningBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        int solveTime = getIntent().getIntExtra("solve_time_sec", 15);
        binding.tvGmSolveTime.setText(solveTime + "s");

        int streak = StatsManager.getInstance(this).getCurrentStreak();
        binding.tvGmStreak.setText("Current Streak: " + streak + " Day" + (streak != 1 ? "s" : ""));

        // Fetch duration of last completed session
        com.deadsleep.app.data.AppDatabase.getExecutor().execute(() -> {
            List<SleepSession> sessions = DeadSleepRepository.getInstance(this).getRecentSessionsSync(1);
            if (!sessions.isEmpty()) {
                SleepSession last = sessions.get(0);
                int durationMin = last.getActualDurationMinutes();
                runOnUiThread(() -> {
                    binding.tvGmDuration.setText(TimeUtils.formatDuration(durationMin > 0 ? durationMin : 480));
                });
            }
        });

        binding.btnStartDay.setOnClickListener(v -> {
            Intent main = new Intent(this, MainActivity.class);
            main.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(main);
            finish();
        });
    }
}
