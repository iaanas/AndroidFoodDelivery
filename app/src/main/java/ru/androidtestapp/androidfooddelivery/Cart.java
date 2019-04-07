package ru.androidtestapp.androidfooddelivery;

import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import ru.androidtestapp.ViewHolder.CartAdapter;
import ru.androidtestapp.androidfooddelivery.Common.Common;
import ru.androidtestapp.androidfooddelivery.Database.Database;
import ru.androidtestapp.androidfooddelivery.Model.Order;
import ru.androidtestapp.androidfooddelivery.Model.Request;

public class Cart extends AppCompatActivity {
	
	RecyclerView recyclerView;
	RecyclerView.LayoutManager layoutManager;
	
	FirebaseDatabase database;
	DatabaseReference requests;
	
	TextView txtTotalPrice;
	Button btbPlace;
	
	List< Order > cart = new ArrayList <>(  );
	CartAdapter adapter;
	
	@Override
	protected void onCreate( Bundle savedInstanceState ) {
		super.onCreate( savedInstanceState );
		setContentView( R.layout.activity_cart );
		
		//FireBase
		database = FirebaseDatabase.getInstance();
		requests = database.getReference("Request");
		
		//Init
		recyclerView = (RecyclerView) findViewById( R.id.listCart );
		recyclerView.setHasFixedSize( true );
		layoutManager = new LinearLayoutManager( this );
		recyclerView.setLayoutManager( layoutManager );
		
		txtTotalPrice = (TextView) findViewById( R.id.total );
		btbPlace = (Button) findViewById( R.id.btnPlaceOrder );
		
		btbPlace.setOnClickListener( new View.OnClickListener( ) {
			@Override
			public void onClick( View v ) {
				
				//Create new Request
				if(cart.size() > 0){
					showAlertDialog();
				} else {
					Toast.makeText( Cart.this, "Your cart is empty!",
							Toast.LENGTH_SHORT).show();
				}
			}
		} );
		
		loadListFood();
		
	}
	
	private void showAlertDialog() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder( Cart.this );
		alertDialog.setTitle( "One more step!" );
		alertDialog.setMessage( "Enter your address: " );
		
		final EditText edtAddress = new EditText( Cart.this );
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT
		);
		edtAddress.setLayoutParams( lp );
		alertDialog.setView( edtAddress );
		alertDialog.setIcon( R.drawable.ic_shopping_cart_black_24dp );
		
		alertDialog.setPositiveButton( "YES" , new DialogInterface.OnClickListener( ) {
			@Override
			public void onClick( DialogInterface dialog , int which ) {
				Request request = new Request(
						Common.currentUser.getPhone(),
						Common.currentUser.getName(),
						edtAddress.getText().toString(),
						txtTotalPrice.getText().toString(),
						cart
				);
				
				//Submit to FireBase
				//We will using System.CurrentMilli to key
				requests.child( String.valueOf( System.currentTimeMillis() ) )
						.setValue( request );
				//Delete cart
				new Database( getBaseContext() ).cleanCart();
				Toast.makeText( Cart.this, "Thank you, Order Place",
						Toast.LENGTH_SHORT).show();
				finish();
			}
		} );
		alertDialog.setNegativeButton( "NO" , new DialogInterface.OnClickListener( ) {
			@Override
			public void onClick( DialogInterface dialog , int which ) {
				dialog.dismiss();
			}
		} );
		alertDialog.show();
	}
	
	private void loadListFood( ) {
		cart = new Database(this).getCarts();
		adapter = new CartAdapter( cart, this );
		adapter.notifyDataSetChanged();
		recyclerView.setAdapter( adapter );
		
		//Calculate total price
		int total = 0;
		for (Order order:cart) {
			total+=(Integer.parseInt( order.getPrice() ))*(Integer.parseInt( order.getQuantity() ));
			
			Locale locale = new Locale( "en", "US" );
			NumberFormat fmt = NumberFormat.getCurrencyInstance(locale);
			
			txtTotalPrice.setText( fmt.format( total ) );
		}
	}
	
	@Override
	public boolean onContextItemSelected( MenuItem item ) {
		if(item.getTitle().equals( Common.DELETE )){
			deleteCart(item.getOrder());
		}
		return true;
	}
	
	private void deleteCart( int position ) {
		
		cart.remove( position );
		new Database( this ).cleanCart();
		
		for(Order item:cart){
			new Database( this ).addToCart( item );
		}
		
		loadListFood();
	
	}
}
