package ru.androidtestapp.androidfooddelivery.Remote;


import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import ru.androidtestapp.androidfooddelivery.Model.MyResponse;
import ru.androidtestapp.androidfooddelivery.Model.Sender;

public interface APIService {
	@Headers(
			{
					"Content-Type:application/json",
					"Authorization:key=AAAAA0_Z8gg:APA91bGmHz6DgBqMMH_aEwmTYeqvjQ219avT5-c2jMFtDPo6V3OVjYq-jgZdYWM8p6WbHSzRr27LgElt_-32ZipplBv3KJBmM_rexo-IBTciH0MloLOoYNB-pnmeZtIYCrV8d-L2kWHL"
			}
	)
	
	@POST("fcm/send")
	Call< MyResponse > sendNotification( @Body Sender body );
}
