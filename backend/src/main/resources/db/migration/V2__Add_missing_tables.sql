-- ============================
--  AVATAR & BACKGROUND TABLES
-- ============================

-- Create sequences first
CREATE SEQUENCE IF NOT EXISTS avatar_id_seq;
CREATE SEQUENCE IF NOT EXISTS background_id_seq;

-- Avatar table
CREATE TABLE IF NOT EXISTS public.avatar (
  id bigint NOT NULL DEFAULT nextval('avatar_id_seq'::regclass),
  name character varying NOT NULL UNIQUE,
  image_path character varying NOT NULL UNIQUE,
  active boolean NOT NULL DEFAULT true,
  CONSTRAINT avatar_pkey PRIMARY KEY (id),
  CONSTRAINT uq_avatar_name UNIQUE (name),
  CONSTRAINT uq_avatar_image_path UNIQUE (image_path)
);

-- Background table
CREATE TABLE IF NOT EXISTS public.background (
  id bigint NOT NULL DEFAULT nextval('background_id_seq'::regclass),
  name character varying NOT NULL UNIQUE,
  image_path character varying NOT NULL UNIQUE,
  active boolean NOT NULL DEFAULT true,
  CONSTRAINT background_pkey PRIMARY KEY (id),
  CONSTRAINT uq_background_name UNIQUE (name),
  CONSTRAINT uq_background_image_path UNIQUE (image_path)
);

-- ============================
--  DEVICE MANAGEMENT (ESP32)
-- ============================

-- Device registration table
CREATE TABLE IF NOT EXISTS public.device (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  device_id character varying NOT NULL UNIQUE,
  api_key_hash character varying(256) NOT NULL UNIQUE,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  last_seen_at timestamp with time zone,
  description character varying(500),
  device_type character varying,
  firmware_version character varying,
  CONSTRAINT device_pkey PRIMARY KEY (id),
  CONSTRAINT uq_device_device_id UNIQUE (device_id),
  CONSTRAINT uq_device_api_key_hash UNIQUE (api_key_hash)
);

CREATE INDEX IF NOT EXISTS idx_device_active ON public.device(active);
CREATE INDEX IF NOT EXISTS idx_device_last_seen ON public.device(last_seen_at);

-- Device ownership history (allows device transfer)
CREATE TABLE IF NOT EXISTS public.device_ownership (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  device_id character varying NOT NULL,
  user_id bigint,
  active boolean NOT NULL DEFAULT true,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  transferred_at timestamp with time zone,
  notes character varying(500),
  CONSTRAINT device_ownership_pkey PRIMARY KEY (id),
  CONSTRAINT device_ownership_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE SET NULL
);

CREATE INDEX IF NOT EXISTS idx_device_ownership_device ON public.device_ownership(device_id);
CREATE INDEX IF NOT EXISTS idx_device_ownership_user ON public.device_ownership(user_id);
CREATE INDEX IF NOT EXISTS idx_device_ownership_active ON public.device_ownership(active);

-- Ensure only one active ownership per device
CREATE UNIQUE INDEX IF NOT EXISTS uq_device_ownership_active_device 
  ON public.device_ownership(device_id) 
  WHERE active = true;

-- ============================
--  TELEMETRY TABLES (ESP32)
-- ============================

-- Live telemetry (one row per device, always updated)
CREATE TABLE IF NOT EXISTS public.live_telemetry (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  device_id character varying NOT NULL UNIQUE,
  last_updated timestamp with time zone NOT NULL,
  speed_kph double precision,
  rpm double precision,
  latitude double precision,
  longitude double precision,
  CONSTRAINT live_telemetry_pkey PRIMARY KEY (id),
  CONSTRAINT uq_live_telemetry_device_id UNIQUE (device_id)
);

CREATE INDEX IF NOT EXISTS idx_live_telemetry_device ON public.live_telemetry(device_id);
CREATE INDEX IF NOT EXISTS idx_live_telemetry_last_updated ON public.live_telemetry(last_updated);

