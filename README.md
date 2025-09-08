## 📱 Drug Interaction & Regional Alerts App (Android, Firebase)

point: learning Firebase Auth, Firestore, and Android UI with Material components.

so far user can:

-  register/login with email (with verification required) or Google account
- manage a personal drug list (saved in users/drugs)
- browse global drugs (drugs collection, read-only)
- check interactions between drugs and products (interactions collection, read-only)
- get regional alerts on products (products collection, read-only) with examples + GPS-based mapping
- edit personal profile (stored in users)

### logic behind flow:

1. user signs up (Firebase Auth)

2. after login, user can browse global drug DB (cannot edit)

3. user can add drugs to their personal list (editable)

4. app checks drug–food/product interactions & drug–region alerts

5. app shows risks with chips, lists, and grouped warnings

6. user can maintain their profile & medication schedule

### Setup requirements


Firestore (with collections: drugs, interactions, products)

Authentication (Email+Password, Google)

google-services.json must be placed in /app/ (not included in repo)


### 📂 Project structure

 🔻Main collections in Firestore (read-only for global data):

1. drugs → base info, synonyms, brand names

2. interactions → interactions between drugs & products

3. products → food/products with regional dishes & risk tags

🔻User-specific collections (editable):

1. users/{uid} → profile info

2. users/{uid}/drugs → personal drug list

🔻Android app features:

1. activities → login, register, drug search, profile, interactions

2. layout/ → XMLs (reusable cards, chip groups, etc.)

3. RegionMapper → maps GPS country → regional group (e.g., "USA", "Middle East")



### ✨ todo list

- Notifications/reminders for drug intake (user already enters times, needs WorkManager/AlarmManager impl)
- Drug-to-drug interaction checks (would require extra Firestore structure)
- Export/share user medication history
- Smarter search (Lucene/Algolia for typo tolerance & relevance ranking)
