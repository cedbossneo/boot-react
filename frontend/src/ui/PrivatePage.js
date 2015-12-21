import React, { Component } from 'react';
import { connect } from 'react-redux';
import { retrievePath } from 'redux-falcor';

import Translate from 'react-translate-component';

export class PrivatePage extends Component {
  constructor(props) {
    super(props)
  }

  componentDidMount(){
    this.props.retrievePath('users[0].username');
  }

  render() {
    var users = Object.keys((this.props.users || {})).map((id) => <li>{this.props.users[id].username}</li>)
    return (
      <div>
        <Translate component="h2" content="private.title" />

        <Translate component="p" content="private.greeting" name={username} />
        <h1>Users :</h1>
        <ul>
          {users}
        </ul>
      </div>);
  }
};

export default connect(
  state => ({username: authentication.username, users: state.entities.users}),
  {retrievePath}
)(PrivatePage);
