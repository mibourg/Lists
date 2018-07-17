package com.example.michel.lists;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class OpenListActivity extends AppCompatActivity {

    ArrayList<ItemList> itemLists = new ArrayList<>();

    ItemList openedItemList;

    ArrayList<CheckBox> checkBoxesForItems = new ArrayList<>();

    LinearLayout itemsHolder;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_open_list);

        openedItemList = (ItemList) getIntent().getSerializableExtra("openedItemList");

        itemsHolder = (LinearLayout) findViewById(R.id.ll_items_holder);

        setTitle(openedItemList.getName());

        List<?> genericItemLists = (List<?>) getIntent().getSerializableExtra("itemLists");
        for (Object object : genericItemLists) {
            if (object instanceof ItemList) {
                itemLists.add((ItemList) object);
            }
        }

        createCheckBoxesForItems();
        populateItemsHolder();
    }

    @Override
    public void onBackPressed() {
        Intent startMainActivityIntent = new Intent(this, MainActivity.class);
        startMainActivityIntent.putExtra("openedItemList", openedItemList);
        startActivity(startMainActivityIntent);
    }

    @Override
    protected void onPause() {
        updateListInArrayList();
        saveItemListsToFile();
        super.onPause();
    }

    private void updateListInArrayList() {
        int indexToRemove = 0;
        boolean foundMatch = false;
        for (ItemList itemList : itemLists) {
            if (itemList.getName().equals(openedItemList.getName())) {
                foundMatch = true;
                indexToRemove = itemLists.indexOf(itemList);
            }
        }
        if (foundMatch) {
            itemLists.remove(indexToRemove);
            itemLists.add(indexToRemove, openedItemList);
        }
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.delete_checked) {
            createAlertDialogToDeleteCheckedItems();
            return true;
        } else if (item.getItemId() == R.id.sort_by_checked) {
            sortItemsByChecked();
            return true;
        } else if (item.getItemId() == R.id.sort_by_unchecked) {
            sortItemsByUnchecked();
            return true;
        } else if (item.getItemId() == R.id.sort_A_Z) {
            sortItemsAlphabetically();
            return true;
        } else if (item.getItemId() == android.R.id.home) {
            Intent startMainActivityIntent = new Intent(this, MainActivity.class);
            startMainActivityIntent.putExtra("openedItemList", openedItemList);
            startActivity(startMainActivityIntent);
            return true;
        }
        return false;
    }

    private void createAlertDialogToDeleteCheckedItems() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.are_you_sure);
        alertDialogBuilder.setMessage(R.string.no_retrieving_items);
        alertDialogBuilder.setIcon(android.R.drawable.ic_dialog_alert);
        alertDialogBuilder.setPositiveButton(R.string.delete_checked, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteCheckedItems();
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

    private void deleteCheckedItems() {
        ArrayList<Item> itemsToDelete = new ArrayList<>();
        for (Item itemToCheck : openedItemList.getItems()) {
            if (itemToCheck.isCompleted()) {
                itemsToDelete.add(itemToCheck);
            }
        }
        for (Item itemToDelete : itemsToDelete) {
            openedItemList.removeItem(itemToDelete);
        }
        createCheckBoxesForItems();
        populateItemsHolder();
    }

    private void sortItemsByChecked() {
        Collections.sort(openedItemList.getItems(), new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if (!o1.isCompleted() && o2.isCompleted()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        createCheckBoxesForItems();
        populateItemsHolder();
    }

    private void sortItemsByUnchecked() {
        Collections.sort(openedItemList.getItems(), new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                if (o1.isCompleted() && !o2.isCompleted()) {
                    return 1;
                } else {
                    return -1;
                }
            }
        });
        createCheckBoxesForItems();
        populateItemsHolder();
    }

    private void sortItemsAlphabetically() {
        Collections.sort(openedItemList.getItems(), new Comparator<Item>() {
            @Override
            public int compare(Item o1, Item o2) {
                return o1.getDescription().compareTo(o2.getDescription());
            }
        });
        createCheckBoxesForItems();
        populateItemsHolder();
    }

    private void createCheckBoxesForItems() {
        checkBoxesForItems.clear();
        for (final Item item : openedItemList.getItems()) {
            final CheckBox checkBoxForItem = new CheckBox(this);
            checkBoxForItem.setText(item.getDescription());
            if (item.isCompleted()) {
                checkBoxForItem.setChecked(true);
            } else {
                checkBoxForItem.setChecked(false);
            }
            checkBoxForItem.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (item.isCompleted()) {
                        item.setCompleted(false);
                    } else {
                        item.setCompleted(true);
                    }
                }
            });
            checkBoxesForItems.add(checkBoxForItem);
        }
    }

    private void populateItemsHolder() {
        itemsHolder.removeAllViews();
        for (CheckBox checkBox : checkBoxesForItems) {
            itemsHolder.addView(checkBox);
        }
    }

    public void onClickAddItemButton(View view) {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setTitle(R.string.add_item);
        final EditText itemDescriptionEditText = new EditText(this);
        itemDescriptionEditText.setHint(R.string.item_description);
        alertDialogBuilder.setView(itemDescriptionEditText);
        alertDialogBuilder.setPositiveButton(R.string.add_item, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemDescription = itemDescriptionEditText.getText().toString();
                Item itemToAdd = new Item(itemDescription, false);
                openedItemList.addItem(itemToAdd);
                createCheckBoxesForItems();
                populateItemsHolder();
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
