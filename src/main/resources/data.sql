-- Risk Rules initialization
INSERT INTO risk_rules (rule_name, rule_type, description, threshold_value, time_window_minutes, risk_score, enabled) VALUES
('Large Amount Transfer', 'AMOUNT_LIMIT', 'Flag transfers over 10000', 10000.00, NULL, 30, true),
('Very Large Amount', 'AMOUNT_LIMIT', 'High risk for transfers over 50000', 50000.00, NULL, 50, true),
('High Frequency', 'FREQUENCY_LIMIT', 'More than 10 transactions in 10 minutes', 10, 10, 40, true),
('Daily Limit Check', 'DAILY_LIMIT', 'Daily transaction limit exceeded', 100000.00, NULL, 60, true),
('New Account Restriction', 'NEW_ACCOUNT', 'New accounts limited to 5000 per transaction', 5000.00, NULL, 25, true);

-- Demo user (password: demo123)
INSERT INTO users (username, email, password, phone, real_name, verified, created_at, updated_at) VALUES
('demo', 'demo@quickpay.com', '$2a$10$N9qo8uLOickgx2ZMRZoMy.MqrqzuH.VzB6nwYPCPQTCz8gF3rX7Gy', '13800138000', 'Demo User', true, CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);

-- Demo account
INSERT INTO accounts (user_id, account_no, balance, frozen_amount, daily_limit, status, created_at, updated_at) VALUES
(1, 'QP1000000001DEMO', 10000.00, 0.00, 50000.00, 'ACTIVE', CURRENT_TIMESTAMP, CURRENT_TIMESTAMP);
