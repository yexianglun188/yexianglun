-- ============================================================
-- 校园二手交易平台 - 初始化数据
-- 密码统一为 123456（BCrypt 加密后的值）
-- ============================================================

USE campus_market;

-- 用户（admin / 123456，普通用户 zhangsan / 123456）
INSERT INTO `user` (`username`, `password`, `nickname`, `phone`, `student_no`, `campus`, `role`)
VALUES ('admin', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '平台管理员', '13800000000', 'ADMIN001', '中心校区', 1),
       ('zhangsan', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '张三', '13800000001', '20230101', '东校区', 0),
       ('lisi', '$2a$10$N.zmdr9k7uOCQb376NoUnuTJ8iAt6Z5EHsM8lE9lBOsl7iKTVKIUi', '李四', '13800000002', '20230102', '西校区', 0);

-- 分类
INSERT INTO `category` (`name`, `icon`, `sort`)
VALUES ('教材书籍', 'book', 1),
       ('数码电子', 'phone', 2),
       ('生活用品', 'home', 3),
       ('运动户外', 'sport', 4),
       ('服饰鞋包', 'cloth', 5),
       ('其他', 'more', 6);

-- 商品
INSERT INTO `product` (`user_id`, `category_id`, `title`, `description`, `price`, `original_price`, `item_condition`, `trade_place`, `status`, `view_count`)
VALUES (2, 1, '《数据结构与算法分析》Java语言描述', '上学期课程用书，无笔记无划线，保存完好。考研党也可用。', 25.00, 59.00, '几乎全新', '图书馆一楼大厅', 1, 32),
       (2, 2, '小米蓝牙耳机 Air2 SE', '买了不到三个月，换了新耳机所以出。功能正常，配充电盒。', 45.00, 129.00, '轻微使用', '东校区食堂门口', 1, 58),
       (3, 3, '宿舍小台灯 LED 护眼款', '毕业清宿舍，台灯完好，三档亮度可调，带USB充电口。', 15.00, 39.00, '轻微使用', '西校区宿舍楼下', 1, 21),
       (3, 1, '英语四级真题集（近五年）', '做题痕迹较少，答案完整，适合备考。', 12.00, 35.00, '几乎全新', '教学楼B座', 1, 17),
       (2, 4, '篮球 斯伯丁标准7号', '打了一学期，气密性好，外观有磨损不影响使用。', 55.00, 158.00, '明显使用', '东校区篮球场', 1, 44);
