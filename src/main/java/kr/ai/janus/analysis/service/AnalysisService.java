package kr.ai.janus.analysis.service;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import kr.ai.janus.analysis.dto.AnalysisResponse;
import kr.ai.janus.analysis.dto.ParticipantsResponse;
import kr.ai.janus.common.exception.BusinessException;
import kr.ai.janus.common.exception.ErrorCode;
import kr.ai.janus.parsing.grammar.CsvMessageParser;
import kr.ai.janus.parsing.model.ParseStats;
import kr.ai.janus.parsing.model.PreScanSummary;
import kr.ai.janus.parsing.model.RawMessage;
import kr.ai.janus.parsing.scan.AnalysisScanner;
import kr.ai.janus.parsing.scan.PreScanner;
import kr.ai.janus.parsing.validation.PreScanValidator;
import lombok.RequiredArgsConstructor;

/**
 * 원문은 요청 처리 중에만 메모리에 두고 응답 후 폐기한다(저장하지 않음)
 */
@Service
@RequiredArgsConstructor
public class AnalysisService {

    private final CsvMessageParser csvMessageParser;
    private final PreScanner preScanner;
    private final PreScanValidator preScanValidator;
    private final AnalysisScanner analysisScanner;

    public ParticipantsResponse findParticipants(MultipartFile file) {
        PreScanSummary summary = preScanner.summarize(parse(file));
        preScanValidator.validateChat(summary);
        return ParticipantsResponse.from(summary);
    }

    public AnalysisResponse analyze(MultipartFile file, String ownerName) {
        List<RawMessage> messages = parse(file);
        PreScanSummary summary = preScanner.summarize(messages);
        preScanValidator.validate(summary, ownerName);
        ParseStats stats = analysisScanner.scan(messages, ownerName);
        return AnalysisResponse.from(stats);
    }

    private List<RawMessage> parse(MultipartFile file) {
        try (Reader reader = new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8)) {
            return csvMessageParser.parse(reader);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_READ_FAILED, e);
        }
    }
}
