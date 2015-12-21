import { combineReducers } from 'redux';
import simple from './simple';
import authentication from './authentication';
import locale from './locale';
import { routeReducer } from 'redux-simple-router';
import { falcorReducer } from 'redux-falcor';

export default combineReducers({
  routing: routeReducer,
  entities: falcorReducer,
  simple,
  authentication,
  locale
});
