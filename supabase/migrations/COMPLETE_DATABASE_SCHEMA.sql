-- ============================================
-- COMPLETE DATABASE SCHEMA - Tripwire Project
-- ============================================
-- WARNING: This schema is for context only and is not meant to be run.
-- Table order and constraints may not be valid for execution.
-- Use migrations in supabase/migrations/ for actual database setup.
-- ============================================

-- ============================
--  ROLES & USERS
-- ============================

CREATE TABLE public.role (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  name text NOT NULL UNIQUE,
  CONSTRAINT role_pkey PRIMARY KEY (id)
);

CREATE TABLE public.app_user (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  username text NOT NULL UNIQUE,
  name text NOT NULL,
  password text,
  email text NOT NULL UNIQUE,
  role_id bigint NOT NULL,
  created_at timestamp with time zone DEFAULT now(),
  avatar_id bigint,
  background_id bigint,
  CONSTRAINT app_user_pkey PRIMARY KEY (id),
  CONSTRAINT app_user_role_id_fkey FOREIGN KEY (role_id) REFERENCES public.role(id),
  CONSTRAINT fk_app_user_avatar FOREIGN KEY (avatar_id) REFERENCES public.avatar(id) ON DELETE SET NULL,
  CONSTRAINT fk_app_user_background FOREIGN KEY (background_id) REFERENCES public.background(id) ON DELETE SET NULL
);

CREATE UNIQUE INDEX uq_app_user_username ON public.app_user (username);
CREATE UNIQUE INDEX uq_app_user_email ON public.app_user (email);


-- ============================
--  AVATAR & BACKGROUND
-- ============================

CREATE TABLE public.avatar (
  id bigint NOT NULL DEFAULT nextval('avatar_id_seq'::regclass),
  name character varying NOT NULL UNIQUE,
  image_path character varying NOT NULL UNIQUE,
  active boolean NOT NULL DEFAULT true,
  CONSTRAINT avatar_pkey PRIMARY KEY (id),
  CONSTRAINT uq_avatar_name UNIQUE (name),
  CONSTRAINT uq_avatar_image_path UNIQUE (image_path)
);

CREATE SEQUENCE avatar_id_seq;

CREATE TABLE public.background (
  id bigint NOT NULL DEFAULT nextval('background_id_seq'::regclass),
  name character varying NOT NULL UNIQUE,
  image_path character varying NOT NULL UNIQUE,
  active boolean NOT NULL DEFAULT true,
  CONSTRAINT background_pkey PRIMARY KEY (id),
  CONSTRAINT uq_background_name UNIQUE (name),
  CONSTRAINT uq_background_image_path UNIQUE (image_path)
);

CREATE SEQUENCE background_id_seq;


-- ============================
--  DEVICE MANAGEMENT (ESP32)
-- ============================

