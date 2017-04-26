package com.example.rza.shopping;

import android.content.ClipData;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public class ItemListActivity extends AppCompatActivity {
    static ArrayAdapter<String> listAdapter;
    ArrayAdapter<String> listPurchAdapter;
    ListView listView;
    EditText editTextTitle;
    FloatingActionButton fab2;
    ArrayList<String> items;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        editTextTitle = (EditText) findViewById(R.id.editTextTitle);
        listView = (ListView) findViewById(R.id.listView);
        items = new ArrayList<String>();
        Intent intent = getIntent();
        final String listaFK = intent.getStringExtra("listID");
        Log.i("listIDILA", listaFK);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //otvara itemAddActivity i prenosi kljuc liste
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(ItemListActivity.this, ItemAddActivity.class);
                i.putExtra("listID", listaFK);
                MainActivity.sql.execSQL("UPDATE List set list_name ='" + editTextTitle.getText().toString() + "' where list_id = " + Integer.parseInt(listaFK) + ";");
                startActivity(i);
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2); //otvara MainActivity
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FAB", "CLICKED");
                Intent i = new Intent(ItemListActivity.this, MainActivity.class);
                startActivity(i);
            }
        });

        try {
            Cursor c = MainActivity.sql.rawQuery("SELECT item_id, item_name, uom, quantity FROM Item where lista =" + listaFK + " order by purchased;", null);
            c.moveToFirst(); //sql upit sortiran po kupljenim itemima
            for (int i = 0; i < c.getCount(); i++) {
                int itemID = c.getColumnIndex("item_id");
                int itemNameIndex = c.getColumnIndex("item_name");
                int uomIndex = c.getColumnIndex("uom");
                int quantity = c.getColumnIndex("quantity");

                items.add(Integer.toString(c.getInt(itemID)) + " Naziv: " + c.getString(itemNameIndex) + " " + c.getString(quantity) + " " + c.getString(uomIndex));
                c.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            Cursor c = MainActivity.sql.rawQuery("SELECT list_name FROM List where list_id =" + listaFK + ";", null);
            c.moveToFirst();
            int listItemIndex = c.getColumnIndex("list_name");
            editTextTitle.setText(c.getString(listItemIndex));
        } catch (Exception e) {
            e.printStackTrace();
        }


        listAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_checked, items);
        listView.setAdapter(listAdapter);


        try {
            Cursor cursor = MainActivity.sql.rawQuery("SELECT count(item_id) as 'cnt' from Item where purchased = 1 and lista = " + listaFK, null);
            cursor.moveToFirst();
            int itemIDCount = cursor.getColumnIndex("cnt");
            String position = cursor.getString(itemIDCount);
            int countPurchased = Integer.parseInt(position);
            int countAllItems = listView.getCount();


            if (countAllItems == countPurchased) {
                MainActivity.sql.execSQL("UPDATE List set filled = 1 where list_id = " + listaFK);
            }
            Log.i("position", position);
            for (int i = countAllItems - countPurchased; i <= countAllItems; i++) {
                listView.setItemChecked(i, true); //pozelenjava kvacice u listView-u
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) {
                AlertDialog.Builder dialog = new AlertDialog.Builder(ItemListActivity.this);
                dialog.setTitle("Purchased or Delete item?");
                dialog.setPositiveButton("Purchased", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listContent = listView.getItemAtPosition(position).toString();
                        String listID[] = listContent.split(" ", 0);
                        Log.i("listID", listID[0]);
                        MainActivity.sql.execSQL("UPDATE Item set purchased = 1 where item_id = " + listID[0] + ";");
                        finish();
                        startActivity(getIntent());
                        Toast.makeText(getApplicationContext(), "Item Marked as purchased!", Toast.LENGTH_SHORT).show();

                    }
                })
                        .setNegativeButton("Delete", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                MainActivity.sql.execSQL("DELETE from Item where lista = " + listaFK);
                                listAdapter.remove(listAdapter.getItem(position));
                                listAdapter.notifyDataSetChanged();
                                Toast.makeText(ItemListActivity.this, "Item Deleted!", Toast.LENGTH_SHORT).show();
                            }
                        }).show();
                return true;
            }

        });




    }

}
