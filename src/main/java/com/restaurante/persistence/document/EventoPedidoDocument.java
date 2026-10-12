package com.restaurante.persistence.document;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Document(collection = "eventos_pedido")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EventoPedidoDocument {

    @Id
    private String id;

    private Long idPedido;
    private String tipo;
    private String descripcion;
    private LocalDateTime timestamp;
}
