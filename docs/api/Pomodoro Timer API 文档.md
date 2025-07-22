# Pomodoro Timer API 文档

## 基本信息
- **Base URL**: `http://localhost:8080/api/pomodoro`
- **Content-Type**: `application/json`
- **支持的HTTP方法**: GET, POST, DELETE
- **版本**: v1.0
- **最后更新**: 2025-07-22

---

## 🎯 API 接口列表

### 1. 创建计时器
**POST** `/create`

创建一个新的番茄钟计时器会话。

#### 请求参数
```json
{
  "sessionId": "string",
  "timerMode": "CONTINUOUS"
}
```

#### TimerMode 枚举值
- `CONTINUOUS` - 连续模式

#### 请求示例
```bash
curl -X POST http://localhost:8080/api/pomodoro/create \
  -H "Content-Type: application/json" \
  -d '{
    "sessionId": "user123",
    "timerMode": "CONTINUOUS"
  }'
```

#### 响应示例
```json
{
  "success": true,
  "message": "计时器创建成功",
  "sessionId": "user123",
  "timerMode": "CONTINUOUS"
}
```

#### 错误响应
```json
{
  "success": false,
  "error": "会话ID不能为空",
  "timestamp": 1690012345678
}
```

---

### 2. 启动计时器
**POST** `/{sessionId}/start`

启动指定会话的计时器。

#### 路径参数
- `sessionId` (string) - 会话ID

#### 请求示例
```bash
curl -X POST http://localhost:8080/api/pomodoro/user123/start
```

#### 响应示例
```json
{
  "success": true,
  "message": "计时器已启动",
  "sessionId": "user123",
  "currentState": "WORK",
  "remainingTime": 1500
}
```

---

### 3. 暂停计时器
**POST** `/{sessionId}/pause`

暂停指定会话的计时器。

#### 路径参数
- `sessionId` (string) - 会话ID

#### 请求示例
```bash
curl -X POST http://localhost:8080/api/pomodoro/user123/pause
```

#### 响应示例
```json
{
  "success": true,
  "message": "计时器已暂停",
  "sessionId": "user123",
  "currentState": "WORK",
  "remainingTime": 1200
}
```

---

### 4. 重置计时器
**POST** `/{sessionId}/reset`

重置指定会话的计时器到初始状态。

#### 路径参数
- `sessionId` (string) - 会话ID

#### 请求示例
```bash
curl -X POST http://localhost:8080/api/pomodoro/user123/reset
```

#### 响应示例
```json
{
  "success": true,
  "message": "计时器已重置",
  "sessionId": "user123",
  "currentState": "WORK",
  "remainingTime": 1500
}
```

---

### 5. 删除计时器
**DELETE** `/{sessionId}`

删除指定的计时器会话。

#### 路径参数
- `sessionId` (string) - 会话ID

#### 请求示例
```bash
curl -X DELETE http://localhost:8080/api/pomodoro/user123
```

#### 响应示例
```json
{
  "success": true,
  "message": "计时器已删除",
  "sessionId": "user123"
}
```

---

### 6. 获取计时器状态
**GET** `/{sessionId}/status`

获取指定会话的计时器当前状态。

#### 路径参数
- `sessionId` (string) - 会话ID

#### 请求示例
```bash
curl -X GET http://localhost:8080/api/pomodoro/user123/status
```

#### 响应示例
```json
{
  "success": true,
  "sessionId": "user123",
  "timerMode": "CONTINUOUS",
  "currentState": "WORK",
  "remainingTime": 1200,
  "workTime": 1500,
  "isRunning": true,
  "shortBreakDuration": 300,
  "longBreakDuration": 900,
  "shortBreakTimes": [300, 600, 900],
  "nextShortBreakIndex": 0
}
```

---

### 7. 获取计时器详细信息
**GET** `/{sessionId}/info`

获取指定会话的计时器详细信息，包含格式化时间和进度百分比。

