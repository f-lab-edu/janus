package kr.ai.janus.analysis.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import kr.ai.janus.analysis.dto.AnalysisResponse;
import kr.ai.janus.analysis.dto.ParticipantsResponse;
import kr.ai.janus.analysis.service.AnalysisService;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class AnalysisController {

    private final AnalysisService analysisService;

    /** 본인 선택 전 단계 */
    @PostMapping("/analyses/participants")
    public ParticipantsResponse findParticipants(@RequestParam("file") MultipartFile file) {
        return analysisService.findParticipants(file);
    }

    @PostMapping("/analyses")
    public AnalysisResponse analyze(
            @RequestParam("file") MultipartFile file,
            @RequestParam("ownerName") String ownerName) {
        return analysisService.analyze(file, ownerName);
    }
}
