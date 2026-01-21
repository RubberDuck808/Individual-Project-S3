-- Seed default roles
INSERT INTO public.role (name) VALUES ('USER') ON CONFLICT (name) DO NOTHING;
INSERT INTO public.role (name) VALUES ('ADMIN') ON CONFLICT (name) DO NOTHING;
