package dev.mam.buizsol.mamshop.config;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import tools.jackson.core.TreeNode;
import tools.jackson.databind.DefaultTyping;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JavaType;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import tools.jackson.databind.jsontype.PolymorphicTypeValidator;
import tools.jackson.databind.jsontype.impl.DefaultTypeResolverBuilder;

import java.time.Duration;

@Configuration
@EnableCaching
@ConditionalOnProperty(name = "spring.cache.type", havingValue = "redis")
public class RedisCacheConfig {

    @Value("${spring.cache.redis.time-to-live:600000}")
    private Long ttlMillis;

    // Custom TypeResolverBuilder to force typing for all non-primitive classes (mimics DefaultTyping.EVERYTHING)
    public static class EverythingTypeResolverBuilder extends DefaultTypeResolverBuilder {
        public EverythingTypeResolverBuilder(PolymorphicTypeValidator ptv) {
            super(ptv, DefaultTyping.NON_FINAL, JsonTypeInfo.As.WRAPPER_ARRAY);
        }

        @Override
        public boolean useForType(JavaType t) {
            if (t.isPrimitive()) {
                return false;
            }
            if (TreeNode.class.isAssignableFrom(t.getRawClass())) {
                return false;
            }
            return true;
        }
    }

    @Bean
    public RedisCacheManager cacheManager(final RedisConnectionFactory connectionFactory) {
        // permissive validator to allow polymorphic types in collections
        final PolymorphicTypeValidator ptv = BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build();

        // Configure Jackson 3 ObjectMapper with EVERYTHING typing and WRAPPER_ARRAY format for lists serialization
        final ObjectMapper mapper = JsonMapper.builder()
                .setDefaultTyping(new EverythingTypeResolverBuilder(ptv))
                .configure(DeserializationFeature.FAIL_ON_NULL_FOR_PRIMITIVES, false)
                .build();

        final RedisCacheConfiguration config = RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMillis(ttlMillis))
                .disableCachingNullValues()
                .serializeKeysWith(
                        RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair.fromSerializer(
                        new GenericJacksonJsonRedisSerializer(mapper)));

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(config)
                .transactionAware()
                .build();
    }
}
