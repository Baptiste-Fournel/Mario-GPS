# 🧭 Mario GPS – Planificateur d'itinéraire intelligent

Bienvenue dans **Mario GPS** !  
Ce projet JavaFX illustre l’implémentation d’algorithmes de pathfinding (Dijkstra, A*) sur une carte personnalisée, le tout animé le meilleur plombier italien.

---

## 🎮 Fonctionnalités

- 🗺️ Affichage d’une carte composée de différents types de tuiles (herbe, château, eau…)
- 👈 Sélection d’un point de départ et d’un point d’arrivée sur la carte
- 🚦 Choix entre plusieurs algorithmes de pathfinding (Dijkstra, A*)
- 📏 Affichage du temps d’exécution réel de chaque algorithme
- 🏃‍♂️ Animation du déplacement de Mario le long du chemin trouvé
- 🔁 Réinitialisation dynamique de la carte avec une **nouvelle taille personnalisée**
- 🧪 Export automatique de la carte au format **GeoJSON**
- 🎨 Interface JavaFX responsive avec styles CSS externes

---

## 🧱 Technologies utilisées

- **Java 23** – Langage principal
- **JavaFX** – Interface utilisateur graphique
- **Lombok** – Réduction du code boilerplate
- **Clean Architecture** – Séparation claire des responsabilités
- **GeoJSON** – Format d’export de la carte

---


---

## 🚀 Lancer le projet

### ✅ Prérequis

- Java 17 ou supérieur (Java 23 recommandé)
- IDE compatible (IntelliJ recommandé)
- JavaFX SDK installé et correctement configuré

### 💻 Démarrage rapide

1. Clone ce repo :
   ```bash
   git clone https://github.com/ton-repo/mario-gps.git
   cd mario-gps
2. Ouvre le projet avec IntelliJ

3. Configure JavaFX dans les paramètres du projet

4. Lance AppOrchestrator.java

💡 Utilisation
Clique sur "Point de départ", puis sélectionne une case verte (herbe)

Clique sur "Point d’arrivée", puis sélectionne une autre case

Choisis un algorithme (Dijkstra ou A*)

Appuie sur "Calculer le chemin"

Observe Mario parcourir le chemin trouvé 🏁

Tu peux réinitialiser la carte avec "Réinitialiser"
— choisis d'abord une nouvelle largeur et hauteur via les menus déroulants si besoin !
