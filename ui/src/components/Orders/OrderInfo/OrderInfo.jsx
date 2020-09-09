// @flow
import React from 'react';
import { Form, Errors } from 'react-redux-form';
import { withTranslation, Trans } from "react-i18next";
import { isRequired } from 'util/Validator';
import { modelPath } from 'modules/Orders/OrderInfoFormModule';
import FormControl from 'components/FormControl';
import Button from 'react-bootstrap/Button';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import CoreContainer from 'containers/CoreContainer';
import './OrderInfo.scss';

/*
 *  Author: Ievgeniia Ozirna
 *  Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

type Props = {
  form: {[string]: FormProps},
  i18n: Object,
  t: Object,
  isPending: boolean,
  followOrder: (data: Object) => any,
}

export const OrderInfoComponent = ({
  i18n, t, form, isPending, followOrder,
}: Props) => (
  <CoreContainer>
    <Row className="flex-grow-1">
      <Col md={2} xl={3} className="d-none d-md-flex"></Col>
      <Col xs={12} md={8} xl={6}>
        <Row id="orderinfo-title-row" className="py-1 d-flex flex-row justify-content-center">
          <div id="orderinfo-header">
            {t('orderInfo')}
          </div>
        </Row>
        <div id="orderinfo-msg" className="my-4">
          {t('orderInfoMsg')}
        </div>
        <Form model={modelPath} onSubmit={followOrder} autoComplete="off">
          <FormControl
            className={
              form.orderNumber.touched && !form.orderNumber.valid ?
              "followOrder-num-error-form" :
              "followOrder-num-form"
            }
            id="orderNumber"
            formProps={form.orderNumber}
            controlProps={{
              type: 'text',
              placeholder: t('orderNum'),
              maxLength: 255,
            }}
            validators={{
              isRequired,
            }}
          />
          <Errors
            model=".orderNumber"
            messages={{
                isRequired:  t('enterOrderNum'),
            }}
            wrapper={(props) => <div className="orderinfo-ordernum-error">
            {props.children[0]}<br />{props.children[1]}</div>}
            show="focus"
          />
         <div id="orderinfo-ordernum-error"></div>
         <Button
           className={
             isPending ?
             (
               i18n.translator.language === "it" ?
               "mt-4 follow-order-pending-it-btn" :
               "mt-4 follow-order-pending-btn"
             ) :
             "mt-4 follow-order-btn"
           }
           type="submit"
           disabled={!form.$form.valid || isPending}>
            {isPending ? <Trans>LOADING</Trans> : <div>{t('searchOrderNum')}</div>}
          </Button>
        </Form>
      </Col>
      <Col md={2} xl={3} className="d-none d-md-flex"></Col>
    </Row>
  </CoreContainer>
);

export default withTranslation()(OrderInfoComponent);
