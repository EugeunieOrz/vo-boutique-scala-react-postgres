// @flow
import { createAction, handleActions } from 'redux-actions';

export const addItemToBagAlertState = {
  isShown: false
}

export const closeAddItemToBagAlert = createAction('CLOSE_ADD_ITEM_TO_BAG_ALERT');
export const toggleAddItemToBagAlert = createAction('TOGGLE_ADD_ITEM_TO_BAG_ALERT');

export default handleActions({
  [toggleAddItemToBagAlert]: (state, action) => ({...state, isShown: !state.isShown }),
  [closeAddItemToBagAlert]: (state, action) => ({...state, isShown: false }),
}, addItemToBagAlertState);
