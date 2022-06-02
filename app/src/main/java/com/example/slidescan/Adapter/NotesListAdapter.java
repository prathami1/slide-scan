package com.example.slidescan.Adapter;

import android.content.Context;
import android.text.Layout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.slidescan.Models.Notes;
import com.example.slidescan.NotesClickListener;
import com.example.slidescan.R;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class NotesListAdapter extends RecyclerView.Adapter<NotesViewHolder>
{
    Context context;
    List<Notes> list;
    NotesClickListener listener;

    public NotesListAdapter(Context context, List<Notes> list, NotesClickListener listener)
    {
        this.context = context;
        this.list = list;
        this.listener = listener;
    }

    @NonNull
    @Override
    public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    { return new NotesViewHolder(LayoutInflater.from(context).inflate(R.layout.notes_view, parent, false)); }

    @Override
    public void onBindViewHolder(@NonNull NotesViewHolder holder, int position)
    {
        holder.title.setText(list.get(position).getTitle());
        holder.title.setSelected(true);

        holder.note.setText(list.get(position).getNotes());

        holder.date.setText(list.get(position).getDate());
        holder.date.setSelected(true);

        if(list.get(position).getPinned())
            holder.pin.setImageResource(R.drawable.pin);
        else
            holder.pin.setImageResource(0);

        int colorCode = getRandomColor();
        holder.container.setCardBackgroundColor(holder.itemView.getResources().getColor(colorCode, null));

        holder.container.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                listener.onClick(list.get(holder.getAdapterPosition()));
            }
        });

        holder.container.setOnLongClickListener(new View.OnLongClickListener()
        {
            @Override
            public boolean onLongClick(View view)
            {
                listener.onLongClick(list.get(holder.getAdapterPosition()), holder.container);
                return true;
            }
        });
    }

    private int getRandomColor()
    {
        List<Integer> colorCode = new ArrayList<>();

        colorCode.add(R.color.color1);
        colorCode.add(R.color.color2);
        colorCode.add(R.color.color3);
        colorCode.add(R.color.color4);
        colorCode.add(R.color.color5);

        Random random = new Random();
        int randomColor = random.nextInt(colorCode.size());
        return colorCode.get(randomColor);
    }


    @Override
    public int getItemCount()
    { return list.size(); }

    public void filterList(List<Notes> filteredList)
    {
        list = filteredList;
        notifyDataSetChanged();
    }
}

class NotesViewHolder extends RecyclerView.ViewHolder
{
    CardView container;
    TextView title, note, date;
    ImageView pin;
    public NotesViewHolder(@NonNull View itemView)
    {
        super(itemView);
        container = itemView.findViewById(R.id.id_notesContainer);
        title = itemView.findViewById(R.id.id_title);
        note = itemView.findViewById(R.id.id_notes);
        date = itemView.findViewById(R.id.id_date);
        pin = itemView.findViewById(R.id.id_pin);
    }
}
