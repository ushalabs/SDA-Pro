package edu.sdapro.threat;

public record ReputationResult(String indicator, int score, String verdict, String provider) {}
