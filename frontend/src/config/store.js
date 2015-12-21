import React from 'react';
import reducer from 'reducers';
import { createStore, combineReducers, applyMiddleware, compose } from 'redux';
import isDev from 'isdev';
import DevTools from 'config/devtools';
import promiseMiddleware from 'config/promiseMiddleware';
import { createFalcorMiddleware } from 'redux-falcor';
import { Model } from 'falcor';
import HttpDataSource from 'falcor-http-datasource';

const falcorUserModel = new Model({
  source: new HttpDataSource('/api/users/users.json', {
    headers: {
      'x-auth-token': localStorage.getItem('auth-token')
    }})
});


const middlewares = isDev ?
  [applyMiddleware(promiseMiddleware), applyMiddleware(createFalcorMiddleware(falcorUserModel)), DevTools.instrument()] :
  [applyMiddleware(promiseMiddleware), applyMiddleware(createFalcorMiddleware(falcorUserModel))];
const finalCreateStore = compose(...middlewares)(createStore);

var initialize = (initialState = {}) => {
  const store = finalCreateStore(reducer, initialState);

  if (module.hot) {
    // Enable Webpack hot module replacement for reducers
    module.hot.accept('../reducers', () => {
      const nextReducer = require('../reducers');
      store.replaceReducer(nextReducer);
    });
  }
  return store;
};

export default initialize;

