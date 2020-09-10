// @flow
import { call, put, take } from 'redux-saga/effects';
import { handleError } from 'util/Saga';
import { toggleProfileActiveKey } from 'bundles/Account/modules/ProfileActiveKeyModule';
import { closeMenu } from 'modules/Menu/MenuModule';
import { openMyWishlist } from 'modules/Wishlist/SignInWPageModule';
import { history } from 'modules/LocationModule';
import config from 'config/index';
/*
Author: Ievgeniia Ozirna
Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
*/
export function* openMyWishlistSaga(): Generator<*, *, *> {
  while (yield take(openMyWishlist().type)) {
    try {
      yield put(closeMenu());
      yield call(history.push, config.route.account.index);
      yield put(toggleProfileActiveKey("wishlist"));
    } catch (e) {
      yield call(handleError, e);
    }
  }
}

export default openMyWishlistSaga;
