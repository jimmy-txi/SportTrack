# SportTrack

Application web de suivi sportif construite avec Spring Boot et Thymeleaf.

SportTrack permet a un athlete de suivre ses activites, definir des objectifs, participer a des challenges et interagir avec ses amis. Un espace administrateur permet aussi de gerer les activites et les sports proposes sur la plateforme.


# Lien de la documentation  

[https://sonarcloud.io/project/overview?id=jimmy-txi_SportTrack](https://jimmy-txi.github.io/SportTrack/)

# Lien de SonarQube

https://sonarcloud.io/project/overview?id=jimmy-txi_SportTrack

## Deploiement en ligne

La version de presentation est disponible ici :

**https://sporttrack.jimmy-tech.fr/**

## Fonctionnalites principales

- Authentification (connexion, inscription, securisation des routes)
- Gestion des activites sportives (creation, edition, suppression)
- Espace athlete (profil, liste d'activites, suivi personnel)
- Gestion des amis (demandes, acceptation, blocage, consultation du profil)
- Objectifs sportifs (creation, consultation, suppression)
- Challenges (creation et suivi)
- Espace administrateur (gestion des sports et moderation des activites)

## Stack technique

- Java 21
- Spring Boot 4
- Spring MVC + Thymeleaf
- Spring Security
- Spring Data JPA + Hibernate
- SQLite (base locale dans db/sporttrack.db)
- Maven

## Lancer le projet en local

### Prerequis

- JDK 21
- Maven (ou utilisation du wrapper Maven fourni)

### Installation et execution

1. Cloner le projet
2. Se placer a la racine du repository
3. Lancer l'application

Sous Windows :

```bash
./mvnw.cmd spring-boot:run
```

Sous Linux / macOS :

```bash
./mvnw spring-boot:run
```

Puis ouvrir :

- http://localhost:8080

## Exemples de routes utiles

- `/` : page d'accueil
- `/login` : connexion
- `/register` : inscription
- `/athlete/activities` : activites de l'athlete
- `/friends` : gestion des relations
- `/objectives` : objectifs personnels
- `/challenges` : liste des challenges
- `/admin` : tableau de bord administrateur

## Structure du projet

```text
src/main/java/fr/utc/miage/sporttrack/
|- config/       # configuration (security, etc.)
|- controller/   # controleurs web MVC
|- dto/          # objets de transfert
|- entity/       # modeles metier (activity, event, user, ...)
|- repository/   # acces aux donnees
|- service/      # logique metier

src/main/resources/
|- templates/    # vues Thymeleaf
|- static/       # ressources front
|- application.properties
```

## Tests

Executer les tests :

```bash
./mvnw test
```

## A propos

Projet realise dans le cadre d'un travail universitaire MIAGE
