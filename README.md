
## 📌 URL Shortener Service

Trackable short links with click analytics.
Built using Spring Boot, JPA, and an in-memory H2 database.

You paste a long URL. This system converts it into a short code.
Anyone who opens that short link will be redirected to the original site, and the visit is counted.

---

## 🚀 Features Completed (Phase 1 + Phase 2)

| Feature                               | Status |
| ------------------------------------- | :----: |
| Shorten long URLs                     |    ✅   |
| Redirect short URLs to original links |    ✅   |
| Save URL details in database          |    ✅   |
| Track click count                     |    ✅   |
| Track last accessed time              |    ✅   |
| View analytics through API            |    ✅   |

More features coming soon: link expiry, QR codes, a dashboard, authentication…

---

## 🧱 Architecture: MVC Pattern

| Layer      | Class                   | What it does                        |
| ---------- | ----------------------- | ----------------------------------- |
| Model      | ShortUrl                | Represents the URL in the database  |
| Repository | ShortUrlRepository      | Talks to the database               |
| Service    | UrlShortenerService     | Creates unique short codes          |
| Controller | UrlShortenerController  | API endpoints                       |
| Main       | UrlShortenerApplication | Starts the app and scans components |

Spring Boot connects these pieces for us automatically.

---

## 🗂 Project Structure

```
src/main/java/com/swaraj/url_shortener
│
├── controller
│   └── UrlShortenerController.java
│
├── service
│   └── UrlShortenerService.java
│
├── repository
│   └── ShortUrlRepository.java
│
├── model
│   └── ShortUrl.java
│
└── UrlShortenerApplication.java
```

---

## 📝 API Guide

### 1) Shorten URL

POST: `http://localhost:8080/shorten`

Form Body:

```
longUrl=https://youtube.com
```

Response:

```
http://localhost:8080/MMvAkh0
```

---

### 2) Redirect to Original URL

GET: `http://localhost:8080/{code}`

Example:

```
GET http://localhost:8080/MMvAkh0
```

You will be redirected to YouTube.

---

### 3) View Analytics

GET: `http://localhost:8080/stats/{code}`

Example Response:

```json
{
  "id": 1,
  "longUrl": "https://youtube.com",
  "shortCode": "MMvAkh0",
  "clickCount": 3,
  "createdAt": "2025-11-24T15:56:13.236664",
  "lastAccessedAt": "2025-11-24T15:56:28.756118",
  "expiresAt": null
}
```

---

## 🧪 Database (Current Setup)

| DB           | Reason                        |
| ------------ | ----------------------------- |
| H2 in-memory | Quick testing and development |

We can switch to PostgreSQL or MySQL later when deploying.

---

## 🔍 Daily Progress Log

| Date                | What we did                                                                   |
| ------------------- | ----------------------------------------------------------------------------- |
| 24 Nov 2025         | Setup project, created Entity + Repository, POST API working                  |
| 24 Nov 2025 Evening | Click tracking + Analytics API. Errors solved: JPA bean injection + Optional. |

Phase 2 finished strong. 🔥

---

## 🏁 What’s Next? (Phase 3 Plan)

| Feature               | Why                    |
| --------------------- | ---------------------- |
| Expiry time for links | Disable outdated URLs  |
| QR code generation    | Easy sharing           |
| UI dashboard          | Better visualization   |
| Authentication        | Secure link management |
| Custom short codes    | Brand personalization  |

---

## 💪 What I've learned so far

✔ What MVC means in Spring
✔ How a real URL shortener works
✔ Working with JPA repositories
✔ Handling redirects
✔ Designing REST endpoints
✔ Saving and updating analytics

