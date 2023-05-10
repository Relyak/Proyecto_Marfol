package adapters;


import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.tfg.marfol.R;

import java.util.ArrayList;
import java.util.List;

import entities.Persona;
import entities.Plato;

public class PersonaBdAdapter extends RecyclerView.Adapter<PersonaBdAdapter.PersonaBdAdapterResultHolder> {

    private List<Persona> resultsPersona = new ArrayList<>();

    private onItemClickListener mListener;

    @NonNull
    @Override
    public PersonaBdAdapterResultHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.row_persona, parent, false);

        return new PersonaBdAdapterResultHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PersonaBdAdapterResultHolder holder, int position) {

        //Insertamos para cada persona en el Recycler su nombre
        holder.tvPersonaRow.setText(resultsPersona.get(position).getNombre());

        //Insertamos para cada persona en el Recycler su imagen
        holder.ivPersonaRow.setImageURI(Uri.parse(resultsPersona.get(position).getUrlImage()));

        //Método onclick
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mListener != null) {
                    int position = holder.getAdapterPosition();
                    if (position != RecyclerView.NO_POSITION) {
                        mListener.onItemClick(position);
                    }
                }
            }
        });

    }

    @Override
    public int getItemCount() {
        return resultsPersona.size();
    }

    public void setResultsPersona(List<Persona> resultsPersona) {
        this.resultsPersona = resultsPersona;
        notifyDataSetChanged();

    }

    public void setmListener(onItemClickListener mListener) {
        this.mListener = mListener;
    }

    class PersonaBdAdapterResultHolder extends RecyclerView.ViewHolder {

        private TextView tvPersonaRow;
        private ImageView ivPersonaRow;

        public PersonaBdAdapterResultHolder(@NonNull View itemView) {
            super(itemView);

            tvPersonaRow = itemView.findViewById(R.id.tvPersonaRow);
            ivPersonaRow = itemView.findViewById(R.id.ivPersonaRow);

        }

    }

    public interface onItemClickListener{
        void onItemClick(int position);
    }

}