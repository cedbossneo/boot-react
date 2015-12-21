package react.utils

import com.fasterxml.jackson.databind.ObjectMapper
import com.netflix.falcor.model.FalcorMethod
import com.netflix.falcor.model.FalcorModel
import com.netflix.falcor.model.FalcorPath
import com.netflix.falcor.router.FalcorRequest
import com.netflix.falcor.router.FalcorRouter
import com.netflix.falcor.router.Route
import rx.Subscriber

import javax.servlet.http.HttpServletRequest
import javax.servlet.http.HttpServletResponse

/**
 * Created by chauber on 18/12/15.
 */
class FalcorRouterUtils {
  static void createRouter(HttpServletRequest request, HttpServletResponse response, String paths, String method, Route<HttpServletRequest> route, ObjectMapper objectMapper) throws IOException {
    FalcorMethod falcorMethod = FalcorMethod.valueOf(method.toUpperCase());
    //noinspection GroovyAssignabilityCheck
    FalcorPath[] falcorPath = objectMapper.readValue(paths, FalcorPath[].class);
    FalcorRequest<HttpServletRequest> falcorRequest = new FalcorRequest<>(request, falcorMethod, falcorPath);
    FalcorRouter<HttpServletRequest> router = new FalcorRouter<>(route, new FalcorModel());
    router.route(falcorRequest).subscribe(new Subscriber<Object>() {

      @Override
      public final void onCompleted() {
        try {
          response.getWriter().write(router.getModel().toString());
          response.getWriter().flush();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }

      @Override
      public final void onError(Throwable e) {
        e.printStackTrace();
      }

      @Override
      public final void onNext(Object args) {

      }

    });
  }
}
