// @flow
import React from 'react';
import { Form, Errors } from 'react-redux-form';
import { withTranslation, Trans } from "react-i18next";
import { isRequired } from 'util/Validator';
import { modelPath } from 'modules/Orders/ReturnFormModule';
import FormControl from 'components/FormControl';
import Button from 'react-bootstrap/Button';
import Col from 'react-bootstrap/Col';
import Row from 'react-bootstrap/Row';
import './ReturnForm.scss';

/*
 *  Author: Ievgeniia Ozirna
 *  Licensed under the CC BY-NC-ND 3.0: http://creativecommons.org/licenses/by-nc-nd/3.0/
 */

type Props = {
  form: {[string]: FormProps},
  i18n: Object,
  t: Object,
  isPending: boolean,
  fillReturnForm: (data: Object, userID: string) => any,
  userID: string,
}

export const ReturnFormComponent = ({
  i18n, t, form, isPending, fillReturnForm,
  userID,
}: Props) => (
  <Row className="flex-grow-1 mt-5">
    <Col md={2} xl={3} className="d-none d-md-flex"></Col>
    <Col xs={12} md={8} xl={6}>
      <div id="return-form-header">
        {t('returnForm-2')}
      </div>
      <div id="return-form-msg" className="my-4">
        {t('returnFormMsg')}
      </div>
      <Form
        model={modelPath}
        onSubmit={data => fillReturnForm({
          data: data,
          userID: userID
        })}
        autoComplete="off">
        <FormControl
          className={
            form.orderNumber.touched && !form.orderNumber.valid ?
            "returnform-num-error-form" :
            "returnform-num-form"
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
          wrapper={(props) => <div className="return-ordernum-error">
          {props.children[0]}<br />{props.children[1]}</div>}
          show="focus"
        />
      <div id={form.orderNumber.value.length === 24 ? "return-order-num-error-blank" : "return-order-num-error"}></div>
      <Button
        id={
          isPending ?
          (
            i18n.translator.language === "it" ?
            "fill-return-form-pending-it-btn" :
            "fill-return-form-btn"
          ) :
          "fill-return-form-btn"
        }
        className="mt-3"
        type="submit"
        disabled={!form.$form.valid || isPending}>
          {isPending ? <Trans>LOADING</Trans> : <div>{t('searchOrderNum-2')}</div>}
      </Button>
      </Form>
    </Col>
    <Col md={2} xl={3} className="d-none d-md-flex"></Col>
  </Row>
);

export default withTranslation()(ReturnFormComponent);
