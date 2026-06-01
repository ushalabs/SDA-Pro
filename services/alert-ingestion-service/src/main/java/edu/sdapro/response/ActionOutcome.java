package edu.sdapro.response;

public record ActionOutcome(String actionType, boolean success, String message) {}
