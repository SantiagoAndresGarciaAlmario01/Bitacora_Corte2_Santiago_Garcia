-- =====================================================================
-- Consultas para verificar (y capturar como evidencia) que los datos que
-- se crean desde la API quedan guardados en PostgreSQL.
--   docker exec -it sushicraft-postgres psql -U sushicraft -d sushicraft_db
-- =====================================================================

-- 1. Usuarios y roles (la contrasena solo aparece como hash BCrypt)
SELECT id, correo, rol, LEFT(contrasena_hash, 7) || '...' AS hash
FROM usuarios
ORDER BY id;

-- 2. Menu disponible por categoria
SELECT categoria, COUNT(*) AS platos, MIN(precio) AS desde, MAX(precio) AS hasta
FROM platos
WHERE disponible
GROUP BY categoria
ORDER BY categoria;

-- 3. Estado del salon
SELECT estado, COUNT(*) AS mesas, SUM(capacidad) AS puestos
FROM mesas
GROUP BY estado;

-- 4. Detalle de cada pedido con su total calculado
SELECT p.id AS pedido, m.numero AS mesa, p.estado,
       COUNT(i.id) AS items,
       COALESCE(SUM(i.precio_unitario * i.cantidad), 0) AS total
FROM pedidos p
JOIN mesas m ON m.id = p.id_mesa
LEFT JOIN items_pedido i ON i.id_pedido = p.id
GROUP BY p.id, m.numero, p.estado
ORDER BY p.id;

-- 5. Cuentas cerradas: el total guardado debe coincidir con lo entregado
SELECT c.id AS cuenta, c.total AS total_cobrado,
       COALESCE(SUM(i.precio_unitario * i.cantidad)
                FILTER (WHERE p.estado = 'ENTREGADO'), 0) AS total_recalculado
FROM cuentas c
LEFT JOIN pedidos p ON p.id_cuenta = c.id
LEFT JOIN items_pedido i ON i.id_pedido = p.id
WHERE c.estado = 'CERRADA'
GROUP BY c.id, c.total
ORDER BY c.id;

-- 6. Platos mas pedidos
SELECT nombre_plato, SUM(cantidad) AS unidades
FROM items_pedido
GROUP BY nombre_plato
ORDER BY unidades DESC
LIMIT 5;

-- 7. Proximas reservas vigentes
SELECT r.id, m.numero AS mesa, r.nombre_cliente, r.fecha_hora, r.numero_personas
FROM reservas r
JOIN mesas m ON m.id = r.id_mesa
WHERE NOT r.cancelada AND r.fecha_hora > NOW()
ORDER BY r.fecha_hora;

-- 8. Vehiculos dentro del parqueadero y tiempo que llevan
SELECT placa, hora_entrada,
       EXTRACT(EPOCH FROM (NOW() - hora_entrada)) / 60 AS minutos
FROM registros_parqueadero
WHERE hora_salida IS NULL
ORDER BY hora_entrada;

-- ---------------------------------------------------------------------
-- MongoDB (historial de pedidos):
--   docker exec -it sushicraft-mongo mongosh sushicraft_historial
--   db.historial_pedidos.find({ idPedido: 1 }).sort({ fecha: 1 })
--   db.historial_pedidos.aggregate([{ $group: { _id: "$tipo", total: { $sum: 1 } } }])
-- ---------------------------------------------------------------------
