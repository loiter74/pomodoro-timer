# 🍅 Pomodoro Timer - 番茄钟

一个现代化的番茄工作法计时器，采用React前端 + Spring Boot后端架构，帮助您提高专注力和工作效率。

## ✨ 特性

- 🎯 **专注工作** - 25分钟专注时间，5分钟短休息
- 🔄 **自动循环** - 支持连续模式，自动切换工作和休息状态
- 🎨 **美观界面** - 现代化UI设计，番茄造型的可交互计时器
- ⚡ **实时同步** - 前后端实时状态同步
- 📱 **响应式设计** - 支持桌面和移动设备
- 🎵 **音效提醒** - 工作和休息切换时的音效提示（可扩展）

## 🚀 快速开始

### 环境要求

- Node.js 16+
- Java 8+
- Maven 3.6+

### 安装和运行

#### 1. 克隆项目
```bash
git clone <https://github.com/loiter74/pomodorp.git>
cd pomodoro-timer
```

#### 2. 启动后端服务
```bash
# 进入后端目录
cd backend

# 启动Spring Boot应用
mvn spring-boot:run

# 或者使用jar包运行
mvn clean package
java -jar target/pomodoro-timer-backend.jar
```

后端服务将运行在 `http://localhost:8080`

#### 3. 启动前端应用
```bash
# 进入前端目录
cd frontend

# 安装依赖
npm install

# 启动开发服务器
npm start
```

前端应用将运行在 `http://localhost:3000`

## 📁 项目结构

```
pomodoro-timer/
├── frontend/                 # React前端
│   ├── public/
│   └── src/
│       ├── assets/          # 静态资源
│       │   ├── backgroundImages/
│       │   ├── pomodoroImages/
│       │   └── sounds/
│       ├── components/      # React组件
│       ├── hooks/          # 自定义Hooks
│       ├── services/       # API服务
│       └── utils/          # 工具函数
├── backend/                 # Spring Boot后端
│   └── src/main/java/
└── docs/                   # 项目文档
    └── API文档.md
```

## 🎮 使用说明

### 基本操作

1. **开始计时** - 点击番茄图标或"开始"按钮
2. **暂停计时** - 点击番茄图标或"暂停"按钮
3. **重置计时** - 点击"重置"按钮回到初始状态

### 工作流程

1. 🍅 **工作阶段** (3-5分钟) - 专注完成任务
2. ☕ **短休息** (10秒) - 放松休息
3. 🔄 **循环重复** -(90分钟) 自动进入下一个工作周期
4. 🛌 **长休息** (20分钟) - 每完成几个周期后的长休息

## 🔧 技术栈

### 前端
- **React 18** - 用户界面框架
- **Styled Components** - CSS-in-JS样式解决方案
- **Ant Design** - UI组件库
- **Custom Hooks** - 状态管理和业务逻辑

### 后端
- **Spring Boot** - Java后端框架
- **RESTful API** - 接口设计规范
- **内存存储** - 会话状态管理

## 📚 API文档

详细的API文档请参考：[Pomodoro Timer API 文档](./docs/API文档.md)

### 主要接口

| 方法 | 路径 | 描述 |
|------|------|------|
| POST | `/api/pomodoro/create` | 创建计时器会话 |
| POST | `/api/pomodoro/{sessionId}/start` | 启动计时器 |
| POST | `/api/pomodoro/{sessionId}/pause` | 暂停计时器 |
| POST | `/api/pomodoro/{sessionId}/reset` | 重置计时器 |
| GET | `/api/pomodoro/{sessionId}/info` | 获取计时器详细信息 |
| DELETE | `/api/pomodoro/{sessionId}` | 删除计时器会话 |

## 🛠️ 开发指南

### 本地开发

1. **环境配置**
   ```bash
   # 前端环境变量
   echo "REACT_APP_API_BASE_URL=http://localhost:8080" > frontend/.env
   ```

2. **代码规范**
    - 使用ESLint进行代码检查
    - 遵循React Hooks最佳实践
    - 组件采用函数式编程

3. **调试技巧**
    - 使用React Developer Tools
    - 后端日志查看：`tail -f logs/application.log`

### 自定义配置

#### 修改计时时长
在后端代码中修改默认时长：
```java
    // 连续工作时间（单位：秒），这里设置为90分钟
    private int continuousWorkTime = 90 * 60;
    
    // 短休息持续时间（单位：秒），这里设置为10秒
    private int continuousShortBreakDuration = 10;
    
    // 长休息持续时间（单位：秒），这里设置为20分钟
    private int continuousLongBreakDuration = 20 * 60;
    
    // 连续短休息的最小间隔时间（单位：秒），这里设置为3分钟
    private int continuousShortBreakMinInterval = 3 * 60;
    
    // 连续短休息的最大间隔时间（单位：秒），这里设置为5分钟
    private int continuousShortBreakMaxInterval = 5 * 60;
```

#### 添加音效
将音频文件放入 `frontend/src/assets/sounds/` 目录，并在组件中引用。

## 📱 移动端适配

项目支持响应式设计，可在移动设备上正常使用。如需开发原生App或小程序版本，请参考：

- **React Native** - 移动App开发
- **Taro框架** - 微信小程序开发
- **Expo** - 快速原型开发

## 🤝 贡献指南

1. Fork 项目
2. 创建特性分支 (`git checkout -b feature/AmazingFeature`)
3. 提交更改 (`git commit -m 'Add some AmazingFeature'`)
4. 推送到分支 (`git push origin feature/AmazingFeature`)
5. 打开 Pull Request

## 📄 许可证

本项目采用 MIT 许可证 - 查看 [LICENSE](LICENSE) 文件了解详情

## 🙏 致谢

- 感谢 [Francesco Cirillo](https://francescocirillo.com/pages/pomodoro-technique) 发明的番茄工作法
- UI设计灵感来源于现代化的番茄钟应用
- 感谢开源社区提供的优秀工具和库

## 📞 联系方式

如有问题或建议，请通过以下方式联系：

- 📧 Email: 302626787@qq.com
---

⭐ 如果这个项目对您有帮助，请给个Star支持一下！

**专注每一刻，成就更好的自己** 🍅✨