package kr.ai.janus.parsing.model;

/**
 * 1:1 대화의 두 화자 역할
 */
public enum SpeakerRole {
    OWNER,
    PARTNER;

    public static SpeakerRole resolve(String speakerName, String ownerName) {
        return ownerName.equals(speakerName) ? OWNER : PARTNER;
    }
}
