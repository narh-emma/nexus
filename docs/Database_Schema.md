# Database Schema

## Auth Database (`nexus_auth_db`)

### Table: `users`

| Column | Type | Description |
|--------|------|-------------|
| id | UUID | Primary key |
| email | VARCHAR(255) | User's email (unique) |
| full_name | VARCHAR(255) | User's full name |
| index_number | VARCHAR(50) | Student/staff ID |
| password_hash | VARCHAR(255) | BCrypt encrypted password |
| preferred_language | VARCHAR(10) | Default 'en' |
| sign_dialect | VARCHAR(20) | Default 'ASL' |
| created_at | TIMESTAMP | Account creation time |
| updated_at | TIMESTAMP | Last update time |

## News Database (`nexus_news_db`) - Coming Soon

### Table: `health_ticker`

| Column | Type | Description |
|--------|------|-------------|
| entry_id | UUID | Primary key |
| headline | VARCHAR(300) | News title |
| summary | TEXT | News content |
| source | VARCHAR(120) | Source (WHO, CDC, etc.) |
| priority | INTEGER | 0=normal, 1=alert, 2=critical |

### Table: `audit_log`
Column|	Type|	Description
id|	VARCHAR(255)|	Primary key
actor_id|	VARCHAR(255)|	User ID who performed the action
action|	VARCHAR(255)|	Action performed (LOGIN, DELETE, UPDATE, etc.)
target_id|	VARCHAR(255)|	Target entity ID (optional)
details	TEXT|	Additional details about the action
created_at|	TIMESTAMP	When the action was performed

### Table: video_vault
Column|	Type|	Description|
id|	UUID|	Primary key|
title|	VARCHAR(255)|	Video title (not null)|
description|	TEXT|	Video description|
category|	VARCHAR(50)|	Video category (not null)|
video_url|	VARCHAR(500)|	URL to the video (not null)|
thumbnail_url|	VARCHAR(500)|	URL to thumbnail image|
duration|	INTEGER	Video duration in seconds|
is_offline_available|	BOOLEAN|	Whether video is available offline|
view_count|	BIGINT|	Number of views
created_at|	TIMESTAMP|	When the video was uploaded|
updated_at|	TIMESTAMP|	Last update time|

### Table: video_tags
Column|	Type|	Description|
video_id|	UUID|	Foreign key to video_vault|
tag|	VARCHAR(50)|	Tag name (not null)|

### Foreign Key:
video_id references video_vault(id) on DELETE CASCADE