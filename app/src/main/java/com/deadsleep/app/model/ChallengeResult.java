package com.deadsleep.app.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Persists the result of a cognitive challenge.
 */
@Entity(tableName = "challenge_results")
public class ChallengeResult {

    @PrimaryKey(autoGenerate = true)
    private long id;

    private long sessionId;
    private String challengeType; // MATH, MEMORY, PATTERN, REACTION, SHAKE
    private String difficulty; // EASY, MEDIUM, HARD
    private int timeTakenSec;
    private int attempts;
    private boolean isPassed;
    private long timestamp;

    public ChallengeResult(long sessionId, String challengeType, String difficulty, int timeTakenSec, int attempts, boolean isPassed) {
        this.sessionId = sessionId;
        this.challengeType = challengeType;
        this.difficulty = difficulty;
        this.timeTakenSec = timeTakenSec;
        this.attempts = attempts;
        this.isPassed = isPassed;
        this.timestamp = System.currentTimeMillis();
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getSessionId() { return sessionId; }
    public void setSessionId(long sessionId) { this.sessionId = sessionId; }

    public String getChallengeType() { return challengeType; }
    public void setChallengeType(String challengeType) { this.challengeType = challengeType; }

    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }

    public int getTimeTakenSec() { return timeTakenSec; }
    public void setTimeTakenSec(int timeTakenSec) { this.timeTakenSec = timeTakenSec; }

    public int getAttempts() { return attempts; }
    public void setAttempts(int attempts) { this.attempts = attempts; }

    public boolean isPassed() { return isPassed; }
    public void setPassed(boolean passed) { isPassed = passed; }

    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}
