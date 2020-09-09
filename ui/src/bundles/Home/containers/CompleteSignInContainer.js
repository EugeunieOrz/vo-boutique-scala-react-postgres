import { connect } from 'react-redux';
import { actions } from 'react-redux-form';
import lifecycle from 'components/Lifecycle';
import { getCustomerEmail } from 'selectors/EmailSelector';
import { getShoppingBag } from 'selectors/ShoppingSelector';
import { modelPath, completeSignInForm } from 'bundles/Home/modules/CompleteSignInFormModule';
import CompleteSignIn from 'bundles/Home/components/CompleteSignIn';

/**
 * Maps the state properties to the React component `props`.
 *
 * @param {Object} state The application state.
 * @returns {Object} The props passed to the react component.
 */
const mapStateToProps = state => ({
  form: state.home.completeSignInForm.form,
  ...state.home.completeSignInForm.request,
  email: getCustomerEmail(),
  shoppingBag: getShoppingBag(state),
});

/**
 * Maps the store `dispatch` function to the React component `props`.
 *
 * @param {Function} dispatch The Redux store dispatch function.
 * @returns {Object} The props passed to the react component.
 */

const mapDispatchToProps = dispatch => ({
  completeSignInForm: data => dispatch(completeSignInForm(data)),
  componentWillUnmount: () => dispatch(actions.reset(modelPath)),
});

export default connect(mapStateToProps, mapDispatchToProps)(lifecycle(CompleteSignIn));
