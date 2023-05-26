package mainActivity.detalle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.tfg.marfol.R;

import java.util.ArrayList;

import adapters.CrudPlatosAdapter;
import adapters.PersonaAdapter;
import adapters.PlatoRecordarAdapter;
import adapters.PlatoRecordarAdapterBd;
import entities.Persona;
import entities.Plato;

public class RecordarPlatoActivity extends AppCompatActivity implements PlatoRecordarAdapter.onItemClickListener, PlatoRecordarAdapterBd.onItemClickListenerBd {
    private TextView tvTitleRecordar, tvAnterRecordar;
    private RecyclerView rvPlatoRecordar, rvPlatoRecordarBd;
    private Plato platoDefecto = new Plato("", "", 0, 0, "android.resource://com.tfg.marfol/" + R.drawable.add_icon, false, new ArrayList<>());
    private Intent intentObtener;
    private ArrayList<Plato> platos;
    private Plato platoBd;
    private ArrayList<Plato> listaPlatosBd;
    private ArrayList<Plato> listaPlatos;
    private ArrayList<Persona> nombreCompartir;
    private FirebaseFirestore db;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private int personaCode;
    private PlatoRecordarAdapter recordarAdapter;
    private PlatoRecordarAdapterBd recordarAdapterBd;
    private String email,nombre,imagen,varRestaurante="";
    private double precio;
    private CollectionReference platosRef;
    private Query consulta;
    private boolean enviarRestaurante;
    private String nombreRestaurante;
    private Intent intent;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recordar_plato);

        intent = getIntent();
        enviarRestaurante = intent.getBooleanExtra("enviarRestaurante",false);
        if(intent.getBooleanExtra("enviarRestaurante",enviarRestaurante)){
            nombreRestaurante = intent.getStringExtra("nombreRestaurante");
            Toast.makeText(this, nombreRestaurante, Toast.LENGTH_SHORT).show();
        }else {
            nombreRestaurante= "";
        }


        //Recibe la lista de platos y filtra los repetidos
        recibirDatos();

        //Método que asigna IDs a los elementos
        asignarId();

        //Método que asigna los efectos a los elementos
        asignarEfectos();

        //Método que muestra el contenido del adaptader
        mostrarAdapter();

        //Muestra adapter bd
        mostrarAdapterBd();

    }

    private void mostrarAdapterBd() {

        rvPlatoRecordarBd.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        recordarAdapterBd = new PlatoRecordarAdapterBd();
        rvPlatoRecordarBd.setAdapter(recordarAdapterBd);
        recordarAdapterBd.setmListenerBd(this);
        if (currentUser != null) {
            cargarDatosBd();
        }
    }

    private void cargarDatosBd() {
        if (currentUser != null) {
            listaPlatosBd = new ArrayList<>();
            email = currentUser.getEmail(); // Utiliza el email como ID único del usuario
            // Obtén la colección "personas" en Firestore
            platosRef = db.collection("platos");
            // Realiza la consulta para obtener todas las personas del usuario actual
            consulta = platosRef.whereEqualTo("usuario", email)
                    .whereEqualTo("restaurante",nombreRestaurante);
            consulta.get().addOnCompleteListener(task -> {
                if (task.isSuccessful()) {
                    // Recorrer los documentos obtenidos y agregar los datos al ArrayList
                    for (DocumentSnapshot document : task.getResult()) {
                        nombre = document.getString("nombre");
                        imagen = document.getString("imagen");
                        precio = document.getDouble("precio");
                        platoBd = new Plato(imagen,nombre,precio);
                        listaPlatosBd.add(platoBd);
                    }
                    // Notificar al adapter que los datos han cambiado
                    recordarAdapterBd.setResultsPlatoBd(listaPlatosBd);
                }
            });
        }
    }

    public void recibirDatos() {
        listaPlatos = new ArrayList<>();
        intentObtener = getIntent();
        platos = (ArrayList<Plato>) intentObtener.getSerializableExtra("arrayListPlatos");
        //Recibo desde Editar o Añadir
        nombreCompartir = (ArrayList<Persona>) intentObtener.getSerializableExtra("arrayListComenComp");
        personaCode = intentObtener.getIntExtra("comensalCode", 0);

        //Obtenemos la lista de todos los platos
        for (Persona persona : nombreCompartir) {
            for (Plato plato : persona.getPlatos()) {
                //Comprobamos que el plato ya existe en la lista
                if (!plato.getNombre().equalsIgnoreCase("")) {
                    listaPlatos.add(plato);
                }
            }
        }
        //Añadimos el plato por defecto (botón)
        listaPlatos.add(0, platoDefecto);

    }

    public void mostrarAdapter() {
        //Prepara el Adapter para su uso
        rvPlatoRecordar.setLayoutManager(new GridLayoutManager(this, 2));
        recordarAdapter = new PlatoRecordarAdapter();
        rvPlatoRecordar.setAdapter(recordarAdapter);
        recordarAdapter.setmListener(this);

        //Insertamos en el adapter la lista modificada
        recordarAdapter.setResultsPlato(listaPlatos);
    }

    public void asignarEfectos() {
        //Asigna el degradado de colores a los textos
        int[] colors = {getResources().getColor(R.color.redBorder),
                getResources().getColor(R.color.redTitle)
        };

        float[] positions = {0f, 0.2f};

        LinearGradient gradient = new LinearGradient(0, 0, 40,
                tvTitleRecordar.getPaint().getTextSize(),
                colors,
                positions,
                Shader.TileMode.REPEAT);

        tvTitleRecordar.getPaint().setShader(gradient);

        // Asigna sombreado al texto
        float shadowRadius = 10f;
        float shadowDx = 0f;
        float shadowDy = 5f;
        int shadowColor = Color.BLACK;

        tvTitleRecordar.getPaint().setShadowLayer(shadowRadius, shadowDx, shadowDy, shadowColor);

    }

    public void asignarId() {
        tvTitleRecordar = findViewById(R.id.tvTitleRecordar);
        rvPlatoRecordar = findViewById(R.id.rvPlatoRecordar);
        rvPlatoRecordarBd = findViewById(R.id.rvPlatoRecordarBd);
        tvAnterRecordar = findViewById(R.id.tvAnteriormenteRecordar);
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
        currentUser = mAuth.getCurrentUser();
    }

    @Override
    public void onItemClick(int position) {
        if (position < 1) {
            //Se va a añadir un plato nuevo accedemos a su actividad
            Intent intentPlato = new Intent();
            intentPlato.putExtra("nuevoPlato", true);
            setResult(Activity.RESULT_OK, intentPlato);
            finish();
        } else {
            //Pasamos el plato a no compartido
            listaPlatos.get(position).setCompartido(false);
            Intent intentPlato = new Intent();

            intentPlato.putExtra("recordarNuevo", listaPlatos.get(position));
            setResult(Activity.RESULT_OK, intentPlato);
            finish();
        }
    }

    @Override
    public void onItemClickBd(int position) {

    }
}