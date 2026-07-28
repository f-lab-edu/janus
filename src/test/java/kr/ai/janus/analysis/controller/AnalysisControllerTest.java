package kr.ai.janus.analysis.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import kr.ai.janus.analysis.service.AnalysisService;
import kr.ai.janus.auth.service.TokenVerifier;
import kr.ai.janus.config.CorsProperties;
import kr.ai.janus.config.SecurityConfig;
import kr.ai.janus.parsing.grammar.CsvMessageParser;
import kr.ai.janus.parsing.scan.AnalysisScanner;
import kr.ai.janus.parsing.scan.PreScanner;
import kr.ai.janus.parsing.validation.PreScanValidator;

/**
 * 업로드 API 통합 테스트. 서비스를 mock하지 않고 실제 파싱 파이프라인을 주입해
 * 엔드포인트 → 파싱 → 검증 → 분석까지 실제로 도는지 검증
 */
@WebMvcTest(AnalysisController.class)
@Import({SecurityConfig.class, AnalysisService.class, CsvMessageParser.class,
        PreScanner.class, PreScanValidator.class, AnalysisScanner.class})
@EnableConfigurationProperties(CorsProperties.class)
class AnalysisControllerTest {

    @Autowired
    MockMvc mockMvc;
    @MockitoBean
    TokenVerifier tokenVerifier;   // SecurityConfig가 필요로 함 (요청엔 토큰 없음)

    private MockMultipartFile fixture(String path) throws IOException {
        byte[] bytes = getClass().getClassLoader().getResourceAsStream(path).readAllBytes();
        return new MockMultipartFile("file", "chat.csv", "text/csv", bytes);
    }

    @Test
    void 참가자_조회는_두_화자와_요약을_반환한다() throws Exception {
        mockMvc.perform(multipart("/analyses/participants").file(fixture("fixtures/csv/valid.csv")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.speakers[0]").value("민지"))
                .andExpect(jsonPath("$.speakers[1]").value("지훈"))
                .andExpect(jsonPath("$.messageCount").value(60));
    }

    @Test
    void 분석은_본인_선택을_받아_통계를_반환한다() throws Exception {
        mockMvc.perform(multipart("/analyses")
                        .file(fixture("fixtures/csv/valid.csv"))
                        .param("ownerName", "민지"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCount").value(60))
                .andExpect(jsonPath("$.ownerCount").value(30))
                .andExpect(jsonPath("$.partnerCount").value(30))
                .andExpect(jsonPath("$.textCount").value(60))
                .andExpect(jsonPath("$.activeDayCount").value(1));
    }

    @Test
    void 메시지가_기준보다_적으면_INSUFFICIENT_TOTAL_MESSAGES() throws Exception {
        mockMvc.perform(multipart("/analyses/participants").file(fixture("fixtures/csv/simple.csv")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INSUFFICIENT_TOTAL_MESSAGES"));
    }

    @Test
    void 본인이_두_화자에_없으면_UNKNOWN_SPEAKER() throws Exception {
        mockMvc.perform(multipart("/analyses")
                        .file(fixture("fixtures/csv/valid.csv"))
                        .param("ownerName", "철수"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("UNKNOWN_SPEAKER"));
    }

    @Test
    void 파일_파트가_없으면_400() throws Exception {
        mockMvc.perform(multipart("/analyses/participants"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }

    @Test
    void ownerName이_없으면_400() throws Exception {
        mockMvc.perform(multipart("/analyses").file(fixture("fixtures/csv/valid.csv")))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value("INVALID_REQUEST"));
    }
}
