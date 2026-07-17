# Architecture — À Givre Ouvert

Ce document décrit l’architecture actuellement implémentée dans le dépôt.

## Vue d’ensemble

```text
Téléphone Android
  ├─ CameraX + ML Kit       lecture du code-barres
  ├─ Jetpack Compose        interface Material 3
  ├─ InventoryViewModel     état de l’application
  └─ Retrofit / OkHttp      HTTP local sur le port 8096
             │
             ▼
Backend FastAPI
  ├─ SQLModel / SQLite      inventaire et dictionnaire UPC
  ├─ HTTPX                  Open Food Facts
  └─ google-genai           recettes Gemini
```

Le backend constitue la source de vérité. Android conserve seulement une copie en lecture seule du dernier inventaire récupéré avec succès.

## Structure du dépôt

```text
AGivreOuvert/
├── android/
│   ├── app/src/main/java/com/example/congeloinventaire/
│   │   ├── MainActivity.kt
│   │   ├── network/ApiClient.kt
│   │   ├── theme/
│   │   │   ├── Color.kt
│   │   │   ├── Theme.kt
│   │   │   └── Type.kt
│   │   ├── ui/screens/
│   │   │   ├── HomeScreen.kt
│   │   │   ├── InventoryScreen.kt
│   │   │   ├── RecipesScreen.kt
│   │   │   └── ScannerScreen.kt
│   │   └── viewmodel/InventoryViewModel.kt
│   ├── app/build.gradle.kts
│   └── local.properties        # local et ignoré par Git
├── backend/
│   ├── app/
│   │   ├── database.py
│   │   ├── main.py
│   │   ├── models.py
│   │   └── services/
│   │       ├── open_food_facts.py
│   │       └── recipes.py
│   ├── requirements.txt
│   ├── .env.example
│   ├── .env                    # local et ignoré par Git
│   └── inventory.db            # local et ignoré par Git
├── README.md
├── architecture.md
├── changelog.md
└── handoff.md
```

## Backend

### Modèle de données

`InventoryItem` représente un lot individuel :

- `id` : clé primaire SQLite, générée par le serveur;
- `name` : nom affiché;
- `upc` : code-barres facultatif et indexé;
- `date_added` : date d’entrée utilisée pour le tri FIFO et l’âge.

`KnownProduct` constitue le dictionnaire local :

- `upc` : clé primaire;
- `name` : nom mémorisé ou corrigé par l’utilisateur.

### Routes HTTP

| Méthode | Route | Fonction |
|---|---|---|
| `GET` | `/api/inventory` | Retourner les lots triés par date |
| `POST` | `/api/inventory/add` | Ajouter un lot et mémoriser son UPC |
| `DELETE` | `/api/inventory/item/{id}` | Retirer un lot précis |
| `GET` | `/api/scanner/lookup/{upc}` | Chercher un produit et ses lots locaux |
| `GET` | `/api/recipes/suggestions` | Générer les suggestions Gemini |

### Identification d’un code-barres

Le backend recherche successivement :

1. les lots présents dans l’inventaire;
2. le nom enregistré dans `KnownProduct`;
3. Open Food Facts si le produit n’est pas connu localement.

### Génération de recettes

1. Les 15 lots les plus anciens sont lus dans SQLite.
2. S’il y en a plus de 10, le backend en sélectionne 10 aléatoirement.
3. Les noms et dates sont transmis à `gemini-flash-lite-latest`.
4. Gemini doit retourner une liste JSON contenant `title`, `target`, `freezer`, `fresh` et `steps`.
5. En cas de clé absente ou d’erreur Gemini, le backend retourne une carte explicative compatible avec le même format.

## Android

### État et cycle des données

`InventoryViewModel` est partagé par les écrans et expose des `StateFlow` pour :

- l’inventaire;
- les recettes;
- l’état de chargement;
- le résultat du scanner;
- l’état de connexion au serveur;
- les erreurs propres aux recettes.

Lorsqu’un chargement d’inventaire réussit, la réponse JSON est enregistrée dans `SharedPreferences`. Si le serveur devient inaccessible, cette copie est affichée en lecture seule et les opérations de modification sont bloquées.

### Réseau

L’URL de base est construite avec `BACKEND_IP` provenant de `android/local.properties` :

```text
http://<BACKEND_IP>:8096/
```

Le délai de connexion est de 2 secondes afin de détecter rapidement un serveur absent. Le délai de lecture est de 60 secondes afin de laisser le temps à Gemini de générer les recettes.

L’application Android native n’est pas soumise aux règles CORS des navigateurs. Le manifeste autorise le trafic HTTP en clair, car le déploiement actuel vise un réseau local de confiance.

### Interface

- `MainActivity` héberge le graphe de navigation et la barre inférieure.
- `HomeScreen` affiche le résumé d’âge et les actions principales.
- `InventoryScreen` présente les lots FIFO et leur statut.
- `RecipesScreen` affiche les aperçus, les ingrédients manquants et les détails repliables.
- `ScannerScreen` orchestre CameraX, ML Kit et la feuille de résultat.
- `theme/` centralise les rôles de couleurs, la typographie et les formes Material 3.

Les couleurs dynamiques Android sont désactivées afin de conserver une identité visuelle stable. Les rôles sémantiques du thème prennent en charge les modes clair et sombre.

## Limites actuelles

- L’adresse IP du backend doit être configurée manuellement.
- Le backend ne possède ni authentification ni chiffrement TLS; il doit rester sur un réseau local de confiance.
- Le mode hors ligne ne permet pas les modifications.
- La génération de recettes dépend d’Internet, d’une clé Gemini valide et du service externe.
- La réponse Gemini est validée par son analyse JSON, mais aucun schéma métier strict n’est appliqué au contenu généré.
- Aucun test automatisé n’est actuellement conservé dans le dépôt.
