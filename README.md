# Localisation - GPS, Backend PHP/MySQL et Map

## Description

Ce projet est une application Android développée en Java dans le cadre du module **Programmation Mobile : Android avec Java**.

L’objectif du laboratoire est de réaliser une application de localisation en temps réel qui récupère les coordonnées GPS d’un appareil, les envoie vers un serveur PHP, les stocke dans une base de données MySQL, puis affiche les positions enregistrées sur une carte.

L’application permet de :

* récupérer la latitude et la longitude ;
* afficher les coordonnées dans l’interface Android ;
* envoyer les coordonnées au serveur avec Volley ;
* enregistrer les données dans une base MySQL ;
* récupérer les positions enregistrées depuis le serveur ;
* afficher les positions sous forme de markers sur une carte.

## Remarque importante sur la carte

L’énoncé mentionne Google Maps.
Cependant, comme Google Maps nécessite une clé API Google Cloud avec une configuration de facturation, ce projet utilise une alternative gratuite basée sur **OpenStreetMap** avec la bibliothèque **osmdroid**.

Le projet conserve les objectifs pédagogiques du TP :

* affichage d’une carte ;
* affichage des positions sous forme de markers ;
* zoom sur les positions ;
* intégration avec les données GPS stockées en base.

## Architecture générale

Le projet est composé de deux parties principales :

### Partie Android

La partie Android permet de :

* demander la permission de localisation ;
* récupérer la position actuelle ;
* envoyer latitude, longitude, date et identifiant appareil au serveur ;
* ouvrir une carte ;
* charger les positions depuis le backend ;
* afficher les positions sous forme de markers.

### Partie serveur

La partie serveur contient :

* une base de données MySQL ;
* une classe métier `Position` ;
* une classe de connexion PDO ;
* une interface DAO ;
* un service d’accès aux données ;
* une API d’insertion `createPosition.php` ;
* une API de lecture `showPositions.php`.

## Technologies utilisées

### Android

* Java
* Android Studio
* XML Layouts
* Volley
* LocationManager
* LocationListener
* Runtime Permissions
* OpenStreetMap
* osmdroid

### Backend

* PHP
* MySQL
* PDO
* XAMPP
* phpMyAdmin
* JSON

## Structure du projet

```text
Localisation/
│
├── app/
│   └── src/main/
│       ├── java/com/example/localisation/
│       │   ├── MainActivity.java
│       │   └── MapsActivity.java
│       │
│       ├── res/
│       │   ├── layout/
│       │   │   ├── activity_main.xml
│       │   │   └── activity_maps.xml
│       │   │
│       │   └── values/
│       │       ├── strings.xml
│       │       └── themes.xml
│       │
│       └── AndroidManifest.xml
│
├── backend-php/
│   ├── classe/
│   │   └── Position.php
│   ├── connexion/
│   │   └── Connexion.php
│   ├── dao/
│   │   └── IDao.php
│   ├── service/
│   │   └── PositionService.php
│   ├── createPosition.php
│   ├── showPositions.php
│   ├── test.html
│   └── database.sql
│
├── README.md
└── .gitignore
```

## Base de données

La base de données utilisée s’appelle :

```text
localisation
```

La table utilisée s’appelle :

```text
position
```

Elle contient les champs suivants :

| Champ     | Type     | Rôle                              |
| --------- | -------- | --------------------------------- |
| id        | INT      | Identifiant automatique           |
| latitude  | DOUBLE   | Latitude de la position           |
| longitude | DOUBLE   | Longitude de la position          |
| date      | DATETIME | Date et heure de l’enregistrement |
| imei      | VARCHAR  | Identifiant de l’appareil         |

## Script SQL

Le fichier `database.sql` permet de créer la base de données et la table :

