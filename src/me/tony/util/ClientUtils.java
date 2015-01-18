package me.tony.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

public class ClientUtils {


	public   static   String   inputStream2String(InputStream   is)   throws   IOException{ 
		ByteArrayOutputStream   baos   =   new   ByteArrayOutputStream(); 
		int   i=-1; 
		while((i=is.read())!=-1){ 
			baos.write(i); 
		} 
		return   baos.toString(); 
	}

	public static synchronized String handlePost(String name,String url,Map<String,String> params,HttpClient client){
		HttpResponse resp_category;
		try {
			resp_category = client.execute(postForm(url,params));
			System.out.println("------start "+name+" ...");
			System.out.println(resp_category.getStatusLine());
			String result=EntityUtils.toString(resp_category.getEntity());
			EntityUtils.consume(resp_category.getEntity());
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public static synchronized String handle(String name,String url,HttpClient client){
		HttpGet get_category=new HttpGet(url);
		try {
			HttpResponse resp_category=client.execute(get_category);
			System.out.println("------start "+name+" ...");
			System.out.println(resp_category.getStatusLine());
			String result=EntityUtils.toString(resp_category.getEntity());
			EntityUtils.consume(resp_category.getEntity());
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	public static synchronized HttpPost postForm(String url, Map<String, String> params){  
		HttpPost httpost = new HttpPost(url);  
		List<NameValuePair> nvps = new ArrayList <NameValuePair>();  
		Set<String> keySet = params.keySet();  
		for(String key : keySet) {  
			nvps.add(new BasicNameValuePair(key, params.get(key)));  
		}  
		try {  
			httpost.setEntity(new UrlEncodedFormEntity(nvps, HTTP.UTF_8));  
		} catch (UnsupportedEncodingException e) {  
			e.printStackTrace();  
		}  
		return httpost;  
	}

	public static synchronized String handlePut(String name,String url,HttpClient client){
		HttpPut put=new HttpPut(url);
		HttpResponse resp_category;
		try {
			resp_category = client.execute(put);
			System.out.println("------start "+name+" ...");
			System.out.println(resp_category.getStatusLine());
			String result=EntityUtils.toString(resp_category.getEntity());
			EntityUtils.consume(resp_category.getEntity());
			return result;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
}
