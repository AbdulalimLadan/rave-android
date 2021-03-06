package com.flutterwave.raveandroid.verification.web;


import com.flutterwave.raveandroid.data.Callbacks;
import com.flutterwave.raveandroid.data.NetworkRequestImpl;
import com.flutterwave.raveandroid.data.RequeryRequestBody;
import com.flutterwave.raveandroid.responses.RequeryResponse;

import javax.inject.Inject;

public class WebPresenter implements WebContract.UserActionsListener {

    @Inject
    NetworkRequestImpl networkRequest;
    private WebContract.View mView;

    @Inject
    public WebPresenter(WebContract.View mView) {
        this.mView = mView;
    }

    @Override
    public void init(String flwRef, String publicKey) {
        requeryTx(flwRef, publicKey);

    }

    @Override
    public void requeryTx(final String flwRef, final String publicKey) {
        RequeryRequestBody body = new RequeryRequestBody();
        body.setOrder_ref(flwRef); // Uses Order ref instead of flwref
        body.setPBFPubKey(publicKey);

        networkRequest.requeryTx(body, new Callbacks.OnRequeryRequestComplete() {
            @Override
            public void onSuccess(RequeryResponse response, String responseAsJSONString) {
                if (response.getData() == null) {
                    mView.onPaymentFailed(response.getStatus(), responseAsJSONString);
                } else if (response.getData().getChargeResponseCode().equals("02")) {
                    mView.onPollingRoundComplete(flwRef, publicKey);
                } else if (response.getData().getChargeResponseCode().equals("00")) {
                    mView.onPaymentSuccessful(responseAsJSONString);
                } else {
                    mView.onPaymentFailed(response.getData().getStatus(), responseAsJSONString);
                }
            }

            @Override
            public void onError(String message, String responseAsJSONString) {
                mView.onPaymentFailed(message, responseAsJSONString);
            }
        });
    }

    @Override
    public void onAttachView(WebContract.View view) {
        this.mView = view;
    }

    @Override
    public void onDetachView() {
        this.mView = new NullWebView();
    }
}
