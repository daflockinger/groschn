package com.flockinger.groschn.blockchain;

import org.junit.runner.RunWith;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit4.SpringRunner;
import com.flockinger.groschn.blockchain.config.CryptoConfig;
import com.flockinger.groschn.blockchain.config.GeneralConfig;

@RunWith(SpringRunner.class)
@DataMongoTest
@Import({GeneralConfig.class, CryptoConfig.class})
public class BaseDbTest {

}
