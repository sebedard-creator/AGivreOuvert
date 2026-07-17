# Journal des modifications — À Givre Ouvert

Ce fichier résume les changements fonctionnels et techniques importants du projet.

## 17 juillet 2026

### Correction du chargement des recettes

- Identification d’un délai de lecture Android de 2 secondes, inférieur au temps réel de génération Gemini.
- Conservation du délai de connexion de 2 secondes pour détecter rapidement un serveur absent.
- Augmentation du délai de lecture à 60 secondes.
- Ajout d’un état d’erreur propre aux recettes et d’une action « Réessayer ».
- Distinction entre une erreur réseau et un inventaire réellement vide.

### Refonte de l’interface Android

- Remplacement du thème de démonstration violet par une identité bleu glacier, turquoise et corail.
- Définition centralisée des palettes claire et sombre, de la typographie et des formes Material 3.
- Désactivation des couleurs dynamiques afin de conserver une identité stable.
- Ajout d’une barre de navigation inférieure pour Accueil, Inventaire et Recettes.
- Refonte de l’accueil avec résumé de fraîcheur, état hors ligne et actions de scan.
- Remplacement des listes zébrées par des cartes uniformes et des pastilles sémantiques.
- Ajout d’une action de retrait visible et d’une confirmation dans l’inventaire.
- Refonte des états vides, de chargement et d’erreur.

### Scanner

- Ajout d’un véritable cadre de visée et d’un voile autour de la zone de lecture.
- Ancrage de la feuille de résultat au bas de l’écran.
- Refonte de la demande de permission caméra et des résultats d’ajout ou de retrait.
- Fermeture explicite du scanner ML Kit et de son exécuteur lors de la sortie de l’écran.
- Protection contre les lectures concurrentes ou répétées du même code-barres.

### Recettes

- Refonte des cartes et des détails repliables.
- Remplacement de l’accent orange par un corail/framboise.
- Affichage de l’ingrédient ciblé dans l’aperçu.
- Affichage de tous les ingrédients frais manquants avant l’ouverture de la recette.
- Indication explicite lorsqu’aucun ingrédient supplémentaire n’est requis.

### Validation et documentation

- Compilation Kotlin validée avec `:app:compileDebugKotlin`.
- Assemblage de l’APK validé avec `:app:assembleDebug`.
- Tâche `:app:testDebugUnitTest` exécutée avec `NO-SOURCE`; aucun test automatisé n’est présent.
- Mise à jour de `README.md`, `architecture.md`, `changelog.md` et `handoff.md` selon le code actuel.

## 13 juillet 2026

### Architecture initiale

- Choix d’un backend local FastAPI avec SQLite et SQLModel.
- Définition du modèle FIFO : chaque ajout crée un lot individuel daté.
- Choix d’une application Android native en Kotlin et Jetpack Compose.
- Configuration du backend sur `0.0.0.0:8096` pour les essais sur téléphone physique.
- Documentation de l’absence de contrainte CORS pour le client Android natif.

### Backend

- Création des tables `InventoryItem` et `KnownProduct`.
- Ajout des routes d’inventaire, de retrait FIFO, de recherche UPC et de recettes.
- Intégration d’Open Food Facts avec HTTPX.
- Mémorisation locale des produits inconnus saisis manuellement.
- Suppression de la notion de catégorie afin de conserver uniquement les champs nécessaires.
- Remplacement du générateur de recettes fictif par Google Gemini avec `gemini-flash-lite-latest`.
- Sélection de 10 lots au hasard parmi les 15 plus anciens pour générer 10 idées de repas.

### Application Android initiale

- Création du projet Compose et du `InventoryViewModel` partagé.
- Configuration de Retrofit avec `BACKEND_IP` provenant de `local.properties`.
- Implémentation de CameraX et ML Kit Barcode Scanning.
- Création des écrans Accueil, Inventaire, Scanner et Recettes.
- Ajout de l’icône de lancement et adoption du nom « À Givre Ouvert ».
- Correction de l’insertion en base en rendant l’identifiant Android nullable.

### Mode hors ligne en lecture seule

- Ajout d’une copie JSON de l’inventaire dans `SharedPreferences`.
- Affichage de la dernière copie synchronisée si le backend est inaccessible.
- Blocage des opérations de modification, du scanner et des recettes lorsque le serveur est déclaré hors ligne.
- Ajout d’un indicateur visuel de l’état du serveur.

### Tests

- Des tests exploratoires ont été créés durant l’implémentation initiale, puis les dossiers de tests devenus orphelins ont été retirés.
- Le dépôt ne contient actuellement aucun test automatisé.
