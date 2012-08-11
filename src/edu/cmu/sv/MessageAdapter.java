package edu.cmu.sv;


import java.util.ArrayList;

import edu.cmu.sv.model.Message;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class MessageAdapter extends ArrayAdapter<Message> {
	private final Context context;
	private final ArrayList<Message> values;

	public MessageAdapter(Context context, ArrayList<Message> values) {
		super(context, R.layout.message_list, values);
		this.context = context;
		this.values = values;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View rowView = inflater.inflate(R.layout.message_item, parent, false);
		TextView messageText = (TextView) rowView.findViewById(R.id.each_message_text);
		TextView messageBy = (TextView) rowView.findViewById(R.id.each_message_by);
		TextView blank1 = (TextView) rowView.findViewById(R.id.blank1);
		TextView messageTime = (TextView) rowView.findViewById(R.id.each_message_date);
		
		Message m = values.get(position);
		messageText.setText(m.getText());
		messageBy.setText(m.getEmail());
		blank1.setText(" ");
		messageTime.setText(m.getDateTime());
		return rowView;
	}
} 