# 🎬 Shortify-AI

> AI-Powered Video Highlight & Short Clip Generator built with Java Spring Boot + Python 🚀

Shortify-AI automatically processes long videos, detects engaging moments using AI, and generates short clips optimized for platforms like YouTube Shorts, Instagram Reels, and TikTok.

---

## ✨ Features

✅ Upload or process video URLs  
✅ AI-based highlight detection  
✅ Smart timestamp filtering  
✅ Automatic clip generation using FFmpeg  
✅ Asynchronous video processing  
✅ REST APIs with Spring Boot  
✅ Microservice-ready architecture  
✅ Scalable backend design  

---

## 🛠️ Tech Stack

### Backend
- Java 17
- Spring Boot
- Spring MVC
- REST APIs
- Async Processing

### AI & Processing
- Python
- FFmpeg
- Highlight Detection Logic

### Database
- MySQL / PostgreSQL

### Tools
- Maven
- Git & GitHub
- Postman

---

## 📂 Project Structure

```bash
shortify-ai/
│
├── src/
├── scripts/
├── downloads/
├── pom.xml
├── mvnw
└── README.md
```

---

## ⚡ Getting Started

### 1️⃣ Clone Repository

```bash
git clone https://github.com/Gouravsinghkushwah/shortify-ai.git
```

### 2️⃣ Navigate to Project

```bash
cd shortify-ai
```

### 3️⃣ Build Project

```bash
./mvnw clean install
```

### 4️⃣ Run Application

```bash
./mvnw spring-boot:run
```

---

## 🔥 API Endpoints

| Method | Endpoint | Description |
|--------|----------|-------------|
| POST | `/video/process` | Process Video |
| GET | `/job/{id}` | Get Job Status |
| POST | `/video/url` | Process Video URL |

---

## 🧠 How It Works

1. User uploads video or URL  
2. Backend downloads/processes video  
3. AI detects engaging highlights  
4. FFmpeg cuts clips automatically  
5. Generated shorts are returned  

---

## 🚀 Future Improvements

- [ ] AI Caption Generation  
- [ ] Multi-language Subtitle Support  
- [ ] Viral Score Prediction  
- [ ] Frontend Dashboard  
- [ ] Cloud Deployment  
- [ ] Kafka-based Event Processing  

---

## 📸 Demo

> Demo videos and screenshots coming soon...

---

## 🤝 Contributing

Contributions are welcome!

```bash
Fork → Create Branch → Commit → Push → Pull Request
```

---

## 👨‍💻 Author

### Gourav Singh Kushwah

Java Backend Developer | AI Enthusiast | Microservices Developer

📧 Email: your-email@example.com  
🔗 LinkedIn: Add Your LinkedIn  
🌐 GitHub: https://github.com/Gouravsinghkushwah

---

## ⭐ Support

If you like this project, give it a ⭐ on GitHub!

---

## 📜 License

This project is licensed under the MIT License.
