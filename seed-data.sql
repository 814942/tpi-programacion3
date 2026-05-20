-- ============================================================
-- Seed data: 10 categorías + 50 productos
-- Ejecutar en la BD foodstore (PostgreSQL)
-- ============================================================

-- ==================== CATEGORÍAS ====================

INSERT INTO categorias (nombre, descripcion, imagen, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Hamburguesas', 'Las mejores hamburguesas del mercado', 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400', false, NOW(), NOW(), 0),
    ('Pizzas', 'Pizzas artesanales horneadas a leña', 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400', false, NOW(), NOW(), 0),
    ('Papas Fritas', 'Papas crocantes y acompañamientos', 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400', false, NOW(), NOW(), 0),
    ('Bebidas', 'Gaseosas, aguas y jugos', 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400', false, NOW(), NOW(), 0),
    ('Ensaladas', 'Ensaladas frescas y saludables', 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400', false, NOW(), NOW(), 0),
    ('Sándwiches', 'Sándwiches y wraps', 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', false, NOW(), NOW(), 0),
    ('Tacos', 'Tacos mexicanos bien picantes', 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400', false, NOW(), NOW(), 0),
    ('Postres', 'Dulces y postres caseros', 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', false, NOW(), NOW(), 0),
    ('Cafés', 'Café de especialidad y té', 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', false, NOW(), NOW(), 0),
    ('Combos', 'Combos promocionales para ahorrar', 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400', false, NOW(), NOW(), 0)
) AS c
WHERE NOT EXISTS (SELECT 1 FROM categorias WHERE nombre = c.column1 AND eliminado = false);

-- ==================== PRODUCTOS ====================
-- Usa subquery dinámica para no hardcodear IDs de categorías

-- Hamburguesas
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Hamburguesa Simple',      12000.00, 'Carne 150g, lechuga, tomate y mayonesa',                             50, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Hamburguesas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Hamburguesa Doble',       18000.00, 'Doble carne 300g, cheddar y bacon',                                   40, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Hamburguesas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Hamburguesa Triple',      25000.00, 'Triple carne, cheddar, bacon y cebolla caramelizada',                 30, 'https://images.unsplash.com/photo-1568901346375-23c9450c58cd?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Hamburguesas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Hamburguesa BBQ',         16000.00, 'Carne 200g, salsa BBQ, aros de cebolla',                              35, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Hamburguesas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Hamburguesa Veggie',      14000.00, 'Medallón de lentejas, rúcula y queso vegano',                         25, 'https://images.unsplash.com/photo-1550547660-d9450f859349?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Hamburguesas' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Pizzas
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Pizza Muzzarella',        14000.00, 'Salsa casera, muzzarella y orégano',                                  40, 'https://images.unsplash.com/photo-1513104890138-7c749659a591?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Pizzas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Pizza Pepperoni',         16000.00, 'Pepperoni extra con queso derretido',                                 35, 'https://images.unsplash.com/photo-1628840042765-356cda07504e?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Pizzas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Pizza Especial',          18000.00, 'Jamón, morrón, aceitunas y huevo',                                   30, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Pizzas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Pizza Napolitana',        17000.00, 'Rodajas de tomate, ajo, albahaca y parmesano',                       25, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Pizzas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Pizza Fugazzeta',         19000.00, 'Cebolla, muzzarella, olivas y provenzal',                             20, 'https://images.unsplash.com/photo-1565299624946-b28f40a0ae38?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Pizzas' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Papas Fritas
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Papas Fritas Clásicas',   6000.00,  'Papas crocantes con sal gruesa',                                     80, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Papas Fritas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Papas con Cheddar',       8000.00,  'Papas fritas con salsa cheddar y panceta',                            60, 'https://images.unsplash.com/photo-1630384060421-cb20d0e0649d?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Papas Fritas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Papas Rústicas',          7000.00,  'Papas con piel, romero y aceite de oliva',                            45, 'https://images.unsplash.com/photo-1573080496219-bb080dd4f877?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Papas Fritas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Aros de Cebolla',         7500.00,  'Aros de cebolla empanizados',                                        40, 'https://images.unsplash.com/photo-1639024471283-03518883512d?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Papas Fritas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Bastones de Muzzarella',  9000.00,  'Bastones de muzzarella empanizados',                                 35, 'https://images.unsplash.com/photo-1639024471283-03518883512d?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Papas Fritas' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Bebidas
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Coca Cola 350ml',         2500.00,  'Lata 350ml bien fría',                                              100, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Bebidas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Coca Cola 500ml',         3500.00,  'Botella descartable 500ml',                                           80, 'https://images.unsplash.com/photo-1622483767028-3f66f32aef97?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Bebidas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Agua Mineral 500ml',      2000.00,  'Agua sin gas 500ml',                                                 120, 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Bebidas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Agua con Gas 500ml',      2200.00,  'Agua con gas 500ml',                                                 90, 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Bebidas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Jugo Natural Naranja',    4000.00,  'Jugo de naranja exprimido 400ml',                                    30, 'https://images.unsplash.com/photo-1548839140-29a749e1cf4d?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Bebidas' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Ensaladas
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Ensalada César',          10000.00, 'Lechuga, pollo, crutones, parmesano y aderezo césar',                  25, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Ensaladas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Ensalada Griega',         9500.00,  'Tomate, pepino, aceitunas negras, queso feta',                        20, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Ensaladas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Ensalada Waldorf',        11000.00, 'Manzana, apio, nueces, pasas y mayonesa',                             15, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Ensaladas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Ensalada de Quinoa',      12000.00, 'Quinoa, palta, cherry, maíz y vinagreta de limón',                    18, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Ensaladas' LIMIT 1), false, NOW(), NOW(), 0),
    ('Ensalada Tropical',       10500.00, 'Mix de verdes, mango, palta, camarones y maracuyá',                   12, 'https://images.unsplash.com/photo-1512621776951-a57141f2eefd?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Ensaladas' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Sándwiches
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Sándwich de Lomo',        15000.00, 'Lomo, lechuga, tomate, jamón y queso en pan francés',                 30, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Sándwiches' LIMIT 1), false, NOW(), NOW(), 0),
    ('Sándwich de Pollo',       12000.00, 'Pollo grillado, rúcula, tomates secos y mayonesa de ajo',            25, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Sándwiches' LIMIT 1), false, NOW(), NOW(), 0),
    ('Wrap Veggie',             10000.00, 'Tortilla de espinaca, hummus, vegetales asados y queso',              20, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Sándwiches' LIMIT 1), false, NOW(), NOW(), 0),
    ('Sándwich Milanesa',       13000.00, 'Milanesa de carne, lechuga, tomate y huevo',                          35, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Sándwiches' LIMIT 1), false, NOW(), NOW(), 0),
    ('Club Sandwich',           14000.00, 'Triple capa: pollo, panceta, huevo, lechuga y tomate',                20, 'https://images.unsplash.com/photo-1528735602780-2552fd46c7af?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Sándwiches' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Tacos
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Taco al Pastor',          9000.00,  'Tortilla de maíz, cerdo adobado, piña y cilantro',                    30, 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Tacos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Taco de Carne',           10000.00, 'Carne picada, guacamole, cebolla y cilantro',                         25, 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Tacos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Taco de Pollo',           9000.00,  'Pollo desmenuzado, salsa verde y crema',                              28, 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Tacos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Taco Veggie',             8000.00,  'Frijoles negros, queso, pico de gallo y guacamole',                   20, 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Tacos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Taco de Pescado',         11000.00, 'Pescado tempura, col morada, chipotle y lima',                        15, 'https://images.unsplash.com/photo-1551504734-5ee1c4a1479b?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Tacos' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Postres
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Flan Casero',             5000.00,  'Flan con dulce de leche y crema',                                    25, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Postres' LIMIT 1), false, NOW(), NOW(), 0),
    ('Brownie con Helado',      7000.00,  'Brownie de chocolate con helado de vainilla',                        20, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Postres' LIMIT 1), false, NOW(), NOW(), 0),
    ('Cheesecake',              6500.00,  'Cheesecake de frutos rojos con base de galleta',                     18, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Postres' LIMIT 1), false, NOW(), NOW(), 0),
    ('Helado Artesanal 2 bochas', 4500.00, 'Dos bochas de helado artesanal a elección',                         40, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Postres' LIMIT 1), false, NOW(), NOW(), 0),
    ('Torta de Chocolate',      8000.00,  'Porción de torta de chocolate con ganache',                          15, 'https://images.unsplash.com/photo-1551024506-0bccd828d307?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Postres' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Cafés
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Café Expresso',           3000.00,  'Café expresso 30ml de especialidad',                                 60, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Cafés' LIMIT 1), false, NOW(), NOW(), 0),
    ('Café Latte',              3500.00,  'Expresso con leche vaporizada',                                      50, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Cafés' LIMIT 1), false, NOW(), NOW(), 0),
    ('Cappuccino',              3500.00,  'Expresso con leche, espuma y canela',                                45, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Cafés' LIMIT 1), false, NOW(), NOW(), 0),
    ('Té de la casa',           2500.00,  'Selección de tés orgánicos',                                         40, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Cafés' LIMIT 1), false, NOW(), NOW(), 0),
    ('Submarino',               4000.00,  'Chocolate derretido con leche caliente',                              30, 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Cafés' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- Combos
INSERT INTO productos (nombre, precio, descripcion, stock, imagen, disponible, id_categoria, eliminado, created_at, updated_at, version)
SELECT * FROM (VALUES
    ('Combo Hamburguesa + Papas + Bebida',  20000.00, 'Hamburguesa simple, papas fritas y Coca 350ml',           40, 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Combos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Combo Pizza + Bebida',                18000.00, 'Pizza muzzarella grande + Coca 500ml',                     30, 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Combos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Combo Taco + Bebida',                 13000.00, '3 tacos al pastor + cerveza o gaseosa',                    25, 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Combos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Combo Familiar',                      35000.00, '2 hamburguesas, 1 pizza, papas grandes y 4 bebidas',       15, 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Combos' LIMIT 1), false, NOW(), NOW(), 0),
    ('Combo Postre + Café',                 8000.00,  'Porción de cheesecake + café latte',                       20, 'https://images.unsplash.com/photo-1553621042-f6e147245754?w=400', true, (SELECT id FROM categorias WHERE nombre = 'Combos' LIMIT 1), false, NOW(), NOW(), 0)
) AS p
WHERE NOT EXISTS (SELECT 1 FROM productos WHERE nombre = p.column1 AND eliminado = false);

-- ==================== VERIFICACIÓN ====================
SELECT 'Categorías:' AS tabla, COUNT(*) AS total FROM categorias WHERE eliminado = false
UNION ALL
SELECT 'Productos:', COUNT(*) FROM productos WHERE eliminado = false;
