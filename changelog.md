# Changelog - CongeloInventaire

Toutes les modifications techniques majeures apportées au projet seront répertoriées dans ce fichier.

## [2026-07-13] - Questionnaire et Plan d'implémentation finalisé
- **Analyse UI & Grill-me :** Séance de clarification `/grill-me` menant aux choix d'architecture définitifs (uniquement champs essentiels de BDD, gestion de lots individuels FIFO, mock du service de recettes prêt pour Claude/autre, et serveur écoutant sur 0.0.0.0 pour tests sur téléphone physique).
- **Revue Architecture (Gemini 3.1 Pro) :** Enrichissement du document [architecture.md](file:///y:/CongeloInventaire/architecture.md) avec le modèle de base de données explicite (FIFO), la topologie réseau, et la correction technique sur l'absence de besoin de CORS pour Android.
- **Planification :** Mise à jour et finalisation de [implementation_plan.md](file:///C:/Users/sebed/.gemini/antigravity/brain/b20914eb-db0a-4918-b797-73f232a9950c/implementation_plan.md).

## [2026-07-13] - Implémentation du Backend
- **Développement :** Création du backend Python (FastAPI + SQLModel).
- **Fonctionnalités :** 
  - Base de données SQLite pour l'inventaire avec tri FIFO natif.
  - Intégration de l'API Open Food Facts pour le scan (`httpx`).
  - Implémentation du mock des recommandations de recettes IA.
- **Tests :** Rédaction et validation des tests unitaires (`pytest`) pour les flux métier.

## [2026-07-13] - Implémentation du Frontend Android
- **Projet** : Génération de l'application "Empty Compose Activity" via l'outil en ligne de commande.
- **Réseau** : Intégration de `local.properties` (clé `BACKEND_IP`) avec Retrofit pour faire le pont avec le backend FastAPI.
- **Scanner** : Implémentation de `CameraX` et `ML Kit Barcode Scanning` pour lire les codes UPC hors-ligne.
- **Interface** : Reproduction de la maquette HTML IceTrack via Jetpack Compose (`HomeScreen`, `InventoryScreen`, `ScannerScreen`, `RecipesScreen`), et gestion de l'état via `ViewModel` (StateFlow).

## [2026-07-13] - Refactoring : Suppression de la notion de Catégorie
- **Architecture** : Suppression complète du champ "catégorie" du modèle de données de la base SQLite et de l'API FastAPI pour alléger le système.
- **Service** : L'intégration Open Food Facts n'extrait et ne traduit plus l'arbre des catégories.
- **Android** : Nettoyage de l'interface utilisateur (couleurs des textes adaptées en noir, suppression des champs de saisie manuelle de catégorie dans le scanner, etc.) et mise à jour des Data Classes Retrofit.

## [2026-07-13] - Feature : Mémorisation des produits inconnus
- **Backend (DB)** : Ajout de la table `KnownProduct` servant de dictionnaire local des codes UPC.
- **Backend (API)** : Lorsqu'un produit non reconnu par Open Food Facts est ajouté manuellement via `POST /api/inventory/add`, il est inséré dans le dictionnaire. Lors des futurs scans (`GET /api/scanner/lookup/{upc}`), le système consulte ce dictionnaire en priorité pour retrouver automatiquement le nom du produit, même si l'inventaire est vide.

## [2026-07-13] - Feature : IA Gemini & Polissage Final de l'Interface
- **IA (Gemini)** : Remplacement du mock par une vraie intégration API de Google Gemini (`gemini-flash-lite-latest`).
- **Logique Anti-Gaspillage** : Le backend sélectionne désormais 15 vieux produits, en pioche 10 au hasard, et les envoie à l'IA pour générer 10 idées de repas créatives en évitant le gaspillage.
- **UI (Android)** :
  - L'application est renommée officiellement "**À Givre Ouvert**".
  - Ajout d'une vraie icône de lancement (Flocon de neige `AcUnit` sur fond bleu uni) via Adaptive Icons.
  - Centrage des boutons du Dashboard.
  - Alternance de couleurs (Zébrage Blanc / Bleu clair) pour une meilleure lisibilité dans l'Inventaire et les Recettes.
  - Formatage en italique et mise entre parenthèses des ingrédients frais suggérés pour les recettes, suppression du mot "Cible".
- **Bug Fix** : Résolution du crash d'insertion en base de données causé par le champ `id` qui n'était plus nullable.
- **Dette Technique** : Nettoyage proactif de tous les dossiers de tests orphelins (Android & Python).
