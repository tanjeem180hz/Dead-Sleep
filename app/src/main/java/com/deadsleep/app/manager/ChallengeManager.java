package com.deadsleep.app.manager;

import android.content.Context;

import com.deadsleep.app.challenge.Challenge;
import com.deadsleep.app.challenge.ChallengeFactory;
import com.deadsleep.app.data.AppDatabase;
import com.deadsleep.app.data.DeadSleepRepository;
import com.deadsleep.app.data.PreferencesManager;
import com.deadsleep.app.model.ChallengeResult;
import com.deadsleep.app.model.SleepSchedule;
import com.deadsleep.app.util.Constants;

/**
 * Coordinates challenge instantiation, difficulty auto-escalation, and result recording.
 */
public class ChallengeManager {

    private static volatile ChallengeManager INSTANCE;
    private final Context context;
    private final DeadSleepRepository repository;
    private final PreferencesManager preferencesManager;

    private ChallengeManager(Context context) {
        this.context = context.getApplicationContext();
        this.repository = DeadSleepRepository.getInstance(this.context);
        this.preferencesManager = PreferencesManager.getInstance(this.context);
    }

    public static ChallengeManager getInstance(Context context) {
        if (INSTANCE == null) {
            synchronized (ChallengeManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ChallengeManager(context);
                }
            }
        }
        return INSTANCE;
    }

    /**
     * Prepares the challenge for a specific schedule, escalating difficulty if snoozes were used.
     */
    public Challenge getChallengeForAlarm(long scheduleId, int snoozeCount) {
        SleepSchedule schedule = repository.getScheduleById(scheduleId);

        String type = (schedule != null) ? schedule.getChallengeType() : preferencesManager.getDefaultChallenge();
        String difficulty = (schedule != null) ? schedule.getChallengeDifficulty() : preferencesManager.getDefaultDifficulty();

        if (schedule != null && schedule.isAutoEscalateChallenge() && snoozeCount > 0) {
            difficulty = escalateDifficulty(difficulty, snoozeCount);
        }

        return ChallengeFactory.createChallenge(context, type, difficulty);
    }

    /**
     * Escalates difficulty by one tier per snooze.
     */
    public String escalateDifficulty(String currentDifficulty, int snoozeCount) {
        if (snoozeCount <= 0) return currentDifficulty;
        if (Constants.DIFFICULTY_EASY.equalsIgnoreCase(currentDifficulty)) {
            return snoozeCount == 1 ? Constants.DIFFICULTY_MEDIUM : Constants.DIFFICULTY_HARD;
        } else if (Constants.DIFFICULTY_MEDIUM.equalsIgnoreCase(currentDifficulty)) {
            return Constants.DIFFICULTY_HARD;
        }
        return Constants.DIFFICULTY_HARD;
    }

    /**
     * Records challenge outcome in the database for stats.
     */
    public void recordResult(String challengeType, String difficulty, int timeTakenSec, int attempts, boolean isPassed) {
        long sessionId = preferencesManager.getActiveSessionId();
        ChallengeResult result = new ChallengeResult(
                sessionId, challengeType, difficulty, timeTakenSec, attempts, isPassed
        );
        repository.insertChallengeResult(result);
    }
}