CREATE TABLE public.device (
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

CREATE INDEX idx_device_active ON public.device(active);
CREATE INDEX idx_device_last_seen ON public.device(last_seen_at);

CREATE TABLE public.device_ownership (
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

CREATE INDEX idx_device_ownership_device ON public.device_ownership(device_id);
CREATE INDEX idx_device_ownership_user ON public.device_ownership(user_id);
CREATE INDEX idx_device_ownership_active ON public.device_ownership(active);

-- Only one active ownership per device
CREATE UNIQUE INDEX uq_device_ownership_active_device 
  ON public.device_ownership(device_id) 
  WHERE active = true;


-- ============================
--  TELEMETRY TABLES (ESP32)
-- ============================

CREATE TABLE public.live_telemetry (
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

CREATE INDEX idx_live_telemetry_device ON public.live_telemetry(device_id);
CREATE INDEX idx_live_telemetry_last_updated ON public.live_telemetry(last_updated);

CREATE TABLE public.telemetry_history (
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

CREATE INDEX idx_device_timestamp ON public.telemetry_history(device_id, timestamp);
CREATE INDEX idx_telemetry_history_device ON public.telemetry_history(device_id);
CREATE INDEX idx_telemetry_history_timestamp ON public.telemetry_history(timestamp);

-- Legacy telemetry table (for backward compatibility)
CREATE TABLE public.telemetry (
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

CREATE INDEX idx_telemetry_device ON public.telemetry(device_id);
CREATE INDEX idx_telemetry_timestamp ON public.telemetry(timestamp);


-- ============================
--  FRIENDSHIPS
-- ============================

CREATE TABLE public.friendship (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  requester_id bigint,
  addressee_id bigint,
  status text, -- REQUESTED, ACCEPTED, DECLINED, BLOCKED
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT friendship_pkey PRIMARY KEY (id),
  CONSTRAINT friendship_requester_id_fkey FOREIGN KEY (requester_id) REFERENCES public.app_user(id) ON DELETE CASCADE,
  CONSTRAINT friendship_addressee_id_fkey FOREIGN KEY (addressee_id) REFERENCES public.app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_friendship_status ON public.friendship(status);
CREATE INDEX idx_friendship_requester ON public.friendship(requester_id);
CREATE INDEX idx_friendship_addressee ON public.friendship(addressee_id);


-- ============================
--  FAVOURITE LOCATIONS
-- ============================

CREATE TABLE public.favourite_location (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  user_id bigint,
  name text,
  latitude double precision,
  longitude double precision,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT favourite_location_pkey PRIMARY KEY (id),
  CONSTRAINT favourite_location_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_favourite_location_user ON public.favourite_location(user_id);


-- ============================
--  ACHIEVEMENTS
-- ============================

CREATE TABLE public.achievement (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  name text,
  description text,
  criteria_type text, -- e.g., "REPORTS", "TRIPS"
  criteria_value integer,
  icon_url text,
  CONSTRAINT achievement_pkey PRIMARY KEY (id)
);

CREATE TABLE public.user_achievement (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  user_id bigint,
  achievement_id bigint,
  unlocked_at timestamp with time zone,
  progress integer,
  CONSTRAINT user_achievement_pkey PRIMARY KEY (id),
  CONSTRAINT user_achievement_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE,
  CONSTRAINT user_achievement_achievement_id_fkey FOREIGN KEY (achievement_id) REFERENCES public.achievement(id) ON DELETE CASCADE
);

CREATE INDEX idx_user_achievement_user ON public.user_achievement(user_id);
CREATE INDEX idx_user_achievement_achievement ON public.user_achievement(achievement_id);


-- ============================
--  HAZARD CATEGORIES & REPORTS
-- ============================

CREATE TABLE public.hazard_category (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  name text,
  icon_path character varying,
  active boolean NOT NULL DEFAULT true,
  CONSTRAINT hazard_category_pkey PRIMARY KEY (id)
);

CREATE TABLE public.hazard_report (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  latitude double precision,
  longitude double precision,
  category_id bigint,
  created_by_id bigint,
  status text, -- OPEN, VERIFIED, RESOLVED, REJECTED
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT hazard_report_pkey PRIMARY KEY (id),
  CONSTRAINT hazard_report_category_id_fkey FOREIGN KEY (category_id) REFERENCES public.hazard_category(id),
  CONSTRAINT hazard_report_created_by_id_fkey FOREIGN KEY (created_by_id) REFERENCES public.app_user(id) ON DELETE SET NULL
);

CREATE INDEX idx_hazard_report_status ON public.hazard_report(status);
CREATE INDEX idx_hazard_report_created_at ON public.hazard_report(created_at);
CREATE INDEX idx_hazard_report_category ON public.hazard_report(category_id);
CREATE INDEX idx_hazard_report_created_by ON public.hazard_report(created_by_id);


-- ============================
--  VOTES
-- ============================

CREATE TABLE public.vote (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  vote_type text, -- UPVOTE, DOWNVOTE
  user_id bigint,
  hazard_report_id bigint,
  created_at timestamp with time zone DEFAULT now(),
  CONSTRAINT vote_pkey PRIMARY KEY (id),
  CONSTRAINT vote_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE,
  CONSTRAINT vote_hazard_report_id_fkey FOREIGN KEY (hazard_report_id) REFERENCES public.hazard_report(id) ON DELETE CASCADE,
  CONSTRAINT uq_vote_user_hazard UNIQUE (user_id, hazard_report_id)
);


-- ============================
--  STATISTICS
-- ============================

CREATE TABLE public.statistics (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  user_id bigint UNIQUE,
  total_trips integer,
  total_distance_km double precision,
  total_hazards_reported integer,
  total_votes integer,
  CONSTRAINT statistics_pkey PRIMARY KEY (id),
  CONSTRAINT statistics_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_statistics_user ON public.statistics(user_id);


-- ============================
--  TRIPS
-- ============================

CREATE TABLE public.trip (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  user_id bigint NOT NULL,
  distance_km double precision NOT NULL,
  started_at timestamp with time zone NOT NULL,
  ended_at timestamp with time zone NOT NULL,
  convoy_id bigint,
  start_lat double precision NOT NULL,
  start_lng double precision NOT NULL,
  end_lat double precision NOT NULL,
  end_lng double precision NOT NULL,
  created_at timestamp with time zone NOT NULL DEFAULT now(),
  CONSTRAINT trip_pkey PRIMARY KEY (id),
  CONSTRAINT trip_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE CASCADE
);

CREATE INDEX idx_trip_user ON public.trip(user_id);
CREATE INDEX idx_trip_started_at ON public.trip(started_at);


-- ============================
--  LICENSE PLATE INFO
-- ============================

CREATE TABLE public.license_plate_info (
  id bigint GENERATED ALWAYS AS IDENTITY NOT NULL,
  license_plate text,
  brand text,
  model text,
  year_of_manufacture integer,
  fuel_type text,
  fetched_at timestamp with time zone,
  user_id bigint,
  CONSTRAINT license_plate_info_pkey PRIMARY KEY (id),
  CONSTRAINT license_plate_info_user_id_fkey FOREIGN KEY (user_id) REFERENCES public.app_user(id) ON DELETE SET NULL
);


-- ============================================
-- SCHEMA SUMMARY
-- ============================================
-- Total Tables: 20
-- 
-- Core Tables:
--   - role, app_user
--   - avatar, background
-- 
-- Device & Telemetry:
--   - device, device_ownership
--   - live_telemetry, telemetry_history, telemetry
-- 
-- Social Features:
--   - friendship, favourite_location
--   - achievement, user_achievement
-- 
-- Hazard System:
--   - hazard_category, hazard_report, vote
-- 
-- Trip & Statistics:
--   - trip, statistics
-- 
-- Vehicle Info:
--   - license_plate_info
-- ============================================
