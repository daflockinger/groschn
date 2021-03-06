package com.flockinger.groschn.blockchain;

import com.flockinger.groschn.blockchain.config.CommonsConfig;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.junit4.SpringRunner;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import({GeneralTestConfig.class, CryptoConfig.class, CommonsConfig.class})
@DirtiesContext
public abstract class BaseDbTest {

}
