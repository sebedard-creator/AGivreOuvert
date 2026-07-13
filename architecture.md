# Architecture du Projet - À Givre Ouvert

Ce document décrit la structure, la stack technique et les choix d'architecture pour le projet de gestion d'inventaire de congélateur.

## 1. Stack Technique

### Backend (Python)
- **Framework :** FastAPI (rapide, typage statique, documentation OpenAPI automatique).
- **Base de données :** SQLite (léger, local, parfait pour un usage personnel).
- **ORM :** SQLAlchemy (ou SQLModel) pour la gestion des modèles de données.
- **Intégration Barcodes :** API Open Food Facts (gratuite, ouverte, spécialisée dans l'alimentation).
- **Recettes IA :** Intégration de l'API Google Gemini (`gemini-flash-lite-latest`) pour générer des recettes basées sur un échantillon aléatoire des 15 produits les plus anciens.

### Frontend (Android)
- **Langage :** Kotlin.
- **UI Framework :** Jetpack Compose (moderne, déclaratif).
- **Scanner :** ML Kit Barcode Scanning (Google) pour scanner via la caméra locale sans dépendances lourdes.
- **Réseau :** Retrofit ou Ktor pour interagir avec l'API FastAPI.

---

## 2. Structure du Projet

```text
CongeloInventaire/
├── backend/            # Service Python (FastAPI)
│   ├── app/
│   │   ├── database.py # Configuration SQLite/SQLAlchemy
│   │   ├── models.py   # Modèles de base de données
│   │   ├── schemas.py  # Schémas Pydantic
│   │   ├── main.py     # Points d'entrée de l'API
│   │   └── services/   # Services externes (ex: Open Food Facts API)
│   ├── requirements.txt
│   └── .env.example
├── android/            # Application Android (Kotlin / Jetpack Compose)
│   # Géré et compilé manuellement par l'utilisateur via Android Studio
├── architecture.md     # Ce fichier
├── changelog.md        # Journal des modifications
└── handoff.md          # État de la session et prochaines étapes
```

---

## 3. Modèle de Données & Logique Métier
- **Principe FIFO (First In, First Out)** : Chaque produit scanné est inséré comme une ligne unique (un "lot") dans la base de données.
- **Table `InventoryItem`** :
  - `id` : Identifiant unique du lot (Clé primaire).
  - `name` : Nom complet du produit.
  - `upc` : Code-barres (peut être null en cas d'ajout manuel sans code).
  - `date_added` : Date d'entrée dans le congélateur pour calculer la fraîcheur.
- **Table `KnownProduct` (Dictionnaire Local)** :
  - `upc` : Code-barres (Clé primaire).
  - `name` : Nom personnalisé donné par l'utilisateur (écrase Open Food Facts si modifié).

---

## 4. Topologie Réseau & Communication
- **Déploiement Local** : Le backend FastAPI doit écouter sur toutes les interfaces (`0.0.0.0`) pour être accessible par des appareils externes sur le réseau local.
- **Découverte de l'IP** : L'application Android aura besoin de l'adresse IP locale de l'ordinateur hôte (ex: `192.168.1.x`) configurée manuellement ou via les paramètres de l'application.
- **Sécurité (CORS)** : Contrairement aux applications web classiques, l'application native Android (Retrofit) n'est pas soumise aux politiques CORS du navigateur. Le middleware CORS côté FastAPI n'est donc **pas requis** pour que l'application Android communique avec le serveur.

---

## 5. Conventions
- Les variables d'environnement sensibles sont stockées dans `backend/.env` et listées dans `backend/.env.example`.
- Le fichier `.env` doit être exclu dans le `.gitignore` racine.
- Le backend doit être stable et testé avant d'entamer l'application mobile ("Backend en premier").
