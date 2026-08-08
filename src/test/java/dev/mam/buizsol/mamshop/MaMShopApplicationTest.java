package dev.mam.buizsol.mamshop;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
class MaMShopApplicationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Test
    void contextLoads() {}

    @Test
    void mainMethodTest() {
        try (MockedStatic<SpringApplication> mocked = Mockito.mockStatic(SpringApplication.class)) {
            mocked.when(() -> SpringApplication.run(MaMShopApplication.class, new String[] {"--server.port=0"}))
                    .thenReturn(null);
            assertDoesNotThrow(() -> MaMShopApplication.main(new String[] {"--server.port=0"}));
            mocked.verify(() -> SpringApplication.run(MaMShopApplication.class, new String[] {"--server.port=0"}));
        }
    }
}
