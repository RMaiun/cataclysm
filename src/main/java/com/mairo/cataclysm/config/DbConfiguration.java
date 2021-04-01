package com.mairo.cataclysm.config;

import com.mairo.cataclysm.properties.DbProps;
import com.mongodb.reactivestreams.client.MongoClient;
import com.mongodb.reactivestreams.client.MongoClients;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.ReactiveMongoTemplate;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

@Configuration
@RequiredArgsConstructor
public class DbConfiguration {

  private final DbProps dbProps;


  @Bean
  public MongoClient mongoClient() {
    String url = String.format("mongodb://%s:%d/%s", dbProps.getHost(), dbProps.getPort(), dbProps.getDatabase());
    return MongoClients.create(url);
  }

  @Bean
  public ReactiveMongoTemplate reactiveMongoTemplate(MongoCustomConversions customConversions) {
    ReactiveMongoTemplate rmt = new ReactiveMongoTemplate(mongoClient(), dbProps.getDatabase());
    MappingMongoConverter mongoMapping = (MappingMongoConverter) rmt.getConverter();
    mongoMapping.setTypeMapper(new DefaultMongoTypeMapper(null));
    mongoMapping.setCustomConversions(customConversions);
    mongoMapping.afterPropertiesSet();
    return rmt;
  }

  @Bean
  public MongoCustomConversions customConversions() {
    List<Converter<?, ?>> converters = new ArrayList<>();
    converters.add(new ZonedDateTimeReadConverter());
    converters.add(new ZonedDateTimeWriteConverter());
    return new MongoCustomConversions(converters);
  }
}
