-- Seed default hazard categories so the app (and E2E tests) can create reports on a fresh DB.
-- Idempotent: safe to run multiple times even without a unique constraint on name.

DO $$
BEGIN
  IF NOT EXISTS (SELECT 1 FROM public.hazard_category WHERE name = 'Pothole') THEN
    INSERT INTO public.hazard_category (name, icon_path, active) VALUES ('Pothole', NULL, true);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.hazard_category WHERE name = 'Accident') THEN
    INSERT INTO public.hazard_category (name, icon_path, active) VALUES ('Accident', NULL, true);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.hazard_category WHERE name = 'Debris') THEN
    INSERT INTO public.hazard_category (name, icon_path, active) VALUES ('Debris', NULL, true);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.hazard_category WHERE name = 'Construction') THEN
    INSERT INTO public.hazard_category (name, icon_path, active) VALUES ('Construction', NULL, true);
  END IF;

  IF NOT EXISTS (SELECT 1 FROM public.hazard_category WHERE name = 'Flood') THEN
    INSERT INTO public.hazard_category (name, icon_path, active) VALUES ('Flood', NULL, true);
  END IF;
END $$;

