package com.loadup.notification;

import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration;
import org.springframework.boot.data.redis.autoconfigure.DataRedisReactiveAutoConfiguration;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.CacheManager;
import org.springframework.cloud.stream.binder.test.TestChannelBinderConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

@SpringBootTest
@Import(TestChannelBinderConfiguration.class)
@EnableAutoConfiguration(exclude = {DataRedisAutoConfiguration.class, DataRedisReactiveAutoConfiguration.class})
class NotificationServiceApplicationTests {

	@MockitoBean
	CacheManager cacheManager;

	@Test
	void contextLoads() {
	}

}
