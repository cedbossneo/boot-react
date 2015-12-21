package react.config

import groovy.json.JsonBuilder
import groovy.json.JsonSlurper
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.core.Authentication
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.core.userdetails.UsernameNotFoundException
import org.springframework.security.provisioning.UserDetailsManager
import react.models.User

/**
 * Created by chauber on 21/12/15.
 */
class RedisUserDetailsManager implements UserDetailsManager {
  private RedisTemplate redisTemplate

  def RedisUserDetailsManager(RedisTemplate redisTemplate) {
    this.redisTemplate = redisTemplate
  }

  @Override
  void createUser(UserDetails userDetails) {
      User user = new User(
        id: UUID.randomUUID().toString(),
        username: userDetails.username,
        password: userDetails.password,
        accountNonExpired: userDetails.accountNonExpired,
        accountNonLocked: userDetails.accountNonLocked,
        enabled: userDetails.enabled,
        credentialsNonExpired: userDetails.credentialsNonExpired,
        roles: userDetails.authorities.collect { authority -> authority.authority}
      );
      putUser(user)
  }

  @Override
  void updateUser(UserDetails userDetails) {
    User user = loadByUsername(userDetails.username)
    user.credentialsNonExpired = userDetails.credentialsNonExpired
    user.enabled = userDetails.enabled
    user.accountNonLocked = userDetails.accountNonLocked
    user.accountNonExpired = userDetails.accountNonExpired
    user.roles = userDetails.authorities.collect { authority -> authority.authority}
    putUser(user)
  }

  private putUser(User user) {
    redisTemplate.opsForHash().put('users', user.username, new JsonBuilder(user).toString())
  }

  private User loadByUsername(String username) {
    new JsonSlurper().parseText(redisTemplate.opsForHash().get('users', username) as String) as User
  }

  @Override
  void deleteUser(String username) {
    redisTemplate.opsForHash().delete('users', username)
  }

  @Override
  void changePassword(String oldPassword, String newPassword) {
    Authentication currentUser = SecurityContextHolder.getContext()
      .getAuthentication();

    if (currentUser == null) {
      // This would indicate bad coding somewhere
      throw new AccessDeniedException(
        "Can't change password as no Authentication object found in context "
          + "for current user.");
    }

    String username = currentUser.getName();
    User user = loadByUsername(username)
    if (!oldPassword.equals(user.password)){
      throw new java.nio.file.AccessDeniedException("The old password is not correct")
    }
    user.password = newPassword
    putUser(user)
  }

  @Override
  boolean userExists(String username) {
    return redisTemplate.opsForHash().hasKey('users', username)
  }

  @Override
  UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    User user = loadByUsername(username)
    return new org.springframework.security.core.userdetails.User(user.username, user.password, user.enabled,
      user.accountNonExpired,  user.credentialsNonExpired,
      user.accountNonLocked, user.roles.collect { role -> new SimpleGrantedAuthority(role)})
  }
}
