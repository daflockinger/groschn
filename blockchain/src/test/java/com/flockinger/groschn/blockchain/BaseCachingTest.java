package com.flockinger.groschn.blockchain;

import com.flockinger.groschn.blockchain.config.CacheConfig;
import com.flockinger.groschn.blockchain.config.CommonsConfig;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.boot.test.mock.mockito.MockitoTestExecutionListener;
import org.springframework.boot.test.mock.mockito.ResetMocksTestExecutionListener;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;

@ActiveProfiles("test")
@RunWith(SpringRunner.class)
@ContextConfiguration(initializers=ConfigFileApplicationContextInitializer.class)
@Import({CacheConfig.class, TestConfig.class, CommonsConfig.class})
@TestExecutionListeners({DependencyInjectionTestExecutionListener.class, MockitoTestExecutionListener.class, ResetMocksTestExecutionListener.class})
public abstract class BaseCachingTest {

}
