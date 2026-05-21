-- Risk Rules initialization
INSERT INTO risk_rules (rule_name, rule_type, description, threshold_value, time_window_minutes, risk_score, enabled) VALUES
('Large Amount Transfer', 'AMOUNT_LIMIT', 'Flag transfers over 10000', 10000.00, NULL, 30, true),
('Very Large Amount', 'AMOUNT_LIMIT', 'High risk for transfers over 50000', 50000.00, NULL, 50, true),
('High Frequency', 'FREQUENCY_LIMIT', 'More than 10 transactions in 10 minutes', 10, 10, 40, true),
('Daily Limit Check', 'DAILY_LIMIT', 'Daily transaction limit exceeded', 100000.00, NULL, 60, true),
('New Account Restriction', 'NEW_ACCOUNT', 'New accounts limited to 5000 per transaction', 5000.00, NULL, 25, true);

-- Demo user (password: demo123)
INSERT INTO users (username, email, password, phone, real_name, verified, customer_no, created_at, updated_at) VALUES
('demo', 'demo@quickpay.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqzuH.VzB6nwYPCPQTCz8gF3rX7Gy', '13800138000', 'Demo User', true, NULL, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Demo account
INSERT INTO accounts (user_id, account_no, balance, frozen_amount, daily_limit, status, created_at, updated_at) VALUES
(1, 'QP1000000001DEMO', 10000.00, 0.00, 50000.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 新增演示用户（密码：password123）
INSERT INTO users (username, email, password, phone, real_name, verified, customer_no, created_at, updated_at) VALUES
('zhangwei', 'zhangwei@quickpay.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13900139001', '张伟', true, 'CUST001', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('lina', 'lina@quickpay.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13900139002', '李娜', true, 'CUST002', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
('wangqiang', 'wangqiang@quickpay.com', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', '13900139003', '王强', true, 'CUST003', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- 新增演示账户（user_id 2/3/4 对应张伟/李娜/王强）
INSERT INTO accounts (user_id, account_no, balance, frozen_amount, daily_limit, status, created_at, updated_at) VALUES
(2, 'QP1000000002ZHAW', 80000.00, 0.00, 50000.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(3, 'QP1000000003LINA', 45000.00, 0.00, 50000.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP),
(4, 'QP1000000004WANQ', 32000.00, 0.00, 50000.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
