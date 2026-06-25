
## Team Roles & Responsibilities

| Role | Person | Works in | Responsibilities |
|------|--------|----------|------------------|
| Backend Lead | Emmanuel Narh | `api-gateway/`, `src/` | Project setup, API Gateway, code review |
|Admin Service| Emmanuel Narh/ Nana Kwadwo|adminservice/ | User management, audit logs, dashboard stats, system health
| Auth Service | Emmanuel Narh | `auth-service/` | Login, Register, JWT, User profiles |
| News Service | Emmanuel Narh | `news-service/` | Health ticker, external news API |
| Media Service | Member 4 | `media-service/` | Video upload, streaming, storage |
| Translate Service | Member 5 | `translate-service/` | Text translation, sign language mapping |
|Documentation| Emmanuel Narh|`docs/`| documentaion|

---

## Git Workflow

### Daily Routine

```bash
# Start of day - get latest changes from everyone
git pull origin main

# Create a branch for your feature
git checkout -b feature/your-feature-name

# Work on your code...
# Commit your changes
git add .
git commit -m "Describe what you changed"

# Push to GitHub
git push origin feature/your-feature-name

# Create a Pull Request on GitHub
# After review, merge to main