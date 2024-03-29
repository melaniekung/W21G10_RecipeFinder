package com.example.recipefinder;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity implements OnIngredClick {

    TextView txtViewIngredsList;

    List<String> ingredientsList = new ArrayList<>(Arrays.asList());
    //For recipes.csv -------------------------------------------------------
    List<String> recipeTitlesList = new ArrayList<>(Arrays.asList());
    List<Integer> recipeImagesList = new ArrayList<>(Arrays.asList());
    List<String> recipeIngredientsList = new ArrayList<>(Arrays.asList());
    List<String> recipeDirectionsList = new ArrayList<>(Arrays.asList());
    List<String> cuisineList = new ArrayList<>(Arrays.asList());
    List<String> servingList = new ArrayList<>(Arrays.asList());
    List<String> prepTimeList = new ArrayList<>(Arrays.asList());
    List<String> cookTimeList = new ArrayList<>(Arrays.asList());
    List<String> totalTimeList = new ArrayList<>(Arrays.asList());
    //------------------------------------------------------------------------

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Insert Recipes' data to the recipes table in DB.
        addRecipesToDB();

        // call method to read ingredients csv into ingredientsList
        ingredientsList = ReadIngredients();

        // send ingredients list to grid view adapter to set checkboxes
        GridView gridViewIngredients = findViewById(R.id.gridViewIngredients);
        IngredientsAdapter ingredientsAdapter = new IngredientsAdapter(ingredientsList, this);
        gridViewIngredients.setAdapter(ingredientsAdapter);

        // instantiate views for ingredient search
        EditText editTxtSearchIngredient = findViewById(R.id.editTxtSearchIngredient);
        Button btnSearchIngredients = findViewById(R.id.btnSearchIngredients);
        txtViewIngredsList = findViewById(R.id.txtViewIngredsList);

        // setonclicklistener for search ingredients to check corresponding checkbox
        btnSearchIngredients.setOnClickListener((View view) -> {
            String searchIngred = editTxtSearchIngredient.getText().toString();
            if (ingredientsList.contains(searchIngred)) {
                boolean check = checkList(searchIngred, txtViewIngredsList.getText().toString());
                if (check) {
                    txtViewIngredsList.append(searchIngred + ", ");
                    editTxtSearchIngredient.setText(""); // clear edit text after saving
                } else {
                    Toast.makeText(this, searchIngred + " not found", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // instantiate button for search recipes
        Button btnSearchRecipe = findViewById(R.id.btnSearchRecipes);
        // create bundle to pass checked ingredients to recipe list page
        Bundle ingredientsBundle = new Bundle();

        // setonclicklistener for search recipe to collect checked ingredients, put into bundle and pass to next intent
        btnSearchRecipe.setOnClickListener((View view) -> {
            String ingredients = txtViewIngredsList.getText().toString();

            if (ingredients.length() != 0) {// create array to store keys for bundle
                ArrayList<String> keys = new ArrayList<>(Arrays.asList());
                String[] ingList = ingredients.split(", ");

                for (int i = 0; i < ingList.length; i++) {
                    keys.add(ingList[i]);
                }

                ingredientsBundle.putStringArrayList("KEYS", keys);
                ingredientsBundle.putString("FAVORITE_USER", null);
                Intent intent = new Intent(this, RecipeResultListActivity.class);
                intent.putExtras(ingredientsBundle);

                startActivity(intent);

                // reset checkboxes
                for (int i = 0; i < gridViewIngredients.getChildCount(); i++) {
                    CheckBox child = (CheckBox) gridViewIngredients.getChildAt(i);
                    if (child.isChecked()) {
                        child.setChecked(false);
                    }
                }

                // reset search ingredients textview
                txtViewIngredsList.setText("");
            } else {
                Toast.makeText(this, "Please select ingredients", Toast.LENGTH_SHORT).show();
            }
        });

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        Button btnFavorites = findViewById(R.id.btnFavorites);
        //This function is to go to my favorite screen
        btnFavorites.setOnClickListener((View view) -> {

            RecipeFinderDBManager dbManager;
            String loginUser = "";
            if (sharedPreferences.contains("LOGIN_SESSION")){
                loginUser = sharedPreferences.getString("LOGIN_SESSION","");
                if(loginUser !="") {
                    Log.d("[HKKO]", "_User("+loginUser+") select <favorite lists> button.");

                    Bundle favoriteBundle = new Bundle();
                    favoriteBundle.putString("FAVORITE_USER", loginUser);
                    Intent intent = new Intent(this, RecipeResultListActivity.class);
                    intent.putExtras(favoriteBundle);

                    startActivity(intent);

                }else{
                    Log.d("[HKKO]", "This user is unknown.");
                    Toast.makeText(MainActivity.this, "You need to login to use FAVOURITE service.", Toast.LENGTH_SHORT).show();
                }

            }
            else{
                Log.d("[HKKO]", "This user is unknown.");
                Toast.makeText(MainActivity.this, "You need to login to use FAVOURITE service.", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private boolean checkList(String searchIngred, String ingredsList) {
        String[] ingrds = ingredsList.split(", ");
        List<String> ingreds = Arrays.asList(ingrds);
        if (!ingreds.contains(searchIngred)) {
            return true;
        } else {
            return false;
        }
    }

    // method to read ingredients csv
    private List<String> ReadIngredients() {

        List<String> ingredList = new ArrayList<>(Arrays.asList());
        InputStream inputStream = getResources().openRawResource(R.raw.ingredients);
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try {
            String line;
            while ((line = reader.readLine()) != null) {
                ingredList.add(line);
            }
        } catch (IOException ex) {
            throw new RuntimeException("Error reading file " + ex);
        } finally {
            try {
                inputStream.close();
            } catch (IOException ex) {
                throw new RuntimeException("Error closing input stream " + ex);
            }
        }

        return ingredList;
    }

    //insert recipes from Recipes.csv fle to Recipes table in DB.
    private void addRecipesToDB(){
        boolean result;
        Log.d("[HKKO]", " addRecipesToDB in.");
        Log.d("[HKKO]", " addRecipesToDB Before calling dbInsert.");
        //call method to read Recipes.csv.
        readCSVRecipes();

        RecipeFinderDBManager dbManager = RecipeFinderDBManager.getInstance(this);

        Log.d("[HKKO]", " _MainActivity_add Recipes into Table.");
        for(int i = 1; i < recipeTitlesList.size() ; i++){
            //result = dbManager.addRecipe(recipeTitlesList.get(i), recipeImagesList.get(i), recipeIngredientsList.get(i), recipeDirectionsList.get(i),
                    result = dbManager.updateRecipe(recipeTitlesList.get(i), recipeImagesList.get(i), recipeIngredientsList.get(i), recipeDirectionsList.get(i),
                    cuisineList.get(i), servingList.get(i), prepTimeList.get(i), cookTimeList.get(i), totalTimeList.get(i));

            if(!result){
                Log.d("[HKKO]", " addRecipesToDB["+i+"] is failed.");
            }
            else{
                Log.d("[HKKO]", " addRecipesToDB["+i+"] is successful.");
            }
        }

        Log.d("[HKKO]", " addRecipesToDB after calling dbInsert.");


    }

    // method to read recipes csv
    private void readCSVRecipes(){

        //populate the list
        InputStream inputStream = getResources().openRawResource(R.raw.recipes); //students.csv file
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));

        try{
            int i=0;
            String csvLine;
            String imgDrawableName;
            int imgID;
            while((csvLine = reader.readLine()) != null){
                String[] fieldArray = csvLine.split(",");

                recipeTitlesList.add(fieldArray[0]);
                imgDrawableName = fieldArray[1].toLowerCase();
                imgID = getResources().getIdentifier(imgDrawableName, "drawable", getPackageName());
                recipeImagesList.add(imgID); //temporarily, we are using fixed number;
                recipeIngredientsList.add(fieldArray[2]);
                recipeDirectionsList.add(fieldArray[3]);
                cuisineList.add(fieldArray[4]);
                servingList.add(fieldArray[5]);
                prepTimeList.add(fieldArray[6]);
                cookTimeList.add(fieldArray[7]);
                totalTimeList.add(fieldArray[8]);

                i++;
            }
        }catch(IOException ex){
            Log.d("[HKKO]","__RecipeResultListActivity_ex1_"+ex);
            throw new RuntimeException("Error reading CSV file " + ex);
        }finally{
            try{
                inputStream.close();
            }catch(IOException ex){
                Log.d("[HKKO]","__RecipeResultListActivity_ex2_"+ex);
                throw new RuntimeException("Error closing input stream " + ex);
            }
        }
    }

    // override OnIngredClick Interface to add passed item from adapter's on click listener to ingredients textview
    @Override
    public void onClick(String text) {
        txtViewIngredsList = findViewById(R.id.txtViewIngredsList);
        boolean check = checkList(text, txtViewIngredsList.getText().toString());
        if (check){
            txtViewIngredsList.append(text + ", ");
        } else {
            String tempStr = txtViewIngredsList.getText().toString();
            txtViewIngredsList.setText(tempStr.replaceAll(text+", ", ""));
            Log.d("[RECIPEFINDER_LOG]", "unchecked "+ text);
            //Toast.makeText(this, text + " has already been added to ingredients list", Toast.LENGTH_SHORT).show();
        }
    }
}