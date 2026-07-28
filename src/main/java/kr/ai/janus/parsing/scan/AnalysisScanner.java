package kr.ai.janus.parsing.scan;

import java.util.List;

import kr.ai.janus.parsing.model.ParseStats;
import kr.ai.janus.parsing.model.RawMessage;

/**
 * 2차 스캔. 화자, 본인이 정해진 뒤 분석용 통계를 모은다.
 */
public final class AnalysisScanner {

    public ParseStats scan(List<RawMessage> messages, String ownerName) {
        MessageStatsCounter counter = new MessageStatsCounter(ownerName);
        for (RawMessage message : messages) {
            counter.add(message);
        }
        return counter.toStats();
    }
}
