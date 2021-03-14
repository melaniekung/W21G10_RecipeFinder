package com.example.recipefinder;

import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;

import java.util.List;

public class IngredientsAdapter extends BaseAdapter {
    // ingredients list passed from main activity
    List<String> ingredientsList;

    public IngredientsAdapter(List<String> ingredientsList) {
        this.ingredientsList = ingredientsList;
    }

    @Override
    public int getCount() {
        return ingredientsList.size();
    }

    @Override
    public Object getItem(int i) {
        return ingredientsList.get(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        // create check box for each ingredient
        CheckBox chkBoxIng = new CheckBox(viewGroup.getContext());
        chkBoxIng.setText(ingredientsList.get(i));
        view = chkBoxIng;

        return view;
    }
}
