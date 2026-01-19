-- Update the notifications_type_check constraint to include REPORT_REVIEWED
ALTER TABLE notifications DROP CONSTRAINT IF EXISTS notifications_type_check;

ALTER TABLE notifications ADD CONSTRAINT notifications_type_check 
CHECK (type IN ('FOLLOW', 'LIKE', 'COMMENT', 'NEW_POST', 'REPORT_REVIEWED'));
