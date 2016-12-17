package com.example.gunluk.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.gunluk.R;
import com.example.gunluk.time.timeSet;

public class MySettings extends Fragment {

	@Override
	public View onCreateView(LayoutInflater inflater,
							 @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
		
		View v = inflater.inflate(R.layout.fragment_settings, container, false);

		Button btn=(Button) v.findViewById (R.id.alarm);
		btn.setOnClickListener (new View.OnClickListener ( ) {
			@Override
			public void onClick(View view) {
				Intent intent =new Intent(getContext (),timeSet.class);
				startActivity (intent);
			}
		});
		return v;
	}

}
