
INSERT INTO napt.users (`created_date_time`,`updated_date_time`,`can_create`,`can_delete`,`can_edit`,`can_view`,`email`,`is_active`,`password`,`username`)
VALUES('2022-12-30 14:44:33','2022-12-30 00:00:00',b'1',b'1',b'1',b'1','admin@gmail.com',b'1','$2a$10$zNsexrBziALVDF3X06brPOVd4Vzo7Wau8Ac8xiHgXwChZ.3JRHxBK','admin');

Insert into napt.roles(`name`) values ('ROLE_ADMIN');

Insert INTO napt.user_role (`user_id`,`role_id`) values (1,1);

INSERT INTO napt.users (`created_date_time`,`updated_date_time`,`can_create`,`can_delete`,`can_edit`,`can_view`,`email`,`is_active`,`password`,`username`)
VALUES ('2022-12-30 14:44:33','2022-12-30 00:00:00',b'1',b'1',b'1',b'1','viewer@gmail.com',b'1','$2a$10$ypjhFvIJmoqMCF2QA5BxSebp9RQHOaNgUVYDKUzPV.ohE1XMoGrJK','viewer');

Insert into napt.roles(`name`) values ('ROLE_VIEWER');

Insert INTO napt.user_role (`user_id`,`role_id`) values (2,2);


INSERT INTO napt.users (`created_date_time`,`updated_date_time`,`can_create`,`can_delete`,`can_edit`,`can_view`,`email`,`is_active`,`password`,`username`)
VALUES (NOW(),NOW(),b'1',b'1',b'1',b'1','tapuser@nisum.com',b'1','$2a$10$RE8F8H/0kO4iOEjPgV8YsO0ml9F1ljf3R1rQSnEQI7LuIwLJ6vrPi','tapadmin');

Insert INTO napt.user_role (`user_id`,`role_id`) values (3,2);