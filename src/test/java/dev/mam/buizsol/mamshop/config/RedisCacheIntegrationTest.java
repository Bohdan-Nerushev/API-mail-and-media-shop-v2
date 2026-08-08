package dev.mam.buizsol.mamshop.config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import dev.mam.buizsol.mamshop.customer.model.Brand;
import dev.mam.buizsol.mamshop.product.model.MailProduct;
import dev.mam.buizsol.mamshop.product.model.Product;
import dev.mam.buizsol.mamshop.product.service.ProductRepository;
import dev.mam.buizsol.mamshop.product.service.ProductService;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.util.ReflectionTestUtils;
import redis.clients.jedis.Jedis;

@SpringBootTest(
        properties = {
            "spring.cache.type=redis",
            "spring.data.redis.host=${REDIS_HOST:localhost}",
            "spring.data.redis.port=6379",
            "billing.minimal-discount-amount=0.10"
        })
class RedisCacheIntegrationTest {

    @MockitoBean
    private JwtDecoder jwtDecoder;

    @Autowired
    private ProductService productService;

    @MockitoBean
    private ProductRepository productRepository;

    @Autowired
    private CacheManager cacheManager;

    @BeforeAll
    static void checkRedisAvailability() {
        boolean redisUp = false;
        try (Jedis jedis = new Jedis(System.getenv().getOrDefault("REDIS_HOST", "localhost"), 6379)) {
            redisUp = "PONG".equals(jedis.ping());
        } catch (Exception e) {
            // Redis is absent or not fully initialized yet
            redisUp = false;
        }
        Assumptions.assumeTrue(redisUp, "Redis is not running or not ready. Skipping integration tests.");
    }

    @BeforeEach
    void clearCache() {
        Optional.ofNullable(cacheManager.getCache("products")).ifPresent(Cache::clear);

        Optional.ofNullable(cacheManager.getCache("productsByBrand")).ifPresent(Cache::clear);
    }

    @Test
    @DisplayName("Should cache product by ID and retrieve it from cache on subsequent calls")
    void shouldCacheProductById() {
        final UUID id = UUID.randomUUID();
        final Product product = new MailProduct("Test Product", Brand.GMX, BigDecimal.ONE, BigDecimal.TEN, 1024L);
        ReflectionTestUtils.setField(product, "id", id);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        final Optional<Product> firstCall = productService.findById(id);

        assertThat(firstCall).isPresent();
        verify(productRepository, times(1)).findById(id);

        final Optional<Product> secondCall = productService.findById(id);

        assertThat(secondCall).isPresent();
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    @DisplayName("Should cache products by brand and evict on product creation")
    void shouldCacheByBrandAndEvictOnCreation() {
        final Brand brand = Brand.WEB_DE;
        final Product product = new MailProduct("Web.de Product", brand, BigDecimal.ONE, BigDecimal.TEN, 1024L);
        when(productRepository.findByBrand(brand)).thenReturn(List.of(product));

        productService.findByBrand(brand);
        verify(productRepository, times(1)).findByBrand(brand);

        productService.findByBrand(brand);
        verify(productRepository, times(1)).findByBrand(brand);

        productService.createProduct(product);

        productService.findByBrand(brand);
        verify(productRepository, times(1)).findByBrand(brand);
    }

    @Test
    @DisplayName("Should evict both caches on monthly fee update")
    void shouldEvictOnMonthlyFeeUpdate() throws Exception {
        final UUID id = UUID.randomUUID();
        final Brand brand = Brand.MAIL_COM;
        final Product product = new MailProduct("Mail.com Product", brand, BigDecimal.ONE, BigDecimal.TEN, 1024L);
        ReflectionTestUtils.setField(product, "id", id);

        when(productRepository.findById(id)).thenReturn(Optional.of(product));
        when(productRepository.findByBrand(brand)).thenReturn(List.of(product));

        productService.findById(id);
        productService.findByBrand(brand);
        verify(productRepository, times(1)).findById(id);
        verify(productRepository, times(1)).findByBrand(brand);

        productService.updateMonthlyFee(id, new BigDecimal("15.00"));

        productService.findById(id);
        productService.findByBrand(brand);

        verify(productRepository, times(3)).findById(id);
        verify(productRepository, times(2)).findByBrand(brand);
    }
}
