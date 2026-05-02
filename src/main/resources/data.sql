-- Use INSERT IGNORE to avoid duplicate key errors on restart
INSERT IGNORE INTO role (role_name) VALUES ('ROLE_USER');
INSERT IGNORE INTO role (role_name) VALUES ('ROLE_ADMIN');

-- BCrypt hash for "123456"
INSERT IGNORE INTO user (username, password, email, enabled)
VALUES ('admin', '$2a$10$joRMgZrWyaPDi41RmssanOCNXYHK.M09ZV1cyoGIaSPFgWiB26Qva', 'admin@petmgt.com', 1);
INSERT IGNORE INTO user (username, password, email, enabled)
VALUES ('user', '$2a$10$joRMgZrWyaPDi41RmssanOCNXYHK.M09ZV1cyoGIaSPFgWiB26Qva', 'user@petmgt.com', 1);

INSERT IGNORE INTO user_role (user_id, role_id) VALUES (1, 2);
INSERT IGNORE INTO user_role (user_id, role_id) VALUES (2, 1);

INSERT IGNORE INTO pet_breed (breed_name, pet_type) VALUES
('British Shorthair', '猫'),
('Persian', '猫'),
('Siamese', '猫'),
('Maine Coon', '猫'),
('Ragdoll', '猫'),
('Golden Retriever', '狗'),
('Corgi', '狗'),
('Husky', '狗'),
('Labrador', '狗'),
('Poodle', '狗'),
('Holland Lop', '兔'),
('Netherland Dwarf', '兔'),
('Mini Rex', '兔');

INSERT IGNORE INTO pet (name, breed_id, gender, age, weight, health_status, vaccine_status, sterilization_status, personality, adoption_requirement, status, created_by)
VALUES
('橘橘', 1, '公', 12, 4.5, '健康', '已接种', '已绝育', '温顺亲人，喜欢晒太阳，已会用猫砂', '有稳定住所，同意定期回访', 'available', 1),
('雪球', 2, '母', 8, 3.2, '健康', '已接种', '未绝育', '安静优雅，喜欢被抚摸，适合安静家庭', '室内饲养，不笼养', 'available', 1),
('小暹', 3, '公', 6, 3.0, '健康', '已接种', '已绝育', '活泼好动，叫声洪亮，非常粘人', '有时间陪伴，能接受猫咪话多', 'available', 1),
('大毛', 4, '公', 24, 7.5, '健康', '已接种', '已绝育', '体型大但性格温柔，喜欢玩水', '空间足够大，有养猫经验优先', 'available', 1),
('布布', 5, '母', 10, 4.0, '轻微疾病', '部分接种', '已绝育', '布偶猫典型性格，安静粘人，需要定期梳毛', '有耐心打理长毛猫', 'available', 1),
('大黄', 6, '公', 18, 30.0, '健康', '已接种', '已绝育', '金毛典型暖男性格，喜欢捡球，对小孩友善', '每天至少遛两次，有足够活动空间', 'available', 1),
('短腿', 7, '公', 14, 12.0, '健康', '已接种', '未绝育', '精力充沛，喜欢跑跳，柯基标志性微笑', '注意控制体重，避免爬楼梯伤脊椎', 'available', 1),
('二哈', 8, '母', 16, 22.0, '健康', '已接种', '已绝育', '活泼好动，表情丰富，需要大量运动', '有耐心，能接受拆家风险，最好有院子', 'available', 1),
('拉布', 9, '公', 20, 28.0, '健康', '已接种', '已绝育', '温顺忠诚，智商高，适合做导盲犬或家庭犬', '能保证每天充足运动量', 'adopted', 1),
('卷卷', 10, '母', 9, 6.0, '健康', '已接种', '未绝育', '聪明机灵，不易掉毛，适合过敏体质', '定期美容护理，有养贵宾经验优先', 'available', 1),
('团子', 11, '公', 5, 1.5, '健康', '未接种', '未绝育', '可爱温顺，喜欢被抱着，会用兔厕所', '提供足够干草和活动空间', 'available', 1),
('小黑', 12, '母', 4, 1.0, '健康', '未接种', '未绝育', '体型小巧，活泼好动，适合小空间饲养', '注意保暖，笼子不能太小', 'available', 1),
('绒绒', 13, '母', 7, 2.0, '轻微疾病', '未接种', '未绝育', '毛发柔软如丝绒，性格温顺安静', '有养兔经验，注意饮食管理', 'available', 1);