```sql
CREATE DATABASE IF NOT EXISTS localisation
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE localisation;

DROP TABLE IF EXISTS `position`;

CREATE TABLE `position` (
  `id` int(11) NOT NULL PRIMARY KEY AUTO_INCREMENT,
  `latitude` double NOT NULL,
  `longitude` double NOT NULL,
  `date` datetime NOT NULL,
  `imei` varchar(100) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

Les noms `position` et `date` sont entourés par des backticks afin d’éviter les conflits avec certains mots réservés ou interprétations SQL.

## Partie backend PHP

### Position.php

La classe `Position` représente une position géographique.

Elle contient :

* id ;
* latitude ;
* longitude ;
* date ;
* imei.

Chaque objet `Position` correspond à une ligne de la table `position`.

### Connexion.php

La classe `Connexion` permet d’ouvrir une connexion PDO vers la base MySQL `localisation`.

Elle centralise la connexion pour éviter de répéter le même code dans plusieurs fichiers.

### IDao.php

L’interface `IDao` définit les méthodes classiques d’accès aux données :

* create ;
* update ;
* delete ;
* getById ;
* getAll.

### PositionService.php

La classe `PositionService` contient la logique d’accès aux données.

Elle permet principalement :

* d’insérer une nouvelle position avec `create()` ;
* de récupérer toutes les positions avec `getAll()`.

### createPosition.php

Le fichier `createPosition.php` reçoit les données envoyées par l’application Android en méthode POST.

Les paramètres reçus sont :

```text
latitude
longitude
date
imei
```

Après réception, le script crée un objet `Position` puis l’enregistre dans la base de données.

La réponse est renvoyée au format JSON.

Exemple de réponse :

```json
{
  "ok": true,
  "message": "Position saved",
  "ip": "127.0.0.1"
}
```

### showPositions.php

Le fichier `showPositions.php` récupère toutes les positions enregistrées dans la base de données et les renvoie au format JSON.

Exemple de réponse :

```json
{
  "positions": [
    {
      "id": 1,
      "latitude": 31.6295,
      "longitude": -7.9811,
      "date": "2026-06-06 12:00:00",
      "imei": "test-device"
    }
  ]
}
```

Cette API est utilisée par l’activité de carte pour afficher les markers.

## Partie Android

## Permissions utilisées

Dans `AndroidManifest.xml`, les permissions suivantes sont déclarées :

```xml
<uses-permission android:name="android.permission.ACCESS_FINE_LOCATION" />
<uses-permission android:name="android.permission.ACCESS_COARSE_LOCATION" />
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

### Rôle des permissions

| Permission             | Rôle                                                                       |
| ---------------------- | -------------------------------------------------------------------------- |
| ACCESS_FINE_LOCATION   | Accéder à la localisation précise                                          |
| ACCESS_COARSE_LOCATION | Accéder à la localisation approximative                                    |
| INTERNET               | Envoyer les données vers le serveur PHP                                    |
| READ_PHONE_STATE       | Tenter de récupérer un identifiant appareil sur anciennes versions Android |

## Identifiant appareil

L’énoncé utilise le champ `imei`.

Sur les versions Android récentes, l’accès direct au véritable IMEI est restreint pour des raisons de sécurité et de confidentialité.

Dans ce projet, l’application utilise :

* `ANDROID_ID` comme identifiant principal ;
* une tentative de récupération via `TelephonyManager` si disponible ;
* `UNKNOWN_DEVICE` si aucun identifiant n’est accessible.

La valeur est envoyée dans le champ `imei` afin de respecter la structure du TP.

## MainActivity.java

`MainActivity` est responsable de :

* demander les permissions nécessaires ;
* récupérer la position GPS ;
* afficher la latitude et la longitude ;
* envoyer les données vers `createPosition.php` avec Volley ;
* ouvrir l’activité de carte.

L’envoi vers le serveur se fait avec une requête HTTP POST.

Les paramètres envoyés sont :

```text
latitude
longitude
date
imei
```

## MapsActivity.java

`MapsActivity` est responsable de :

* charger la carte OpenStreetMap ;
* appeler `showPositions.php` avec Volley ;
* lire la réponse JSON ;
* parcourir le tableau `positions` ;
* ajouter un marker pour chaque position enregistrée ;
* zoomer sur la position la plus récente.

## Interface Android

### activity_main.xml

L’interface principale contient :

* un TextView pour la latitude ;
* un TextView pour la longitude ;
* un TextView pour la réponse du serveur ;
* un bouton pour envoyer la position ;
* un bouton pour afficher la carte.

### activity_maps.xml

L’interface de carte contient un `MapView` fourni par osmdroid :

```xml
<org.osmdroid.views.MapView
    android:id="@+id/map"
    android:layout_width="match_parent"
    android:layout_height="match_parent" />
```

## Dépendances Android

Les dépendances principales sont :

Avec Kotlin DSL :

