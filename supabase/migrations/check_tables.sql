-- ============================================
-- CHECK ALL REQUIRED TABLES EXIST
-- ============================================
-- This query checks if all required tables exist in the database
-- Run this to verify your database schema is complete
-- ============================================

-- Check core tables
SELECT 
    'Core Tables' as category,
    table_name,
    CASE 
        WHEN table_name IS NOT NULL THEN '✅ EXISTS'
        ELSE '❌ MISSING'
    END as status
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'role',
    'app_user',
    'friendship',
    'favourite_location',
    'achievement',
    'user_achievement',
    'hazard_category',
    'hazard_report',
    'vote',
    'statistics',
    'trip',
    'license_plate_info'
)
ORDER BY table_name;

-- Check device & telemetry tables (NEW - may be missing)
SELECT 
    'Device & Telemetry Tables' as category,
    table_name,
    CASE 
        WHEN table_name IS NOT NULL THEN '✅ EXISTS'
        ELSE '❌ MISSING'
    END as status
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'device',
    'device_ownership',
    'live_telemetry',
    'telemetry_history',
    'telemetry'
)
ORDER BY table_name;

-- Check asset tables (NEW - may be missing)
SELECT 
    'Asset Tables' as category,
    table_name,
    CASE 
        WHEN table_name IS NOT NULL THEN '✅ EXISTS'
        ELSE '❌ MISSING'
    END as status
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'avatar',
    'background'
)
ORDER BY table_name;

-- ============================================
-- COMPREHENSIVE CHECK - ALL TABLES AT ONCE
-- ============================================

SELECT 
    table_name,
    CASE 
        WHEN table_name IN (
            'role', 'app_user', 'friendship', 'favourite_location',
            'achievement', 'user_achievement', 'hazard_category', 
            'hazard_report', 'vote', 'statistics', 'trip', 
            'license_plate_info', 'device', 'device_ownership',
            'live_telemetry', 'telemetry_history', 'telemetry',
            'avatar', 'background'
        ) THEN '✅ EXISTS'
        ELSE '❌ MISSING'
    END as status,
    CASE 
        WHEN table_name IN ('device', 'device_ownership', 'live_telemetry', 
                            'telemetry_history', 'telemetry', 'avatar', 'background')
        THEN 'NEW (Required for Admin)'
        ELSE 'Core'
    END as table_type
FROM information_schema.tables 
WHERE table_schema = 'public' 
AND table_name IN (
    'role', 'app_user', 'friendship', 'favourite_location',
    'achievement', 'user_achievement', 'hazard_category', 
    'hazard_report', 'vote', 'statistics', 'trip', 
    'license_plate_info', 'device', 'device_ownership',
    'live_telemetry', 'telemetry_history', 'telemetry',
    'avatar', 'background'
)
ORDER BY 
    CASE table_type
        WHEN 'Core' THEN 1
        WHEN 'NEW (Required for Admin)' THEN 2
    END,
    table_name;

-- ============================================
-- QUICK CHECK - MISSING TABLES ONLY
-- ============================================
-- This shows which tables are MISSING

SELECT 
    'MISSING TABLE' as issue,
    expected_table as table_name
FROM (
    VALUES 
        ('role'), ('app_user'), ('friendship'), ('favourite_location'),
        ('achievement'), ('user_achievement'), ('hazard_category'), 
        ('hazard_report'), ('vote'), ('statistics'), ('trip'), 
        ('license_plate_info'), ('device'), ('device_ownership'),
        ('live_telemetry'), ('telemetry_history'), ('telemetry'),
        ('avatar'), ('background')
) AS expected(expected_table)
WHERE NOT EXISTS (
    SELECT 1 
    FROM information_schema.tables 
    WHERE table_schema = 'public' 
    AND table_name = expected.expected_table
)
ORDER BY expected_table;

-- ============================================
-- COUNT SUMMARY
-- ============================================

SELECT 
    COUNT(*) FILTER (WHERE table_name IN (
        'role', 'app_user', 'friendship', 'favourite_location',
        'achievement', 'user_achievement', 'hazard_category', 
        'hazard_report', 'vote', 'statistics', 'trip', 
        'license_plate_info'
    )) as core_tables_count,
    COUNT(*) FILTER (WHERE table_name IN (
        'device', 'device_ownership', 'live_telemetry', 
        'telemetry_history', 'telemetry'
    )) as device_telemetry_tables_count,
    COUNT(*) FILTER (WHERE table_name IN (
        'avatar', 'background'
    )) as asset_tables_count,
    COUNT(*) FILTER (WHERE table_name IN (
        'role', 'app_user', 'friendship', 'favourite_location',
        'achievement', 'user_achievement', 'hazard_category', 
        'hazard_report', 'vote', 'statistics', 'trip', 
        'license_plate_info', 'device', 'device_ownership',
        'live_telemetry', 'telemetry_history', 'telemetry',
        'avatar', 'background'
    )) as total_required_tables,
    19 as expected_total_tables,
    CASE 
        WHEN COUNT(*) FILTER (WHERE table_name IN (
            'role', 'app_user', 'friendship', 'favourite_location',
            'achievement', 'user_achievement', 'hazard_category', 
            'hazard_report', 'vote', 'statistics', 'trip', 
            'license_plate_info', 'device', 'device_ownership',
            'live_telemetry', 'telemetry_history', 'telemetry',
            'avatar', 'background'
        )) = 19 THEN '✅ ALL TABLES EXIST'
        ELSE '❌ SOME TABLES MISSING - RUN MIGRATION'
    END as status
FROM information_schema.tables 
WHERE table_schema = 'public';
