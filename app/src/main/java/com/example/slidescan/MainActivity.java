package com.example.slidescan;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.example.slidescan.Adapter.NotesListAdapter;
import com.example.slidescan.Database.RoomDatabase;
import com.example.slidescan.Models.Notes;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class MainActivity extends AppCompatActivity implements PopupMenu.OnMenuItemClickListener
{
    RecyclerView recyclerView;
    NotesListAdapter notesListAdapter;
    List<Notes> notes = new ArrayList<>();
    RoomDatabase database;
    FloatingActionButton fab;
    SearchView searchBar;
    Notes selectedNote;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        getSupportActionBar().hide();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.id_recyclerView);
        fab = findViewById(R.id.id_FAB);
        searchBar = findViewById(R.id.id_searchbar);

        database = RoomDatabase.getInstance(this);
        notes = database.mainDataAccessObject().getNotesAll();

        updateRecycler(notes);

        fab.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intent = new Intent(MainActivity.this, MakeActivity.class);
                startActivityForResult(intent, 100);
            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener()
        {
            @Override
            public boolean onQueryTextSubmit(String query)
            { return false; }

            @Override
            public boolean onQueryTextChange(String newText)
            {
                filter(newText);
                return true;
            }
        });
    }

    private void filter(String newText)
    {
        List<Notes> filteredList = new ArrayList<>();
        for(Notes sNote : notes)
            if(sNote.getTitle().toLowerCase().contains(newText.toLowerCase()) || sNote.getNotes().toLowerCase().contains(newText.toLowerCase()))
                filteredList.add(sNote);
        notesListAdapter.filterList(filteredList);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == 100)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Notes newNote = (Notes) data.getSerializableExtra("note");
                database.mainDataAccessObject().insert(newNote);
                notes.clear();
                notes.addAll(database.mainDataAccessObject().getNotesAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
        else if (requestCode == 200)
        {
            if(resultCode == Activity.RESULT_OK)
            {
                Notes newNote = (Notes) data.getSerializableExtra("note");
                database.mainDataAccessObject().update(newNote.getId(), newNote.getTitle(), newNote.getNotes());
                notes.clear();
                notes.addAll(database.mainDataAccessObject().getNotesAll());
                notesListAdapter.notifyDataSetChanged();
            }
        }
    }

    private void updateRecycler(List<Notes> notes)
    {
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new StaggeredGridLayoutManager(2, LinearLayoutManager.VERTICAL));
        notesListAdapter = new NotesListAdapter(MainActivity.this, notes, notesClickListener);
        recyclerView.setAdapter(notesListAdapter);
    }

    private final NotesClickListener notesClickListener = new NotesClickListener()
    {
        @Override
        public void onClick(Notes notes)
        {
            Intent intent = new Intent(MainActivity.this, MakeActivity.class);
            intent.putExtra("oldNote", notes);
            startActivityForResult(intent, 200);
        }

        @Override
        public void onLongClick(Notes notes, CardView cardView)
        {
            selectedNote = new Notes();
            selectedNote = notes;
            displayOptions(cardView);
        }
    };

    private void displayOptions(CardView cardView)
    {
        PopupMenu popupMenu = new PopupMenu(this, cardView);
        popupMenu.setOnMenuItemClickListener(this);
        popupMenu.inflate(R.menu.longpress_menu);
        popupMenu.show();
    }


    @SuppressLint({"NonConstantResourceId", "NotifyDataSetChanged"})
    @Override
    public boolean onMenuItemClick(MenuItem item)
    {
        switch(item.getItemId())
        {
            case R.id.id_toggle_pin:
                if(selectedNote.getPinned())
                {
                    database.mainDataAccessObject().pin(selectedNote.getId(), false);
                    Toast.makeText(MainActivity.this, "Unpinned!", Toast.LENGTH_SHORT).show();
                }
                else
                {
                    database.mainDataAccessObject().pin(selectedNote.getId(), true);
                    Toast.makeText(MainActivity.this, "Pinned!", Toast.LENGTH_SHORT).show();
                }
                notes.clear();
                notes.addAll(database.mainDataAccessObject().getNotesAll());
                notesListAdapter.notifyDataSetChanged();
                return true;
            case R.id.id_toggle_delete:
                database.mainDataAccessObject().delete(selectedNote);
                notes.remove(selectedNote);
                notesListAdapter.notifyDataSetChanged();
                Toast.makeText(MainActivity.this, "Deleted!", Toast.LENGTH_SHORT).show();
                return true;
            default:
                return false;
        }
    }
}