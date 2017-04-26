package com.example.rza.shopping;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class ItemAddActivity extends AppCompatActivity {
    Spinner s;
    private String array[];
    TextView textViewTitle;
    Intent i;
    Intent intent;
    EditText editItemName;
    EditText editQ;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_add2);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        textViewTitle = (TextView) findViewById(R.id.textViewTitle);
        editItemName = (EditText) findViewById(R.id.editItemName);
        editQ = (EditText) findViewById(R.id.editQuantity);
        array = new String[5];
        array[0] = "";
        array[4] = "g";
        array[2] = "L";
        array[3] = "m";
        array[1] = "unit";
        s = (Spinner) findViewById(R.id.spinner2);
        final ArrayAdapter adapter = new ArrayAdapter(getApplicationContext(), android.R.layout.simple_spinner_item, array);
        s.setAdapter(adapter);

        i = getIntent();
        String listName = i.getStringExtra("ListName");
        final String listID = i.getStringExtra("listID");
        final String newListItemID = i.getStringExtra("newListItemID");
        textViewTitle.setText(listName);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //dodaje novi item u bazu (listu)
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (newListItemID == null) {
                    Log.i("add btn", "clicked");
                    MainActivity.sql.execSQL("INSERT INTO Item (lista, item_name, uom, quantity) values (" + listID + ", '" + editItemName.getText() + "'," +
                            "'" + s.getSelectedItem().toString() + "'," + editQ.getText() + ");");
                    MainActivity.sql.execSQL("UPDATE List set filled = 0 where list_id = " + listID + ";");
                } else {
                    MainActivity.sql.execSQL("INSERT INTO Item (lista, item_name, uom, quantity) values (" + newListItemID + ", '" + editItemName.getText() + "'," +
                            "'" + s.getSelectedItem().toString() + "'," + editQ.getText() + ");");
                    MainActivity.sql.execSQL("UPDATE List set filled = 0 where list_id = " + listID + ";");
                }
                Toast.makeText(getApplicationContext(), editItemName.getText() + " created!", Toast.LENGTH_SHORT).show();

                editItemName.setText(" ");
                s.setAdapter(adapter);
                editQ.setText(" ");
                editItemName.requestFocus();
            }
        });


        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.floatingActionButton); //startuje main activity
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FAB", "CLICKED");
                Intent i = new Intent(ItemAddActivity.this, MainActivity.class);
                startActivity(i);
            }
        });
    }

}
