# Diagrama de clases - Sushi Craft



## Clases y sus relaciones

- **Mesa 1 -- 0..\* Pedido**: una mesa puede tener varios pedidos a lo largo
  del tiempo (uno por cada vez que se ocupa), guardados por `idMesa`.
- **Mesa 1 -- 0..\* Reserva**: una mesa puede tener varias reservas
  programadas, tambien por `idMesa`.
- **Mesa 1 -- 0..1 Cuenta**: una mesa tiene a lo sumo una cuenta abierta a la
  vez.
- **Pedido 1 -- 0..\* ItemPedido (composicion)**: un pedido esta hecho de sus
  items; si se elimina el pedido, sus items no tienen sentido por separado
  (por eso es composicion, con el diamante relleno, y no solo asociacion).
- **ItemPedido --\> Plato (dependencia)**: un item de pedido solo *lee* el
  plato al momento de crearse (para copiar `nombrePlato` y congelar el
  precio); despues de eso ya no depende de el, por eso es una flecha punteada
  y no una asociacion fija. Si el plato cambia de precio despues, los items
  ya creados no se ven afectados (precio "congelado").
- **RegistroVehiculo**: no tiene relacion con ninguna otra clase del dominio,
  es independiente (el parqueadero no esta ligado a una mesa ni a un
  cliente).
- **Mesa --\> EstadoMesa**, **Pedido --\> EstadoPedido**,
  **Cuenta --\> EstadoCuenta**: cada clase depende de su propio enum de
  estado (flecha punteada), no es una asociacion entre objetos sino que el
  atributo `estado` es de ese tipo.

## Por que no hay relaciones de Java (`@ManyToOne`/`@OneToMany`)

Este es el diagrama de **dominio puro**, no el de persistencia. Todas las
relaciones se guardan como un simple `Long` (por ejemplo `idMesa` dentro de
`Pedido`), sin que el objeto de dominio cargue una referencia al objeto
completo. Eso fue una decision de diseño para mantener el dominio simple y
evitar problemas de referencias circulares entre `Mesa` y `Pedido`/`Reserva`/
`Cuenta`. La traduccion a tablas reales (con las mismas relaciones logicas
por id) esta en el [modelo relacional](modelo-relacional-sushicraft.drawio).

## Enums

- `EstadoMesa`: LIBRE, OCUPADA, RESERVADA
- `EstadoPedido`: RECIBIDO, EN_PREPARACION, LISTO, ENTREGADO, CANCELADO
- `EstadoCuenta`: ABIERTA, CERRADA
