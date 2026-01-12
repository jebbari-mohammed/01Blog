-- Fix username for user with email jebb@gmail.com
-- This updates the username to 'mojebbari' instead of the email

UPDATE _user 
SET username = 'mojebbari' 
WHERE email = 'jebb@gmail.com';

-- Verify the change
SELECT id, username, email, role FROM _user WHERE email = 'jebb@gmail.com';
