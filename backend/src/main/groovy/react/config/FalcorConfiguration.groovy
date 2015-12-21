package react.config

import com.netflix.falcor.model.*
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.converter.json.Jackson2ObjectMapperBuilder

/**
 * Created by cedric on 22/11/2015.
 */
@Configuration
class FalcorConfiguration {
  @Bean
  public Jackson2ObjectMapperBuilder objectMapperBuilder() {
    Jackson2ObjectMapperBuilder builder = new Jackson2ObjectMapperBuilder();
    builder.serializerByType(FalcorTree, new FalcorTreeSerializer())
    builder.serializerByType(Atom, new AtomSerializer())
    builder.serializerByType(NullKey, new NullKeySerializer())
    builder.serializerByType(NumberRange, new NumberRangeSerializer())
    builder.serializerByType(NumericSet, new NumericSetSerializer())
    builder.deserializerByType(FalcorTree, new FalcorTreeDeserializer())
    builder.deserializerByType(KeySegment, new KeySegmentDeserializer())
    builder.deserializerByType(NumericKey, new NumericKeyDeserializer())
    builder.deserializerByType(SimpleKey, new SimpleKeyDeserializer())
    return builder;
  }
}
