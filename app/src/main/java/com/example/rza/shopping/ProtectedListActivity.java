package com.example.rza.shopping;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class ProtectedListActivity extends AppCompatActivity {
    ArrayAdapter<String> adapter;
    ArrayList<String> list;
    ListView listView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_protected_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView);
        list = new ArrayList<String>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //raisuje dialog za dodavanje nove liste
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder newListDialog = new AlertDialog.Builder(ProtectedListActivity.this);
                newListDialog.setTitle("Create New Shopping List");
                newListDialog.setMessage("New List Name: ");

                final EditText input = new EditText(ProtectedListActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                newListDialog.setView(input);
                newListDialog.setPositiveButton("Create New List",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //dialog za dodavanje nove liste
                                list.add(input.getText().toString());
                                MainActivity.sql.execSQL("INSERT INTO List (list_name, pprotected) values ('" + input.getText().toString() + "', 1);");
                                Toast.makeText(getApplicationContext(), input.getText().toString() + " created... (Locked)", Toast.LENGTH_LONG).show();
                                Intent i = new Intent(ProtectedListActivity.this, ItemAddActivity.class);
                                Cursor cursor = MainActivity.sql.rawQuery("select max(list_id) as 'maxid' from List;", null);
                                cursor.moveToFirst();
                                int maxID = cursor.getColumnIndex("maxid");
                                Log.i("maxid", cursor.getString(maxID));

                                i.putExtra("newListItemID", cursor.getString(maxID));
                                i.putExtra("ListName", input.getText().toString());
                                startActivity(i);
                            }
                        });

                newListDialog.setNegativeButton("Changed my Mind",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = newListDialog.create();
                alert.show();
            }
        });

        try {
            Cursor c = MainActivity.sql.rawQuery("SELECT * FROM List where pprotected = 1 order by filled;", null);
            int listIdIndex = c.getColumnIndex("list_id");
            int listName = c.getColumnIndex("list_name");
            c.moveToFirst();
            for (int i = 0; i < c.getCount(); i++) {
                list.add("ID: " + Integer.toString(c.getInt(listIdIndex)) + " " + "Naziv: " + c.getString(listName)); //dodaje u list podatke iz baze
                c.moveToNext();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        adapter = new ArrayAdapter<String>(ProtectedListActivity.this, android.R.layout.simple_list_item_checked, list);
        listView.setAdapter(adapter);

        try {
            Cursor cursor = MainActivity.sql.rawQuery("SELECT count(list_id) as 'cnt' from List where filled = 1 and pprotected = 1", null);
            cursor.moveToFirst();
            int countPurchased = cursor.getColumnIndex("cnt");
            String cntPurchased = cursor.getString(countPurchased);
            int countFilled = Integer.parseInt(cntPurchased);
            int countAllFilled = listView.getCount();

            for (int i = countAllFilled - countFilled; i <= countAllFilled; i++) {
                listView.setItemChecked(i, true);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) { //otvara listu sa njenim itemima
                String listContent = listView.getItemAtPosition(position).toString();
                String listID[] = listContent.split(" ", 0);
                Log.i("listID", listID[1]);
                Intent i = new Intent(ProtectedListActivity.this, ItemListActivity.class);
                i.putExtra("listID", listID[1]);
                startActivity(i);
            }
        });


        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) { //otvara dialoga za brisanje liste
                AlertDialog.Builder dialog = new AlertDialog.Builder(ProtectedListActivity.this);
                dialog.setMessage("Delete List?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listContent = listView.getItemAtPosition(position).toString();
                        String listID[] = listContent.split(" ", 0);
                        Toast.makeText(getApplicationContext(), "List with ID: " + listID[1] + " deleted!", Toast.LENGTH_SHORT).show();
                        MainActivity.sql.execSQL("DELETE FROM List where list_id = " + listID[1]);
                        adapter.remove(adapter.getItem(position));
                        adapter.notifyDataSetChanged();
                    }
                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                dialog.show();

                return true;
            }
        });

        FloatingActionButton fab2 = (FloatingActionButton) findViewById(R.id.fab2); //otvara MainActivity
        fab2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.i("FAB", "CLICKED");
                Intent i = new Intent(ProtectedListActivity.this, MainActivity.class);
                startActivity(i);
            }
        });


    }

}
