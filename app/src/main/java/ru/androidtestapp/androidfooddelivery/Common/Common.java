package ru.androidtestapp.androidfooddelivery.Common;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import ru.androidtestapp.androidfooddelivery.Model.User;
import ru.androidtestapp.androidfooddelivery.Remote.APIService;
import ru.androidtestapp.androidfooddelivery.Remote.RetrofitClient;

public class Common {
	public static User currentUser;
	
	private static final String BASE_URL = "https://fcm.googleapis.com/";
	public static String PHONE_TEXT = "\"userPhone\"";
	
	public static APIService getFCMService(){
		return RetrofitClient.getClient( BASE_URL ).create( APIService.class );
	}
	
	public static String convertCodeToStatus(String status) {
		if(status.equals( "0" )){
			return "Placed";
		} else if(status.equals( "1" )){
			return "On my way";
		} else {
			return "Shipped";
		}
	}
	
	public static final String DELETE = "Удалить";
	
	public static final String USER_KEY = "Пользователь";
	public static final String PWD_KEY = "Пароль";
	
	public static boolean isConnectedToInternet( Context context ){
		ConnectivityManager connectivityManager =
				(ConnectivityManager)context.getSystemService(
						Context.CONNECTIVITY_SERVICE
				);
		if(connectivityManager != null){
			NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
			if(info != null){
				for(int i = 0; i<info.length; i++){
					if(info[i].getState() == NetworkInfo.State.CONNECTED){
						return true;
					}
				}
			}
		}
		return false;
	}
	
}
