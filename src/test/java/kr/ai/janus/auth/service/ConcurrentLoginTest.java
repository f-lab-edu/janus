package kr.ai.janus.auth.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import kr.ai.janus.auth.dto.TokenResponse;
import kr.ai.janus.auth.kakao.KakaoClient;
import kr.ai.janus.auth.kakao.KakaoProfile;
import kr.ai.janus.auth.repository.OAuthAccountRepository;
import kr.ai.janus.support.IntegrationTest;
import kr.ai.janus.user.repository.UserAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

class ConcurrentLoginTest extends IntegrationTest {

    @MockitoBean
    KakaoClient kakaoClient;
    @Autowired
    AuthService authService;
    @Autowired
    UserAccountRepository userAccountRepository;
    @Autowired
    OAuthAccountRepository oauthAccountRepository;

    @BeforeEach
    void clean() {
        oauthAccountRepository.deleteAll();
        userAccountRepository.deleteAll();
    }

    @Test
    @DisplayName("같은 카카오 계정으로 동시 로그인해도 user는 하나만 생성되고 모두 성공한다")
    void concurrentSignupCreatesSingleUser() throws Exception {
        given(kakaoClient.fetchProfile(anyString())).willReturn(new KakaoProfile("race-subject"));

        int threads = 8;
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        AtomicInteger success = new AtomicInteger();

        for (int i = 0; i < threads; i++) {
            pool.submit(() -> {
                try {
                    start.await();
                    TokenResponse token = authService.loginWithKakao("code");
                    if (token.accessToken() != null) {
                        success.incrementAndGet();
                    }
                } catch (Exception ignored) {
                    // 실패하면 success가 안 올라가 아래 단언에서 잡힌다
                } finally {
                    done.countDown();
                }
            });
        }

        start.countDown();
        done.await(20, TimeUnit.SECONDS);
        pool.shutdown();

        assertThat(success.get()).isEqualTo(threads);
        assertThat(userAccountRepository.count()).isEqualTo(1);
        assertThat(oauthAccountRepository.count()).isEqualTo(1);
    }
}
