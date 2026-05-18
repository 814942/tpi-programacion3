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
       AND t.relname = 'usuarios'
       AND c.contype = 'u'
       AND (
            SELECT array_agg(a.attname ORDER BY x.ord)
            FROM unnest(c.conkey) WITH ORDINALITY AS x(attnum, ord)
            JOIN pg_attribute a ON a.attrelid = c.conrelid AND a.attnum = x.attnum
        ) = ARRAY['email']::name[];

    IF constraint_name IS NOT NULL THEN
        EXECUTE format('ALTER TABLE usuarios DROP CONSTRAINT %I', constraint_name);
    END IF;
END
$$;

DROP INDEX IF EXISTS usuarios_email_key;

CREATE UNIQUE INDEX IF NOT EXISTS ux_usuarios_email_activo
    ON usuarios (email)
    WHERE eliminado = false;