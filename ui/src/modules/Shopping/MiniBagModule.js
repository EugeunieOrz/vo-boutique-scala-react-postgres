// @flow
import { createAction, handleActions } from 'redux-actions';

export const miniBagState = {
  isShown: false
}

export const closeMiniBag = createAction('CLOSE_MINI_BAG');
export const closeMiniBagEtc = createAction('CLOSE_MINI_BAG_ETC');
export const toggleMiniBag = createAction('TOGGLE_MINI_BAG');

export default handleActions({
  [toggleMiniBag]: (state, action) => ({...state, isShown: !state.isShown }),
  [closeMiniBag]: (state, action) => ({...state, isShown: false }),
}, miniBagState);
