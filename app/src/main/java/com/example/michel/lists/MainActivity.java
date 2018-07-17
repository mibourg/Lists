package com.example.michel.lists;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    ArrayList<ItemList> itemLists = new ArrayList<>();

    LinearLayout listsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        listsHolder = (LinearLayout) findViewById(R.id.ll_lists_holder);

        loadItemListsFromFile();

        ItemList openedItemList = (ItemList) getIntent().getSerializableExtra("openedItemList");
        if (openedItemList != null) {
            Log.d("Got an ItemList back", "true");
            Log.d("Opened ItemList name", openedItemList.getName());
            updateItemListsWith(openedItemList);
        }

        populateListsHolder();
    }

    @Override
    public void onBackPressed() {
        moveTaskToBack(true);
    }

    private void updateItemListsWith(ItemList openedItemList) {
        int indexToRemove = 0;
        boolean foundMatch = false;
        for (ItemList itemList : itemLists) {
            Log.d("ItemList name", itemList.getName());
            if (itemList.getName().equals(openedItemList.getName())) {
                Log.d("Found match", "true");
                foundMatch = true;
                indexToRemove = itemLists.indexOf(itemList);
            }
        }
        if (foundMatch) {
            itemLists.remove(indexToRemove);
            itemLists.add(indexToRemove, openedItemList);
        }
    }

    private void loadItemListsFromFile() {
        FileInputStream fileInputStream;
        ObjectInputStream objectInputStream;
        try {
            fileInputStream = openFileInput("itemListsDatabase");
            objectInputStream = new ObjectInputStream(fileInputStream);
            List<?> genericListFromFile = (List<?>) objectInputStream.readObject();
            populateItemListsFrom(genericListFromFile);
        } catch (FileNotFoundException e) {
            File file = new File("itemListsDatabase");
        } catch (IOException e) {
            Log.e("IOException", "Error opening the file.");
        } catch (ClassNotFoundException e) {
            Log.e("ClassNotFoundException", "No lists found in database.");
        }
    }

    private void populateItemListsFrom(List<?> genericList) {
        for (Object object : genericList) {
            if (object instanceof ItemList) {
                itemLists.add((ItemList) object);
            }
        }
    }

    private void populateListsHolder() {
        listsHolder.removeAllViews();
        for (final ItemList itemList : itemLists) {
            LinearLayout linearLayout = new LinearLayout(this);
            getLayoutInflater().inflate(R.layout.open_and_delete_list_buttons, linearLayout);

            Button openListButton = (Button) linearLayout.findViewById(R.id.btn_open_list);
            openListButton.setText(itemList.getName());
            openListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent startOpenListActivityIntent = new Intent(MainActivity.this, OpenListActivity.class);
                    startOpenListActivityIntent.putExtra("itemLists", itemLists);
                    startOpenListActivityIntent.putExtra("openedItemList", itemList);
                    startActivity(startOpenListActivityIntent);
                }
            });

            Button deleteListButton = (Button) linearLayout.findViewById(R.id.btn_delete_list);
            deleteListButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(MainActivity.this);
                    alertDialogBuilder.setTitle(R.string.are_you_sure);
                    alertDialogBuilder.setMessage(R.string.no_retrieval);
                    alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
                    alertDialogBuilder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            itemLists.remove(itemList);
                            populateListsHolder();
                        }
                    });
                    alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();
                        }
                    });
                    alertDialogBuilder.create().show();
                }
            });
            listsHolder.addView(linearLayout);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveItemListsToFile();
    }

    private void saveItemListsToFile() {
        FileOutputStream fileOutputStream;
        ObjectOutputStream objectOutputStream;
        try {
            fileOutputStream = openFileOutput("itemListsDatabase", Context.MODE_PRIVATE);
            objectOutputStream = new ObjectOutputStream(fileOutputStream);
            objectOutputStream.writeObject(itemLists);
        } catch (FileNotFoundException e) {
            File file = new File("itemListsDatabase");
        } catch (IOException e) {
            Log.e("IOException", "Error opening the file.");
        }
    }

    public void onClickAddListButton(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.add_list);
        final EditText listNameEditText = new EditText(this);
        alertDialogBuilder.setView(listNameEditText);
        alertDialogBuilder.setPositiveButton(R.string.add_list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemListName = listNameEditText.getText().toString();
                ItemList itemListToAdd = new ItemList(itemListName, new ArrayList<Item>());
                itemLists.add(itemListToAdd);
                populateListsHolder();
            }
        });
        alertDialogBuilder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });
        alertDialogBuilder.create().show();
    }
}
