package com.echo.littleapple;

import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;

//TODO please refer to: http://blog.csdn.net/flying_tao/article/details/6553601
public class Util {

	// TODO can be used to submit data or get data from the server
	public static String httpPost(String uri, List<NameValuePair> nameValuePairs){
		
		HttpPost httpPost = new HttpPost(uri);
		String resultString = null;
		try {
			if (nameValuePairs != null) {
				httpPost.setEntity(new UrlEncodedFormEntity(nameValuePairs, HTTP.UTF_8));
			}

			HttpResponse httpResponse = new DefaultHttpClient().execute(httpPost);
			
			if (httpResponse.getStatusLine().getStatusCode() == 200) {
				resultString = EntityUtils.toString(httpResponse.getEntity());
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return resultString;
	} 
	
}
