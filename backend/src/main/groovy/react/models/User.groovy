package react.models

import com.sun.org.apache.xpath.internal.operations.Bool
import groovy.transform.Canonical

/**
 * Created by chauber on 17/12/15.
 */
@Canonical
class User {
  String id
  String username
  String password
  Boolean accountNonExpired
  Boolean accountNonLocked
  Boolean enabled
  Boolean credentialsNonExpired
  List<String> roles
}
