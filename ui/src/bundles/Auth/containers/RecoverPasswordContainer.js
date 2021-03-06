import { connect } from 'react-redux';
import { actions } from 'react-redux-form';
import lifecycle from 'components/Lifecycle';
import { toggleSignInWToFalse } from 'modules/Wishlist/SignInWPageModule';
import { modelPath, recoverPassword } from 'bundles/Auth/modules/RecoverPasswordModule';
import RecoverPassword from 'bundles/Auth/components/RecoverPassword';

/**
 * Maps the state properties to the React component `props`.
 *
 * @param {Object} state The application state.
 * @returns {Object} The props passed to the react component.
 */
const mapStateToProps = state => ({
  form: state.auth.recoverPassword.form,
  ...state.auth.recoverPassword.request,
});

/**
 * Maps the store `dispatch` function to the React component `props`.
 *
 * @param {Function} dispatch The Redux store dispatch function.
 * @returns {Object} The props passed to the react component.
 */
const mapDispatchToProps = dispatch => ({
  onSend: data => dispatch(recoverPassword(data)),
  componentDidMount: () => dispatch(toggleSignInWToFalse()),
  componentWillUnmount: () => dispatch(actions.reset(modelPath)),
});

export default connect(mapStateToProps, mapDispatchToProps)(lifecycle(RecoverPassword));
