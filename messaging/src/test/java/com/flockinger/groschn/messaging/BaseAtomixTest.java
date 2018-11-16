package com.flockinger.groschn.messaging;

import org.junit.runner.RunWith;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.context.ConfigFileApplicationContextInitializer;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.messaging.config.AtomixConfig;
import com.flockinger.groschn.messaging.config.MessagingProtocolConfiguration;

@RunWith(SpringRunner.class)
@EnableConfigurationProperties(AtomixConfig.class)
@ContextConfiguration(initializers=ConfigFileApplicationContextInitializer.class)
@Import(MessagingProtocolConfiguration.class)
public abstract class BaseAtomixTest {

}