#### 路径参数
- `sessionId` (string) - 会话ID

#### 请求示例
```bash
curl -X GET http://localhost:8080/api/pomodoro/user123/info
```

#### 响应示例
```json
{
  "success": true,
  "sessionId": "user123",
  "timerMode": "CONTINUOUS",
  "currentState": "WORK",
  "remainingTime": 1200,
  "remainingTimeFormatted": "20:00",
  "workTime": 1500,
  "workTimeFormatted": "25:00",
  "isRunning": true,
  "shortBreakDuration": 300,
  "shortBreakDurationFormatted": "05:00",
  "longBreakDuration": 900,
  "longBreakDurationFormatted": "15:00",
  "progressPercentage": 20.0,
  "shortBreakTimes": [300, 600, 900],
  "nextShortBreakIndex": 0,
  "nextShortBreakTime": 300,
  "nextShortBreakTimeFormatted": "05:00"
}
```

---

### 8. 健康检查
**GET** `/health`

检查服务是否正常运行。

#### 请求示例
```bash
curl -X GET http://localhost:8080/api/pomodoro/health
```

#### 响应示例
```json
{
  "success": true,
  "message": "Timer service is running",
  "timestamp": 1690012345678
}
```

---

## 📊 数据模型

### TimerState 枚举
```
WORK          - 工作状态
SHORT_BREAK   - 短休息状态  
LONG_BREAK    - 长休息状态
PAUSED        - 暂停状态
```

### TimerMode 枚举
```
CONTINUOUS    - 连续模式
```

### PomodoroSession 对象
```
{
  "sessionId": "string",              // 会话ID
  "timerMode": "CONTINUOUS",          // 计时器模式
  "currentState": "WORK",             // 当前状态
  "remainingTime": 1500,              // 剩余时间（秒）
  "workTime": 1500,                   // 工作时间（秒）
  "isRunning": true,                  // 是否正在运行
  "shortBreakDuration": 300,          // 短休息时长（秒）
  "longBreakDuration": 900,           // 长休息时长（秒）
  "shortBreakTimes": [300, 600, 900], // 短休息时间点数组
  "nextShortBreakIndex": 0            // 下一个短休息索引
}
```

---

## ❌ 错误码说明

| HTTP状态码 | 说明 | 示例场景 |
|-----------|------|----------|
| 200 | 成功 | 操作成功完成 |
| 400 | 请求错误 | 参数缺失或格式错误 |
| 404 | 资源不存在 | 指定的sessionId不存在 |
| 405 | 方法不允许 | 使用了不支持的HTTP方法 |
| 500 | 服务器内部错误 | 服务器处理异常 |

### 通用错误响应格式
```json
{
  "success": false,
  "error": "错误描述信息",
  "timestamp": 1690012345678
}
```

### 常见错误信息
- `"会话ID不能为空"` - sessionId参数缺失或为空
- `"计时器模式不能为空"` - timerMode参数缺失
- `"指定的会话不存在"` - 使用了不存在的sessionId
- `"计时器已经在运行"` - 尝试启动已运行的计时器
- `"计时器未运行"` - 尝试暂停未运行的计时器

---

## 📝 使用说明

1. **创建会话**: 首先使用 `POST /create` 创建一个新的计时器会话
2. **启动计时器**: 使用 `POST /{sessionId}/start` 启动计时器
3. **监控状态**: 使用 `GET /{sessionId}/status` 或 `GET /{sessionId}/info` 获取当前状态
4. **控制计时器**: 使用 `POST /{sessionId}/pause` 暂停或 `POST /{sessionId}/reset` 重置
5. **清理资源**: 使用 `DELETE /{sessionId}` 删除不需要的会话

## 🔧 开发建议

- 建议在客户端实现定时轮询状态接口来更新UI
- 使用 `/info` 接口获取格式化的时间显示
- 合理处理网络异常和服务器错误
- 在应用关闭时记得删除不需要的会话以释放服务器资源