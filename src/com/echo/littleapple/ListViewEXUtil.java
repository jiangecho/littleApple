package com.echo.littleapple;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ListAdapter;
import android.widget.ListView;

public class ListViewEXUtil {
	public static void setListViewHeightBasedOnChild(ListView listView) {
		ListAdapter adapter = listView.getAdapter();
		if (adapter == null) {
			return;
		}

		int totalHeight = 0;
		int count = adapter.getCount();
		View listItemView;
		for (int i = 0; i < count; i++) {
			listItemView = adapter.getView(i, null, listView);
			listItemView.measure(0, 0);
			totalHeight += listItemView.getMeasuredHeight();
		}

		ViewGroup.LayoutParams params = listView.getLayoutParams();
		params.height = totalHeight + listView.getDividerHeight() * (count - 1);
		listView.setLayoutParams(params);
	}
}
