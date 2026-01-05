DO $$
BEGIN
  IF NOT EXISTS (
    SELECT 1
    FROM pg_constraint
    WHERE conname = 'uq_vote_user_hazard'
      AND conrelid = 'public.vote'::regclass
  ) THEN
    DROP INDEX IF EXISTS public.uq_vote_user_hazard;
    ALTER TABLE public.vote
    ADD CONSTRAINT uq_vote_user_hazard UNIQUE (user_id, hazard_report_id);
  END IF;
END $$;
