package react.config

import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.config.annotation.authentication.ProviderManagerBuilder
import org.springframework.security.config.annotation.authentication.configurers.provisioning.UserDetailsManagerConfigurer

/**
 * Created by chauber on 21/12/15.
 */
class RedisUserDetailsManagerConfigurer<B extends ProviderManagerBuilder<B>>
  extends UserDetailsManagerConfigurer<B, RedisUserDetailsManagerConfigurer<B>> {

  /**
   * Creates a new instance
   */
  protected RedisUserDetailsManagerConfigurer(RedisTemplate redisTemplate) {
    super(new RedisUserDetailsManager(redisTemplate))
  }

}
