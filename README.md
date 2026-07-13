# ❄️ À Givre Ouvert

**À Givre Ouvert** est une application Android moderne doublée d'un backend en Python, conçue pour réinventer la gestion de votre congélateur. Dites adieu au gaspillage alimentaire et bonjour aux recettes créatives générées par intelligence artificielle !

---

## ✨ Fonctionnalités Principales

### 📸 Scanner Intelligent Intégré
- **Lecture de code-barres hors-ligne** grâce à la technologie Google ML Kit (rapide, sans latence).
- Interrogation de la gigantesque base de données publique **Open Food Facts** pour identifier instantanément les produits scannés.
- **Mémorisation intelligente** : Si vous scannez un produit inconnu et lui donnez un nom manuellement, l'application le retient dans son dictionnaire local pour les prochaines fois.

### 📦 Gestion d'Inventaire FIFO (First In, First Out)
- Les produits sont gérés sous forme de **lots uniques**.
- Code couleur intuitif basé sur l'âge du produit pour identifier d'un coup d'œil ce qui doit être consommé :
  - 🟢 **Vert** : Moins de 30 jours
  - 🟡 **Jaune** : Entre 30 et 90 jours
  - 🔴 **Rouge** : Plus de 90 jours

### 🧑‍🍳 Générateur de Recettes Anti-Gaspillage (IA)
- Un bouton magique qui analyse l'état de votre congélateur.
- Le backend sélectionne les **15 produits les plus anciens** et en tire **10 au hasard** pour éviter la répétition.
- Ces ingrédients sont envoyés à **Google Gemini (flash-lite)** qui se transforme en véritable chef cuisinier pour vous proposer 10 idées de recettes originales, rapides, et spécifiquement conçues pour écouler vos vieux stocks de façon délicieuse.

---

## 🛠️ Stack Technique

### Frontend (Application Mobile)
- **Android** natif développé en **Kotlin**.
- Interface utilisateur 100% **Jetpack Compose** (UI moderne, zébrages alternés, design ergonomique).
- Communication HTTP asynchrone via **Retrofit**.
- Architecture robuste **MVVM** (Model-View-ViewModel) gérant les flux d'états réactifs (StateFlow).

### Backend (Serveur Maison)
- **Python** propulsé par **FastAPI** (performant et asynchrone).
- Base de données locale **SQLite** orchestrée par l'ORM **SQLModel**.
- Requêtes HTTP vers l'extérieur (Open Food Facts & Gemini) gérées avec **HTTPX**.

---

## 🚀 Installation & Démarrage rapide

1. **Backend** : 
   - Créez et activez l'environnement virtuel Python.
   - Installez les dépendances : `pip install -r requirements.txt`.
   - Lancez le serveur : `uvicorn app.main:app --host 0.0.0.0 --port 8096`.
2. **Configuration** :
   - Ajoutez votre clé d'API Google Gemini dans le fichier `backend/.env` (variable `GEMINI_API_KEY`).
3. **Android** :
   - Ouvrez le projet dans Android Studio.
   - Modifiez le fichier `local.properties` pour pointer vers l'adresse IP locale de votre ordinateur (ex: `BACKEND_IP="192.168.1.XX"`).
   - Compilez et lancez sur votre smartphone physique !

---

*Conçu par Sébastien Bédard*
