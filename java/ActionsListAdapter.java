package com.example.expensetracker;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class ActionsListAdapter extends ArrayAdapter<Action> {

    Context context;
    ArrayList<Action> actions;


    public ActionsListAdapter(@NonNull Context context, int resource, int textViewResourceId, @NonNull List<Action> objects) {
        super(context, resource, textViewResourceId, objects);

        this.context = context;
        this.actions = (ArrayList<Action>) objects;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Action curAction = this.actions.get(position); // get the current action to display

        // inflate the layout
        LayoutInflater inflater = ((Activity)this.context).getLayoutInflater();
        View container = inflater.inflate(R.layout.actions_list_cell, parent, false);

        // get the widgets
        ImageView ivDesc = container.findViewById(R.id.ivDesc);
        TextView tvDate = container.findViewById(R.id.tvDate);
        TextView tvDesc = container.findViewById(R.id.tvDesc);
        TextView tvSum = container.findViewById(R.id.tvSum);
        TextView tvCategory = container.findViewById(R.id.tvCategory);

        // show the data
        ivDesc.setImageBitmap(ImageUtils.stringToBitmap(curAction.getImage()));
        tvDesc.setText(curAction.getDesc());
        tvDate.setText(curAction.getDate().toLocalDate().toString());
        tvSum.setText(curAction.getSum() + "");
        tvCategory.setText(curAction.getCategory());

        container.setBackgroundColor(curAction.getColor(this.context));
//        // tvSum.setTextColor(curAction.getColor());
//
//        // determine cell's background color - green for an income, red for an outcome
//        if (curAction instanceof Income){
//            container.setBackgroundColor(this.context.getResources().get(R.color.incomeColor, null));
//        } else {
//            container.setBackgroundColor(this.context.getResources().getColor(R.color.outcomeColor, null));
//        }

        return container;
    }
}
