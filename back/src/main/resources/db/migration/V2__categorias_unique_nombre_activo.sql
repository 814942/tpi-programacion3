-- Crear la tabla si no existe (Flyway corre antes que Hibernate ddl-auto)
CREATE TABLE IF NOT EXISTS categorias (
    id BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion VARCHAR(500),
    imagen VARCHAR(500),
    eliminado BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP,
    updated_at TIMESTAMP,
    version BIGINT
);

DO $$
DECLARE
    constraint_name text;
BEGIN
    SELECT c.conname
      INTO constraint_name
      FROM pg_constraint c
      JOIN pg_class t ON t.oid = c.conrelid
      JOIN pg_namespace n ON n.oid = t.relnamespace
     WHERE n.nspname = current_schema()
       AND t.relname = 'categorias'
       AND c.contype = 'u'
       AND (
            SELECT array_agg(a.attname ORDER BY x.ord)
            FROM unnest(c.conkey) WITH ORDINALITY AS x(attnum, ord)
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = x.attnum
        ) = ARRAY['nombre']::name[];

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE categorias DROP CONSTRAINT %I', constraint_name);
    END IF;
END
$$;

DROP INDEX IF EXISTS categorias_nombre_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_categorias_nombre_activo
    ON categorias (nombre)
    WHERE eliminado = false;
