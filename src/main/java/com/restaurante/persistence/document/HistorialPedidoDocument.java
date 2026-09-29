package com.restaurante.persistence.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.restaurante.model.domain.EstadoPedido;
import com.restaurante.model.domain.TipoEventoPedido;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Un evento en la vida de un pedido. Se guarda en MongoDB (coleccion
 * {@code historial_pedidos}) porque es un registro de solo-agregar, sin
 * relaciones y con un esquema que puede crecer (detalle libre).
 */
@Document(collection = "historial_pedidos")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class HistorialPedidoDocument {

    @Id
    private String id;

    @Indexed
    private Long idPedido;

    private TipoEventoPedido tipo;
    private EstadoPedido estadoAnterior;
    private EstadoPedido estadoNuevo;
    private String detalle;

    /** Correo del usuario autenticado que hizo el cambio ("sistema" si no hay). */
    private String usuario;

    private LocalDateTime fecha;
}
