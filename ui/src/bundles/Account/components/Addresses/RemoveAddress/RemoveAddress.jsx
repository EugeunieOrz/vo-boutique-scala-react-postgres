// @flow
import React from 'react';
import { withTranslation } from "react-i18next";
import Button from 'react-bootstrap/Button';
import Col from 'react-bootstrap/Col';
import Image from 'react-bootstrap/Image';
import Modal from 'react-bootstrap/Modal';
import Row from 'react-bootstrap/Row';
import '../../RemoveElement.scss';
import closeSign from '../../../../../static/icons/close.svg';

/*
Author: Ievgeniia Ozirna
Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
*/

type Props = {
  i18n: Object,
  t: Object,
  indexToRemoveAddress: Number,
  onRemoveAddress: (userID: string, indexToRemoveAddress: Number) => any,
  onShowRemoveAddressModal: (indexToRemoveAddress: Number) => any,
  removeAddressAlertIsShown: boolean,
  userID: string,
}

export const RemoveAddressComponent = ({
  i18n, t, indexToRemoveAddress, onRemoveAddress, onShowRemoveAddressModal,
  removeAddressAlertIsShown, userID,
}: Props) => (
  <Modal
    id="remove-container"
    show={removeAddressAlertIsShown}
    onHide={onShowRemoveAddressModal}
    size="lg">
    <Modal.Body className="pl-0 py-0">
      <Row className="d-flex justify-content-end pt-2 pr-2">
        <Image
          id="remove-close-img"
          src={closeSign}
          alt=""
          width="24"
          height="24"
          onClick={() => onShowRemoveAddressModal(indexToRemoveAddress)}
         />
      </Row>
      <Row className="my-2 mx-5 justify-content-center">
        <p
          id={
            i18n.translator.language === "ar" ?
            "remove-msg-ar" : "remove-msg"
          }
          className="text-center">
          {t('qDeleteAddr')}
        </p>
      </Row>
      <Row className="mt-4 mb-5 mx-5">
        <Col xs={12} lg={6} className="d-flex justify-content-center mb-3 mb-lg-0">
          <Button
            id={
              i18n.translator.language === "ar" ?
              "rm-confirm-btn-ar" : "rm-confirm-btn"
            }
            onClick={() => onRemoveAddress(userID, indexToRemoveAddress)}>
            {t('CONFIRM')}
          </Button>
        </Col>
        <Col xs={12} lg={6} className="d-flex justify-content-center mt-3 mt-lg-0">
          <Button
            id={
              i18n.translator.language === "ar" ?
              "rm-cancel-btn-ar" : "rm-cancel-btn"
            } 
            onClick={() => onShowRemoveAddressModal(indexToRemoveAddress)}>
            {t('CANCEL')}
          </Button>
        </Col>
      </Row>
    </Modal.Body>
  </Modal>
);

export default withTranslation()(RemoveAddressComponent);
