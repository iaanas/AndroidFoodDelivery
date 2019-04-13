package ru.androidtestapp.Service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;

import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import ru.androidtestapp.androidfooddelivery.Common.Common;
import ru.androidtestapp.androidfooddelivery.Model.Request;
import ru.androidtestapp.androidfooddelivery.OrderStatus;
import ru.androidtestapp.androidfooddelivery.R;

public class ListenOrder extends Service implements ChildEventListener {
	FirebaseDatabase db;
	DatabaseReference request;
	
	public ListenOrder( ) {
	}
	
	@Override
	public IBinder onBind( Intent intent ) {
		return null;
	}
	
	@Override
	public void onCreate( ) {
		super.onCreate( );
		db = FirebaseDatabase.getInstance();
		request = db.getReference("Request");
	}
	
	@Override
	public int onStartCommand( Intent intent , int flags , int startId ) {
		request.addChildEventListener( this );
		return super.onStartCommand( intent , flags , startId );
	}
	
	@Override
	public void onChildAdded( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {
	
	}
	
	@Override
	public void onChildChanged( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {
		Request request = dataSnapshot.getValue(Request.class);
		showNotification(dataSnapshot.getKey(), request);
	}
	
	private void showNotification( String key , Request request ) {
		Intent intent = new Intent( getBaseContext(), OrderStatus.class );
		intent.putExtra( "userPhone", request.getPhone() );
		PendingIntent contentIntent = PendingIntent.getActivity(
				getBaseContext(), 0, intent, PendingIntent.FLAG_UPDATE_CURRENT );
		NotificationCompat.Builder builder = new NotificationCompat.Builder( getBaseContext() );
		builder.setAutoCancel( true )
				.setDefaults( Notification.DEFAULT_ALL )
				.setWhen( System.currentTimeMillis() )
				.setTicker("EDMTDev")
				.setContentInfo( "Информация о вашем заказе была обновлена" )
				.setContentText( "Статус заказа # "+ key + " был обновлен. Новый статус: " +
						Common.convertCodeToStatus( request.getStatus() ) )
				.setContentIntent( contentIntent )
				.setContentInfo( "Info" )
				.setSmallIcon( R.mipmap.ic_launcher );
		NotificationManager notificationManager = (NotificationManager) getBaseContext().getSystemService( Context.NOTIFICATION_SERVICE );
		notificationManager.notify( 1, builder.build() );
		
	}
	
	@Override
	public void onChildRemoved( @NonNull DataSnapshot dataSnapshot ) {
	
	}
	
	@Override
	public void onChildMoved( @NonNull DataSnapshot dataSnapshot , @Nullable String s ) {
	
	}
	
	@Override
	public void onCancelled( @NonNull DatabaseError databaseError ) {
	
	}
}
