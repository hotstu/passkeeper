package com.somebody.passkeeper;

import java.util.ArrayList;
import java.util.HashMap;

import com.somebody.passkeeper.model.Host;
import com.somebody.passkeeper.model.User;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

public class MyExpandableListAdapter extends BaseExpandableListAdapter {
	private Context context;
	private ArrayList<Host> hosts;
	private HashMap<Integer, ArrayList<User>> users;

	public MyExpandableListAdapter(Context context, ArrayList<Host> hosts,
			HashMap<Integer, ArrayList<User>> users) {
		this.context = context;
		this.hosts = hosts;
		this.users = users;
	}

	@Override
	public int getGroupCount() {
		return hosts.size();
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return users.get(hosts.get(groupPosition).get_id()).size();
	}

	@Override
	public Host getGroup(int groupPosition) {
		return hosts.get(groupPosition);
	}

	@Override
	public User getChild(int groupPosition, int childPosition) {
		return users.get(hosts.get(groupPosition).get_id()).get(childPosition);
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = getGroup(groupPosition).getHostname();
		if (convertView == null) {
			LayoutInflater infalInflater = LayoutInflater.from(context);
			convertView = infalInflater.inflate(R.layout.list_group, null);
		}

		TextView lblListHeader = (TextView) convertView
				.findViewById(R.id.lblListHeader);
		lblListHeader.setTypeface(null, Typeface.BOLD);
		lblListHeader.setText(headerTitle);

		return convertView;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {
		final String childText = getChild(groupPosition, childPosition).getUsername();
		 
        if (convertView == null) {
            LayoutInflater infalInflater = LayoutInflater.from(context);
            convertView = infalInflater.inflate(R.layout.list_item, null);
        }
 
        TextView txtListChild = (TextView) convertView
                .findViewById(R.id.lblListItem);
 
        txtListChild.setText(childText);
        return convertView;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}