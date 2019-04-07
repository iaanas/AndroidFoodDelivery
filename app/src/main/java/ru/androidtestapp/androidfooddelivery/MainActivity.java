package ru.androidtestapp.androidfooddelivery;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Objects;

import io.paperdb.Paper;
import ru.androidtestapp.androidfooddelivery.Common.Common;
import ru.androidtestapp.androidfooddelivery.Model.User;

public class MainActivity extends AppCompatActivity {
	
	Button btnSignIn, btnSignUp;
	TextView txtSlogan;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_main );
		
		btnSignIn = findViewById( R.id.btnSignIn );
		btnSignUp = findViewById( R.id.btnSignUp );
		
		txtSlogan = findViewById( R.id.txtSlogan );
		Typeface face = Typeface.createFromAsset( getAssets(), "fonts/Nabila.ttf" );
		txtSlogan.setTypeface( face );
		
		//Init Paper
		Paper.init( this );
		
		btnSignIn.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				
				Intent intent = new Intent( MainActivity.this, SignIn.class );
				startActivity( intent );
			
			}
		} );
		
		btnSignUp.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				
				Intent intent = new Intent( MainActivity.this, SignUp.class );
				startActivity( intent );
			
			}
		} );
		
		String user = Paper.book().read( Common.USER_KEY );
		String pwd = Paper.book().read( Common.PWD_KEY );
		if(user != null && pwd != null){
			if(!user.isEmpty() && !pwd.isEmpty()){
				login(user, pwd);
			}
		}
	}
	
	private void login( final String phone , final String pwd ) {
		
		//Init FireBase
		final FirebaseDatabase database = FirebaseDatabase.getInstance();
		final DatabaseReference table_user = database.getReference("User");
		
		if(Common.isConnectedToInternet( getBaseContext() )){
			
			//Save user & password
			
			final ProgressDialog mDialog = new ProgressDialog( MainActivity.this );
			mDialog.setMessage( "Please waiting... " );
			mDialog.show();
			
			table_user.addValueEventListener( new ValueEventListener( ) {
				@Override
				public void onDataChange( @NonNull DataSnapshot dataSnapshot ) {
					//Check if user not exist in database
					if(dataSnapshot.child( phone ).exists()) {
						
						//Get User information
						mDialog.dismiss( );
						User user = dataSnapshot.child( phone ).getValue( User.class );
						user.setPhone( phone );
						
						if ( Objects.requireNonNull( user ).getPassword( ).equals( pwd ) ) {
							
							Intent homeIntent = new Intent( MainActivity.this, Home.class );
							Common.currentUser = user;
							startActivity( homeIntent );
							finish();
							
							
						} else {
							Toast.makeText( MainActivity.this , "Sign in not successfully :(" ,
									Toast.LENGTH_SHORT ).show( );
						}
					} else {
						mDialog.dismiss();
						Toast.makeText( MainActivity.this , "User not exist" ,
								Toast.LENGTH_SHORT ).show( );
					}
					
				}
				
				@Override
				public void onCancelled( @NonNull DatabaseError databaseError ) {
				
				}
			} );
		}
		else {
			Toast.makeText( MainActivity.this, "Please check your connection !!!", Toast.LENGTH_SHORT ).show();
		}
	}
}
