package mainActivity.menu.crudBd.detalle;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.tfg.marfol.R;

import java.util.ArrayList;

import entities.Restaurantes;

public class DetalleEditarRestaurantesBd extends AppCompatActivity {
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private FirebaseUser currentUser;
    private String email, nombreNuevo, nombreAntiguo;
    private Button btnBorrarDetalleRestaurantes, btnEditarDetalleRestaurantes;
    private EditText etDetalleRestaurantes;
    private Restaurantes restaurantesBd;
    private Intent intent;
    private ArrayList<Restaurantes> restaurantesTotales;
    private CollectionReference restaurantesRef, platosRef;
    private DocumentSnapshot document;
    private boolean comprobarNombre = true;
    private ProgressDialog progressDialog;
    private Handler handler;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detalle_editar_restaurantes_bd);
        asignarId();
        intent = getIntent();
        restaurantesBd = (Restaurantes) intent.getSerializableExtra("restauranteDetalle");
        restaurantesTotales = (ArrayList<Restaurantes>) intent.getSerializableExtra("restaurantesTotales");
        mostrarDatos();
        btnEditarDetalleRestaurantes.setOnClickListener(view -> {
            //espero dos segundos para que se realicen bien los cambios
            editarRestaurante();
            progressDialog = ProgressDialog.show(this, "", "Actualización en curso...", true);
            handler = new Handler();
            handler.postDelayed(() -> {
                // Quitar el ProgressDialog después de 2 segundos adicionales
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }, 2000);
        });
        btnBorrarDetalleRestaurantes.setOnClickListener(view -> {
            borrarRestaurante();
            progressDialog = ProgressDialog.show(this, "", "Borrado en curso...", true);
            handler = new Handler();
            handler.postDelayed(() -> {
                if (progressDialog != null && progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
                finish();
            }, 2000);
        });
    }

    private void editarRestaurante() {
        nombreAntiguo = restaurantesBd.getNombreRestaurante();
        nombreNuevo = etDetalleRestaurantes.getText().toString();
        for (int i = 0; i < restaurantesTotales.size(); i++) {
            if (nombreNuevo.equalsIgnoreCase(restaurantesTotales.get(i).getNombreRestaurante())) {
                // Ignorar el elemento "nombre" enviado desde otra actividad
                if (!restaurantesTotales.get(i).getNombreRestaurante().equalsIgnoreCase(restaurantesBd.getNombreRestaurante())) {
                    comprobarNombre = false;
                    break;
                }
            }
        }
        if (nombreNuevo.isEmpty()) {
            // El campo de texto está vacío, mostrar un mensaje de error
            Toast.makeText(DetalleEditarRestaurantesBd.this, "El campo nombre de restaurante no puede estar vacío, no se realizaron los cambios.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Realizar la actualización sin verificar si existe previamente en la base de datos
        restaurantesRef = db.collection("restaurantes");
        restaurantesRef.whereEqualTo("usuarioId", email).get().addOnSuccessListener(querySnapshot -> {
            DocumentSnapshot document = querySnapshot.getDocuments().get(0);
            if (document != null) {
                if (comprobarNombre) {
                    document.getReference().update("nombreRestaurante", nombreNuevo).addOnSuccessListener(aVoid -> {
                        // La actualización se realizó exitosamente
                        // Actualizar las referencias en la colección "Platos"
                        actualizarReferencias(nombreAntiguo, nombreNuevo);
                    }).addOnFailureListener(e -> {
                        // Ocurrió un error al actualizar los datos
                        Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al actualizar los datos", Toast.LENGTH_SHORT).show();
                    });
                }
            }
        }).addOnFailureListener(e -> {
            // Ocurrió un error al buscar el restaurante en la base de datos
            Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al buscar el restaurante en la base de datos", Toast.LENGTH_SHORT).show();
        });
    }


    private void actualizarReferencias(String nombreAntiguo, String nombreNuevo) {
        // Obtener la referencia a la colección "Platos"
        platosRef = db.collection("platos");
        // Realizar una consulta para obtener los platos con el nombre antiguo
        platosRef.whereEqualTo("restaurante", nombreAntiguo).whereEqualTo("usuario", currentUser.getEmail()).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                // Actualizar cada documento que coincida con el nombre antiguo
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    document.getReference().update("restaurante", nombreNuevo).addOnSuccessListener(aVoid -> {
                        // La actualización se realizó exitosamente para el documento actual
                    }).addOnFailureListener(e -> {
                        // Ocurrió un error al actualizar el documento

                    });
                }
            }
        }).addOnFailureListener(e -> {
            // Ocurrió un error al realizar la consulta
        });
    }

    private void borrarReferencias(String nombreRestaurante) {
        // Obtener la referencia a la colección "Platos"
        platosRef = db.collection("platos");
        // Realizar una consulta para obtener los platos con el nombre del restaurante
        platosRef.whereEqualTo("restaurante", nombreRestaurante).whereEqualTo("usuario", currentUser.getEmail()).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                // Eliminar cada documento que coincida con el nombre del restaurante
                for (DocumentSnapshot document : querySnapshot.getDocuments()) {
                    document.getReference().delete().addOnSuccessListener(aVoid -> {
                        // La eliminación se realizó exitosamente para el documento actual
                    }).addOnFailureListener(e -> {
                        // Ocurrió un error al eliminar el documento
                    });
                }
            }
        }).addOnFailureListener(e -> {
            // Ocurrió un error al realizar la consulta
        });
    }

    private void borrarRestaurante() {
        // Eliminar el comensal de la base de datos
        restaurantesRef = db.collection("restaurantes");

        restaurantesRef.whereEqualTo("nombreRestaurante", restaurantesBd.getNombreRestaurante()).whereEqualTo("usuarioId", email).get().addOnSuccessListener(querySnapshot -> {
            if (!querySnapshot.isEmpty()) {
                document = querySnapshot.getDocuments().get(0);
                document.getReference().delete().addOnSuccessListener(aVoid -> {
                    // La eliminación se realizó exitosamente
                    borrarReferencias(restaurantesBd.getNombreRestaurante());
                }).addOnFailureListener(e -> {
                    // Ocurrió un error al eliminar el comensal
                    Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al eliminar el restaurante", Toast.LENGTH_SHORT).show();
                });
            }
        }).addOnFailureListener(e -> {
            // Ocurrió un error al buscar el comensal en la base de datos
            Toast.makeText(DetalleEditarRestaurantesBd.this, "Error al buscar el comensal en la base de datos", Toast.LENGTH_SHORT).show();
        });
    }

    private void mostrarDatos() {
        etDetalleRestaurantes.setText(restaurantesBd.getNombreRestaurante());
    }

    private void asignarId() {
        auth = FirebaseAuth.getInstance();
        currentUser = auth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        email = currentUser.getEmail();
        btnBorrarDetalleRestaurantes = findViewById(R.id.btnBorrarDetalleRestaurantes);
        btnEditarDetalleRestaurantes = findViewById(R.id.btnEditarDetalleRestaurantes);
        etDetalleRestaurantes = findViewById(R.id.etDetalleRestaurantes);
    }
}