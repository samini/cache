/*
   Copyright 2010 Shahriyar Amini

   Licensed under the Apache License, Version 2.0 (the "License");
   you may not use this file except in compliance with the License.
   You may obtain a copy of the License at

       http://www.apache.org/licenses/LICENSE-2.0

   Unless required by applicable law or agreed to in writing, software
   distributed under the License is distributed on an "AS IS" BASIS,
   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
   See the License for the specific language governing permissions and
   limitations under the License.
 */

package edu.cmu.ece.cache.sample.yelp;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;

public class CustomItemizedOverlay extends ItemizedOverlay<OverlayItem> {
	
	private ArrayList<OverlayItem> mOverlays = new ArrayList<OverlayItem>();
	private Context mContext;

	public CustomItemizedOverlay(Drawable defaultMarker) {
		super(boundCenterBottom(defaultMarker));
		populate();
	}
	
	public CustomItemizedOverlay(Drawable defaultMarker, Context context) {
		  this(defaultMarker);
		  mContext = context;
	}
	
	public void addOverlay(OverlayItem overlay) {
		if (mOverlays == null) return;
		if (overlay == null) return;
	    mOverlays.add(overlay);
	    populate();
	}
	
	public void removeOverlay(OverlayItem overlay) {
		if (mOverlays == null) return;
		if (overlay == null) return;
		mOverlays.remove(overlay);
		populate();
	}
	
	public void clear() {
		if (mOverlays != null)
			mOverlays.clear();
		populate();
	}

	@Override
	protected OverlayItem createItem(int i) {
		if (mOverlays == null) return null;
		return mOverlays.get(i);
	}

	@Override
	public int size() {
		if (mOverlays == null) return 0;
		else
			return mOverlays.size();
	}
	
	@Override
	protected boolean onTap(int index) {
		
		if (mOverlays == null) return true;
		OverlayItem item = mOverlays.get(index);
		  
		if (mContext == null) return true;
		if (item == null) return true;
		
		AlertDialog.Builder dialog = new AlertDialog.Builder(mContext);
		dialog.setTitle(item.getTitle());
		dialog.setMessage(item.getSnippet());
		dialog.show();
	  
	    return true;
	}

}
