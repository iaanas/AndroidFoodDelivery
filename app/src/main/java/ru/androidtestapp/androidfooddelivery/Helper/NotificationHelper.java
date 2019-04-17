package ru.androidtestapp.androidfooddelivery.Helper;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.ContextWrapper;
import android.net.Uri;
import android.os.Build;

import ru.androidtestapp.androidfooddelivery.R;

public class NotificationHelper extends ContextWrapper {
	
	private static final String LYNCMED_CHANEL_ID = "ru.androidtestapp.androidfooddelivery.Lyncmed";
	private static final String LYNCMED_CHANEL_NAME = "Lyncmed Russia";
	
	private NotificationManager manager;
	
	public NotificationHelper( Context base ) {
		super( base );
		
		if( Build.VERSION.SDK_INT >= Build.VERSION_CODES.O ){
			createChannel();
		}
	}
	
	@TargetApi( Build.VERSION_CODES.O )
	private void createChannel( ) {
		NotificationChannel edmtChannel = new NotificationChannel( LYNCMED_CHANEL_ID,
				LYNCMED_CHANEL_NAME,
				NotificationManager.IMPORTANCE_DEFAULT);
		edmtChannel.enableLights( false );
		edmtChannel.enableVibration( true );
		edmtChannel.setLockscreenVisibility( Notification.VISIBILITY_PRIVATE );
		
		getManager().createNotificationChannel(edmtChannel);
	}
	
	public NotificationManager getManager( ) {
		if(manager == null){
			manager = (NotificationManager) getSystemService( Context.NOTIFICATION_SERVICE );
		}
		return manager;
	}
	
	@TargetApi( Build.VERSION_CODES.O )
	public android.app.Notification.Builder getEatITChannelNotification( String title, String body,
	                                                                     PendingIntent contentIntent,
	                                                                     Uri soundUri) {
		return new android.app.Notification.Builder( getApplicationContext(), LYNCMED_CHANEL_ID)
				.setContentIntent( contentIntent )
				.setContentTitle( title )
				.setContentText( body )
				.setSmallIcon( R.mipmap.ic_launcher )
				.setSound( soundUri )
				.setAutoCancel( false );
				
	}
}