```kotlin
implementation("androidx.appcompat:appcompat:1.6.1")
implementation("androidx.constraintlayout:constraintlayout:2.1.4")
implementation("com.android.volley:volley:1.2.1")
implementation("org.osmdroid:osmdroid-android:6.1.20")
```

Avec Gradle classique :

```gradle
implementation 'androidx.appcompat:appcompat:1.6.1'
implementation 'androidx.constraintlayout:constraintlayout:2.1.4'
implementation 'com.android.volley:volley:1.2.1'
implementation 'org.osmdroid:osmdroid-android:6.1.20'
```

## Configuration du serveur

Pour utiliser le backend :

1. Démarrer Apache et MySQL avec XAMPP.
2. Copier le dossier `backend-php` dans le dossier `htdocs`.
3. Renommer ce dossier en :

```text
localisation
```

Le chemin final doit être :

```text
C:\xampp\htdocs\localisation
```

ou selon l’installation :

```text
D:\xampp\htdocs\localisation
```

4. Importer ou exécuter le fichier :

```text
database.sql
```

5. Tester l’insertion avec :

```text
http://localhost/localisation/test.html
```

6. Tester la récupération JSON avec :

```text
http://localhost/localisation/showPositions.php
```

## Configuration des URLs Android

Pour l’émulateur Android, les URLs utilisées sont :

```text
http://10.0.2.2/localisation/createPosition.php
http://10.0.2.2/localisation/showPositions.php
```

`10.0.2.2` permet à l’émulateur d’accéder au serveur local de l’ordinateur.

Pour un téléphone réel, il faut remplacer `10.0.2.2` par l’adresse IP locale du PC.

Exemple :

```text
http://192.168.1.10/localisation/createPosition.php
http://192.168.1.10/localisation/showPositions.php
```

Le téléphone et le PC doivent être connectés au même réseau Wi-Fi.

## Autorisation HTTP

Comme le serveur local utilise HTTP et non HTTPS, l’attribut suivant est ajouté dans le manifeste Android :

```xml
android:usesCleartextTraffic="true"
```

Cela permet à l’application d’envoyer des requêtes vers le serveur PHP local pendant le TP.

## Test de l’application

### Étape 1 : Tester le backend

1. Démarrer Apache et MySQL.
2. Ouvrir phpMyAdmin.
3. Vérifier que la base `localisation` existe.
4. Vérifier que la table `position` existe.
5. Ouvrir :

```text
http://localhost/localisation/test.html
```

6. Envoyer une position de test.
7. Vérifier l’insertion dans phpMyAdmin.

### Étape 2 : Tester l’application Android

1. Lancer l’application Android.
2. Accepter les permissions.
3. Simuler une localisation si l’application tourne sur un émulateur.
4. Cliquer sur `Envoyer position`.
5. Vérifier que la position est enregistrée dans MySQL.
6. Cliquer sur `Afficher Map`.
7. Vérifier que les markers apparaissent sur la carte.

## Test sur émulateur

Sur un émulateur Android, la position réelle n’est pas utilisée automatiquement.

Pour simuler une position :

1. Ouvrir l’émulateur.
2. Cliquer sur les trois points.
3. Aller dans `Location`.
4. Entrer une latitude et une longitude.
5. Cliquer sur `Set Location`.

Exemple pour Marrakech :

```text
Latitude: 31.6295
Longitude: -7.9811
```

## Résultat attendu

À la fin du test :

* l’application affiche les coordonnées GPS ;
* une ligne est ajoutée dans la table `position` ;
* `showPositions.php` retourne les positions au format JSON ;
* la carte affiche les positions enregistrées sous forme de markers ;
* la carte zoome sur la position la plus récente.

## Vidéo démonstrative

La vidéo démonstrative du projet est disponible ici :

[Voir la vidéo de démonstration](https://drive.google.com/file/d/1dPblwnHmUxFGFETooLp-EYZZ6S28NOLr/view?usp=sharing)

## Bilan pédagogique

Ce laboratoire permet de comprendre un flux complet entre Android et un backend web.

Les notions principales abordées sont :

* permissions Android ;
* récupération GPS ;
* LocationManager ;
* LocationListener ;
* envoi HTTP avec Volley ;
* backend PHP ;
* architecture DAO/service ;
* insertion MySQL ;
* récupération JSON ;
* affichage de markers sur une carte ;
* intégration d’OpenStreetMap comme alternative gratuite.


