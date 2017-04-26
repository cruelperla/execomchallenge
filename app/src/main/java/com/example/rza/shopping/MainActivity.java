package com.example.rza.shopping;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.support.annotation.IntegerRes;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {
    ListView listView;
    ArrayList<String> list;
    static ArrayAdapter<String> adapter;
    static SQLiteDatabase sql;


    @Override
    protected void onCreate(Bundle savedInstanceState) {


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        listView = (ListView) findViewById(R.id.listView);
        list = new ArrayList<String>();

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab); //raisuje dialog za dodavanje nove liste
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder newListDialog = new AlertDialog.Builder(MainActivity.this);
                newListDialog.setTitle("Create New Shopping List");
                newListDialog.setMessage("New List Name: ");

                final EditText input = new EditText(MainActivity.this);
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                newListDialog.setView(input);
                newListDialog.setPositiveButton("Create New List",
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) { //dialog za dodavanje nove liste
                                AlertDialog.Builder dialogPass = new AlertDialog.Builder(MainActivity.this);
                                dialogPass.setTitle("Protect with password?");
                                dialogPass.setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        list.add(input.getText().toString());
                                        sql.execSQL("INSERT INTO List (list_name) values ('" + input.getText().toString() + "');");
                                        Toast.makeText(getApplicationContext(), input.getText().toString() + " created...", Toast.LENGTH_LONG).show();
                                        Intent i = new Intent(MainActivity.this, ItemAddActivity.class);
                                        Cursor cursor = sql.rawQuery("select max(list_id) as 'maxid' from List;", null);
                                        cursor.moveToFirst();
                                        int maxID = cursor.getColumnIndex("maxid");
                                        Log.i("maxid", cursor.getString(maxID));

                                        i.putExtra("newListItemID", cursor.getString(maxID));
                                        i.putExtra("ListName", input.getText().toString());
                                        startActivity(i);
                                    }
                                })
                                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which) {
                                                list.add(input.getText().toString());
                                                sql.execSQL("INSERT INTO List (list_name, pprotected) values ('" + input.getText().toString() + "', 1);");
                                                Toast.makeText(getApplicationContext(), input.getText().toString() + " created... (Locked)", Toast.LENGTH_LONG).show();
                                                Intent i = new Intent(MainActivity.this, ItemAddActivity.class);
                                                Cursor cursor = sql.rawQuery("select max(list_id) as 'maxid' from List;", null);
                                                cursor.moveToFirst();
                                                int maxID = cursor.getColumnIndex("maxid");
                                                Log.i("maxid", cursor.getString(maxID));

                                                i.putExtra("newListItemID", cursor.getString(maxID));
                                                i.putExtra("ListName", input.getText().toString());
                                                startActivity(i);
                                            }
                                        }).show();


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
        sql = this.openOrCreateDatabase("ShoppingList", MODE_PRIVATE, null); //kreira ili otvara bazu ukoliko postoji
        //sql.execSQL("DROP TABLE List"); //brise tabelu List
        //sql.execSQL("DROP TABLE Item"); //Brise tabelu Item
        sql.execSQL("CREATE TABLE IF NOT EXISTS List (list_id INTEGER PRIMARY KEY AUTOINCREMENT, list_name varchar(50), filled INTEGER default 0, pProtected INTEGER default 0)");
        //kreiranje tabele List

        sql.execSQL("CREATE TABLE IF NOT EXISTS Item (item_id INTEGER PRIMARY KEY, lista INTEGER REFERENCES List(list_id) ON DELETE CASCADE," +
                " item_name varchar(50), uom varchar(10), quantity varchar(10), purchased INTEGER DEFAULT 0); ");
        //kreiranje tabele Item

        try {
            Cursor c = sql.rawQuery("SELECT * FROM List where pprotected = 0 order by filled;", null);
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


        adapter = new ArrayAdapter<String>(this, android.R.layout.simple_list_item_checked, list); //definicija adaptera
        listView.setAdapter(adapter);


        try {
            Cursor cursor = sql.rawQuery("SELECT count(list_id) as 'cnt' from List where filled = 1 and pprotected = 0", null);
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
                Intent i = new Intent(MainActivity.this, ItemListActivity.class);
                i.putExtra("listID", listID[1]);
                startActivity(i);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, final int position, long id) { //otvara dialoga za brisanje liste
                AlertDialog.Builder dialog = new AlertDialog.Builder(MainActivity.this);
                dialog.setMessage("Delete List?").setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String listContent = listView.getItemAtPosition(position).toString();
                        String listID[] = listContent.split(" ", 0);
                        Toast.makeText(getApplicationContext(), "List with ID: " + listID[1] + " deleted!", Toast.LENGTH_SHORT).show();
                        sql.execSQL("DELETE FROM List where list_id = " + listID[1]);
                        adapter.remove(adapter.getItem(position));
                        adapter.notifyDataSetChanged();
                    }
                })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        })
                .setNeutralButton("Delete All", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        sql.execSQL("DROP TABLE List");
                        sql.execSQL("DROP TABLE Item");
                        adapter.clear();
                        adapter.notifyDataSetChanged();
                        sql.execSQL("CREATE TABLE IF NOT EXISTS List (list_id INTEGER PRIMARY KEY AUTOINCREMENT, list_name varchar(50), filled INTEGER default 0, pProtected INTEGER default 0)");
                        //kreiranje tabele List

                        sql.execSQL("CREATE TABLE IF NOT EXISTS Item (item_id INTEGER PRIMARY KEY, lista INTEGER REFERENCES List(list_id) ON DELETE CASCADE," +
                                " item_name varchar(50), uom varchar(10), quantity varchar(10), purchased INTEGER DEFAULT 0); ");
                        //kreiranje tabele Item
                        Toast.makeText(MainActivity.this, "Everything has been deleted", Toast.LENGTH_SHORT).show();

                    }
                });
                dialog.show();

                return true;
            }
        });


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        AlertDialog.Builder dialogPass = new AlertDialog.Builder(MainActivity.this);
        final EditText inputPass = new EditText(MainActivity.this);
        inputPass.setTransformationMethod(PasswordTransformationMethod.getInstance());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);
        inputPass.setLayoutParams(lp);
        dialogPass.setView(inputPass);
        dialogPass.setIcon(R.drawable.katanac);
        final String pass = "execomchallenge";
        dialogPass.setTitle("This is Password Protected")
                .setMessage("Enter the pass here")
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (inputPass.getText().toString().equals(pass)) {
                            Intent i = new Intent(MainActivity.this, ProtectedListActivity.class);
                            startActivity(i);
                        } else {
                            Toast.makeText(MainActivity.this, "Wrong Password, Sorry!", Toast.LENGTH_SHORT).show();
                        }

                    }
                }).show();

        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


}
