package application.hdlakhani.com.heartland;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.ConsoleMessage;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class PaymentHeartlandActivity extends AppCompatActivity {

    Button button;
    WebView webView;
    int iCapturePaymentAPIMaxTryCount;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        webView = (WebView) findViewById(R.id.webview);
        class JsObject {
            @JavascriptInterface
            public String toString() { return "injectedObject"; }
        }
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new JsObject(), "injectedObject");
        Random rand = new Random();


        int randomNum = rand.nextInt((99999 - 0) + 1) + 0;
        Map<String, String> mapParams = new HashMap<>();
        mapParams.put("OrderRefNo", String.valueOf(randomNum));
        mapParams.put("Amount", "1");
        mapParams.put("StoreId", "1");

        webview_ClientPost(webView, "http://m1superstore.azurewebsites.net/Payment/HeartLandPaymentPage", mapParams.entrySet());
//        webView.loadUrl("http://m1superstore.azurewebsites.net/Payment/HeartLandPaymentPage");
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                android.util.Log.d("WebView", consoleMessage.message());
                return true;
            }
        });




    }

    public void webview_ClientPost(WebView webView, String url,Collection<Map.Entry<String, String>> postData) {
        StringBuilder sb = new StringBuilder();

        sb.append("<html><head></head>");
        sb.append("<body onload='form1.submit()'>");
        sb.append(String.format("<form id='form1' action='%s' method='%s'>",url, "post"));
        for (Map.Entry<String, String> item : postData) {
            sb.append(String.format("<input name='%s' type='hidden' value='%s' />",item.getKey(), item.getValue()));
        }
        sb.append("</form></body></html>");
        Log.d("webview_ClientPost called", "webview_ClientPost called");
        webView.getSettings().setJavaScriptEnabled(true);
        webView.addJavascriptInterface(new HeartLandJavaScriptInterface(this), "HeartLand");
        webView.loadData(sb.toString(), "text/html", "utf-8");
    }

    public class HeartLandJavaScriptInterface {
        Context mContext;

        /**
         * Instantiate the interface and set the context
         */
        HeartLandJavaScriptInterface(Context c) {
            mContext = c;
        }


        @JavascriptInterface
        public void success(final String paymentId, final String amount, final String status, final String txnid) {

          Toast.makeText(PaymentHeartlandActivity.this,"Payment successfully done!!!",Toast.LENGTH_LONG).show();
        }

        @JavascriptInterface
        public void failure(final String paymentId, final String amount, final String status, final String txnid) {

            showAlertForTryAgain();
        }
    }

    private void showAlertForTryAgain() {
        final AlertDialog alertDlgTryAgain = new AlertDialog.Builder(this).create();
        alertDlgTryAgain.setTitle("error");
        alertDlgTryAgain.setMessage("Transaction Fail");
        alertDlgTryAgain.setCancelable(false);
        alertDlgTryAgain.setButton(DialogInterface.BUTTON_POSITIVE,"Try Again",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        iCapturePaymentAPIMaxTryCount++;
                        if (iCapturePaymentAPIMaxTryCount < 4) {
                            alertDlgTryAgain.dismiss();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
//                                    postNewData();
                                }
                            });
                        }
                    }

                });
        alertDlgTryAgain.setButton(DialogInterface.BUTTON_NEGATIVE,"Cancel",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        alertDlgTryAgain.dismiss();

                    }
                });
        alertDlgTryAgain.show();
    }


}
