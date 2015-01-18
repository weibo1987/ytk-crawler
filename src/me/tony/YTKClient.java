package me.tony;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

import me.tony.util.ClientUtils;

import org.apache.http.HttpResponse;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
public class YTKClient {

	private final static HttpClient client=new DefaultHttpClient();;
	public YTKClient() {
//		init();
	}
	
	private static void init(){
		ResourceBundle bundle=ResourceBundle.getBundle("config/course");
		
		String url="http://www.yuantiku.com/iphone/users/login?version=3.0.1&av=2";
		String user_agent="猿题库 3.0.1 (iPhone; iPhone OS 8.0.2; zh_CN)";
		String device="iphone:"+bundle.getString("type")+":7506DC6E-B08E-45F8-BF83-761970EF369C";
		String email=bundle.getString("account");
		String password=bundle.getString("password");
		String persistent=1+"";
		
		client.getParams().setParameter("user_agent", user_agent);
		client.getParams().setParameter(CoreConnectionPNames.CONNECTION_TIMEOUT,300000);
		client.getParams().setParameter(CoreConnectionPNames.SO_TIMEOUT,300000);
		
		Map<String,String> map=new HashMap<String,String>();
		map.put("device", device);
		map.put("phone",email);
		map.put("password",password);
		map.put("persistent", "1");
		HttpPost post = ClientUtils.postForm(url, map); 
		post.setHeader("content_type", "application/x-www-form-urlencoded; charset=utf-8");
		HttpResponse response;
		try {
	             response=client.execute(post);
			System.out.println(client);
			EntityUtils.consume(response.getEntity());
			System.out.println("login succcess!");
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static HttpClient getClient(){
		init();
		return client;
	}
}
