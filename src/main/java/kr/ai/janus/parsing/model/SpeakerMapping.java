package kr.ai.janus.parsing.model;

import java.util.Objects;

public record SpeakerMapping(
        String ownerName,
        String partnerName
) {

    public SpeakerMapping {
        Objects.requireNonNull(ownerName, "ownerName");
        Objects.requireNonNull(partnerName, "partnerName");
        if (ownerName.equals(partnerName)) {
            throw new IllegalArgumentException("OWNER와 PARTNER는 서로 달라야 합니다.");
        }
    }

    public SpeakerRole roleOf(String speakerName) {
        if (ownerName.equals(speakerName)) {
            return SpeakerRole.OWNER;
        }
        if (partnerName.equals(speakerName)) {
            return SpeakerRole.PARTNER;
        }
        return SpeakerRole.EXCLUDED;
    }
}
