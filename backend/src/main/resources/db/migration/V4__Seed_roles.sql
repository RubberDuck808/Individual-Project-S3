-- Seed default roles (only if they don't exist)
-- This migration is idempotent and safe to run multiple times
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM public.role WHERE name = 'USER') THEN
        INSERT INTO public.role (name) VALUES ('USER');
    END IF;
    IF NOT EXISTS (SELECT 1 FROM public.role WHERE name = 'ADMIN') THEN
        INSERT INTO public.role (name) VALUES ('ADMIN');
    END IF;
END $$;
