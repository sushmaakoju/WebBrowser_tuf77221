package com.example.webbrowser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

import android.support.v7.app.ActionBarActivity;
import android.support.v7.appcompat.R.string;
import android.test.UiThreadTest;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnKeyListener;
import android.view.inputmethod.EditorInfo;
import android.webkit.URLUtil;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;


//***********************Web Browser code implementation - tuf77221 ***********************
public class MainActivity extends ActionBarActivity {

	private Button goButton;
	private WebView webView;
	private EditText url_edittext;

	//handler for loading URL to webview
	final Handler showContent = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			//Load browser
			webView.loadData(msg.obj.toString(), "text/html; charset=UTF-8", null);
			Log.e(INPUT_SERVICE, msg.obj.toString());
			return false;
		}
	});
	
	//Handler for error message
final Handler showErrorMsg = new Handler(new Handler.Callback() {
		
		@Override
		public boolean handleMessage(Message msg) {
			// TODO Auto-generated method stub
			Context contxt = (Context) msg.obj;
			Toast.makeText(contxt, R.string.ErrorMessage, Toast.LENGTH_LONG).show();
			Log.e(INPUT_SERVICE, msg.obj.toString());
			return false;
		}
	});
	
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		goButton = (Button) findViewById(R.id.go_button);
		webView = (WebView) findViewById(R.id.url_webView);
		url_edittext = (EditText) findViewById(R.id.url_edittext);
		/*url_edittext.setOnFocusChangeListener(new EditText.OnFocusChangeListener(){
			@Override
			public void onFocusChange(View v, boolean hasFocus) {
				// TODO Auto-generated method stub
				if(!hasFocus && !checkURL(url_edittext.getText().toString()))
				{
					url_edittext.setError("Please enter a valid URL" + "example: www.xxxx.com");
				}
			}
		});*/

		
		webView.setWebViewClient(new WebViewClient());
		goButton.setOnClickListener(new View.OnClickListener() {
			
			@Override
			public void onClick(View v) {
				// TODO Auto-generated method stub
				
				
				Thread loadContent = new Thread(){ 
				
					@SuppressLint("SetJavaScriptEnabled")
					@Override
					public void run() {
											
						if(isNetworkActive())
						{
							URL url = null;
							URLConnection uc;
							String response = "", tmpResponse ="", str= "";
							//URL Check
															
								String strUrl = url_edittext.getText().toString();
								if(checkURL((CharSequence)strUrl))
								{
									if(!strUrl.startsWith("http://") && strUrl.contains("."))
										strUrl = "http://" + strUrl;
									try
									{
										url = new URL(strUrl);
									}
									catch(MalformedURLException e){
										e.printStackTrace();								
									}
								}
								else
								{
									if(!strUrl.startsWith("http://") && strUrl.contains("."))
										strUrl = "http://" + strUrl;
									try
									{
										url = new URL(strUrl);
									}
									catch(MalformedURLException e){
										
										Context context= getBaseContext();
										Message msg = Message.obtain();
										msg.obj = context;
										
										showErrorMsg.sendMessage(msg);
										
										e.printStackTrace();								
									}
							}
							
							
							//end URL checks
							try
							{
								WebSettings webSettings = webView.getSettings();
								if(webSettings != null){
									webSettings.setJavaScriptEnabled(true);
								}
							}
							catch(Exception e){
								e.printStackTrace();
							}
							//
							try {
								BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
								
								tmpResponse = reader.readLine();
								while(tmpResponse != null)
								{
									response = response + tmpResponse;
									tmpResponse = reader.readLine();
								}
								
								
								
								Message msg = Message.obtain();
								msg.obj = response;
								
								showContent.sendMessage(msg);
							}catch (Exception e){e.printStackTrace();}
						}
					}
				};
					loadContent.start();
			}
			
				
		});
		//Press Back to retrieve history
		webView.setOnKeyListener(new View.OnKeyListener() {
			
			@Override
			public boolean onKey(View v, int keyCode, KeyEvent event) {
				// TODO Auto-generated method stub
				if(event.getAction() == KeyEvent.ACTION_DOWN){
					switch(keyCode){
						case KeyEvent.KEYCODE_BACK:				
							if(webView.canGoBack()){
								webView.goBack();
								return true;
							}
							break;			
						}
					}
				return false;
			}
		});
	}
	
	//Check Network connectivity
	public boolean isNetworkActive()
	{
		boolean result = false;
		ConnectivityManager conn = (ConnectivityManager) this.getSystemService(Context.CONNECTIVITY_SERVICE);
		if(conn != null)
		{
			NetworkInfo nwinfo = conn.getActiveNetworkInfo();
			if(nwinfo != null && nwinfo.isConnected())
			{
				result = true;
			}
			else
			{
				result = false;
			}
		}
		return result;
	}
	
	//check if a valid network URL
		public static boolean checkURL(CharSequence url){
			if(TextUtils.isEmpty(url)){
				return false;
			}
			
			Pattern url_pattern = Patterns.WEB_URL;
			boolean url_result = url_pattern.matcher(url).matches();
				if(!url_result){
					String urlStr = url + "";
					if(URLUtil.isNetworkUrl(urlStr))
					{
						try{
							new URL(urlStr);
							url_result = true;
						}catch (Exception e){ e.printStackTrace();
					}

				}
					else
					{
						if(!urlStr.startsWith("http://"))
							urlStr = "http://" + urlStr;
						if(!urlStr.contains("."))
						{
							url_result = false;
						}
						else
						{
							url_result = true;
						}
					}
			}
				else
				{
				url_result = false;
				}
				return url_result;
		}
		

	
	
	//to define a specific URL behaviour, if any. Commented for now, as it was not needed. A generic in-website URL behaviour was handled by webclientview class.
	/*public class MyWebViewClient extends WebViewClient {
		boolean res = false;
		@Override
			public boolean shouldOverrideUrlLoading(WebView veiw, String url)
			{
			if(url != null)
			{
				
				String str = Uri.parse(url).getHost();
				if(str != null)
				{
					if(Uri.parse(url).toString().contains((CharSequence) str)){			
					
						Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
						startActivity(intent);
						res = true;
					}
				}
				else
				{
					res = true;
				}
			}
				return res;
			}
		}*/
	
		/*public void showError(String title, String message, String text)
	{
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(title);
		alert.setMessage(message);
		alert.setNegativeButton("Ok", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				
			}
		});
		alert.setIcon(R.drawable.ic_launcher);
		alert.create().show();
	}*/
}
