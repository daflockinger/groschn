package com.flockinger.groschn.messaging;

import com.flockinger.TestMessagingProtocolConfiguration;
import com.flockinger.groschn.messaging.config.AtomixConfig;
import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(AtomixConfig.class)
@ContextConfiguration(initializers=ConfigFileApplicationContextInitializer.class)
@Import(TestMessagingProtocolConfiguration.class)
public abstract class BaseAtomixTest {

}