-- Telemetry history (all data points over time)
CREATE TABLE IF NOT EXISTS public.telemetry_history (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  device_id character varying NOT NULL,
  timestamp timestamp with time zone NOT NULL,
  -- Basic engine data
  speed_kph double precision,
  rpm double precision,
  throttle_pct double precision,
  -- Extended OBD data
  coolant_temp_c double precision,
  battery_voltage_v double precision,
  oil_temp_c double precision,
  fuel_level_pct double precision,
  intake_air_temp_c double precision,
  engine_load_pct double precision,
  maf_air_flow double precision,
  map_pressure double precision,
  timing_advance double precision,
  -- Diagnostic codes
  diagnostic_codes character varying(500),
  CONSTRAINT telemetry_history_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_device_timestamp ON public.telemetry_history(device_id, timestamp);
CREATE INDEX IF NOT EXISTS idx_telemetry_history_device ON public.telemetry_history(device_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_history_timestamp ON public.telemetry_history(timestamp);

-- Legacy telemetry table (for backward compatibility)
CREATE TABLE IF NOT EXISTS public.telemetry (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  device_id character varying NOT NULL,
  timestamp timestamp with time zone NOT NULL,
  -- Basic engine data
  speed_kph double precision,
  rpm double precision,
  throttle_pct double precision,
  -- Extended OBD data
  coolant_temp_c double precision,
  battery_voltage_v double precision,
  oil_temp_c double precision,
  fuel_level_pct double precision,
  intake_air_temp_c double precision,
  engine_load_pct double precision,
  maf_air_flow double precision,
  map_pressure double precision,
  timing_advance double precision,
  -- Diagnostic codes
  diagnostic_codes character varying(500),
  CONSTRAINT telemetry_pkey PRIMARY KEY (id)
);

CREATE INDEX IF NOT EXISTS idx_telemetry_device ON public.telemetry(device_id);
CREATE INDEX IF NOT EXISTS idx_telemetry_timestamp ON public.telemetry(timestamp);

-- ============================
--  ADD MISSING COLUMNS & CONSTRAINTS
-- ============================

-- Add avatar_id and background_id to app_user if not exists
DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'app_user' 
    AND column_name = 'avatar_id'
  ) THEN
    ALTER TABLE public.app_user ADD COLUMN avatar_id bigint;
    ALTER TABLE public.app_user 
      ADD CONSTRAINT fk_app_user_avatar 
      FOREIGN KEY (avatar_id) REFERENCES public.avatar(id) ON DELETE SET NULL;
  END IF;
END $$;

DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'app_user' 
    AND column_name = 'background_id'
  ) THEN
    ALTER TABLE public.app_user ADD COLUMN background_id bigint;
    ALTER TABLE public.app_user 
      ADD CONSTRAINT fk_app_user_background 
      FOREIGN KEY (background_id) REFERENCES public.background(id) ON DELETE SET NULL;
  END IF;
END $$;

-- Update trip table to use coordinates instead of location references (if needed)
DO $$ 
BEGIN
  -- Add coordinate columns if they don't exist
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'trip' 
    AND column_name = 'start_lat'
  ) THEN
    ALTER TABLE public.trip ADD COLUMN start_lat double precision;
    ALTER TABLE public.trip ADD COLUMN start_lng double precision;
    ALTER TABLE public.trip ADD COLUMN end_lat double precision;
    ALTER TABLE public.trip ADD COLUMN end_lng double precision;
    ALTER TABLE public.trip ADD COLUMN created_at timestamp with time zone DEFAULT now();
  END IF;
  
  -- Add convoy_id column if it doesn't exist
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'trip' 
    AND column_name = 'convoy_id'
  ) THEN
    ALTER TABLE public.trip ADD COLUMN convoy_id bigint;
  END IF;
END $$;

-- Add icon_path and active to hazard_category if not exists
DO $$ 
BEGIN
  IF NOT EXISTS (
    SELECT 1 FROM information_schema.columns 
    WHERE table_schema = 'public' 
    AND table_name = 'hazard_category' 
    AND column_name = 'icon_path'
  ) THEN
    ALTER TABLE public.hazard_category ADD COLUMN icon_path character varying;
    ALTER TABLE public.hazard_category ADD COLUMN active boolean NOT NULL DEFAULT true;
  END IF;
END $$;

-- ============================
--  PERFORMANCE INDEXES
-- ============================

-- Indexes for common queries
CREATE INDEX IF NOT EXISTS idx_hazard_report_status ON public.hazard_report(status);
CREATE INDEX IF NOT EXISTS idx_hazard_report_created_at ON public.hazard_report(created_at);
CREATE INDEX IF NOT EXISTS idx_hazard_report_category ON public.hazard_report(category_id);
CREATE INDEX IF NOT EXISTS idx_hazard_report_created_by ON public.hazard_report(created_by_id);

CREATE INDEX IF NOT EXISTS idx_friendship_status ON public.friendship(status);
CREATE INDEX IF NOT EXISTS idx_friendship_requester ON public.friendship(requester_id);
CREATE INDEX IF NOT EXISTS idx_friendship_addressee ON public.friendship(addressee_id);

CREATE INDEX IF NOT EXISTS idx_trip_user ON public.trip(user_id);
CREATE INDEX IF NOT EXISTS idx_trip_started_at ON public.trip(started_at);

CREATE INDEX IF NOT EXISTS idx_user_achievement_user ON public.user_achievement(user_id);
CREATE INDEX IF NOT EXISTS idx_user_achievement_achievement ON public.user_achievement(achievement_id);

CREATE INDEX IF NOT EXISTS idx_favourite_location_user ON public.favourite_location(user_id);

CREATE INDEX IF NOT EXISTS idx_statistics_user ON public.statistics(user_id);
