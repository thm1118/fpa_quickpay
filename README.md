# QuickPay - 快捷支付演示平台

## 项目简介
QuickPay是一个快捷支付演示平台，模拟电子支付系统的核心功能，包括转账、交易管理、风险控制等。

## 技术栈
- Java 17
- Spring Boot 3.2.0
- Spring Data JPA
- Spring Security + JWT
- H2 Database
- Maven

## 快速启动
1. `cd quickpay`
2. `mvn clean install`
3. `mvn spring-boot:run`
4. 访问 http://localhost:8081/swagger-ui.html

## 主要功能
1. 用户管理：注册、登录、实名认证
2. 账户管理：余额查询、充值、提现
3. 转账支付：账户转账、扫码支付
4. 交易管理：交易记录、交易详情
5. 风险控制：异常检测、风险评分
6. 通知服务：交易通知、安全提醒

## 注意
这是一个演示项目，不对接真实的支付渠道。
