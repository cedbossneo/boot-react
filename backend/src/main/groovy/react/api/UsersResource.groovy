package react.api

import com.fasterxml.jackson.databind.JsonNode
import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.falcor.model.FalcorPath
import com.netflix.falcor.model.StringKey
import com.netflix.falcor.router.RequestContext
import com.netflix.falcor.router.Route
import com.netflix.falcor.router.Route1
import com.netflix.falcor.util.RouteResult
import groovy.json.JsonSlurper
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.redis.core.RedisTemplate
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController
import react.models.User
import react.utils.FalcorRouterUtils
import rx.Observable

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

import static com.netflix.falcor.router.BasicDirectives.firstOf
import static com.netflix.falcor.router.MethodDirectives.get
import static com.netflix.falcor.router.MethodDirectives.set
import static com.netflix.falcor.router.PathDirectives.*
import static com.netflix.falcor.router.PathMatchers.key
import static com.netflix.falcor.router.PathMatchers.stringKey
import static com.netflix.falcor.router.RouteDirectives.inMemory
import static com.netflix.falcor.router.RouteDirectives.ref

@RestController
@RequestMapping("/api/users")
class UsersResource {

  @Autowired
  ObjectMapper objectMapper;

  @Autowired
  RedisTemplate redisTemplate

  @RequestMapping(value = "/users.json")
  void usersFalcor(HttpServletRequest request, HttpServletResponse response, @RequestParam String paths, @RequestParam String method, @RequestParam(required = false) Object value) throws IOException {
    ArrayList<Route<HttpServletRequest>> routes = new ArrayList<>();
    routes.add(usersRoute());
    routes.add(userIndexedRoute());
    routes.add(userByUsernameRoute());
    routes.add(userSetRoute(value));
    FalcorRouterUtils.createRouter(request, response, paths, method, firstOf(routes), objectMapper);
  }

  // Handle : users[0].password = 'huhu' or userByUsername['user'].password= 'huho'
  private Route userSetRoute(Object value) {
    set(pathPrefix(key("users"), stringKey, stringKey, { String key, String username, String field ->
      def user = redisTemplate.opsForHash().get('users', username) as User
      user[field] = value
      redisTemplate.opsForHash().put('users', username, user)
      ref(FalcorPath.of(new StringKey("userByUsername"), new StringKey(username)))
    }));
  }

  //Handle users[0] or users[0..10]
  private Route userIndexedRoute() {
    get(pathPrefix(key("users"), { String key ->
      def users = redisTemplate.opsForHash().keys('users').asList()
      indexed(users, { index, String username ->
        ref(FalcorPath.of(new StringKey("userByUsername"), new StringKey(username)))
      })
    }));
  }

  // Handles all users
  private Route usersRoute() {
    /*TODO: Find a way to handle Java Lambda in Groovy correctly
    The method should be :
    get(match(key("users"), { String key, RequestContext<HttpServletRequest> context ->
        def users = redisTemplate.opsForHash().keys('users')
        Observable.from(users).flatMap({ String username ->
          ref(FalcorPath.of(new StringKey("userByUsername"), new StringKey(username))).call(context.withPaths(context.getMatched(), context.getUnmatched()))
        })
    }));
    */
    get(match(key("users"), new Route1<HttpServletRequest, String>(){
      @Override
      Observable<RouteResult> call(String key, RequestContext<HttpServletRequest> context) {
        def users = redisTemplate.opsForHash().keys('users')
        Observable.from(users).flatMap({ String username ->
          ref(FalcorPath.of(new StringKey("userByUsername"), new StringKey(username))).call(context.withPaths(context.getMatched(), context.getUnmatched()))
        })
      }

      @Override
      Route<HttpServletRequest> call(String s) {
        null;
      }
    }));
  }

  //Handle userByUsername['user'] or every query that use this ref
  private Route userByUsernameRoute() {
    get(pathPrefix(key("userByUsername"), stringKey, { String key, String username ->
      def user = new JsonSlurper().parseText(redisTemplate.opsForHash().get('users', username) as String) as User
      inMemory(objectMapper.convertValue(user, JsonNode.class));
    }))
  }

}
