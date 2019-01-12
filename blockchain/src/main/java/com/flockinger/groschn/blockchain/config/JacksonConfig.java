package com.flockinger.groschn.blockchain.config;

import com.fasterxml.jackson.datatype.threetenbp.ThreeTenModule;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.threeten.bp.Instant;
import org.threeten.bp.OffsetDateTime;
import org.threeten.bp.ZonedDateTime;

@Configuration
public class JacksonConfig {

  @Bean
  @ConditionalOnMissingBean(ThreeTenModule.class)
  ThreeTenModule threeTenModule() {
    ThreeTenModule module = new ThreeTenModule();
    module.addDeserializer(Instant.class, CustomInstantDeserializerConfig.INSTANT);
    module.addDeserializer(OffsetDateTime.class, CustomInstantDeserializerConfig.OFFSET_DATE_TIME);
    module.addDeserializer(ZonedDateTime.class, CustomInstantDeserializerConfig.ZONED_DATE_TIME);
    return module;
  }
}
